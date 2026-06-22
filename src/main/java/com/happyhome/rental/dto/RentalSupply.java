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
    public RentalSupply(
            String usage,
            String address,
            String area,
            String expectedAmount,
            String houseType,
            String householdCount
    ) {
        this(
                usage,
                address,
                "",
                area,
                expectedAmount,
                expectedAmount == null ? "" : expectedAmount.replace(",", ""),
                houseType,
                householdCount,
                "",
                address,
                mapUrl(address)
        );
    }

    private static String mapUrl(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }
        return "https://map.naver.com/v5/search/" + java.net.URLEncoder.encode(address, java.nio.charset.StandardCharsets.UTF_8);
    }
}

