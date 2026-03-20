package com.seera.lumi.partner.gateway.client.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreBookingResponse {
    private String referenceNo;
    private String displayId;
    private String status;
    private String partnerCode;
    private String bookingMode;
    private LocalDateTime pickupDateTime;
    private LocalDateTime dropOffDateTime;
    private Long pickupBranchId;
    private Long dropOffBranchId;
    private Long vehicleGroupId;
    private String driverFirstName;
    private String driverLastName;
    private String driverEmail;
    private Double totalPrice;
    private Double rentalSum;
    private Double vat;
    private Double discount;
    private String currency;
    private LocalDateTime createdOn;
}
