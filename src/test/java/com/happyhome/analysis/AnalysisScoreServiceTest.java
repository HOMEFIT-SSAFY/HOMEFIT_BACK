package com.happyhome.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.analysis.dto.AnalysisScore;
import com.happyhome.commercial.dto.CommercialSummary;
import com.happyhome.traffic.dto.TrafficRiskSummary;
import com.happyhome.analysis.service.AnalysisScoreService;
import org.junit.jupiter.api.Test;

class AnalysisScoreServiceTest {

    private final AnalysisScoreService scoreService = new AnalysisScoreService();

    @Test
    void rewardsNearbyCommercialAmenities() {
        CommercialSummary commercial = new CommercialSummary(42, 12, 10, 6, 8, 6);
        TrafficRiskSummary traffic = new TrafficRiskSummary(0, 0, "낮음");

        AnalysisScore score = scoreService.calculate(commercial, traffic);

        assertThat(score.total()).isGreaterThanOrEqualTo(85);
        assertThat(score.commercialScore()).isGreaterThan(score.trafficPenalty());
        assertThat(score.level()).isEqualTo("매우 좋음");
        assertThat(score.message()).contains("생활 편의");
    }

    @Test
    void penalizesRoadworkAndTrafficEvents() {
        CommercialSummary commercial = new CommercialSummary(10, 2, 2, 1, 2, 3);
        TrafficRiskSummary traffic = new TrafficRiskSummary(4, 2, "높음");

        AnalysisScore score = scoreService.calculate(commercial, traffic);

        assertThat(score.total()).isLessThan(75);
        assertThat(score.trafficPenalty()).isGreaterThanOrEqualTo(20);
        assertThat(score.message()).contains("교통");
    }
}
