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
public class AddonPageResponse {
    private List<AddonData> content;
    private int totalElements;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddonData {
        private Integer id;
        private String code;
        private Map<String, String> name;
        private Map<String, String> description;
        private String imageUrl;
        private Boolean enabled;
    }
}
