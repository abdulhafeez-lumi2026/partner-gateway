package com.seera.lumi.partner.gateway.client;

import com.seera.lumi.partner.gateway.client.request.InternalAvailabilityRequest;
import com.seera.lumi.partner.gateway.client.request.InternalQuoteRequest;
import com.seera.lumi.partner.gateway.client.response.ActivePromotionResponse;
import com.seera.lumi.partner.gateway.client.response.PartnerResponse;
import com.seera.lumi.partner.gateway.controller.request.ClientCredentialsRequest;
import com.seera.lumi.partner.gateway.controller.response.QuoteResponse;
import com.seera.lumi.partner.gateway.controller.response.TokenResponse;
import com.seera.lumi.partner.gateway.controller.response.AvailabilitySearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "partner-service", url = "${api.partner.baseUrl}")
public interface PartnerServiceClient {

    @PostMapping("/api/partner/auth/token")
    TokenResponse authenticate(@RequestBody ClientCredentialsRequest request);

    @GetMapping("/internal/api/v1/partners/{partnerCode}")
    PartnerResponse getPartner(@PathVariable String partnerCode);

    @GetMapping("/api/v1/partners/validate-api-key")
    Object validateApiKey(@RequestParam String apiKey);

    @PostMapping("/internal/api/v1/availability")
    AvailabilitySearchResponse searchAvailability(@RequestBody InternalAvailabilityRequest request);

    @PostMapping("/internal/api/v1/quote")
    QuoteResponse createQuote(@RequestBody InternalQuoteRequest request);

    @GetMapping("/internal/api/v1/quote/{quoteId}")
    QuoteResponse getQuote(@PathVariable("quoteId") String quoteId);

    @GetMapping("/internal/api/v1/promotions/active")
    ActivePromotionResponse getActivePromotion(@RequestParam("debtorCode") String debtorCode);
}
