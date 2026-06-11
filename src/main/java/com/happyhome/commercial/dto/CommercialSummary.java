package com.happyhome.commercial.dto;

public record CommercialSummary(
        int totalCount,
        int foodCount,
        int cafeCount,
        int medicalCount,
        int convenienceCount,
        int lifeCount
) {
}

