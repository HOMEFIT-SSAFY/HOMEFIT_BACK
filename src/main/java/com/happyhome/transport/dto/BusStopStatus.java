package com.happyhome.transport.dto;

public record BusStopStatus(
        boolean busApiConfigured,
        int busStopCount,
        boolean busDataReady,
        String message
) {
}
