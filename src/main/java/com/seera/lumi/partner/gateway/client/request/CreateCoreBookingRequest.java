package com.seera.lumi.partner.gateway.client.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCoreBookingRequest {
    private String partnerCode;
    private String quoteId;
    private String packageType;
    private String bookingMode;
    private Long pickupBranchId;
    private Long dropOffBranchId;
    private LocalDateTime pickupDateTime;
    private LocalDateTime dropOffDateTime;
    private Long vehicleGroupId;
    // Pricing
    private Double totalPrice;
    private Double rentalSum;
    private Double vat;
    private Double vatPercentage;
    private Double discount;
    private Double discountPercentage;
    private Double insuranceSum;
    private Double dropOffSum;
    private Double soldDays;
    // Driver (passport-only)
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String passportNumber;
    private String nationality;
}
