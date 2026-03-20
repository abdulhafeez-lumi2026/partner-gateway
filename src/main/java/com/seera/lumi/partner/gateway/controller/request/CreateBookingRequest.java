package com.seera.lumi.partner.gateway.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotBlank(message = "quoteId is required")
    private String quoteId;

    @NotBlank(message = "packageType is required")
    private String packageType;

    @NotNull(message = "driver information is required")
    @Valid
    private DriverInfo driver;
}
