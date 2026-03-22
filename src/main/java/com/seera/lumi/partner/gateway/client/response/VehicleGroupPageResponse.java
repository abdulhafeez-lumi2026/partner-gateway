package com.seera.lumi.partner.gateway.client.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
        private Integer faceModelId;
        private VehicleModelBasicResponse vehicleModelBasicResponse;
        private String thumbnail;
        private Boolean enabled;
        private List<VehicleModelBasicResponse> models;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleModelBasicResponse {
        private Integer id;
        private Map<String, String> name;
        private VehicleMakeResponse make;
        private String materialId;
        private List<ModelImageData> images;
        private List<ModelFeatureData> features;
        private VehicleClassResponse vehicleClassResponse;
        private Integer modelYear;
        private Boolean enabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleMakeResponse {
        private Integer id;
        private Map<String, String> name;
        private Boolean enabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleClassResponse {
        private Integer id;
        private Map<String, String> name;
        private Boolean enabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelFeatureData {
        private FeatureInfo feature;
        private List<FeatureValueInfo> featureValue;
        private Boolean primary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureInfo {
        private Integer id;
        private String imageUrl;
        private Map<String, String> name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureValueInfo {
        private Integer id;
        private Map<String, String> name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelImageData {
        private Integer id;
        private String url;
        private Boolean primary;
    }
}
