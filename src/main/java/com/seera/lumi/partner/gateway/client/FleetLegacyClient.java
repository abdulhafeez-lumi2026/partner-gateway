package com.seera.lumi.partner.gateway.client;

import com.seera.lumi.partner.gateway.client.response.InsuranceListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "fleet-legacy-service", url = "${api.fleet-legacy.baseUrl}")
public interface FleetLegacyClient {

    @GetMapping("/insurance")
    InsuranceListResponse getInsuranceList();
}
