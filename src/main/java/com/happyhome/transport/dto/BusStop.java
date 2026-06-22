package com.happyhome.transport.dto;

public record BusStop(
        String nodeId,
        String nodeName,
        String nodeNo,
        String cityCode,
        Double latitude,
        Double longitude
) {
}
