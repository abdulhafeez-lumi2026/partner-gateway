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
public class BookingDetailResponse {
    private String referenceNo;
    private String status;
    private Map<String, Object> vehicle;
    private Map<String, Object> pickupBranch;
    private Map<String, Object> dropOffBranch;
    private String pickupDate;
    private String dropOffDate;
    private Map<String, Object> driver;
    private Map<String, Object> pricing;
    private String createdAt;
}
