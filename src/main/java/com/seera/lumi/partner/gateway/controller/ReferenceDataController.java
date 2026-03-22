package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.client.FleetLegacyClient;
import com.seera.lumi.partner.gateway.client.response.InsuranceListResponse;
import com.seera.lumi.partner.gateway.client.FleetClient;
import com.seera.lumi.partner.gateway.client.response.AddonPageResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/partner/v1")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final FleetLegacyClient fleetLegacyClient;
    private final FleetClient fleetClient;

    @GetMapping("/insurance")
    public ResponseEntity<List<InsuranceListResponse.InsuranceData>> getInsuranceProducts() {
        try {
            InsuranceListResponse response = fleetLegacyClient.getInsuranceList();
            if (response == null || response.getData() == null) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(response.getData());
        } catch (FeignException e) {
            log.error("Failed to fetch insurance products: status={}, message={}", e.status(), e.getMessage());
            throw new PartnerException("INSURANCE_FETCH_ERROR", "Failed to retrieve insurance products", 502, e);
        }
    }

    @GetMapping("/addons")
    public ResponseEntity<List<AddonPageResponse.AddonData>> getAddons() {
        try {
            AddonPageResponse response = fleetClient.getAddons(0, 1000);
            if (response == null || response.getContent() == null) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(response.getContent());
        } catch (FeignException e) {
            log.error("Failed to fetch addons: status={}, message={}", e.status(), e.getMessage());
            throw new PartnerException("ADDON_FETCH_ERROR", "Failed to retrieve addons", 502, e);
        }
    }
}
