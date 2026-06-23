package com.happyhome.transport.controller;

import com.happyhome.transport.dto.BusStop;
import com.happyhome.transport.dto.BusStopStatus;
import com.happyhome.transport.dto.BusStopSyncResult;
import com.happyhome.transport.service.BusStopService;
import com.happyhome.transport.service.BusStopSyncService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bus-stops")
public class BusStopRestController {

    private final BusStopService busStopService;
    private final BusStopSyncService busStopSyncService;

    public BusStopRestController(BusStopService busStopService, BusStopSyncService busStopSyncService) {
        this.busStopService = busStopService;
        this.busStopSyncService = busStopSyncService;
    }

    @GetMapping("/nearby")
    public List<BusStop> nearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "500") int radiusMeters,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return busStopService.findNearby(latitude, longitude, radiusMeters, limit);
    }

    @GetMapping("/nearby/openapi")
    public List<BusStop> nearbyFromOpenApi(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        return busStopService.findNearbyFromOpenApi(latitude, longitude);
    }

    @GetMapping("/status")
    public BusStopStatus status() {
        return busStopSyncService.status();
    }

    @PostMapping("/sync")
    public BusStopSyncResult sync(@RequestParam(required = false) Integer maxCities) {
        return busStopSyncService.syncAll(maxCities);
    }
}
