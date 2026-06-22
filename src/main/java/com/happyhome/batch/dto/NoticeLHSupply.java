package com.happyhome.batch.dto;

import com.happyhome.rental.dto.RentalSupply;

public record NoticeLHSupply(
		String noticeId,
        String usage,
        String address,
        String area,
        String expectedAmount,
        String houseType,
        String householdCount
		) {

	public static NoticeLHSupply from(String noticeId, RentalSupply supply) {
		return new NoticeLHSupply(
				noticeId,
                supply.usage(),
                supply.address(),
                supply.area(),
                supply.expectedAmountRaw(),
                supply.houseType(),
                supply.householdCount()
				);
	}
}
