package com.seera.lumi.partner.gateway.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleGroupResponse {
    private String code;
    private String name;
    private String description;
    private String imageUrl;
    private Integer seats;
    private Integer doors;
    private String transmission;
    private String fuelType;
    private Integer bags;
    private Boolean airConditioning;
}
