package com.happyhome.analysis.dto;

public record AnalysisScore(
        int total,
        int commercialScore,
        int transitScore,
        int trafficSafetyScore,
        String level,
        String message
) {
}
