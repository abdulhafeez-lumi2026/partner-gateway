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
public class CancelCoreBookingResponse {
    private String referenceNo;
    private String status;
    private String partnerCode;
    private Double cancellationFee;
    private Double refundAmount;
    private LocalDateTime cancelledAt;
}
