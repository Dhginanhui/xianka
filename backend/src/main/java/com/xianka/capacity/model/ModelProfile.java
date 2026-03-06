package com.xianka.capacity.model;

public record ModelProfile(
        double modelParamsB,
        double activatedParamsB,
        String quantizationPrecision,
        double quantizationFactor
) {
}
