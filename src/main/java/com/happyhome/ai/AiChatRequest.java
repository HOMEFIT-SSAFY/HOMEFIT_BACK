package com.happyhome.ai;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank(message = "question is required")
        String question,
        String page,
        String route,
        String label,
        Double latitude,
        Double longitude,
        Integer radiusMeters
) {
}
