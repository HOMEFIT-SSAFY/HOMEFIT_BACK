package com.happyhome.rental.dto;

public record RentalSupply(
        String usage,
        String address,
        String area,
        String expectedAmount,
        String houseType,
        String householdCount
) {
}

