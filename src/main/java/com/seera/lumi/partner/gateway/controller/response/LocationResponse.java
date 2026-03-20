package com.seera.lumi.partner.gateway.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private Long locationId;
    private String name;
    private String nameAr;
    private String city;
    private String cityAr;
    private String region;
    private Double latitude;
    private Double longitude;
    private String operationType;
    private String address;
    private String addressAr;
}
