package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.PricingClient;
import com.seera.lumi.partner.gateway.client.request.CreateQuoteRequest;
import com.seera.lumi.partner.gateway.client.response.VehicleQuoteResponse;
import com.seera.lumi.partner.gateway.controller.request.QuoteRequest;
import com.seera.lumi.partner.gateway.controller.response.PricingPackage;
import com.seera.lumi.partner.gateway.controller.response.QuoteResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import com.seera.lumi.partner.gateway.security.PartnerContext;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private static final String QUOTE_CACHE_PREFIX = "partner:quote:";
    private static final long QUOTE_TTL_MINUTES = 30;

    private final PricingClient pricingClient;
    private final RedisTemplate<String, Object> redisTemplate;

    public QuoteResponse createQuote(QuoteRequest request) {
        try {
            // Validate branches and vehicle group against partner's allowed list
            validateAllowedBranch(request.getPickupLocationId(), "pickup");
            validateAllowedBranch(request.getDropoffLocationId(), "dropoff");
            validateAllowedVehicleGroup(request.getVehicleGroup());

            log.info("Creating quote: vehicleGroup={}, pickup={}, dropoff={}, accountNo={}",
                    request.getVehicleGroup(), request.getPickupLocationId(),
                    request.getDropoffLocationId(), PartnerContext.getDebtorCode());

            CreateQuoteRequest quoteRequest = CreateQuoteRequest.builder()
                    .pickupBranchId(request.getPickupLocationId())
                    .dropOffBranchId(request.getDropoffLocationId())
                    .pickupDateTime(request.getPickupDateTime().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .dropOffDateTime(request.getDropoffDateTime().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .vehicleGroupCode(request.getVehicleGroup())
                    .accountNo(PartnerContext.getDebtorCode())
                    .build();

            VehicleQuoteResponse quoteResult = pricingClient.createQuote(quoteRequest);

            String partnerQuoteId = UUID.randomUUID().toString();

            List<PricingPackage> packages = buildPricingPackages(quoteResult);

            QuoteResponse response = QuoteResponse.builder()
                    .quoteId(partnerQuoteId)
                    .vehicleGroup(request.getVehicleGroup())
                    .packages(packages)
                    .pickupLocation(String.valueOf(request.getPickupLocationId()))
                    .dropoffLocation(String.valueOf(request.getDropoffLocationId()))
                    .pickupDateTime(request.getPickupDateTime())
                    .dropoffDateTime(request.getDropoffDateTime())
                    .currency(quoteResult.getCurrency())
                    .validUntil(LocalDateTime.now().plusMinutes(QUOTE_TTL_MINUTES))
                    .build();

            // Cache the quote in Redis
            String cacheKey = QUOTE_CACHE_PREFIX + partnerQuoteId;
            redisTemplate.opsForValue().set(cacheKey, response, QUOTE_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("Quote created and cached: quoteId={}", partnerQuoteId);

            return response;
        } catch (FeignException e) {
            log.error("Failed to create quote from pricing service: status={}, message={}",
                    e.status(), e.getMessage(), e);
            throw new PartnerException("QUOTE_CREATE_ERROR",
                    "Failed to create quote", 502, e);
        }
    }

    public QuoteResponse getQuote(String quoteId) {
        String cacheKey = QUOTE_CACHE_PREFIX + quoteId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached == null) {
            throw new PartnerException("QUOTE_NOT_FOUND",
                    "Quote not found or expired: " + quoteId, 404);
        }

        if (cached instanceof QuoteResponse quoteResponse) {
            return quoteResponse;
        }

        // Handle case where Redis deserializes as LinkedHashMap
        log.warn("Quote cache entry is not QuoteResponse type for quoteId={}, type={}",
                quoteId, cached.getClass().getSimpleName());
        throw new PartnerException("QUOTE_NOT_FOUND",
                "Quote not found or expired: " + quoteId, 404);
    }

    /**
     * Builds BASIC and FULL pricing packages from the quote result.
     * <p>
     * TODO: The BASIC vs FULL package split is approximate. The pricing service returns one quote
     * with CDW included. For BASIC, we subtract CDW. This needs a v2 pricing endpoint with
     * quoteMode support for accurate BASIC/FULL split.
     */
    private List<PricingPackage> buildPricingPackages(VehicleQuoteResponse quote) {
        List<PricingPackage> packages = new ArrayList<>();

        BigDecimal finalPrice = parseBigDecimal(quote.getFinalPrice());
        BigDecimal vatPercentage = parseBigDecimal(quote.getVatPercentage());
        BigDecimal discountPercentage = parseBigDecimal(quote.getDiscountPercentage());
        BigDecimal cdwPerDay = parseBigDecimal(quote.getCdwPerDay());
        BigDecimal pricePerDay = parseBigDecimal(quote.getPricePerDay());
        int soldDays = parseIntSafe(quote.getSoldDays());

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

        // BASIC package: excludes CDW
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

    private void validateAllowedVehicleGroup(String vehicleGroup) {
        List<String> allowed = PartnerContext.getAllowedVehicleGroups();
        if (!allowed.isEmpty() && !allowed.contains(vehicleGroup)) {
            throw new PartnerException("VEHICLE_GROUP_NOT_ALLOWED",
                    "Vehicle group " + vehicleGroup + " is not in your allowed vehicle groups", 403);
        }
    }
}
