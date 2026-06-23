package com.happyhome.transport.service;

import com.happyhome.openapi.KakaoLocalApiClient;
import com.happyhome.transport.dto.SubwayStation;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SubwayStationService {

    private final KakaoLocalApiClient kakaoLocalApiClient;

    public SubwayStationService(KakaoLocalApiClient kakaoLocalApiClient) {
        this.kakaoLocalApiClient = kakaoLocalApiClient;
    }

    public List<SubwayStation> findNearby(double latitude, double longitude, int radiusMeters) {
        int safeRadius = Math.min(Math.max(radiusMeters, 100), 20000);
        return kakaoLocalApiClient.subwayStations(longitude, latitude, safeRadius);
    }
}
