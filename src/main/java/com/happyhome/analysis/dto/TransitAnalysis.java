package com.happyhome.analysis.dto;

import com.happyhome.transport.dto.BusStop;
import com.happyhome.transport.dto.SubwayStation;
import java.util.List;

public record TransitAnalysis(
        TransitSummary summary,
        List<BusStop> busStops,
        List<SubwayStation> subwayStations
) {
}
