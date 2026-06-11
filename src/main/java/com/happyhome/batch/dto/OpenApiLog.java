package com.happyhome.batch.dto;

import java.time.LocalDateTime;

public record OpenApiLog(
		String jobName,
        String apiName,
        String status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        int fetchedCount,
        int savedCount,
        String errorMessage
		) {

}
