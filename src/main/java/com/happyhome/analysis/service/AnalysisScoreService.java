package com.happyhome.analysis.service;

import com.happyhome.analysis.dto.AnalysisScore;
import com.happyhome.analysis.dto.TransitSummary;
import com.happyhome.commercial.dto.CommercialSummary;
import com.happyhome.traffic.dto.TrafficRiskSummary;

public class AnalysisScoreService {

    public AnalysisScore calculate(CommercialSummary commercial, TrafficRiskSummary traffic, TransitSummary transit) {
        int commercialScore = commercialScore(commercial);
        int transitScore = transitScore(transit);
        int trafficSafetyScore = 0;
        int total = clamp(commercialScore + transitScore, 0, 100);

        return new AnalysisScore(
                total,
                commercialScore,
                transitScore,
                trafficSafetyScore,
                level(total),
                message(commercialScore, transitScore, traffic)
        );
    }

    private int commercialScore(CommercialSummary commercial) {
        int density = Math.min(15, commercial.totalCount() / 2);
        int foodAndCafe = Math.min(8, (commercial.foodCount() + commercial.cafeCount()) / 2);
        int medical = Math.min(7, commercial.medicalCount());
        int convenience = Math.min(5, commercial.convenienceCount());
        int rawScore = density + foodAndCafe + medical + convenience;
        return Math.min(50, (int) Math.round(rawScore * 50 / 30.0));
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
        return Math.min(50, (int) Math.round(score * 50 / 30.0));
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
            TrafficRiskSummary traffic
    ) {
        return "상권 %d점, 대중교통 %d점으로 총 %d점입니다. 교통 이벤트 %d건과 도로공사 %d건은 점수에 반영하지 않고 참고 건수로 제공합니다."
                .formatted(
                        commercialScore,
                        transitScore,
                        commercialScore + transitScore,
                        traffic.eventCount(),
                        traffic.roadworkCount()
                );
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
