package com.seera.lumi.partner.gateway.client.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivePromotionResponse {
    private String code;
    private BigDecimal discountPercentage;
    private LocalDate validFrom;
    private LocalDate validTo;
}
