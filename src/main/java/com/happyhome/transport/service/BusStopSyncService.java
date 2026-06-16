package com.happyhome.transport.service;

import com.happyhome.openapi.BusOpenApiClient;
import com.happyhome.transport.dao.BusStopMapper;
import com.happyhome.transport.dto.BusCityCode;
import com.happyhome.transport.dto.BusStop;
import com.happyhome.transport.dto.BusStopStatus;
import com.happyhome.transport.dto.BusStopSyncResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BusStopSyncService {

    private final BusOpenApiClient busOpenApiClient;
    private final BusStopMapper busStopMapper;

    public BusStopSyncService(BusOpenApiClient busOpenApiClient, BusStopMapper busStopMapper) {
        this.busOpenApiClient = busOpenApiClient;
        this.busStopMapper = busStopMapper;
    }

    public BusStopSyncResult syncAll(Integer maxCities) {
        List<String> errors = new ArrayList<>();
        int fetchedCount = 0;
        int savedCount = 0;

        if (!busOpenApiClient.isConfigured()) {
            return new BusStopSyncResult("FAILED", 0, 0, 0,
                    List.of("OPENAPI_BUS_SERVICE_KEY is not configured."));
        }

        List<BusCityCode> cityCodes = busOpenApiClient.cityCodes();
        if (cityCodes.isEmpty()) {
            return new BusStopSyncResult("FAILED", 0, 0, 0,
                    List.of("No city codes returned from the bus open API."));
        }

        int cityLimit = maxCities == null || maxCities <= 0
                ? cityCodes.size()
                : Math.min(maxCities, cityCodes.size());

        for (BusCityCode cityCode : cityCodes.subList(0, cityLimit)) {
            try {
                busStopMapper.upsertCityCode(cityCode);
                List<BusStop> stops = busOpenApiClient.stops(cityCode.cityCode());
                fetchedCount += stops.size();
                for (BusStop stop : stops) {
                    busStopMapper.upsertBusStop(stop);
                    savedCount++;
                }
            } catch (Exception e) {
                errors.add(cityCode.cityCode() + " " + cityCode.cityName() + ": " + e.getMessage());
            }
        }

        String status = errors.isEmpty() ? "SUCCESS" : "PARTIAL_FAILED";
        return new BusStopSyncResult(status, cityLimit, fetchedCount, savedCount, errors);
    }

    public int countBusStops() {
        return busStopMapper.countBusStops();
    }

    public BusStopStatus status() {
        boolean configured = busOpenApiClient.isConfigured();
        int count = busStopMapper.countBusStops();
        boolean ready = count > 0;
        String message;
        if (!configured) {
            message = "OPENAPI_BUS_SERVICE_KEY is not configured.";
        } else if (!ready) {
            message = "No bus stops are loaded. Run POST /api/bus-stops/sync first.";
        } else {
            message = "Bus stop data is ready.";
        }
        return new BusStopStatus(configured, count, ready, message);
    }
}
