package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.BranchClient;
import com.seera.lumi.partner.gateway.controller.response.LocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final BranchClient branchClient;

    public List<LocationResponse> getLocations() {
        // TODO: Implement location retrieval
        // 1. Call branch service to get all active branches/locations
        // 2. Map to partner-facing LocationResponse
        throw new UnsupportedOperationException("Location retrieval not yet implemented");
    }
}
