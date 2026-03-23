package com.seera.lumi.partner.gateway.client.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModelDetailResponse {
    private Integer id;
    private Map<String, String> name;
    private VehicleGroupPageResponse.VehicleMakeResponse make;
    private Map<String, Object> specification;
    private String primaryImageUrl;
    private Boolean enabled;
}
