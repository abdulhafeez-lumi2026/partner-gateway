package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.client.PartnerServiceClient;
import com.seera.lumi.partner.gateway.controller.request.ClientCredentialsRequest;
import com.seera.lumi.partner.gateway.controller.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/partner/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Partner authentication - get JWT token")
public class AuthController {

    private final PartnerServiceClient partnerServiceClient;

    @Operation(summary = "Get access token", description = "Authenticate with client credentials to get a JWT token")
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> getToken(@Valid @RequestBody ClientCredentialsRequest request) {
        log.info("Token request received for clientId: {}", request.getClientId());
        TokenResponse tokenResponse = partnerServiceClient.authenticate(request);
        return ResponseEntity.ok(tokenResponse);
    }
}
