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
public class SearchOffersRequest {
    private Long pickupBranchId;
    private Long dropOffBranchId;
    private LocalDateTime pickupDate;
    private LocalDateTime dropOffDate;
    private String accountNo;
}
