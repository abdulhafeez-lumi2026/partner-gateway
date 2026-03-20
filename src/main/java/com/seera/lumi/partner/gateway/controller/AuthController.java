package com.seera.lumi.partner.gateway.controller;

import com.seera.lumi.partner.gateway.controller.request.ClientCredentialsRequest;
import com.seera.lumi.partner.gateway.controller.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/partner/auth")
public class AuthController {

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> getToken(@Valid @RequestBody ClientCredentialsRequest request) {
        log.info("Token request received for clientId: {}", request.getClientId());
        // TODO: Implement token generation via Keycloak
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
