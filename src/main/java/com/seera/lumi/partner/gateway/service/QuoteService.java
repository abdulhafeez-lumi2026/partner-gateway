package com.seera.lumi.partner.gateway.service;

import com.seera.lumi.partner.gateway.client.PricingClient;
import com.seera.lumi.partner.gateway.controller.request.QuoteRequest;
import com.seera.lumi.partner.gateway.controller.response.QuoteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private final PricingClient pricingClient;
    private final RedisTemplate<String, Object> redisTemplate;

    public QuoteResponse createQuote(QuoteRequest request) {
        // TODO: Implement quote creation
        // 1. Call pricing service to get detailed pricing
        // 2. Generate quote ID
        // 3. Cache quote in Redis with TTL
        // 4. Return quote response
        throw new UnsupportedOperationException("Quote creation not yet implemented");
    }

    public QuoteResponse getQuote(String quoteId) {
        // TODO: Implement quote retrieval from Redis cache
        throw new UnsupportedOperationException("Quote retrieval not yet implemented");
    }
}
