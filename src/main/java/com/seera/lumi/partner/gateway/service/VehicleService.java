package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.FleetClient;
import com.seera.lumi.partner.gateway.controller.response.VehicleGroupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final FleetClient fleetClient;

    public List<VehicleGroupResponse> getVehicleGroups() {
        // TODO: Implement vehicle group retrieval
        // 1. Call fleet service to get all vehicle groups
        // 2. Map to partner-facing VehicleGroupResponse
        throw new UnsupportedOperationException("Vehicle group retrieval not yet implemented");
    }
}
