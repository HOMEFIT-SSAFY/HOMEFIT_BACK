package com.happyhome.property.dto;

public record PropertyDealSearchCondition(
        PropertyType propertyType,
        PropertyDealType dealType,
        String keyword,
        String lawdCd,
        String sidoName,
        String gugunName,
        String dongName,
        int limit
) {
    public PropertyDealSearchCondition {
        limit = limit <= 0 ? 50 : Math.min(limit, 500);
    }
}
