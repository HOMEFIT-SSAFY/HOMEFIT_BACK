package com.happyhome.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.analysis.dto.AnalysisScore;
import com.happyhome.analysis.dto.TransitSummary;
import com.happyhome.analysis.service.AnalysisScoreService;
import com.happyhome.commercial.dto.CommercialSummary;
import com.happyhome.traffic.dto.TrafficRiskSummary;
import org.junit.jupiter.api.Test;

class AnalysisScoreServiceTest {

    private final AnalysisScoreService scoreService = new AnalysisScoreService();

    @Test
    void rewardsCommercialTransitAndSafeTrafficWithoutBasePoints() {
        CommercialSummary commercial = new CommercialSummary(42, 12, 10, 7, 8, 5);
        TrafficRiskSummary traffic = new TrafficRiskSummary(0, 0, "low");
        TransitSummary transit = new TransitSummary(1, 1, 3, 5, 9);

        AnalysisScore score = scoreService.calculate(commercial, traffic, transit);

        assertThat(score.total()).isEqualTo(100);
        assertThat(score.commercialScore()).isEqualTo(35);
        assertThat(score.transitScore()).isEqualTo(40);
        assertThat(score.trafficSafetyScore()).isEqualTo(25);
        assertThat(score.level()).isEqualTo("매우 좋음");
        assertThat(score.message()).contains("상권", "대중교통", "교통 안전");
    }

    @Test
    void penalizesRoadworkAndTrafficEvents() {
        CommercialSummary commercial = new CommercialSummary(10, 2, 2, 1, 2, 3);
        TrafficRiskSummary traffic = new TrafficRiskSummary(4, 2, "high");
        TransitSummary transit = new TransitSummary(0, 0, 0, 1, 2);

        AnalysisScore score = scoreService.calculate(commercial, traffic, transit);

        assertThat(score.total()).isLessThan(45);
        assertThat(score.trafficSafetyScore()).isZero();
        assertThat(score.message()).contains("교통 이벤트");
    }

    @Test
    void doesNotGiveBasePointsWhenNoInfrastructureExists() {
        CommercialSummary commercial = new CommercialSummary(0, 0, 0, 0, 0, 0);
        TrafficRiskSummary traffic = new TrafficRiskSummary(3, 0, "medium");
        TransitSummary transit = new TransitSummary(0, 0, 0, 0, 0);

        AnalysisScore score = scoreService.calculate(commercial, traffic, transit);

        assertThat(score.commercialScore()).isZero();
        assertThat(score.transitScore()).isZero();
        assertThat(score.total()).isEqualTo(12);
        assertThat(score.level()).isEqualTo("주의");
    }
}
