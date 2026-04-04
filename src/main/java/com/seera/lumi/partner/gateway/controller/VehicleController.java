package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.controller.response.VehicleGroupResponse;
import com.seera.lumi.partner.gateway.service.VehicleService;
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
@RequestMapping("/api/partner/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Reference Data", description = "Locations and vehicle groups")
public class VehicleController {

    private final VehicleService vehicleService;

    @Operation(summary = "Get vehicle groups", description = "Get available vehicle groups. Filtered by partner's allowed vehicle groups.")
    @GetMapping("/groups")
    public ResponseEntity<List<VehicleGroupResponse>> getVehicleGroups() {
        log.info("Get all vehicle groups");
        return ResponseEntity.ok(vehicleService.getVehicleGroupsForPartner());
    }
}
