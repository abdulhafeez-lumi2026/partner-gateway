package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.controller.response.VehicleGroupResponse;
import com.seera.lumi.partner.gateway.service.VehicleService;
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
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping("/groups")
    public ResponseEntity<List<VehicleGroupResponse>> getVehicleGroups() {
        log.info("Get all vehicle groups");
        return ResponseEntity.ok(vehicleService.getVehicleGroupsForPartner());
    }
}
