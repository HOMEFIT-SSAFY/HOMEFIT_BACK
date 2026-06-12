package com.happyhome.analysis.service;

import com.happyhome.analysis.dto.TransitSummary;
import com.happyhome.openapi.KakaoLocalApiClient;
import com.happyhome.transport.service.BusStopService;
import org.springframework.stereotype.Service;

@Service
public class TransitAnalysisService {

    private final BusStopService busStopService;
    private final KakaoLocalApiClient kakaoLocalApiClient;

    public TransitAnalysisService(BusStopService busStopService, KakaoLocalApiClient kakaoLocalApiClient) {
        this.busStopService = busStopService;
        this.kakaoLocalApiClient = kakaoLocalApiClient;
    }

    public TransitSummary summarize(double longitude, double latitude) {
        int bus300 = busStopService.findNearby(latitude, longitude, 300, 100).size();
        int bus500 = busStopService.findNearby(latitude, longitude, 500, 100).size();
        int bus1000 = busStopService.findNearby(latitude, longitude, 1000, 300).size();
        int subway500 = kakaoLocalApiClient.subwayStations(longitude, latitude, 500).size();
        int subway1000 = kakaoLocalApiClient.subwayStations(longitude, latitude, 1000).size();

        return new TransitSummary(subway500, subway1000, bus300, bus500, bus1000);
    }
}
