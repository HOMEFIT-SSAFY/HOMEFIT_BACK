package com.happyhome.notice.dto;

public record NoticeRequest(
        String title,
        String content,
        String writer,
        Boolean popupEnabled,
        Boolean pinned,
        Boolean visible
) {
}
