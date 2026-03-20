package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.controller.request.AvailabilityRequest;
import com.seera.lumi.partner.gateway.controller.response.VehicleAvailabilityResponse;
import com.seera.lumi.partner.gateway.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/partner/v1/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping
    public ResponseEntity<List<VehicleAvailabilityResponse>> searchAvailability(
            @Valid @RequestBody AvailabilityRequest request) {
        log.info("Availability search: pickup={}, dropoff={}, from={}, to={}",
                request.getPickupLocationId(), request.getDropoffLocationId(),
                request.getPickupDateTime(), request.getDropoffDateTime());
        return ResponseEntity.ok(availabilityService.searchAvailability(request));
    }
}
