package com.seera.lumi.partner.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "pricing-service", url = "${api.pricing.baseUrl}")
public interface PricingClient {

    // TODO: Define pricing service endpoints
    // Example: @GetMapping("/api/v1/pricing/quote")
    // QuoteDto getQuote(@RequestParam params...);
}
