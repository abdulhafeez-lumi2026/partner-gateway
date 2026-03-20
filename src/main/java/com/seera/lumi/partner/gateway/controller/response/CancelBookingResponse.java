package com.seera.lumi.partner.gateway.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelBookingResponse {
    private String bookingReference;
    private String status;
    private BigDecimal cancellationFee;
    private BigDecimal refundAmount;
}
