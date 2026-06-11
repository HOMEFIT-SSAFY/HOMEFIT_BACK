package com.happyhome.batch.dto;

import java.util.List;

public record NoticeLHResult (
		String status,
		int fetchedCount,
		int savedCount,
		List<String> errors
		) {

}
