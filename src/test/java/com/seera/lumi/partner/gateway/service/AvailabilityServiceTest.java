package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.PricingClient;
import com.seera.lumi.partner.gateway.client.response.RentalOffersResponse;
import com.seera.lumi.partner.gateway.controller.request.AvailabilityRequest;
import com.seera.lumi.partner.gateway.controller.response.PricingPackage;
import com.seera.lumi.partner.gateway.controller.response.VehicleAvailabilityResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private PricingClient pricingClient;

    @InjectMocks
    private AvailabilityService availabilityService;

    private AvailabilityRequest buildRequest() {
        return AvailabilityRequest.builder()
                .pickupLocationId(1L)
                .dropoffLocationId(2L)
                .pickupDateTime(LocalDateTime.of(2026, 4, 1, 10, 0))
                .dropoffDateTime(LocalDateTime.of(2026, 4, 5, 10, 0))
                .build();
    }

    private RentalOffersResponse.VehicleOfferData buildOffer(Long groupId, boolean available,
                                                              String finalPrice, String pricePerDay,
                                                              String cdwPerDay, String soldDays,
                                                              String vatPercentage, String discountPercentage) {
        RentalOffersResponse.VehicleOfferData offer = new RentalOffersResponse.VehicleOfferData();
        offer.setGroupId(groupId);
        offer.setAvailable(available);
        offer.setFinalPrice(finalPrice);
        offer.setPricePerDay(pricePerDay);
        offer.setCdwPerDay(cdwPerDay);
        offer.setSoldDays(soldDays);
        offer.setVatPercentage(vatPercentage);
        offer.setDiscountPercentage(discountPercentage);
        return offer;
    }

    @Test
    void searchAvailability_success_returnsAvailableVehicles() {
        RentalOffersResponse.VehicleOfferData offer = buildOffer(
                10L, true, "1000", "250", "20", "4", "15", "10");

        RentalOffersResponse response = RentalOffersResponse.builder()
                .data(List.of(offer))
                .build();

        when(pricingClient.searchOffers(any())).thenReturn(response);

        List<VehicleAvailabilityResponse> result = availabilityService.searchAvailability(buildRequest());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVehicleGroup()).isEqualTo("10");
        assertThat(result.get(0).getPackages()).hasSize(2);

        verify(pricingClient).searchOffers(argThat(req ->
                req.getPickupBranchId().equals(1L)
                        && req.getDropOffBranchId().equals(2L)
        ));
    }

    @Test
    void searchAvailability_nullResponse_returnsEmptyList() {
        when(pricingClient.searchOffers(any())).thenReturn(null);

        List<VehicleAvailabilityResponse> result = availabilityService.searchAvailability(buildRequest());

        assertThat(result).isEmpty();
    }

    @Test
    void searchAvailability_filtersUnavailableVehicles() {
        RentalOffersResponse.VehicleOfferData available = buildOffer(
                10L, true, "1000", "250", "20", "4", "15", "10");
        RentalOffersResponse.VehicleOfferData unavailable = buildOffer(
                20L, false, "800", "200", "15", "4", "15", "5");

        RentalOffersResponse response = RentalOffersResponse.builder()
                .data(List.of(available, unavailable))
                .build();

        when(pricingClient.searchOffers(any())).thenReturn(response);

        List<VehicleAvailabilityResponse> result = availabilityService.searchAvailability(buildRequest());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVehicleGroup()).isEqualTo("10");
    }

    @Test
    void searchAvailability_feignException_throwsPartnerException() {
        FeignException feignEx = mock(FeignException.class);
        when(feignEx.status()).thenReturn(500);
        when(pricingClient.searchOffers(any())).thenThrow(feignEx);

        assertThatThrownBy(() -> availabilityService.searchAvailability(buildRequest()))
                .isInstanceOf(PartnerException.class)
                .satisfies(ex -> {
                    PartnerException pe = (PartnerException) ex;
                    assertThat(pe.getCode()).isEqualTo("AVAILABILITY_SEARCH_ERROR");
                    assertThat(pe.getHttpStatus()).isEqualTo(502);
                });
    }

    @Test
    void searchAvailability_pricingCalculation() {
        // pricePerDay=250, soldDays=4, finalPrice=1000, cdwPerDay=20, vat=15%, discount=10%
        RentalOffersResponse.VehicleOfferData offer = buildOffer(
                10L, true, "1000", "250", "20", "4", "15", "10");

        RentalOffersResponse response = RentalOffersResponse.builder()
                .data(List.of(offer))
                .build();

        when(pricingClient.searchOffers(any())).thenReturn(response);

        List<VehicleAvailabilityResponse> result = availabilityService.searchAvailability(buildRequest());

        assertThat(result).hasSize(1);
        List<PricingPackage> packages = result.get(0).getPackages();
        assertThat(packages).hasSize(2);

        // FULL package
        PricingPackage fullPkg = packages.stream()
                .filter(p -> "FULL".equals(p.getType())).findFirst().orElseThrow();
        BigDecimal expectedFullBaseRate = new BigDecimal("250").multiply(BigDecimal.valueOf(4)); // 1000
        BigDecimal expectedFullDiscountAmount = expectedFullBaseRate.multiply(new BigDecimal("10"))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP); // 100.00
        BigDecimal expectedFullFinalRate = new BigDecimal("1000");
        BigDecimal expectedFullVatAmount = expectedFullFinalRate.multiply(new BigDecimal("15"))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP); // 150.00
        BigDecimal expectedFullTotalWithVat = expectedFullFinalRate.add(expectedFullVatAmount); // 1150.00

        assertThat(fullPkg.getBaseRate()).isEqualByComparingTo(expectedFullBaseRate);
        assertThat(fullPkg.getDiscountPercentage()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(fullPkg.getDiscountAmount()).isEqualByComparingTo(expectedFullDiscountAmount);
        assertThat(fullPkg.getFinalRate()).isEqualByComparingTo(expectedFullFinalRate);
        assertThat(fullPkg.getVatPercentage()).isEqualByComparingTo(new BigDecimal("15"));
        assertThat(fullPkg.getVatAmount()).isEqualByComparingTo(expectedFullVatAmount);
        assertThat(fullPkg.getTotalWithVat()).isEqualByComparingTo(expectedFullTotalWithVat);
        assertThat(fullPkg.getDeductible()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(fullPkg.getInclusions()).containsExactly("CDW", "Basic Insurance", "Unlimited Mileage Allowance");

        // BASIC package
        PricingPackage basicPkg = packages.stream()
                .filter(p -> "BASIC".equals(p.getType())).findFirst().orElseThrow();
        BigDecimal totalCdw = new BigDecimal("20").multiply(BigDecimal.valueOf(4)); // 80
        BigDecimal expectedBasicFinalRate = new BigDecimal("1000").subtract(totalCdw); // 920
        BigDecimal expectedBasicBaseRate = expectedFullBaseRate.subtract(totalCdw); // 920
        BigDecimal expectedBasicDiscountAmount = expectedBasicBaseRate.multiply(new BigDecimal("10"))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP); // 92.00
        BigDecimal expectedBasicVatAmount = expectedBasicFinalRate.multiply(new BigDecimal("15"))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP); // 138.00
        BigDecimal expectedBasicTotalWithVat = expectedBasicFinalRate.add(expectedBasicVatAmount); // 1058.00

        assertThat(basicPkg.getBaseRate()).isEqualByComparingTo(expectedBasicBaseRate);
        assertThat(basicPkg.getDiscountAmount()).isEqualByComparingTo(expectedBasicDiscountAmount);
        assertThat(basicPkg.getFinalRate()).isEqualByComparingTo(expectedBasicFinalRate);
        assertThat(basicPkg.getVatAmount()).isEqualByComparingTo(expectedBasicVatAmount);
        assertThat(basicPkg.getTotalWithVat()).isEqualByComparingTo(expectedBasicTotalWithVat);
        assertThat(basicPkg.getInclusions()).containsExactly("Basic Insurance");
    }
}
