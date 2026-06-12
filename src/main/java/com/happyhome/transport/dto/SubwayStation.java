package com.happyhome.transport.dto;

public record SubwayStation(
        String id,
        String name,
        String address,
        double latitude,
        double longitude,
        int distanceMeters
) {
}
