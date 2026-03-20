package com.seera.lumi.partner.gateway.client;

import com.seera.lumi.partner.gateway.client.request.CreateCoreBookingRequest;
import com.seera.lumi.partner.gateway.client.response.CancelCoreBookingResponse;
import com.seera.lumi.partner.gateway.client.response.CoreBookingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "core-booking-service", url = "${api.core-booking.baseUrl}")
public interface CoreBookingClient {

    @PostMapping("/internal/api/v2/bookings/partner")
    CoreBookingResponse createPartnerBooking(@RequestBody CreateCoreBookingRequest request);

    @GetMapping("/internal/api/v2/bookings/partner/{referenceNo}")
    CoreBookingResponse getPartnerBooking(@PathVariable("referenceNo") String referenceNo);

    @DeleteMapping("/internal/api/v2/bookings/partner/{referenceNo}")
    CancelCoreBookingResponse cancelPartnerBooking(@PathVariable("referenceNo") String referenceNo);
}
