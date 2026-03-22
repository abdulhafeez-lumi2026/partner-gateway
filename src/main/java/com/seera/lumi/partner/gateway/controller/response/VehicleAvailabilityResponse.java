package com.seera.lumi.partner.gateway.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehicleAvailabilityResponse {
    private String vehicleGroup;
    private String vehicleGroupName;
    private String imageUrl;
    private Integer seats;
    private Integer doors;
    private String transmission;
    private String fuelType;
    private Integer bags;
    private Integer dailyKmAllowance;
    private Double extraKmCharge;
    private List<PricingPackage> packages;
}
