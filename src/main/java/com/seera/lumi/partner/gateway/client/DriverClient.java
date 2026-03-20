package com.seera.lumi.partner.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "driver-service", url = "${api.driver.baseUrl}")
public interface DriverClient {

    // TODO: Define driver service endpoints
    // Example: @PostMapping("/api/v1/drivers")
    // DriverDto createDriver(@RequestBody CreateDriverDto request);
}
