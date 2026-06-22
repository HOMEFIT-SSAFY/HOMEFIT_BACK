package com.happyhome.rental.dto;

public record RentalSupply(
        String usage,
        String address,
        String lotNumber,
        String area,
        String expectedAmount,
        String expectedAmountRaw,
        String houseType,
        String householdCount,
        String internetApplyStatus,
        String mapAddress,
        String mapUrl
) {
}

