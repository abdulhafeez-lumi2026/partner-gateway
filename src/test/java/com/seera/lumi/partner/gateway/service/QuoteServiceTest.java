package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.PricingClient;
import com.seera.lumi.partner.gateway.client.response.VehicleQuoteResponse;
import com.seera.lumi.partner.gateway.controller.request.QuoteRequest;
import com.seera.lumi.partner.gateway.controller.response.QuoteResponse;
import com.seera.lumi.partner.gateway.exception.PartnerException;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private PricingClient pricingClient;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private QuoteService quoteService;

    private QuoteRequest buildQuoteRequest() {
        return QuoteRequest.builder()
                .pickupLocationId(1L)
                .dropoffLocationId(2L)
                .pickupDateTime(LocalDateTime.of(2026, 4, 1, 10, 0))
                .dropoffDateTime(LocalDateTime.of(2026, 4, 5, 10, 0))
                .vehicleGroup("ECAR")
                .build();
    }

    private VehicleQuoteResponse buildQuoteResponse() {
        return VehicleQuoteResponse.builder()
                .quoteId("core-quote-123")
                .finalPrice("1000")
                .pricePerDay("250")
                .cdwPerDay("20")
                .soldDays("4")
                .vatPercentage("15")
                .discountPercentage("10")
                .currency("SAR")
                .groupId(10L)
                .available(true)
                .build();
    }

    @Test
    void createQuote_success_cachesInRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(pricingClient.createQuote(any())).thenReturn(buildQuoteResponse());

        QuoteResponse result = quoteService.createQuote(buildQuoteRequest());

        assertThat(result.getQuoteId()).isNotNull().isNotBlank();
        assertThat(result.getVehicleGroup()).isEqualTo("ECAR");
        assertThat(result.getCurrency()).isEqualTo("SAR");
        assertThat(result.getPickupLocation()).isEqualTo("1");
        assertThat(result.getDropoffLocation()).isEqualTo("2");
        assertThat(result.getPackages()).hasSize(2);
        assertThat(result.getValidUntil()).isNotNull();

        verify(valueOperations).set(
                eq("partner:quote:" + result.getQuoteId()),
                eq(result),
                eq(30L),
                any()
        );
        verify(pricingClient).createQuote(any());
    }

    @Test
    void createQuote_feignException_throwsPartnerException() {
        FeignException feignEx = mock(FeignException.class);
        when(feignEx.status()).thenReturn(500);
        when(pricingClient.createQuote(any())).thenThrow(feignEx);

        assertThatThrownBy(() -> quoteService.createQuote(buildQuoteRequest()))
                .isInstanceOf(PartnerException.class)
                .satisfies(ex -> {
                    PartnerException pe = (PartnerException) ex;
                    assertThat(pe.getCode()).isEqualTo("QUOTE_CREATE_ERROR");
                    assertThat(pe.getHttpStatus()).isEqualTo(502);
                });
    }

    @Test
    void getQuote_found_returnsQuote() {
        QuoteResponse cachedQuote = QuoteResponse.builder()
                .quoteId("abc-123")
                .vehicleGroup("ECAR")
                .currency("SAR")
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("partner:quote:abc-123")).thenReturn(cachedQuote);

        QuoteResponse result = quoteService.getQuote("abc-123");

        assertThat(result.getQuoteId()).isEqualTo("abc-123");
        assertThat(result.getVehicleGroup()).isEqualTo("ECAR");
    }

    @Test
    void getQuote_notFound_throwsPartnerException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("partner:quote:missing-id")).thenReturn(null);

        assertThatThrownBy(() -> quoteService.getQuote("missing-id"))
                .isInstanceOf(PartnerException.class)
                .satisfies(ex -> {
                    PartnerException pe = (PartnerException) ex;
                    assertThat(pe.getCode()).isEqualTo("QUOTE_NOT_FOUND");
                    assertThat(pe.getHttpStatus()).isEqualTo(404);
                    assertThat(pe.getMessage()).contains("missing-id");
                });
    }

    @Test
    void getQuote_wrongType_throwsPartnerException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("partner:quote:bad-type")).thenReturn(new LinkedHashMap<>());

        assertThatThrownBy(() -> quoteService.getQuote("bad-type"))
                .isInstanceOf(PartnerException.class)
                .satisfies(ex -> {
                    PartnerException pe = (PartnerException) ex;
                    assertThat(pe.getCode()).isEqualTo("QUOTE_NOT_FOUND");
                    assertThat(pe.getHttpStatus()).isEqualTo(404);
                });
    }
}
