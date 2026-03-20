package com.seera.lumi.partner.gateway.controller.response;

import com.seera.lumi.partner.gateway.controller.request.DriverInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private String bookingReference;
    private String status;
    private String quoteId;
    private String vehicleGroup;
    private String pickupLocation;
    private String dropoffLocation;
    private LocalDateTime pickupDateTime;
    private LocalDateTime dropoffDateTime;
    private PricingPackage pricing;
    private DriverInfo driver;
    private LocalDateTime createdAt;
}
