package com.seera.lumi.partner.gateway.client;

import com.seera.lumi.partner.gateway.client.response.AddonPageResponse;
import com.seera.lumi.partner.gateway.client.response.VehicleGroupPageResponse;
import com.seera.lumi.partner.gateway.client.response.VehicleModelDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "fleet-service", url = "${api.fleet.baseUrl}")
public interface FleetClient {

    @GetMapping("/v1/vehicle-group")
    VehicleGroupPageResponse getVehicleGroups(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("enabled") boolean enabled);

    @GetMapping("/v1/vehicle-model/{id}")
    VehicleModelDetailResponse getVehicleModel(@PathVariable("id") int id);

    @GetMapping("/v1/addon")
    AddonPageResponse getAddons(
            @RequestParam("page") int page,
            @RequestParam("size") int size);
}
