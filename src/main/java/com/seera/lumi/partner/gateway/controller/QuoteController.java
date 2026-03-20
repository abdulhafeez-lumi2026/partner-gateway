package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.controller.request.QuoteRequest;
import com.seera.lumi.partner.gateway.controller.response.QuoteResponse;
import com.seera.lumi.partner.gateway.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/partner/v1/quote")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(@Valid @RequestBody QuoteRequest request) {
        log.info("Quote request: vehicleGroup={}, pickup={}, dropoff={}",
                request.getVehicleGroup(), request.getPickupLocationId(), request.getDropoffLocationId());
        return ResponseEntity.ok(quoteService.createQuote(request));
    }

    @GetMapping("/{quoteId}")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable String quoteId) {
        log.info("Get quote: quoteId={}", quoteId);
        return ResponseEntity.ok(quoteService.getQuote(quoteId));
    }
}
