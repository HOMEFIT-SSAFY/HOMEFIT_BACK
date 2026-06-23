package com.happyhome.analysis.dto;

import com.happyhome.commercial.dto.CommercialPlace;
import com.happyhome.commercial.dto.CommercialSummary;
import com.happyhome.traffic.dto.TrafficEvent;
import com.happyhome.traffic.dto.TrafficRiskSummary;
import com.happyhome.transport.dto.BusStop;
import com.happyhome.transport.dto.SubwayStation;
import java.util.List;

public record HousingAnalysis(
        String label,
        double latitude,
        double longitude,
        int radiusMeters,
        CommercialSummary commercialSummary,
        TrafficRiskSummary trafficRiskSummary,
        TransitSummary transitSummary,
        AnalysisScore score,
        List<CommercialPlace> places,
        List<TrafficEvent> events,
        List<BusStop> busStops,
        List<SubwayStation> subwayStations,
        String source
) {
}
