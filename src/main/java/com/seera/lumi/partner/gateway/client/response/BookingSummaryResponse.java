package com.seera.lumi.partner.gateway.client.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSummaryResponse {
    private String referenceNo;
    private String status;
    private String vehicleGroup;
    private String pickupBranch;
    private String dropOffBranch;
    private String pickupDate;
    private String dropOffDate;
    private Map<String, Object> pricing;
    private String createdAt;
}
