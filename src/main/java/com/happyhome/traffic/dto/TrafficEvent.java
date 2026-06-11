package com.happyhome.traffic.dto;

public record TrafficEvent(
        String type,
        String message,
        String roadName,
        double longitude,
        double latitude
) {
    public boolean roadwork() {
        return contains(type, "공사") || contains(message, "공사") || contains(message, "통제");
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.contains(keyword);
    }
}
