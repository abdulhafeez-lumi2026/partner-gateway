package com.seera.lumi.partner.gateway.filter;

import com.seera.lumi.partner.gateway.security.PartnerContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-Api-Key";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey != null) {
            log.debug("Request received with API key: {}", apiKey);
            // TODO: Implement actual rate limiting using Redis
        }

        Integer rateLimit = PartnerContext.getRateLimit();
        if (rateLimit != null) {
            log.debug("Partner {} has rate limit: {} requests/min",
                    PartnerContext.getPartnerCode(), rateLimit);
            // TODO: Implement actual rate limiting using Redis with JWT-based rate limit
        }

        filterChain.doFilter(request, response);
    }
}
