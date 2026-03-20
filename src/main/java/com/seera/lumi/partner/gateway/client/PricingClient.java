package com.seera.lumi.partner.gateway.client;

import com.seera.lumi.partner.gateway.client.request.CreateQuoteRequest;
import com.seera.lumi.partner.gateway.client.request.SearchOffersRequest;
import com.seera.lumi.partner.gateway.client.response.RentalOffersResponse;
import com.seera.lumi.partner.gateway.client.response.VehicleQuoteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "pricing-service", url = "${api.pricing.baseUrl}")
public interface PricingClient {

    @PutMapping("/api/pricing/offers")
    RentalOffersResponse searchOffers(@RequestBody SearchOffersRequest request);

    @GetMapping("/api/pricing/offers/{offerId}/quote/{groupId}")
    VehicleQuoteResponse getQuote(
            @PathVariable("offerId") String offerId,
            @PathVariable("groupId") Long groupId,
            @RequestParam(value = "promoCode", required = false) String promoCode,
            @RequestParam(value = "insuranceId", required = false) Long insuranceId);

    @PostMapping("/api/pricing/quote")
    VehicleQuoteResponse createQuote(@RequestBody CreateQuoteRequest request);
}
