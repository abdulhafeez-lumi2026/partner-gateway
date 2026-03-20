package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.BookingClient;
import com.seera.lumi.partner.gateway.client.DriverClient;
import com.seera.lumi.partner.gateway.controller.request.CreateBookingRequest;
import com.seera.lumi.partner.gateway.controller.response.BookingResponse;
import com.seera.lumi.partner.gateway.controller.response.CancelBookingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingClient bookingClient;
    private final DriverClient driverClient;

    public BookingResponse createBooking(CreateBookingRequest request) {
        // TODO: Implement booking creation
        // 1. Validate quote exists and is still valid
        // 2. Create/find driver via driver service
        // 3. Create booking via booking service
        // 4. Return booking response
        throw new UnsupportedOperationException("Booking creation not yet implemented");
    }

    public BookingResponse getBooking(String bookingRef) {
        // TODO: Implement booking retrieval
        throw new UnsupportedOperationException("Booking retrieval not yet implemented");
    }

    public CancelBookingResponse cancelBooking(String bookingRef) {
        // TODO: Implement booking cancellation
        throw new UnsupportedOperationException("Booking cancellation not yet implemented");
    }

    public BookingResponse confirmBooking(String bookingRef) {
        // TODO: Implement booking confirmation
        throw new UnsupportedOperationException("Booking confirmation not yet implemented");
    }
}
