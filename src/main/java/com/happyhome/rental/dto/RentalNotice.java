package com.happyhome.rental.dto;

public record RentalNotice(
        String noticeId,
        String title,
        String regionName,
        String noticeType,
        String detailType,
        String status,
        String noticeDate,
        String closeDate,
        String detailUrl,
        String ccrCnntSysDsCd,
        String uppAisTpCd,
        String aisTpCd,
        String splInfTpCd,
        String source
) {
}

