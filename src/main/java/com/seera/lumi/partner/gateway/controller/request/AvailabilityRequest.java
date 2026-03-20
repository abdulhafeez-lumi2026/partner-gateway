package com.seera.lumi.partner.gateway.controller.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityRequest {

    @NotNull(message = "pickupLocationId is required")
    @Positive(message = "pickupLocationId must be positive")
    private Long pickupLocationId;

    @NotNull(message = "dropoffLocationId is required")
    @Positive(message = "dropoffLocationId must be positive")
    private Long dropoffLocationId;

    @NotNull(message = "pickupDateTime is required")
    @Future(message = "pickupDateTime must be in the future")
    private LocalDateTime pickupDateTime;

    @NotNull(message = "dropoffDateTime is required")
    @Future(message = "dropoffDateTime must be in the future")
    private LocalDateTime dropoffDateTime;
}
