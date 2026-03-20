package com.seera.lumi.partner.gateway.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class PartnerContext {

    private PartnerContext() {
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getClaims() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Map) {
            return (Map<String, Object>) auth.getDetails();
        }
        return Collections.emptyMap();
    }

    public static String getPartnerCode() {
        return (String) getClaims().get("partnerCode");
    }

    public static Long getPartnerId() {
        Object val = getClaims().get("partnerId");
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return null;
    }

    public static String getQuoteMode() {
        return (String) getClaims().get("quoteMode");
    }

    public static String getBookingMode() {
        return (String) getClaims().get("bookingMode");
    }

    @SuppressWarnings("unchecked")
    public static List<String> getAllowedBranches() {
        Object val = getClaims().get("allowedBranches");
        if (val instanceof List) {
            return (List<String>) val;
        }
        return Collections.emptyList();
    }

    public static Integer getRateLimit() {
        Object val = getClaims().get("rateLimit");
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return null;
    }
}
