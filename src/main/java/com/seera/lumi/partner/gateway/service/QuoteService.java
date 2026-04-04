package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.PartnerServiceClient;
import com.seera.lumi.partner.gateway.client.request.InternalQuoteRequest;
import com.seera.lumi.partner.gateway.controller.request.QuoteRequest;
import com.seera.lumi.partner.gateway.controller.response.QuoteResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import com.seera.lumi.partner.gateway.security.PartnerContext;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private final PartnerServiceClient partnerServiceClient;

    public QuoteResponse createQuote(QuoteRequest request) {
        validateAllowedBranch(request.getPickupLocationId(), "pickup");
        validateAllowedBranch(request.getDropoffLocationId(), "dropoff");
        validateAllowedVehicleGroup(request.getVehicleGroup());

        InternalQuoteRequest internalRequest = InternalQuoteRequest.builder()
                .pickupLocationId(request.getPickupLocationId())
                .dropoffLocationId(request.getDropoffLocationId())
                .pickupDateTime(request.getPickupDateTime())
                .dropoffDateTime(request.getDropoffDateTime())
                .vehicleGroup(request.getVehicleGroup())
                .insuranceId(request.getInsuranceId())
                .addOnIds(request.getAddOnIds())
                .partnerCode(PartnerContext.getPartnerCode())
                .allowedBranches(PartnerContext.getAllowedBranches())
                .allowedVehicleGroups(PartnerContext.getAllowedVehicleGroups())
                .build();

        try {
            return partnerServiceClient.createQuote(internalRequest);
        } catch (FeignException e) {
            log.error("Failed to create quote: status={}, message={}", e.status(), e.getMessage(), e);
            throw new PartnerException("QUOTE_CREATE_ERROR", "Failed to create quote", 502, e);
        }
    }

    public QuoteResponse getQuote(String quoteId) {
        try {
            return partnerServiceClient.getQuote(quoteId);
        } catch (FeignException e) {
            if (e.status() == 404 || e.status() == 500) {
                throw new PartnerException("QUOTE_NOT_FOUND", "Quote not found or expired: " + quoteId, 404);
            }
            throw new PartnerException("QUOTE_FETCH_ERROR", "Failed to fetch quote", 502, e);
        }
    }

    private void validateAllowedBranch(Long branchId, String label) {
        List<String> allowed = PartnerContext.getAllowedBranches();
        if (!allowed.isEmpty() && !allowed.contains(String.valueOf(branchId))) {
            throw new PartnerException("BRANCH_NOT_ALLOWED",
                    label + " branch " + branchId + " is not in your allowed branches", 403);
        }
    }

    private void validateAllowedVehicleGroup(String vehicleGroup) {
        List<String> allowed = PartnerContext.getAllowedVehicleGroups();
        if (!allowed.isEmpty() && !allowed.contains(vehicleGroup)) {
            throw new PartnerException("VEHICLE_GROUP_NOT_ALLOWED",
                    "Vehicle group " + vehicleGroup + " is not in your allowed vehicle groups", 403);
        }
    }
}
