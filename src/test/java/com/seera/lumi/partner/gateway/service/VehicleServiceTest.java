package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.FleetClient;
import com.seera.lumi.partner.gateway.client.response.VehicleGroupPageResponse;
import com.seera.lumi.partner.gateway.controller.response.VehicleGroupResponse;
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
class VehicleServiceTest {

    @Mock
    private FleetClient fleetClient;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void getVehicleGroups_success() {
        VehicleGroupPageResponse.VehicleGroupData group1 = new VehicleGroupPageResponse.VehicleGroupData(
                1, "ECAR", "Economy Car", Map.of("en", "Small economy car"), "https://cdn.example.com/ecar.png", true);
        VehicleGroupPageResponse.VehicleGroupData group2 = new VehicleGroupPageResponse.VehicleGroupData(
                2, "CCAR", "Compact Car", Map.of("en", "Compact sedan"), "https://cdn.example.com/ccar.png", true);

        VehicleGroupPageResponse response = VehicleGroupPageResponse.builder()
                .content(List.of(group1, group2))
                .totalElements(2)
                .build();

        when(fleetClient.getVehicleGroups(0, 1000, true)).thenReturn(response);

        List<VehicleGroupResponse> result = vehicleService.getAllVehicleGroups();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("ECAR");
        assertThat(result.get(0).getName()).isEqualTo("Economy Car");
        assertThat(result.get(0).getDescription()).isEqualTo("Small economy car");
        assertThat(result.get(0).getImageUrl()).isEqualTo("https://cdn.example.com/ecar.png");
        assertThat(result.get(1).getCode()).isEqualTo("CCAR");
        assertThat(result.get(1).getName()).isEqualTo("Compact Car");

        verify(fleetClient).getVehicleGroups(0, 1000, true);
    }

    @Test
    void getVehicleGroups_nullResponse_returnsEmpty() {
        when(fleetClient.getVehicleGroups(0, 1000, true)).thenReturn(null);

        List<VehicleGroupResponse> result = vehicleService.getAllVehicleGroups();

        assertThat(result).isEmpty();
    }

    @Test
    void getVehicleGroups_feignException_throwsPartnerException() {
        FeignException feignEx = mock(FeignException.class);
        when(feignEx.status()).thenReturn(500);
        when(fleetClient.getVehicleGroups(0, 1000, true)).thenThrow(feignEx);

        assertThatThrownBy(() -> vehicleService.getAllVehicleGroups())
                .isInstanceOf(PartnerException.class)
                .satisfies(ex -> {
                    PartnerException pe = (PartnerException) ex;
                    assertThat(pe.getCode()).isEqualTo("VEHICLE_GROUP_FETCH_ERROR");
                    assertThat(pe.getHttpStatus()).isEqualTo(502);
                });
    }
}
