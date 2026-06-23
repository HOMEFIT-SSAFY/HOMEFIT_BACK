package com.happyhome.transport.service;

import com.happyhome.openapi.BusOpenApiClient;
import com.happyhome.openapi.KakaoLocalApiClient;
import com.happyhome.transport.dao.BusStopMapper;
import com.happyhome.transport.dto.BusStop;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BusStopService {

    private final BusStopMapper busStopMapper;
    private final BusOpenApiClient busOpenApiClient;
    private final KakaoLocalApiClient kakaoLocalApiClient;

    public BusStopService(
            BusStopMapper busStopMapper,
            BusOpenApiClient busOpenApiClient,
            KakaoLocalApiClient kakaoLocalApiClient
    ) {
        this.busStopMapper = busStopMapper;
        this.busOpenApiClient = busOpenApiClient;
        this.kakaoLocalApiClient = kakaoLocalApiClient;
    }

    public List<BusStop> findNearby(double latitude, double longitude, int radiusMeters, int limit) {
        int safeRadius = Math.min(Math.max(radiusMeters, 100), 3000);
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        List<BusStop> openApiStops = nearbyStopsFromOpenApi(latitude, longitude);
        if (!openApiStops.isEmpty()) {
            return openApiStops.stream()
                    .limit(safeLimit)
                    .toList();
        }
        List<BusStop> kakaoStops = busStopsFromKakao(latitude, longitude, safeRadius);
        if (!kakaoStops.isEmpty()) {
            return kakaoStops.stream()
                    .limit(safeLimit)
                    .toList();
        }
        return busStopMapper.findNearby(latitude, longitude, safeRadius, safeLimit);
    }

    public List<BusStop> findNearbyFromOpenApi(double latitude, double longitude) {
        return busOpenApiClient.nearbyStops(latitude, longitude);
    }

    private List<BusStop> nearbyStopsFromOpenApi(double latitude, double longitude) {
        try {
            return busOpenApiClient.nearbyStops(latitude, longitude);
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private List<BusStop> busStopsFromKakao(double latitude, double longitude, int radiusMeters) {
        try {
            List<BusStop> stops = kakaoLocalApiClient.busStops(longitude, latitude, radiusMeters);
            return stops == null ? List.of() : stops;
        } catch (RuntimeException e) {
            return List.of();
        }
    }
}
