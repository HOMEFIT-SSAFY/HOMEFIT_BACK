package com.happyhome.rental.dto;

public record RentalDetail(
        String contractAddress,
        String contractDetailAddress,
        String applyStartDate,
        String applyEndDate,
        String contact
) {
}

