package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.FleetClient;
import com.seera.lumi.partner.gateway.client.PricingClient;
import com.seera.lumi.partner.gateway.controller.request.AvailabilityRequest;
import com.seera.lumi.partner.gateway.controller.response.VehicleAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final PricingClient pricingClient;
    private final FleetClient fleetClient;

    public List<VehicleAvailabilityResponse> searchAvailability(AvailabilityRequest request) {
        // TODO: Implement availability search
        // 1. Call fleet service to get available vehicle groups
        // 2. Call pricing service to get rates for each group
        // 3. Combine and return
        throw new UnsupportedOperationException("Availability search not yet implemented");
    }
}
