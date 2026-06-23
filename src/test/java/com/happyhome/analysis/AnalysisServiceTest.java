package com.happyhome.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.happyhome.analysis.dao.AnalysisSnapshotMapper;
import com.happyhome.analysis.dto.HousingAnalysis;
import com.happyhome.analysis.service.AnalysisService;
import com.happyhome.analysis.service.TransitAnalysisService;
import com.happyhome.commercial.dto.CommercialPlace;
import com.happyhome.commercial.service.CommercialSummaryService;
import com.happyhome.openapi.CommercialOpenApiClient;
import com.happyhome.openapi.ItsOpenApiClient;
import com.happyhome.openapi.KakaoLocalApiClient;
import com.happyhome.traffic.dto.TrafficEvent;
import com.happyhome.traffic.service.TrafficRiskService;
import com.happyhome.transport.dto.BusStop;
import com.happyhome.transport.dto.SubwayStation;
import com.happyhome.transport.service.BusStopService;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnalysisServiceTest {

    private final CommercialOpenApiClient commercialClient = mock(CommercialOpenApiClient.class);
    private final ItsOpenApiClient itsClient = mock(ItsOpenApiClient.class);
    private final CommercialSummaryService commercialSummaryService = new CommercialSummaryService();
    private final TrafficRiskService trafficRiskService = new TrafficRiskService();
    private final BusStopService busStopService = mock(BusStopService.class);
    private final KakaoLocalApiClient kakaoLocalApiClient = mock(KakaoLocalApiClient.class);
    private final TransitAnalysisService transitAnalysisService = new TransitAnalysisService(
            busStopService,
            kakaoLocalApiClient
    );
    private final AnalysisSnapshotMapper snapshotMapper = mock(AnalysisSnapshotMapper.class);
    private final AnalysisService analysisService = new AnalysisService(
            commercialClient,
            itsClient,
            commercialSummaryService,
            trafficRiskService,
            transitAnalysisService,
            snapshotMapper
    );

    @Test
    void analyzeReusesMaxRadiusTransitLookupsAndIncludesTransitDetails() {
        List<BusStop> busStops = List.of(
                new BusStop("B-300", "near bus", "10001", "23", 37.5005, 127.0005),
                new BusStop("B-1000", "far bus", "10002", "23", 37.506, 127.006)
        );
        List<SubwayStation> subwayStations = List.of(
                new SubwayStation("S-500", "near subway", "address", 37.5004, 127.0004, 80),
                new SubwayStation("S-1000", "far subway", "address", 37.506, 127.006, 900)
        );
        when(commercialClient.places(127.0, 37.5, 1000)).thenReturn(List.of(
                new CommercialPlace("store", "음식", "한식", "address", 127.0, 37.5)
        ));
        when(itsClient.events(127.0, 37.5)).thenReturn(List.of(
                new TrafficEvent("일반", "원활", "road", 127.0, 37.5)
        ));
        when(busStopService.findNearby(37.5, 127.0, 1000, 300)).thenReturn(busStops);
        when(kakaoLocalApiClient.subwayStations(127.0, 37.5, 1000)).thenReturn(subwayStations);

        HousingAnalysis analysis = analysisService.analyze("test", 127.0, 37.5, 1000);

        verify(busStopService, times(1)).findNearby(37.5, 127.0, 1000, 300);
        verify(kakaoLocalApiClient, times(1)).subwayStations(127.0, 37.5, 1000);
        assertThat(analysis.busStops()).isEqualTo(busStops);
        assertThat(analysis.subwayStations()).isEqualTo(subwayStations);
        assertThat(analysis.transitSummary().busStopWithin300m()).isEqualTo(1);
        assertThat(analysis.transitSummary().busStopWithin1000m()).isEqualTo(2);
        assertThat(analysis.transitSummary().subwayWithin500m()).isEqualTo(1);
        assertThat(analysis.transitSummary().subwayWithin1000m()).isEqualTo(2);
    }

    @Test
    void analyzeReturnsCachedResultForSameCoordinateAndRadius() {
        when(commercialClient.places(127.0, 37.5, 1000)).thenReturn(List.of(
                new CommercialPlace("store", "음식", "한식", "address", 127.0, 37.5)
        ));
        when(itsClient.events(127.0, 37.5)).thenReturn(List.of());
        when(busStopService.findNearby(37.5, 127.0, 1000, 300)).thenReturn(List.of());
        when(kakaoLocalApiClient.subwayStations(127.0, 37.5, 1000)).thenReturn(List.of());

        HousingAnalysis first = analysisService.analyze("test", 127.0, 37.5, 1000);
        HousingAnalysis second = analysisService.analyze("test", 127.0, 37.5, 1000);

        assertThat(second).isSameAs(first);
        verify(commercialClient, times(1)).places(127.0, 37.5, 1000);
        verify(itsClient, times(1)).events(127.0, 37.5);
        verify(busStopService, times(1)).findNearby(37.5, 127.0, 1000, 300);
        verify(kakaoLocalApiClient, times(1)).subwayStations(127.0, 37.5, 1000);
    }
}
