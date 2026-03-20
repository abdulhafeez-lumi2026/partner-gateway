package com.seera.lumi.partner.gateway.client;

import com.seera.lumi.partner.gateway.client.response.BookingDetailResponse;
import com.seera.lumi.partner.gateway.client.response.BookingSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "booking-service", url = "${api.booking.baseUrl}")
public interface BookingClient {

    @GetMapping("/booking/details/{referenceNo}")
    BookingDetailResponse getBookingDetails(
            @RequestHeader("Authorization") String token,
            @PathVariable("referenceNo") String referenceNo);

    @GetMapping("/booking/summary/{referenceNo}")
    BookingSummaryResponse getBookingSummary(
            @RequestHeader("Authorization") String token,
            @PathVariable("referenceNo") String referenceNo);
}
