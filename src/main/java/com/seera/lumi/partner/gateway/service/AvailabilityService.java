package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.FleetClient;
import com.seera.lumi.partner.gateway.client.PartnerServiceClient;
import com.seera.lumi.partner.gateway.client.request.InternalAvailabilityRequest;
import com.seera.lumi.partner.gateway.client.response.VehicleGroupPageResponse;
import com.seera.lumi.partner.gateway.controller.request.AvailabilityRequest;
import com.seera.lumi.partner.gateway.controller.response.VehicleAvailabilityResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import com.seera.lumi.partner.gateway.security.PartnerContext;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final PartnerServiceClient partnerServiceClient;
    private final FleetClient fleetClient;

    public List<VehicleAvailabilityResponse> searchAvailability(AvailabilityRequest request) {
        long totalStart = System.currentTimeMillis();
        try {
            validateAllowedBranch(request.getPickupLocationId(), "pickup");
            validateAllowedBranch(request.getDropoffLocationId(), "dropoff");

            log.info("Searching availability: pickup={}, dropoff={}, from={}, to={}, debtorCode={}",
                    request.getPickupLocationId(), request.getDropoffLocationId(),
                    request.getPickupDateTime(), request.getDropoffDateTime(),
                    PartnerContext.getDebtorCode());

            InternalAvailabilityRequest internalRequest = InternalAvailabilityRequest.builder()
                    .pickupLocationId(request.getPickupLocationId())
                    .dropoffLocationId(request.getDropoffLocationId())
                    .pickupDateTime(request.getPickupDateTime())
                    .dropoffDateTime(request.getDropoffDateTime())
                    .debtorCode(PartnerContext.getDebtorCode())
                    .allowedBranches(PartnerContext.getAllowedBranches())
                    .allowedVehicleGroups(PartnerContext.getAllowedVehicleGroups())
                    .build();

            long pricingStart = System.currentTimeMillis();
            List<VehicleAvailabilityResponse> responses = partnerServiceClient.searchAvailability(internalRequest);
            long pricingMs = System.currentTimeMillis() - pricingStart;
            log.info("[TIMING] partner-service availability: {}ms ({}s)", pricingMs, pricingMs / 1000.0);

            if (responses == null || responses.isEmpty()) {
                return List.of();
            }

            // Enrich with vehicle group metadata from core fleet service
            long fleetStart = System.currentTimeMillis();
            Map<Integer, VehicleGroupPageResponse.VehicleGroupData> vehicleGroupMap = getVehicleGroupMap();
            long fleetMs = System.currentTimeMillis() - fleetStart;
            log.info("[TIMING] core-fleet-service vehicle groups: {}ms ({}s)", fleetMs, fleetMs / 1000.0);

            long enrichStart = System.currentTimeMillis();
            List<VehicleAvailabilityResponse> enriched = responses.stream()
                    .map(response -> enrichWithVehicleMetadata(response, vehicleGroupMap))
                    .collect(Collectors.toList());
            long enrichMs = System.currentTimeMillis() - enrichStart;
            log.info("[TIMING] enrichment: {}ms ({}s)", enrichMs, enrichMs / 1000.0);

            long totalMs = System.currentTimeMillis() - totalStart;
            log.info("[TIMING] total availability: {}ms ({}s)", totalMs, totalMs / 1000.0);

            return enriched;
        } catch (FeignException e) {
            log.error("Failed to search availability from partner service: status={}, message={}",
                    e.status(), e.getMessage(), e);
            throw new PartnerException("AVAILABILITY_SEARCH_ERROR",
                    "Failed to search vehicle availability", 502, e);
        }
    }

    private VehicleAvailabilityResponse enrichWithVehicleMetadata(
            VehicleAvailabilityResponse response,
            Map<Integer, VehicleGroupPageResponse.VehicleGroupData> vehicleGroupMap) {

        Integer groupId;
        try {
            groupId = Integer.parseInt(response.getVehicleGroup());
        } catch (NumberFormatException e) {
            return response;
        }

        VehicleGroupPageResponse.VehicleGroupData vg = vehicleGroupMap.get(groupId);
        if (vg == null) {
            return response;
        }

        // Set image from thumbnail or first model image
        response.setImageUrl(vg.getThumbnail());

        // Extract details from face model (vehicleModelBasicResponse)
        VehicleGroupPageResponse.VehicleModelBasicResponse faceModel = vg.getVehicleModelBasicResponse();
        if (faceModel != null) {
            // Build display name: "Make Model" (e.g. "Hyundai Elantra")
            String makeName = faceModel.getMake() != null && faceModel.getMake().getName() != null
                    ? faceModel.getMake().getName().get("en") : null;
            String modelName = faceModel.getName() != null ? faceModel.getName().get("en") : null;
            if (makeName != null && modelName != null) {
                response.setVehicleGroupName(makeName + " " + modelName);
            } else {
                response.setVehicleGroupName(vg.getDisplayName());
            }

            // Set category from vehicle class (e.g. "Economy", "Compact")
            if (faceModel.getVehicleClassResponse() != null
                    && faceModel.getVehicleClassResponse().getName() != null) {
                response.setCategory(faceModel.getVehicleClassResponse().getName().get("en"));
            }

            // Extract features (seats, doors, transmission, fuel type, bags)
            if (faceModel.getFeatures() != null) {
                log.info("Group {} features: {}", groupId, faceModel.getFeatures());
                for (VehicleGroupPageResponse.ModelFeatureData mf : faceModel.getFeatures()) {
                    if (mf.getFeature() == null || mf.getFeature().getName() == null) continue;
                    String featureName = mf.getFeature().getName().get("en");
                    if (featureName == null) continue;

                    String featureValue = getFirstFeatureValue(mf);
                    if (featureValue == null) continue;

                    String fn = featureName.toLowerCase();
                    if (fn.contains("seat") || fn.contains("passenger")) {
                        response.setSeats(parseIntOrNull(featureValue));
                    } else if (fn.contains("door")) {
                        response.setDoors(parseIntOrNull(featureValue));
                    } else if (fn.contains("transmission")) {
                        response.setTransmission(featureValue);
                    } else if (fn.contains("fuel")) {
                        response.setFuelType(featureValue);
                    } else if (fn.contains("bag") || fn.contains("luggage")) {
                        response.setBags(parseIntOrNull(featureValue));
                    }
                }
            }

            // Fallback image from face model images
            if (response.getImageUrl() == null && faceModel.getImages() != null) {
                for (VehicleGroupPageResponse.ModelImageData img : faceModel.getImages()) {
                    if (img.getUrl() != null) {
                        response.setImageUrl(img.getUrl());
                        break;
                    }
                }
            }
        } else {
            response.setVehicleGroupName(vg.getDisplayName());
        }

        return response;
    }

    private String getFirstFeatureValue(VehicleGroupPageResponse.ModelFeatureData mf) {
        if (mf.getFeatureValue() != null && !mf.getFeatureValue().isEmpty()) {
            VehicleGroupPageResponse.FeatureValueInfo fv = mf.getFeatureValue().get(0);
            if (fv.getName() != null) {
                return fv.getName().get("en");
            }
        }
        return null;
    }

    @Cacheable(value = "vehicleGroups")
    public Map<Integer, VehicleGroupPageResponse.VehicleGroupData> getVehicleGroupMap() {
        try {
            VehicleGroupPageResponse response = fleetClient.getVehicleGroups(0, 1000, true);
            log.info("Core fleet service response: totalElements={}", response != null ? response.getTotalElements() : 0);
            if (response == null || response.getContent() == null) {
                return Map.of();
            }
            return response.getContent().stream()
                    .collect(Collectors.toMap(
                            VehicleGroupPageResponse.VehicleGroupData::getId,
                            Function.identity(),
                            (a, b) -> a));
        } catch (Exception e) {
            log.warn("Failed to fetch vehicle groups from core fleet service: {}", e.getMessage());
            return Map.of();
        }
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void validateAllowedBranch(Long branchId, String label) {
        List<String> allowed = PartnerContext.getAllowedBranches();
        if (!allowed.isEmpty() && !allowed.contains(String.valueOf(branchId))) {
            throw new PartnerException("BRANCH_NOT_ALLOWED",
                    label + " branch " + branchId + " is not in your allowed branches", 403);
        }
    }
}
