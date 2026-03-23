package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.CoreBookingClient;
import com.seera.lumi.partner.gateway.client.PartnerServiceClient;
import com.seera.lumi.partner.gateway.client.request.CreateCoreBookingRequest;
import com.seera.lumi.partner.gateway.client.response.CancelCoreBookingResponse;
import com.seera.lumi.partner.gateway.client.response.CoreBookingResponse;
import com.seera.lumi.partner.gateway.controller.request.CreateBookingRequest;
import com.seera.lumi.partner.gateway.controller.request.DriverInfo;
import com.seera.lumi.partner.gateway.controller.response.BookingResponse;
import com.seera.lumi.partner.gateway.controller.response.CancelBookingResponse;
import com.seera.lumi.partner.gateway.controller.response.PricingPackage;
import com.seera.lumi.partner.gateway.controller.response.QuoteResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import com.seera.lumi.partner.gateway.security.PartnerContext;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final CoreBookingClient coreBookingClient;
    private final PartnerServiceClient partnerServiceClient;

    /**
     * Creates a booking for the partner via core-booking-service.
     */
    public BookingResponse createBooking(CreateBookingRequest request) {
        String partnerCode = PartnerContext.getPartnerCode();
        log.info("Creating booking for partner={}, quoteId={}", partnerCode, request.getQuoteId());

        try {
            // Fetch cached quote to get locations, dates, vehicle group, and pricing
            QuoteResponse quote = partnerServiceClient.getQuote(request.getQuoteId());
            if (quote == null) {
                throw new PartnerException("QUOTE_NOT_FOUND",
                        "Quote not found or expired: " + request.getQuoteId(), 404);
            }

            // Find the selected package by type
            PricingPackage selectedPackage = quote.getPackages().stream()
                    .filter(p -> request.getPackageType().equalsIgnoreCase(p.getType()))
                    .findFirst()
                    .orElseThrow(() -> new PartnerException("INVALID_PACKAGE",
                            "Package type '" + request.getPackageType() + "' not found in quote", 400));

            CreateCoreBookingRequest coreRequest = CreateCoreBookingRequest.builder()
                    .partnerCode(partnerCode)
                    .quoteId(request.getQuoteId())
                    .packageType(request.getPackageType())
                    .bookingMode(PartnerContext.getBookingMode())
                    // Location & dates from quote
                    .pickupBranchId(Long.valueOf(quote.getPickupLocation()))
                    .dropOffBranchId(Long.valueOf(quote.getDropoffLocation()))
                    .pickupDateTime(quote.getPickupDateTime())
                    .dropOffDateTime(quote.getDropoffDateTime())
                    .vehicleGroupId(quote.getVehicleGroupId())
                    // Pricing from selected package
                    .totalPrice(toDouble(selectedPackage.getTotalDue()))
                    .rentalSum(toDouble(selectedPackage.getSubtotal()))
                    .vat(toDouble(selectedPackage.getVat()))
                    .vatPercentage(toDouble(selectedPackage.getVatPercent()))
                    .discount(toDouble(selectedPackage.getDiscount()))
                    .discountPercentage(toDouble(selectedPackage.getDiscountPercent()))
                    // Driver info
                    .firstName(request.getDriver().getFirstName())
                    .lastName(request.getDriver().getLastName())
                    .email(request.getDriver().getEmail())
                    .phone(request.getDriver().getPhone())
                    .passportNumber(request.getDriver().getPassportNumber())
                    .nationality(request.getDriver().getNationality())
                    .build();

            CoreBookingResponse coreResponse = coreBookingClient.createPartnerBooking(coreRequest);
            return mapToBookingResponse(coreResponse, quote, selectedPackage, request);
        } catch (PartnerException e) {
            throw e;
        } catch (FeignException e) {
            log.error("Failed to create booking via core-booking-service: status={}, body={}",
                    e.status(), e.contentUTF8(), e);
            throw new PartnerException("BOOKING_CREATE_FAILED",
                    "Failed to create booking. Please try again later.", mapHttpStatus(e));
        }
    }

    private Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }

    /**
     * Retrieves booking details by reference number via core-booking-service.
     */
    public BookingResponse getBooking(String bookingRef) {
        log.info("Getting booking: bookingRef={}", bookingRef);

        try {
            CoreBookingResponse coreResponse = coreBookingClient.getPartnerBooking(bookingRef);
            return mapToBookingResponse(coreResponse);
        } catch (FeignException.NotFound e) {
            log.warn("Booking not found: bookingRef={}", bookingRef);
            throw new PartnerException("BOOKING_NOT_FOUND",
                    "Booking not found: " + bookingRef, 404, e);
        } catch (FeignException e) {
            log.error("Failed to get booking via core-booking-service: status={}, body={}",
                    e.status(), e.contentUTF8(), e);
            throw new PartnerException("BOOKING_FETCH_FAILED",
                    "Failed to retrieve booking. Please try again later.", mapHttpStatus(e));
        }
    }

    /**
     * Cancels a booking by reference number via core-booking-service.
     */
    public CancelBookingResponse cancelBooking(String bookingRef) {
        log.info("Cancelling booking: bookingRef={}", bookingRef);

        try {
            CancelCoreBookingResponse coreResponse = coreBookingClient.cancelPartnerBooking(bookingRef);
            return CancelBookingResponse.builder()
                    .bookingReference(coreResponse.getReferenceNo())
                    .status(coreResponse.getStatus())
                    .cancellationFee(coreResponse.getCancellationFee() != null
                            ? BigDecimal.valueOf(coreResponse.getCancellationFee()) : null)
                    .refundAmount(coreResponse.getRefundAmount() != null
                            ? BigDecimal.valueOf(coreResponse.getRefundAmount()) : null)
                    .build();
        } catch (FeignException.NotFound e) {
            log.warn("Booking not found for cancellation: bookingRef={}", bookingRef);
            throw new PartnerException("BOOKING_NOT_FOUND",
                    "Booking not found: " + bookingRef, 404, e);
        } catch (FeignException e) {
            log.error("Failed to cancel booking via core-booking-service: status={}, body={}",
                    e.status(), e.contentUTF8(), e);
            throw new PartnerException("BOOKING_CANCEL_FAILED",
                    "Failed to cancel booking. Please try again later.", mapHttpStatus(e));
        }
    }

    private BookingResponse mapToBookingResponse(CoreBookingResponse core, QuoteResponse quote,
                                                  PricingPackage selectedPackage, CreateBookingRequest request) {
        DriverInfo driver = DriverInfo.builder()
                .firstName(request.getDriver().getFirstName())
                .lastName(request.getDriver().getLastName())
                .email(request.getDriver().getEmail())
                .phone(request.getDriver().getPhone())
                .passportNumber(request.getDriver().getPassportNumber())
                .nationality(request.getDriver().getNationality())
                .build();

        return BookingResponse.builder()
                .bookingReference(core.getReferenceNo())
                .status(core.getStatus())
                .quoteId(quote.getQuoteId())
                .vehicleGroup(quote.getVehicleGroup())
                .pickupLocation(quote.getPickupLocation())
                .dropoffLocation(quote.getDropoffLocation())
                .pickupDateTime(quote.getPickupDateTime())
                .dropoffDateTime(quote.getDropoffDateTime())
                .pricing(selectedPackage)
                .driver(driver)
                .createdAt(core.getCreatedOn())
                .build();
    }

    private BookingResponse mapToBookingResponse(CoreBookingResponse core) {
        DriverInfo driver = DriverInfo.builder()
                .firstName(core.getDriverFirstName())
                .lastName(core.getDriverLastName())
                .email(core.getDriverEmail())
                .build();

        return BookingResponse.builder()
                .bookingReference(core.getReferenceNo())
                .status(core.getStatus())
                .vehicleGroup(core.getVehicleGroupId() != null ? String.valueOf(core.getVehicleGroupId()) : null)
                .pickupLocation(core.getPickupBranchId() != null ? String.valueOf(core.getPickupBranchId()) : null)
                .dropoffLocation(core.getDropOffBranchId() != null ? String.valueOf(core.getDropOffBranchId()) : null)
                .pickupDateTime(core.getPickupDateTime())
                .dropoffDateTime(core.getDropOffDateTime())
                .driver(driver)
                .createdAt(core.getCreatedOn())
                .build();
    }

    private int mapHttpStatus(FeignException e) {
        int status = e.status();
        if (status == 400) return 400;
        if (status == 404) return 404;
        if (status == 409) return 409;
        if (status == 422) return 422;
        return 502;
    }
}
