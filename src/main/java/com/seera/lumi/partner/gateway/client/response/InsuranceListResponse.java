package com.seera.lumi.partner.gateway.client.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceListResponse {
    private int total;
    private List<InsuranceData> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsuranceData {
        private Long id;
        private String code;
        private Map<String, String> name;
        private Map<String, String> description;
        private boolean recommended;
        private boolean isEnabled;
        private double deductible;
    }
}
