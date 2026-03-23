package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.FleetClient;
import com.seera.lumi.partner.gateway.client.PartnerServiceClient;
import com.seera.lumi.partner.gateway.client.request.InternalAvailabilityRequest;
import com.seera.lumi.partner.gateway.client.response.VehicleGroupPageResponse;
import com.seera.lumi.partner.gateway.client.response.VehicleModelDetailResponse;
import com.seera.lumi.partner.gateway.controller.request.AvailabilityRequest;
import com.seera.lumi.partner.gateway.controller.response.AvailabilitySearchResponse;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final PartnerServiceClient partnerServiceClient;
    private final FleetClient fleetClient;

    // In-memory cache for model specs (populated once from fleet service)
    private final Map<Integer, Map<String, Object>> modelSpecCache = new ConcurrentHashMap<>();

    public AvailabilitySearchResponse searchAvailability(AvailabilityRequest request) {
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
            AvailabilitySearchResponse partnerResponse = partnerServiceClient.searchAvailability(internalRequest);
            long pricingMs = System.currentTimeMillis() - pricingStart;
            log.info("[TIMING] partner-service availability: {}ms ({}s)", pricingMs, pricingMs / 1000.0);

            List<VehicleAvailabilityResponse> responses = partnerResponse != null ? partnerResponse.getVehicles() : null;

            if (responses == null || responses.isEmpty()) {
                return AvailabilitySearchResponse.builder()
                        .pickupLocationId(request.getPickupLocationId())
                        .dropoffLocationId(request.getDropoffLocationId())
                        .pickupDateTime(request.getPickupDateTime())
                        .dropoffDateTime(request.getDropoffDateTime())
                        .promoCode(partnerResponse != null ? partnerResponse.getPromoCode() : null)
                        .totalVehicles(0)
                        .vehicles(List.of())
                        .build();
            }

            // Enrich with vehicle group metadata from core fleet service
            long fleetStart = System.currentTimeMillis();
            Map<String, VehicleGroupPageResponse.VehicleGroupData> vehicleGroupMap = getVehicleGroupMap();
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

            return AvailabilitySearchResponse.builder()
                    .pickupLocationId(request.getPickupLocationId())
                    .dropoffLocationId(request.getDropoffLocationId())
                    .pickupDateTime(request.getPickupDateTime())
                    .dropoffDateTime(request.getDropoffDateTime())
                    .promoCode(partnerResponse.getPromoCode())
                    .totalVehicles(enriched.size())
                    .vehicles(enriched)
                    .build();
        } catch (FeignException e) {
            log.error("Failed to search availability from partner service: status={}, message={}",
                    e.status(), e.getMessage(), e);
            throw new PartnerException("AVAILABILITY_SEARCH_ERROR",
                    "Failed to search vehicle availability", 502, e);
        }
    }

    private VehicleAvailabilityResponse enrichWithVehicleMetadata(
            VehicleAvailabilityResponse response,
            Map<String, VehicleGroupPageResponse.VehicleGroupData> vehicleGroupMap) {

        String groupCode = response.getVehicleGroup();
        VehicleGroupPageResponse.VehicleGroupData vg = vehicleGroupMap.get(groupCode);
        if (vg == null) {
            return response;
        }

        // Set image from thumbnail
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

            // Get specification from vehicle model detail (has seatingCapacity, doors, luggage, etc.)
            Map<String, Object> spec = getModelSpecification(vg.getFaceModelId());
            if (spec != null && !spec.isEmpty()) {
                log.info("Group {} specification: seatingCapacity={}, doors={}, fuelType={}, luggageCountBig={}",
                        groupCode, spec.get("seatingCapacity"), spec.get("doors"),
                        spec.get("fuelType"), spec.get("luggageCountBig"));

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

    private Map<String, Object> getModelSpecification(Integer faceModelId) {
        if (faceModelId == null) return null;
        return modelSpecCache.computeIfAbsent(faceModelId, id -> {
            try {
                long start = System.currentTimeMillis();
                VehicleModelDetailResponse model = fleetClient.getVehicleModel(id);
                long ms = System.currentTimeMillis() - start;
                log.info("[TIMING] fleet getVehicleModel({}): {}ms", id, ms);
                return model != null ? model.getSpecification() : null;
            } catch (Exception e) {
                log.warn("Failed to fetch vehicle model {}: {}", id, e.getMessage());
                return null;
            }
        });
    }

    @Cacheable(value = "vehicleGroups")
    public Map<String, VehicleGroupPageResponse.VehicleGroupData> getVehicleGroupMap() {
        try {
            VehicleGroupPageResponse response = fleetClient.getVehicleGroups(0, 1000, true);
            log.info("Core fleet service response: totalElements={}", response != null ? response.getTotalElements() : 0);
            if (response == null || response.getContent() == null) {
                return Map.of();
            }
            return response.getContent().stream()
                    .filter(vg -> vg.getCode() != null)
                    .collect(Collectors.toMap(
                            VehicleGroupPageResponse.VehicleGroupData::getCode,
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
