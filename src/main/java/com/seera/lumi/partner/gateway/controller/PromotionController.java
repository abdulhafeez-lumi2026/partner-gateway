package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.client.PartnerServiceClient;
import com.seera.lumi.partner.gateway.client.response.ActivePromotionResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import com.seera.lumi.partner.gateway.security.PartnerContext;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/partner/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PartnerServiceClient partnerServiceClient;

    @GetMapping("/active")
    public ResponseEntity<ActivePromotionResponse> getActivePromotion() {
        String debtorCode = PartnerContext.getDebtorCode();
        log.info("Fetching active promotion for debtorCode={}", debtorCode);
        try {
            ActivePromotionResponse promo = partnerServiceClient.getActivePromotion(debtorCode);
            return ResponseEntity.ok(promo);
        } catch (FeignException e) {
            if (e.status() == 404) {
                return ResponseEntity.notFound().build();
            }
            throw new PartnerException("PROMOTION_FETCH_ERROR", "Failed to fetch active promotion", 502, e);
        }
    }
}
