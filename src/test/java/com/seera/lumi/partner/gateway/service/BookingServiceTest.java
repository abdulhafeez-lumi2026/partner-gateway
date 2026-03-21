package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.CoreBookingClient;
import com.seera.lumi.partner.gateway.client.response.CancelCoreBookingResponse;
import com.seera.lumi.partner.gateway.client.response.CoreBookingResponse;
import com.seera.lumi.partner.gateway.controller.request.CreateBookingRequest;
import com.seera.lumi.partner.gateway.controller.request.DriverInfo;
import com.seera.lumi.partner.gateway.controller.response.BookingResponse;
import com.seera.lumi.partner.gateway.controller.response.CancelBookingResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import feign.FeignException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private CoreBookingClient coreBookingClient;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("user", null);
        auth.setDetails(Map.of("partnerCode", "MEILI", "bookingMode", "PAY_LATER"));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createBooking_success() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .quoteId("quote-123")
                .packageType("FULL")
                .driver(DriverInfo.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .email("john@example.com")
                        .phone("+966500000000")
                        .passportNumber("AB123456")
                        .nationality("US")
                        .build())
                .build();

        CoreBookingResponse coreResponse = CoreBookingResponse.builder()
                .referenceNo("BK-001")
                .status("CONFIRMED")
                .vehicleGroupId(10L)
                .pickupBranchId(1L)
                .dropOffBranchId(2L)
                .pickupDateTime(LocalDateTime.of(2026, 4, 1, 10, 0))
                .dropOffDateTime(LocalDateTime.of(2026, 4, 5, 10, 0))
                .driverFirstName("John")
                .driverLastName("Doe")
                .driverEmail("john@example.com")
                .createdOn(LocalDateTime.of(2026, 3, 20, 12, 0))
                .build();

        when(coreBookingClient.createPartnerBooking(any())).thenReturn(coreResponse);

        BookingResponse result = bookingService.createBooking(request);

        assertThat(result.getBookingReference()).isEqualTo("BK-001");
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getVehicleGroup()).isEqualTo("10");
        assertThat(result.getPickupLocation()).isEqualTo("1");
        assertThat(result.getDropoffLocation()).isEqualTo("2");
        assertThat(result.getDriver().getFirstName()).isEqualTo("John");
        assertThat(result.getDriver().getLastName()).isEqualTo("Doe");
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 3, 20, 12, 0));

        verify(coreBookingClient).createPartnerBooking(argThat(req ->
                "MEILI".equals(req.getPartnerCode())
                        && "quote-123".equals(req.getQuoteId())
                        && "FULL".equals(req.getPackageType())
                        && "John".equals(req.getFirstName())
                        && "Doe".equals(req.getLastName())
                        && "john@example.com".equals(req.getEmail())
                        && "+966500000000".equals(req.getPhone())
                        && "AB123456".equals(req.getPassportNumber())
                        && "US".equals(req.getNationality())
        ));
    }

    @Test
    void createBooking_feignException_throws502() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .quoteId("quote-123")
                .packageType("FULL")
                .driver(DriverInfo.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .email("john@example.com")
                        .passportNumber("AB123456")
                        .nationality("US")
                        .build())
                .build();

        FeignException feignEx = mock(FeignException.class);
        when(feignEx.status()).thenReturn(500);
        when(feignEx.contentUTF8()).thenReturn("Internal Server Error");
        when(coreBookingClient.createPartnerBooking(any())).thenThrow(feignEx);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(PartnerException.class)
                .satisfies(ex -> {
                    PartnerException pe = (PartnerException) ex;
                    assertThat(pe.getCode()).isEqualTo("BOOKING_CREATE_FAILED");
                    assertThat(pe.getHttpStatus()).isEqualTo(502);
                });
    }

    @Test
    void getBooking_success() {
        CoreBookingResponse coreResponse = CoreBookingResponse.builder()
                .referenceNo("BK-001")
                .status("CONFIRMED")
                .vehicleGroupId(5L)
                .pickupBranchId(1L)
                .dropOffBranchId(2L)
                .pickupDateTime(LocalDateTime.of(2026, 4, 1, 10, 0))
                .dropOffDateTime(LocalDateTime.of(2026, 4, 5, 10, 0))
                .driverFirstName("Jane")
                .driverLastName("Smith")
                .driverEmail("jane@example.com")
                .createdOn(LocalDateTime.of(2026, 3, 20, 12, 0))
                .build();

        when(coreBookingClient.getPartnerBooking("BK-001")).thenReturn(coreResponse);

        BookingResponse result = bookingService.getBooking("BK-001");

        assertThat(result.getBookingReference()).isEqualTo("BK-001");
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getVehicleGroup()).isEqualTo("5");
        assertThat(result.getDriver().getFirstName()).isEqualTo("Jane");

        verify(coreBookingClient).getPartnerBooking("BK-001");
    }

    @Test
    void getBooking_notFound_throws404() {
        FeignException.NotFound notFoundEx = mock(FeignException.NotFound.class);
        when(coreBookingClient.getPartnerBooking("BK-999")).thenThrow(notFoundEx);

        assertThatThrownBy(() -> bookingService.getBooking("BK-999"))
                .isInstanceOf(PartnerException.class)
                .satisfies(ex -> {
                    PartnerException pe = (PartnerException) ex;
                    assertThat(pe.getCode()).isEqualTo("BOOKING_NOT_FOUND");
                    assertThat(pe.getHttpStatus()).isEqualTo(404);
                    assertThat(pe.getMessage()).contains("BK-999");
                });
    }

    @Test
    void getBooking_feignError_throwsPartnerException() {
        FeignException feignEx = mock(FeignException.class);
        when(feignEx.status()).thenReturn(400);
        when(feignEx.contentUTF8()).thenReturn("Bad Request");
        when(coreBookingClient.getPartnerBooking("BK-002")).thenThrow(feignEx);

        assertThatThrownBy(() -> bookingService.getBooking("BK-002"))
                .isInstanceOf(PartnerException.class)
                .satisfies(ex -> {
                    PartnerException pe = (PartnerException) ex;
                    assertThat(pe.getCode()).isEqualTo("BOOKING_FETCH_FAILED");
                    assertThat(pe.getHttpStatus()).isEqualTo(400);
                });
    }

    @Test
    void cancelBooking_success() {
        CancelCoreBookingResponse coreResponse = CancelCoreBookingResponse.builder()
                .referenceNo("BK-001")
                .status("CANCELLED")
                .cancellationFee(50.0)
                .refundAmount(450.0)
                .build();

        when(coreBookingClient.cancelPartnerBooking("BK-001")).thenReturn(coreResponse);

        CancelBookingResponse result = bookingService.cancelBooking("BK-001");

        assertThat(result.getBookingReference()).isEqualTo("BK-001");
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        assertThat(result.getCancellationFee()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(result.getRefundAmount()).isEqualByComparingTo(BigDecimal.valueOf(450.0));

        verify(coreBookingClient).cancelPartnerBooking("BK-001");
    }

    @Test
    void cancelBooking_notFound_throws404() {
        FeignException.NotFound notFoundEx = mock(FeignException.NotFound.class);
        when(coreBookingClient.cancelPartnerBooking("BK-999")).thenThrow(notFoundEx);

        assertThatThrownBy(() -> bookingService.cancelBooking("BK-999"))
                .isInstanceOf(PartnerException.class)
                .satisfies(ex -> {
                    PartnerException pe = (PartnerException) ex;
                    assertThat(pe.getCode()).isEqualTo("BOOKING_NOT_FOUND");
                    assertThat(pe.getHttpStatus()).isEqualTo(404);
                    assertThat(pe.getMessage()).contains("BK-999");
                });
    }
}
