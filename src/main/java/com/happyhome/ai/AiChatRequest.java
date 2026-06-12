package com.happyhome.ai;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank(message = "question is required")
        String question
) {
}
