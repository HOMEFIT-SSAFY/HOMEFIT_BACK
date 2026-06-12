package com.happyhome.analysis.service;

import com.happyhome.analysis.dao.AnalysisSnapshotMapper;
import com.happyhome.analysis.dto.AnalysisScore;
import com.happyhome.analysis.dto.AnalysisSnapshot;
import com.happyhome.analysis.dto.HousingAnalysis;
import com.happyhome.analysis.dto.TransitSummary;
import com.happyhome.commercial.dto.CommercialPlace;
import com.happyhome.commercial.dto.CommercialSummary;
import com.happyhome.commercial.service.CommercialSummaryService;
import com.happyhome.openapi.CommercialOpenApiClient;
import com.happyhome.openapi.ItsOpenApiClient;
import com.happyhome.traffic.dto.TrafficEvent;
import com.happyhome.traffic.dto.TrafficRiskSummary;
import com.happyhome.traffic.service.TrafficRiskService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

    private final CommercialOpenApiClient commercialClient;
    private final ItsOpenApiClient itsClient;
    private final CommercialSummaryService commercialSummaryService;
    private final TrafficRiskService trafficRiskService;
    private final TransitAnalysisService transitAnalysisService;
    private final AnalysisScoreService scoreService;
    private final AnalysisSnapshotMapper snapshotMapper;

    public AnalysisService(
            CommercialOpenApiClient commercialClient,
            ItsOpenApiClient itsClient,
            CommercialSummaryService commercialSummaryService,
            TrafficRiskService trafficRiskService,
            TransitAnalysisService transitAnalysisService,
            AnalysisSnapshotMapper snapshotMapper
    ) {
        this.commercialClient = commercialClient;
        this.itsClient = itsClient;
        this.commercialSummaryService = commercialSummaryService;
        this.trafficRiskService = trafficRiskService;
        this.transitAnalysisService = transitAnalysisService;
        this.scoreService = new AnalysisScoreService();
        this.snapshotMapper = snapshotMapper;
    }

    public HousingAnalysis analyze(String label, double longitude, double latitude, int radiusMeters) {
        List<CommercialPlace> places = commercialClient.places(longitude, latitude, radiusMeters);
        List<TrafficEvent> events = itsClient.events(longitude, latitude);
        CommercialSummary commercialSummary = commercialSummaryService.summarize(places);
        TrafficRiskSummary trafficSummary = trafficRiskService.summarize(events);
        TransitSummary transitSummary = transitAnalysisService.summarize(longitude, latitude);
        AnalysisScore score = scoreService.calculate(commercialSummary, trafficSummary, transitSummary);
        String source = places.stream().anyMatch(place -> place.name().equals("청년식당")) ? "sample" : "api";

        HousingAnalysis analysis = new HousingAnalysis(
                label,
                latitude,
                longitude,
                radiusMeters,
                commercialSummary,
                trafficSummary,
                transitSummary,
                score,
                places,
                events,
                source
        );
        cacheQuietly(analysis);
        return analysis;
    }

    private void cacheQuietly(HousingAnalysis analysis) {
        try {
            snapshotMapper.insert(new AnalysisSnapshot(
                    analysis.label(),
                    analysis.latitude(),
                    analysis.longitude(),
                    analysis.radiusMeters(),
                    analysis.commercialSummary().totalCount(),
                    analysis.trafficRiskSummary().eventCount(),
                    analysis.score().total(),
                    analysis.trafficRiskSummary().riskLevel(),
                    analysis.source()
            ));
        } catch (Exception ignored) {
            // The analysis page should remain useful even if local cache persistence fails.
        }
    }
}
