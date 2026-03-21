package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.PricingClient;
import com.seera.lumi.partner.gateway.client.request.SearchOffersRequest;
import com.seera.lumi.partner.gateway.client.response.RentalOffersResponse;
import com.seera.lumi.partner.gateway.controller.request.AvailabilityRequest;
import com.seera.lumi.partner.gateway.controller.response.PricingPackage;
import com.seera.lumi.partner.gateway.controller.response.VehicleAvailabilityResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import com.seera.lumi.partner.gateway.security.PartnerContext;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final PricingClient pricingClient;

    public List<VehicleAvailabilityResponse> searchAvailability(AvailabilityRequest request) {
        try {
            // Validate branches against partner's allowed list
            validateAllowedBranch(request.getPickupLocationId(), "pickup");
            validateAllowedBranch(request.getDropoffLocationId(), "dropoff");

            log.info("Searching availability: pickup={}, dropoff={}, from={}, to={}, accountNo={}",
                    request.getPickupLocationId(), request.getDropoffLocationId(),
                    request.getPickupDateTime(), request.getDropoffDateTime(),
                    PartnerContext.getDebtorCode());

            SearchOffersRequest offersRequest = SearchOffersRequest.builder()
                    .pickupBranchId(request.getPickupLocationId())
                    .dropOffBranchId(request.getDropoffLocationId())
                    .pickupDate(request.getPickupDateTime())
                    .dropOffDate(request.getDropoffDateTime())
                    .accountNo(PartnerContext.getDebtorCode())
                    .build();

            RentalOffersResponse offersResponse = pricingClient.searchOffers(offersRequest);

            if (offersResponse == null || offersResponse.getData() == null) {
                return List.of();
            }

            // Filter by allowed vehicle groups
            List<String> allowedGroups = PartnerContext.getAllowedVehicleGroups();

            return offersResponse.getData().stream()
                    .filter(RentalOffersResponse.VehicleOfferData::isAvailable)
                    .filter(offer -> allowedGroups.isEmpty()
                            || allowedGroups.contains(String.valueOf(offer.getGroupId())))
                    .map(this::mapToAvailabilityResponse)
                    .collect(Collectors.toList());
        } catch (FeignException e) {
            log.error("Failed to search availability from pricing service: status={}, message={}",
                    e.status(), e.getMessage(), e);
            throw new PartnerException("AVAILABILITY_SEARCH_ERROR",
                    "Failed to search vehicle availability", 502, e);
        }
    }

    private VehicleAvailabilityResponse mapToAvailabilityResponse(RentalOffersResponse.VehicleOfferData offer) {
        List<PricingPackage> packages = buildPricingPackages(offer);

        return VehicleAvailabilityResponse.builder()
                .vehicleGroup(String.valueOf(offer.getGroupId()))
                .packages(packages)
                .build();
    }

    /**
     * Builds BASIC and FULL pricing packages from the offer data.
     * <p>
     * TODO: The BASIC vs FULL package split is approximate. The pricing service returns one quote
     * with CDW included. For BASIC, we subtract CDW. This needs a v2 pricing endpoint with
     * quoteMode support for accurate BASIC/FULL split.
     */
    private List<PricingPackage> buildPricingPackages(RentalOffersResponse.VehicleOfferData offer) {
        List<PricingPackage> packages = new ArrayList<>();

        BigDecimal finalPrice = parseBigDecimal(offer.getFinalPrice());
        BigDecimal vatPercentage = parseBigDecimal(offer.getVatPercentage());
        BigDecimal discountPercentage = parseBigDecimal(offer.getDiscountPercentage());
        BigDecimal cdwPerDay = parseBigDecimal(offer.getCdwPerDay());
        BigDecimal pricePerDay = parseBigDecimal(offer.getPricePerDay());
        int soldDays = parseIntSafe(offer.getSoldDays());

        // FULL package: includes CDW
        BigDecimal fullBaseRate = pricePerDay.multiply(BigDecimal.valueOf(soldDays));
        BigDecimal fullDiscountAmount = fullBaseRate.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal fullFinalRate = finalPrice;
        BigDecimal fullVatAmount = fullFinalRate.multiply(vatPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal fullTotalWithVat = fullFinalRate.add(fullVatAmount);

        PricingPackage fullPackage = PricingPackage.builder()
                .type("FULL")
                .baseRate(fullBaseRate)
                .discountPercentage(discountPercentage)
                .discountAmount(fullDiscountAmount)
                .finalRate(fullFinalRate)
                .vatPercentage(vatPercentage)
                .vatAmount(fullVatAmount)
                .totalWithVat(fullTotalWithVat)
                .deductible(BigDecimal.ZERO)
                .inclusions(List.of("CDW", "Basic Insurance", "Unlimited Mileage Allowance"))
                .build();
        packages.add(fullPackage);

        // BASIC package: excludes CDW (approximate - subtract CDW from total)
        BigDecimal totalCdw = cdwPerDay.multiply(BigDecimal.valueOf(soldDays));
        BigDecimal basicFinalRate = finalPrice.subtract(totalCdw);
        if (basicFinalRate.compareTo(BigDecimal.ZERO) < 0) {
            basicFinalRate = BigDecimal.ZERO;
        }
        BigDecimal basicBaseRate = fullBaseRate.subtract(totalCdw);
        if (basicBaseRate.compareTo(BigDecimal.ZERO) < 0) {
            basicBaseRate = BigDecimal.ZERO;
        }
        BigDecimal basicDiscountAmount = basicBaseRate.multiply(discountPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal basicVatAmount = basicFinalRate.multiply(vatPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal basicTotalWithVat = basicFinalRate.add(basicVatAmount);

        PricingPackage basicPackage = PricingPackage.builder()
                .type("BASIC")
                .baseRate(basicBaseRate)
                .discountPercentage(discountPercentage)
                .discountAmount(basicDiscountAmount)
                .finalRate(basicFinalRate)
                .vatPercentage(vatPercentage)
                .vatAmount(basicVatAmount)
                .totalWithVat(basicTotalWithVat)
                .deductible(BigDecimal.ZERO) // TODO: Deductible amount TBD, needs v2 pricing endpoint
                .inclusions(List.of("Basic Insurance"))
                .build();
        packages.add(basicPackage);

        return packages;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse BigDecimal value: {}", value);
            return BigDecimal.ZERO;
        }
    }

    private int parseIntSafe(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse int value: {}", value);
            return 0;
        }
    }

    private void validateAllowedBranch(Long branchId, String label) {
        List<String> allowed = PartnerContext.getAllowedBranches();
        if (!allowed.isEmpty() && !allowed.contains(String.valueOf(branchId))) {
            throw new PartnerException("BRANCH_NOT_ALLOWED",
                    label + " branch " + branchId + " is not in your allowed branches", 403);
        }
    }
}
