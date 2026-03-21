package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.BranchClient;
import com.seera.lumi.partner.gateway.client.response.BranchDetailResponse;
import com.seera.lumi.partner.gateway.client.response.BranchListResponse;
import com.seera.lumi.partner.gateway.controller.response.LocationResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import com.seera.lumi.partner.gateway.security.PartnerContext;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

    private final BranchClient branchClient;

    /**
     * Returns locations filtered by the partner's allowed branches.
     * If no restriction is configured, all locations are returned.
     */
    public List<LocationResponse> getLocationsForPartner() {
        List<LocationResponse> all = getAllLocations();
        List<String> allowed = PartnerContext.getAllowedBranches();
        if (allowed.isEmpty()) {
            return all;
        }
        return all.stream()
                .filter(loc -> allowed.contains(String.valueOf(loc.getLocationId())))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "locations")
    public List<LocationResponse> getAllLocations() {
        try {
            log.info("Fetching locations from branch service");
            BranchListResponse branchList = branchClient.getBranches();

            if (branchList == null || branchList.getData() == null) {
                return List.of();
            }

            return branchList.getData().stream()
                    .map(this::mapToLocationResponse)
                    .collect(Collectors.toList());
        } catch (FeignException e) {
            log.error("Failed to fetch locations from branch service: status={}, message={}",
                    e.status(), e.getMessage(), e);
            throw new PartnerException("LOCATION_FETCH_ERROR",
                    "Failed to retrieve locations", 502, e);
        }
    }

    /**
     * Fetches full branch detail for a single location, including lat/lng and city info.
     */
    public LocationResponse getLocationDetail(Long locationId) {
        try {
            BranchDetailResponse detail = branchClient.getBranchById(locationId);
            return mapDetailToLocationResponse(detail);
        } catch (FeignException e) {
            log.error("Failed to fetch location detail for id={}: status={}", locationId, e.status(), e);
            throw new PartnerException("LOCATION_FETCH_ERROR",
                    "Failed to retrieve location detail for id: " + locationId, 502, e);
        }
    }

    private LocationResponse mapToLocationResponse(BranchListResponse.BranchData branch) {
        Map<String, String> name = branch.getName();
        return LocationResponse.builder()
                .locationId(branch.getId())
                .name(name != null ? name.get("en") : null)
                .nameAr(name != null ? name.get("ar") : null)
                .build();
    }

    @SuppressWarnings("unchecked")
    private LocationResponse mapDetailToLocationResponse(BranchDetailResponse detail) {
        Map<String, String> name = detail.getName();

        String cityEn = null;
        String cityAr = null;
        String regionName = null;

        if (detail.getCity() != null) {
            Map<String, Object> city = detail.getCity();
            Object cityNameObj = city.get("name");
            if (cityNameObj instanceof Map) {
                Map<String, String> cityName = (Map<String, String>) cityNameObj;
                cityEn = cityName.get("en");
                cityAr = cityName.get("ar");
            }
            Object regionObj = city.get("region");
            if (regionObj instanceof Map) {
                Map<String, Object> region = (Map<String, Object>) regionObj;
                regionName = (String) region.get("name");
            }
        }

        return LocationResponse.builder()
                .locationId(detail.getId())
                .name(name != null ? name.get("en") : null)
                .nameAr(name != null ? name.get("ar") : null)
                .city(cityEn)
                .cityAr(cityAr)
                .region(regionName)
                .latitude(detail.getLatitude())
                .longitude(detail.getLongitude())
                .operationType(detail.getType())
                .build();
    }
}
