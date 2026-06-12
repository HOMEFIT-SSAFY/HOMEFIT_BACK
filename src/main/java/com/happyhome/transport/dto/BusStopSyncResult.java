package com.happyhome.transport.dto;

import java.util.List;

public record BusStopSyncResult(
        String status,
        int cityCount,
        int fetchedCount,
        int savedCount,
        List<String> errors
) {
}
