package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.controller.request.QuoteRequest;
import com.seera.lumi.partner.gateway.controller.response.QuoteResponse;
import com.seera.lumi.partner.gateway.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/partner/v1/quote")
@RequiredArgsConstructor
@Tag(name = "Quote", description = "Create and retrieve pricing quotes")
public class QuoteController {

    private final QuoteService quoteService;

    @Operation(summary = "Create quote", description = "Create a pricing quote for a vehicle group. Optionally include insuranceId and addOnIds.")
    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(@Valid @RequestBody QuoteRequest request) {
        log.info("Quote request: vehicleGroup={}, pickup={}, dropoff={}",
                request.getVehicleGroup(), request.getPickupLocationId(), request.getDropoffLocationId());
        return ResponseEntity.ok(quoteService.createQuote(request));
    }

    @Operation(summary = "Get quote", description = "Retrieve a previously created quote. Quotes expire after 30 minutes.")
    @GetMapping("/{quoteId}")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable String quoteId) {
        log.info("Get quote: quoteId={}", quoteId);
        return ResponseEntity.ok(quoteService.getQuote(quoteId));
    }
}
