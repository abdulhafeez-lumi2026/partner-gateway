package com.seera.lumi.partner.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "booking-service", url = "${api.booking.baseUrl}")
public interface BookingClient {

    // TODO: Define booking service endpoints
    // Example: @PostMapping("/api/v1/bookings")
    // BookingDto createBooking(@RequestBody CreateBookingDto request);
}
