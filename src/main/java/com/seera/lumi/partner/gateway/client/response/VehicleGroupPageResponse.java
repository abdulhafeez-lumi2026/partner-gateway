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
        private FaceModelResponse faceModelResponse;
        private String thumbnail;
        private Boolean enabled;
        private List<VehicleModelData> models;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FaceModelResponse {
        private Integer id;
        private Map<String, String> name;
        private VehicleMakeResponse make;
        private String modelYear;
        private Map<String, Object> specification;
        private Boolean enabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleMakeResponse {
        private Integer id;
        private Map<String, String> name;
        private String logo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VehicleModelData {
        private Integer id;
        private Map<String, String> name;
        private VehicleMakeResponse make;
        private List<ModelImageData> images;
        private Integer modelYear;
        private Boolean enabled;
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
