package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.controller.request.CreateBookingRequest;
import com.seera.lumi.partner.gateway.controller.response.BookingResponse;
import com.seera.lumi.partner.gateway.controller.response.CancelBookingResponse;
import com.seera.lumi.partner.gateway.service.BookingService;
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
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        log.info("Create booking: quoteId={}, packageType={}", request.getQuoteId(), request.getPackageType());
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping("/{bookingRef}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable String bookingRef) {
        log.info("Get booking: bookingRef={}", bookingRef);
        return ResponseEntity.ok(bookingService.getBooking(bookingRef));
    }

    @DeleteMapping("/{bookingRef}")
    public ResponseEntity<CancelBookingResponse> cancelBooking(@PathVariable String bookingRef) {
        log.info("Cancel booking: bookingRef={}", bookingRef);
        return ResponseEntity.ok(bookingService.cancelBooking(bookingRef));
    }
}
