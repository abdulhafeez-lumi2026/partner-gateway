package com.seera.lumi.partner.gateway.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleFeatures {
    private Integer seats;
    private Integer doors;
    private Integer bags;
    private String transmission;
    private String fuelType;
}
