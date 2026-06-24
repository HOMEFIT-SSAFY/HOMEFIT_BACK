package com.happyhome.property.dto;

public record PropertyDeal(
        Long propertyDealId,
        PropertyType propertyType,
        PropertyDealType dealType,
        String sourceId,
        String lawdCd,
        String sidoName,
        String gugunName,
        String dongName,
        String propertyName,
        String dealDate,
        String dealAmount,
        String depositAmount,
        String monthlyRentAmount,
        String exclusiveArea,
        String floor,
        String buildYear,
        String jibun,
        Double latitude,
        Double longitude,
        String source
) {
}
