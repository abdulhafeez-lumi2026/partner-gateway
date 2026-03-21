package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.BranchClient;
import com.seera.lumi.partner.gateway.client.response.BranchDetailResponse;
import com.seera.lumi.partner.gateway.client.response.BranchListResponse;
import com.seera.lumi.partner.gateway.controller.response.LocationResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private BranchClient branchClient;

    @InjectMocks
    private LocationService locationService;

    @Test
    void getLocations_success() {
        BranchListResponse.BranchData branch1 = new BranchListResponse.BranchData(
                1L, "JED-01", Map.of("en", "Jeddah Airport", "ar", "مطار جدة"));
        BranchListResponse.BranchData branch2 = new BranchListResponse.BranchData(
                2L, "RUH-01", Map.of("en", "Riyadh Airport", "ar", "مطار الرياض"));

        BranchListResponse response = BranchListResponse.builder()
                .total(2)
                .data(List.of(branch1, branch2))
                .build();

        when(branchClient.getBranches()).thenReturn(response);

        List<LocationResponse> result = locationService.getAllLocations();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLocationId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Jeddah Airport");
        assertThat(result.get(0).getNameAr()).isEqualTo("مطار جدة");
        assertThat(result.get(1).getLocationId()).isEqualTo(2L);
        assertThat(result.get(1).getName()).isEqualTo("Riyadh Airport");

        verify(branchClient).getBranches();
    }

    @Test
    void getLocations_nullResponse_returnsEmpty() {
        when(branchClient.getBranches()).thenReturn(null);

        List<LocationResponse> result = locationService.getAllLocations();

        assertThat(result).isEmpty();
    }

    @Test
    void getLocations_feignException_throwsPartnerException() {
        FeignException feignEx = mock(FeignException.class);
        when(feignEx.status()).thenReturn(500);
        when(branchClient.getBranches()).thenThrow(feignEx);

        assertThatThrownBy(() -> locationService.getAllLocations())
                .isInstanceOf(PartnerException.class)
                .satisfies(ex -> {
                    PartnerException pe = (PartnerException) ex;
                    assertThat(pe.getCode()).isEqualTo("LOCATION_FETCH_ERROR");
                    assertThat(pe.getHttpStatus()).isEqualTo(502);
                });
    }

    @Test
    void getLocationDetail_success() {
        BranchDetailResponse detail = BranchDetailResponse.builder()
                .id(1L)
                .code("JED-01")
                .name(Map.of("en", "Jeddah Airport", "ar", "مطار جدة"))
                .latitude(21.6796)
                .longitude(39.1565)
                .type("AIRPORT")
                .city(Map.of(
                        "name", Map.of("en", "Jeddah", "ar", "جدة"),
                        "region", Map.of("name", "Western Region")
                ))
                .build();

        when(branchClient.getBranchById(1L)).thenReturn(detail);

        LocationResponse result = locationService.getLocationDetail(1L);

        assertThat(result.getLocationId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Jeddah Airport");
        assertThat(result.getNameAr()).isEqualTo("مطار جدة");
        assertThat(result.getCity()).isEqualTo("Jeddah");
        assertThat(result.getCityAr()).isEqualTo("جدة");
        assertThat(result.getRegion()).isEqualTo("Western Region");
        assertThat(result.getLatitude()).isEqualTo(21.6796);
        assertThat(result.getLongitude()).isEqualTo(39.1565);
        assertThat(result.getOperationType()).isEqualTo("AIRPORT");

        verify(branchClient).getBranchById(1L);
    }
}
