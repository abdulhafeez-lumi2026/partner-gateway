package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.controller.response.LocationResponse;
import com.seera.lumi.partner.gateway.service.LocationService;
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
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getLocations() {
        log.info("Get all locations");
        return ResponseEntity.ok(locationService.getLocationsForPartner());
    }
}
