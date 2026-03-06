package com.xianka.capacity.model;

import java.util.List;

public record EstimateResponse(
        String fixedRuntimeModel,
        ModelProfile modelProfile,
        double targetTokensPerSecond,
        double contextCoefficient,
        List<CurvePoint> points
) {
}
