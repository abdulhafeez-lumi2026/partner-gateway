package com.seera.lumi.partner.gateway.client.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuoteRequest {
    private Long pickupBranchId;
    private Long dropOffBranchId;
    private Long pickupDateTime;
    private Long dropOffDateTime;
    private Long vehicleGroupId;
    private String vehicleGroupCode;
    private Long insuranceId;
    private String promoCode;
    private List<Long> addOnIds;
    private String accountNo;
}
