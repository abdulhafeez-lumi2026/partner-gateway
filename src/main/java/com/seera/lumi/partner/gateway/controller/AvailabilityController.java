package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.controller.request.AvailabilityRequest;
import com.seera.lumi.partner.gateway.controller.response.AvailabilitySearchResponse;
import com.seera.lumi.partner.gateway.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/partner/v1/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Search available vehicles with pricing")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @Operation(summary = "Search availability", description = "Search available vehicles for given dates and locations. Returns B2C/B2B rates with partner-specific discounts auto-applied.")
    @PostMapping
    public ResponseEntity<AvailabilitySearchResponse> searchAvailability(
            @Valid @RequestBody AvailabilityRequest request) {
        log.info("Availability search: pickup={}, dropoff={}, from={}, to={}",
                request.getPickupLocationId(), request.getDropoffLocationId(),
                request.getPickupDateTime(), request.getDropoffDateTime());
        return ResponseEntity.ok(availabilityService.searchAvailability(request));
    }
}
