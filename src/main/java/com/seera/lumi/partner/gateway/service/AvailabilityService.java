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

    @SuppressWarnings("unchecked")
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

        response.setVehicleGroupName(vg.getDisplayName());
        response.setImageUrl(vg.getThumbnail());

        // Extract specs from face model's specification map
        VehicleGroupPageResponse.FaceModelResponse faceModel = vg.getFaceModelResponse();
        if (faceModel != null && faceModel.getSpecification() != null) {
            Map<String, Object> spec = faceModel.getSpecification();
            log.info("Group {} specs: {}", groupId, spec);

            response.setSeats(toInteger(spec.get("seatingCapacity")));
            response.setDoors(toInteger(spec.get("doors")));
            response.setTransmission(toString(spec.get("transmission")));
            response.setFuelType(toString(spec.get("fuelType")));

            // Total bags = big + medium + small
            int bags = intOrZero(spec.get("luggageCountBig"))
                    + intOrZero(spec.get("luggageCountMedium"))
                    + intOrZero(spec.get("luggageCountSmall"));
            if (bags > 0) {
                response.setBags(bags);
            }

            // Set vehicle make/model name from face model
            if (faceModel.getMake() != null && faceModel.getMake().getName() != null) {
                String makeName = faceModel.getMake().getName().get("en");
                String modelName = faceModel.getName() != null ? faceModel.getName().get("en") : null;
                if (makeName != null && modelName != null) {
                    response.setVehicleGroupName(makeName + " " + modelName);
                }
            }
        }

        // Get primary image from models if thumbnail is null
        if (response.getImageUrl() == null && vg.getModels() != null) {
            for (VehicleGroupPageResponse.VehicleModelData model : vg.getModels()) {
                if (model.getImages() != null) {
                    for (VehicleGroupPageResponse.ModelImageData img : model.getImages()) {
                        if (Boolean.TRUE.equals(img.getPrimary()) && img.getUrl() != null) {
                            response.setImageUrl(img.getUrl());
                            break;
                        }
                    }
                }
                if (response.getImageUrl() != null) break;
            }
        }

        return response;
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

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toString(Object value) {
        return value != null ? value.toString() : null;
    }

    private int intOrZero(Object value) {
        Integer i = toInteger(value);
        return i != null ? i : 0;
    }

    private void validateAllowedBranch(Long branchId, String label) {
        List<String> allowed = PartnerContext.getAllowedBranches();
        if (!allowed.isEmpty() && !allowed.contains(String.valueOf(branchId))) {
            throw new PartnerException("BRANCH_NOT_ALLOWED",
                    label + " branch " + branchId + " is not in your allowed branches", 403);
        }
    }
}
