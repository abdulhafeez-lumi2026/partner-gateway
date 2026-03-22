package com.seera.lumi.partner.gateway.client.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FleetGroupListResponse {
    private int total;
    private List<VehicleGroupData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleGroupData {
        private Long id;
        private String code;
        private String displayName;
        private Map<String, String> description;
        private String thumbnail;
        private List<VehicleGroupImageData> images;
        private List<FeatureData> features;
        private Boolean isEnabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleGroupImageData {
        private Long id;
        private String url;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureData {
        private Long id;
        private Map<String, String> name;
        private String icon;
        private Long valueId;
        private Map<String, String> value;
        private Boolean primary;
    }
}
