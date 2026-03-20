package com.seera.lumi.partner.gateway.client.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerResponse {
    private Long partnerId;
    private String partnerCode;
    private String quoteMode;
    private String bookingMode;
    private List<String> allowedBranches;
    private List<String> allowedVehicleGroups;
    private Integer rateLimit;
    private String status;
    private String webhookUrl;
}
