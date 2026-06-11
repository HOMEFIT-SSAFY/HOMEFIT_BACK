package com.happyhome.traffic.dto;

public record TrafficRiskSummary(
        int eventCount,
        int roadworkCount,
        String riskLevel
) {
}

