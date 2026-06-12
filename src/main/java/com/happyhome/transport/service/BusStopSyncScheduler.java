package com.happyhome.transport.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "batch.bus",
        name = "enabled",
        havingValue = "true"
)
public class BusStopSyncScheduler {

    private final BusStopSyncService busStopSyncService;

    public BusStopSyncScheduler(BusStopSyncService busStopSyncService) {
        this.busStopSyncService = busStopSyncService;
    }

    @Scheduled(cron = "${batch.bus.cron}")
    public void syncBusStops() {
        busStopSyncService.syncAll(null);
    }
}
