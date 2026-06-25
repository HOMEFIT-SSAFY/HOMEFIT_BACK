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
        String applyStartDate,
        String applyEndDate,
        String detailUrl,
        String ccrCnntSysDsCd,
        String uppAisTpCd,
        String aisTpCd,
        String splInfTpCd,
        String source
) {
    public RentalNotice(
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
        this(
                noticeId,
                title,
                regionName,
                noticeType,
                detailType,
                status,
                noticeDate,
                closeDate,
                null,
                null,
                detailUrl,
                ccrCnntSysDsCd,
                uppAisTpCd,
                aisTpCd,
                splInfTpCd,
                source
        );
    }
}

