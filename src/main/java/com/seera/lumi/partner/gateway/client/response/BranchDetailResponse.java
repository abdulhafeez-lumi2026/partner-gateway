package com.seera.lumi.partner.gateway.client.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDetailResponse {
    private Long id;
    private String code;
    private Map<String, String> name;
    private String email;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private Map<String, Object> city;
    private String type;
    private String timezone;
    private String directions;
}
