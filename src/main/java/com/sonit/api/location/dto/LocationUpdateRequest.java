package com.sonit.api.location.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateRequest {

    @NotNull(message = "latitude is required")
    @DecimalMin(value = "-90.0", message = "latitude must be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "latitude must be less than or equal to 90")
    private Double latitude;

    @NotNull(message = "longitude is required")
    @DecimalMin(value = "-180.0", message = "longitude must be greater than or equal to -180")
    @DecimalMax(value = "180.0", message = "longitude must be less than or equal to 180")
    private Double longitude;

    @Builder.Default
    private Boolean visible = Boolean.TRUE;
}
