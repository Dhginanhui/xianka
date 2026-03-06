package com.xianka.capacity.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record EstimateRequest(
        @NotBlank String model,
        @Min(1) int avgInputTokens,
        @Min(1) int avgOutputTokens,
        @NotBlank String scenarioDescription
) {
}
