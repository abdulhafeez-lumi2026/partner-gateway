package com.seera.lumi.partner.gateway.client.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleGroupPageResponse {
    private List<VehicleGroupData> content;
    private int totalElements;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleGroupData {
        private Integer id;
        private String code;
        private String displayName;
        private Map<String, String> description;
        private String thumbnail;
        private Boolean enabled;
    }
}
