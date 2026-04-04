package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.controller.request.CreateBookingRequest;
import com.seera.lumi.partner.gateway.controller.response.BookingResponse;
import com.seera.lumi.partner.gateway.controller.response.CancelBookingResponse;
import com.seera.lumi.partner.gateway.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/partner/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Create, view, and cancel bookings")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create booking", description = "Create a booking from a valid quote. Requires quoteId and driver passport details.")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        log.info("Create booking: quoteId={}, packageType={}", request.getQuoteId(), request.getPackageType());
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @Operation(summary = "Get booking", description = "Retrieve booking details by reference number")
    @GetMapping("/{bookingRef}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingRef) {
        log.info("Get booking: bookingRef={}", bookingRef);
        return ResponseEntity.ok(bookingService.getBooking(bookingRef));
    }

    @Operation(summary = "Cancel booking", description = "Cancel a booking. Only CREATED or UPCOMING bookings can be cancelled.")
    @DeleteMapping("/{bookingRef}")
    public ResponseEntity<CancelBookingResponse> cancelBooking(@PathVariable String bookingRef) {
        log.info("Cancel booking: bookingRef={}", bookingRef);
        return ResponseEntity.ok(bookingService.cancelBooking(bookingRef));
    }
}
