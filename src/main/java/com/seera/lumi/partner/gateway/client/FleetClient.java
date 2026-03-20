package com.seera.lumi.partner.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "fleet-service", url = "${api.fleet.baseUrl}")
public interface FleetClient {

    // TODO: Define fleet service endpoints
    // Example: @GetMapping("/api/v1/vehicles/groups")
    // List<VehicleGroupDto> getVehicleGroups();
}
