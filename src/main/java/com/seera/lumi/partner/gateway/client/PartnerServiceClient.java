package com.seera.lumi.partner.gateway.client;

import com.seera.lumi.partner.gateway.client.response.PartnerResponse;
import com.seera.lumi.partner.gateway.controller.request.ClientCredentialsRequest;
import com.seera.lumi.partner.gateway.controller.response.TokenResponse;
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
}
