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
public class BranchListResponse {
    private int total;
    private List<BranchData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BranchData {
        private Long id;
        private String code;
        private Map<String, String> name;
    }
}
