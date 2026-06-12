package com.happyhome.ai;

public record AiChatResponse(
        String answer,
        int indexedDocumentCount
) {
}
