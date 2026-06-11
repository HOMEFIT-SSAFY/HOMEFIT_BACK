package com.happyhome.batch.dto;

import com.happyhome.rental.dto.RentalDetail;

public record NoticeLHDetail(
		String noticeId,
		String contractAddress,
		String contractDetailAddress,
		String applyStartDate,
		String applyEndDate,
		String contact
		) {
	public static NoticeLHDetail from(String noticeId, RentalDetail detail) {
		return new NoticeLHDetail(
				noticeId,
				detail.contractAddress(),
				detail.contractDetailAddress(),
				detail.applyStartDate(),
				detail.applyEndDate(),
				detail.contact()
				);
	}
}
