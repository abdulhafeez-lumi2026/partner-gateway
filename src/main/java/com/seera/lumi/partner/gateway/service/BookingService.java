package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.BookingClient;
import com.seera.lumi.partner.gateway.client.DriverClient;
import com.seera.lumi.partner.gateway.controller.request.CreateBookingRequest;
import com.seera.lumi.partner.gateway.controller.response.BookingResponse;
import com.seera.lumi.partner.gateway.controller.response.CancelBookingResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingClient bookingClient;
    private final DriverClient driverClient;

    /**
     * Creates a booking for the partner.
     * <p>
     * TODO: Booking creation requires v2 endpoints on booking-service that don't exist yet.
     * The flow would be:
     * 1. Validate quote exists and is still valid (from Redis cache)
     * 2. Create/find driver via driverClient.createDriver()
     * 3. Create booking via booking-service (with source=PARTNER, partnerCode from PartnerContext)
     * 4. Map to BookingResponse
     */
    public BookingResponse createBooking(CreateBookingRequest request) {
        throw new PartnerException("NOT_IMPLEMENTED",
                "Booking creation is not yet available. Requires v2 booking-service endpoints.", 501);
    }

    /**
     * Retrieves booking details by reference number.
     * <p>
     * TODO: This needs an internal service-to-service auth token to call booking-service.
     * The booking summary endpoint requires an Authorization header. Once service-to-service
     * auth is implemented, this can call bookingClient.getBookingSummary() and map the response.
     */
    public BookingResponse getBooking(String bookingRef) {
        throw new PartnerException("NOT_IMPLEMENTED",
                "Booking retrieval is not yet available. Requires service-to-service auth.", 501);
    }

    /**
     * Cancels a booking by reference number.
     * <p>
     * TODO: Requires cancel API endpoint on booking-service that doesn't exist yet.
     */
    public CancelBookingResponse cancelBooking(String bookingRef) {
        throw new PartnerException("NOT_IMPLEMENTED",
                "Booking cancellation is not yet available. Requires cancel API on booking-service.", 501);
    }

    /**
     * Confirms a booking by reference number.
     * <p>
     * TODO: Requires confirm API endpoint on booking-service that doesn't exist yet.
     */
    public BookingResponse confirmBooking(String bookingRef) {
        throw new PartnerException("NOT_IMPLEMENTED",
                "Booking confirmation is not yet available. Requires confirm API on booking-service.", 501);
    }
}
