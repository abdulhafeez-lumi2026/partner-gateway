package com.seera.lumi.partner.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "partner-service", url = "${api.partner.baseUrl}")
public interface PartnerServiceClient {

    @GetMapping("/api/v1/partners/{partnerCode}")
    Object getPartner(@PathVariable String partnerCode);

    @GetMapping("/api/v1/partners/validate-api-key")
    Object validateApiKey(@RequestParam String apiKey);
}
