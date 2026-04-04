package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.controller.response.LocationResponse;
import com.seera.lumi.partner.gateway.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/partner/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Reference Data", description = "Locations and vehicle groups")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "Get locations", description = "Get available pickup/dropoff locations. Filtered by partner's allowed branches.")
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getLocations() {
        log.info("Get all locations");
        return ResponseEntity.ok(locationService.getLocationsForPartner());
    }
}
