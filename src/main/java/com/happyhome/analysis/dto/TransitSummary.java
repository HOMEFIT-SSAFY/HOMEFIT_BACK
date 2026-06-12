package com.happyhome.analysis.dto;

public record TransitSummary(
        int subwayWithin500m,
        int subwayWithin1000m,
        int busStopWithin300m,
        int busStopWithin500m,
        int busStopWithin1000m
) {
}
