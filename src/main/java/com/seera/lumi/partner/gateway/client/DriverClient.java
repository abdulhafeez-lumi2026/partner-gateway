package com.seera.lumi.partner.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "driver-service", url = "${api.driver.baseUrl}")
public interface DriverClient {

    @PostMapping("/v3/driver")
    Map<String, Object> createDriver(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> driverRequest);
}
