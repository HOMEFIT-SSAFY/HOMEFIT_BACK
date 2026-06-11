package com.happyhome.analysis.service;

import com.happyhome.analysis.dto.AnalysisScore;
import com.happyhome.commercial.dto.CommercialSummary;
import com.happyhome.traffic.dto.TrafficRiskSummary;

public class AnalysisScoreService {

    public AnalysisScore calculate(CommercialSummary commercial, TrafficRiskSummary traffic) {
        int commercialScore = Math.min(50, commercial.totalCount() * 2);
        commercialScore += Math.min(10, commercial.foodCount());
        commercialScore += Math.min(10, commercial.cafeCount());
        commercialScore += Math.min(10, commercial.medicalCount() * 2);
        commercialScore += Math.min(10, commercial.convenienceCount() * 2);

        int trafficPenalty = Math.min(35, traffic.eventCount() * 5 + traffic.roadworkCount() * 10);
        int total = clamp(60 + commercialScore - trafficPenalty, 0, 100);
        String level = total >= 85 ? "매우 좋음" : total >= 70 ? "좋음" : total >= 55 ? "보통" : "주의";
        String message = buildMessage(total, trafficPenalty);

        return new AnalysisScore(total, commercialScore, trafficPenalty, level, message);
    }

    private String buildMessage(int total, int trafficPenalty) {
        if (trafficPenalty >= 20) {
            return "교통 공사와 돌발상황은 확인해야 하지만 생활 편의 조건은 함께 비교할 수 있습니다.";
        }
        if (total >= 85) {
            return "생활 편의 시설이 충분하고 교통 리스크가 낮아 청년 주거 후보지로 적합합니다.";
        }
        return "생활 편의와 교통 리스크를 균형 있게 검토해야 하는 지역입니다.";
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
