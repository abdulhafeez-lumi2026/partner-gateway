package com.seera.lumi.partner.gateway.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPackage {
    private String type; // BASIC or FULL
    private BigDecimal baseRate;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal finalRate;
    private BigDecimal vatPercentage;
    private BigDecimal vatAmount;
    private BigDecimal totalWithVat;
    private BigDecimal deductible;
    private List<String> inclusions;
}
