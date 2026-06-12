package com.happyhome.analysis.service;

import com.happyhome.analysis.dto.AnalysisScore;
import com.happyhome.analysis.dto.TransitSummary;
import com.happyhome.commercial.dto.CommercialSummary;
import com.happyhome.traffic.dto.TrafficRiskSummary;

public class AnalysisScoreService {

    public AnalysisScore calculate(CommercialSummary commercial, TrafficRiskSummary traffic, TransitSummary transit) {
        int commercialScore = commercialScore(commercial);
        int transitScore = transitScore(transit);
        int trafficSafetyScore = trafficSafetyScore(traffic);
        int total = clamp(commercialScore + transitScore + trafficSafetyScore, 0, 100);

        return new AnalysisScore(
                total,
                commercialScore,
                transitScore,
                trafficSafetyScore,
                level(total),
                message(commercialScore, transitScore, trafficSafetyScore, traffic)
        );
    }

    private int commercialScore(CommercialSummary commercial) {
        int density = Math.min(15, commercial.totalCount() / 2);
        int foodAndCafe = Math.min(8, (commercial.foodCount() + commercial.cafeCount()) / 2);
        int medical = Math.min(7, commercial.medicalCount());
        int convenience = Math.min(5, commercial.convenienceCount());
        return density + foodAndCafe + medical + convenience;
    }

    private int transitScore(TransitSummary transit) {
        int score = 0;
        if (transit.subwayWithin500m() > 0) {
            score += 18;
        }
        if (transit.subwayWithin1000m() > 0) {
            score += 10;
        }
        if (transit.busStopWithin300m() > 0) {
            score += 8;
        }
        if (transit.busStopWithin500m() >= 3) {
            score += 3;
        }
        if (transit.busStopWithin1000m() >= 8) {
            score += 1;
        }
        return Math.min(40, score);
    }

    private int trafficSafetyScore(TrafficRiskSummary traffic) {
        int score = switch (traffic.eventCount()) {
            case 0 -> 25;
            case 1 -> 18;
            case 2, 3 -> 12;
            default -> 6;
        };
        return clamp(score - traffic.roadworkCount() * 5, 0, 25);
    }

    private String level(int total) {
        if (total >= 85) {
            return "매우 좋음";
        }
        if (total >= 70) {
            return "좋음";
        }
        if (total >= 55) {
            return "보통";
        }
        if (total >= 40) {
            return "아쉬움";
        }
        return "주의";
    }

    private String message(
            int commercialScore,
            int transitScore,
            int trafficSafetyScore,
            TrafficRiskSummary traffic
    ) {
        return "상권 %d점, 대중교통 %d점, 교통 안전 %d점입니다. 교통 이벤트 %d건과 도로공사 %d건을 반영했습니다."
                .formatted(
                        commercialScore,
                        transitScore,
                        trafficSafetyScore,
                        traffic.eventCount(),
                        traffic.roadworkCount()
                );
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
