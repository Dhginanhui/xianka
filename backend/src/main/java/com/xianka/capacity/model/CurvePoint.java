package com.xianka.capacity.model;

public record CurvePoint(
        int concurrency,
        double vramGb,
        double computeTflops
) {
}
