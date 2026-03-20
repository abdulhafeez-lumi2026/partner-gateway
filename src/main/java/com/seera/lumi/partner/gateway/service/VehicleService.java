package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.FleetClient;
import com.seera.lumi.partner.gateway.client.response.VehicleGroupPageResponse;
import com.seera.lumi.partner.gateway.controller.response.VehicleGroupResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final FleetClient fleetClient;

    @Cacheable(value = "vehicleGroups")
    public List<VehicleGroupResponse> getVehicleGroups() {
        try {
            log.info("Fetching vehicle groups from fleet service");
            VehicleGroupPageResponse page = fleetClient.getVehicleGroups(0, 1000, true);

            if (page == null || page.getContent() == null) {
                return List.of();
            }

            return page.getContent().stream()
                    .map(this::mapToVehicleGroupResponse)
                    .collect(Collectors.toList());
        } catch (FeignException e) {
            log.error("Failed to fetch vehicle groups from fleet service: status={}, message={}",
                    e.status(), e.getMessage(), e);
            throw new PartnerException("VEHICLE_GROUP_FETCH_ERROR",
                    "Failed to retrieve vehicle groups", 502, e);
        }
    }

    private VehicleGroupResponse mapToVehicleGroupResponse(VehicleGroupPageResponse.VehicleGroupData data) {
        Map<String, String> description = data.getDescription();
        return VehicleGroupResponse.builder()
                .code(data.getCode())
                .name(data.getDisplayName())
                .description(description != null ? description.get("en") : null)
                .imageUrl(data.getThumbnail())
                .build();
    }
}
