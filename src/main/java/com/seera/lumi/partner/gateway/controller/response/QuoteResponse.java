package com.seera.lumi.partner.gateway.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {
    private String quoteId;
    private String vehicleGroup;
    private List<PricingPackage> packages;
    private String pickupLocation;
    private String dropoffLocation;
    private LocalDateTime pickupDateTime;
    private LocalDateTime dropoffDateTime;
    private String currency;
    private LocalDateTime validUntil;
}
