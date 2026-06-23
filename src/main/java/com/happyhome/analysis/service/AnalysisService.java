package com.happyhome.analysis.service;

import com.happyhome.analysis.dao.AnalysisSnapshotMapper;
import com.happyhome.analysis.dto.AnalysisScore;
import com.happyhome.analysis.dto.AnalysisSnapshot;
import com.happyhome.analysis.dto.HousingAnalysis;
import com.happyhome.analysis.dto.TransitAnalysis;
import com.happyhome.commercial.dto.CommercialPlace;
import com.happyhome.commercial.dto.CommercialSummary;
import com.happyhome.commercial.service.CommercialSummaryService;
import com.happyhome.openapi.CommercialOpenApiClient;
import com.happyhome.openapi.ItsOpenApiClient;
import com.happyhome.traffic.dto.TrafficEvent;
import com.happyhome.traffic.dto.TrafficRiskSummary;
import com.happyhome.traffic.service.TrafficRiskService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final CommercialOpenApiClient commercialClient;
    private final ItsOpenApiClient itsClient;
    private final CommercialSummaryService commercialSummaryService;
    private final TrafficRiskService trafficRiskService;
    private final TransitAnalysisService transitAnalysisService;
    private final AnalysisScoreService scoreService;
    private final AnalysisSnapshotMapper snapshotMapper;
    private final Map<AnalysisCacheKey, CachedAnalysis> cache = new ConcurrentHashMap<>();

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
        AnalysisCacheKey cacheKey = new AnalysisCacheKey(label, longitude, latitude, radiusMeters);
        CachedAnalysis cached = cache.get(cacheKey);
        if (cached != null && !cached.expired()) {
            return cached.analysis();
        }

        List<CommercialPlace> places = commercialClient.places(longitude, latitude, radiusMeters);
        List<TrafficEvent> events = itsClient.events(longitude, latitude);
        CommercialSummary commercialSummary = commercialSummaryService.summarize(places);
        TrafficRiskSummary trafficSummary = trafficRiskService.summarize(events);
        TransitAnalysis transitAnalysis = transitAnalysisService.analyze(longitude, latitude, radiusMeters);
        AnalysisScore score = scoreService.calculate(commercialSummary, trafficSummary, transitAnalysis.summary());
        String source = places.stream().anyMatch(place -> place.name().equals("청년식당")) ? "sample" : "api";

        HousingAnalysis analysis = new HousingAnalysis(
                label,
                latitude,
                longitude,
                radiusMeters,
                commercialSummary,
                trafficSummary,
                transitAnalysis.summary(),
                score,
                places,
                events,
                transitAnalysis.busStops(),
                transitAnalysis.subwayStations(),
                source
        );
        cache.put(cacheKey, new CachedAnalysis(analysis, Instant.now().plus(CACHE_TTL)));
        cacheQuietly(analysis);
        return analysis;
    }

    private record AnalysisCacheKey(String label, double longitude, double latitude, int radiusMeters) {
    }

    private record CachedAnalysis(HousingAnalysis analysis, Instant expiresAt) {
        private boolean expired() {
            return Instant.now().isAfter(expiresAt);
        }
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
