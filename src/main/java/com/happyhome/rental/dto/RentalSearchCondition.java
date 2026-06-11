package com.happyhome.rental.dto;

public record RentalSearchCondition(
        String keyword,
        String regionCode,
        String status,
        int page,
        int size
) {
    public RentalSearchCondition {
        page = page <= 0 ? 1 : page;
        size = size <= 0 ? 12 : size;
    }
}

