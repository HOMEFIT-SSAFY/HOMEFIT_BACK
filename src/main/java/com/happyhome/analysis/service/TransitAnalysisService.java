package com.happyhome.analysis.service;

import com.happyhome.analysis.dto.TransitAnalysis;
import com.happyhome.analysis.dto.TransitSummary;
import com.happyhome.openapi.KakaoLocalApiClient;
import com.happyhome.transport.dto.BusStop;
import com.happyhome.transport.dto.SubwayStation;
import com.happyhome.transport.service.BusStopService;
import java.util.List;
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
        return analyze(longitude, latitude, 1000).summary();
    }

    public TransitAnalysis analyze(double longitude, double latitude, int radiusMeters) {
        int safeRadius = Math.min(Math.max(radiusMeters, 100), 3000);
        List<BusStop> busStops = busStopService.findNearby(latitude, longitude, safeRadius, 300);
        List<SubwayStation> subwayStations = kakaoLocalApiClient.subwayStations(longitude, latitude, safeRadius);

        int bus300 = countBusStopsWithin(busStops, latitude, longitude, 300);
        int bus500 = countBusStopsWithin(busStops, latitude, longitude, 500);
        int bus1000 = countBusStopsWithin(busStops, latitude, longitude, 1000);
        int subway500 = countSubwayStationsWithin(subwayStations, latitude, longitude, 500);
        int subway1000 = countSubwayStationsWithin(subwayStations, latitude, longitude, 1000);

        return new TransitAnalysis(
                new TransitSummary(subway500, subway1000, bus300, bus500, bus1000),
                busStops,
                subwayStations
        );
    }

    private int countBusStopsWithin(List<BusStop> stops, double latitude, double longitude, int radiusMeters) {
        int count = 0;
        for (BusStop stop : stops) {
            if (stop.latitude() != null
                    && stop.longitude() != null
                    && distanceMeters(latitude, longitude, stop.latitude(), stop.longitude()) <= radiusMeters) {
                count++;
            }
        }
        return count;
    }

    private int countSubwayStationsWithin(
            List<SubwayStation> stations,
            double latitude,
            double longitude,
            int radiusMeters
    ) {
        int count = 0;
        for (SubwayStation station : stations) {
            int distance = station.distanceMeters() > 0
                    ? station.distanceMeters()
                    : distanceMeters(latitude, longitude, station.latitude(), station.longitude());
            if (distance <= radiusMeters) {
                count++;
            }
        }
        return count;
    }

    private int distanceMeters(double fromLat, double fromLon, double toLat, double toLon) {
        double earthRadiusMeters = 6371000;
        double dLat = Math.toRadians(toLat - fromLat);
        double dLon = Math.toRadians(toLon - fromLon);
        double lat1 = Math.toRadians(fromLat);
        double lat2 = Math.toRadians(toLat);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) Math.round(earthRadiusMeters * c);
    }
}
