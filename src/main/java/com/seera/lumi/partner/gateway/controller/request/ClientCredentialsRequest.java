package com.seera.lumi.partner.gateway.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCredentialsRequest {

    @NotBlank(message = "clientId is required")
    private String clientId;

    @NotBlank(message = "clientSecret is required")
    private String clientSecret;

    @NotBlank(message = "grantType is required")
    private String grantType;
}
