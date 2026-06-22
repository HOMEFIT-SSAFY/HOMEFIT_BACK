package com.happyhome.rental.service;

import com.happyhome.batch.dto.NoticeLHDetail;
import com.happyhome.batch.dto.NoticeLHSupply;
import com.happyhome.batch.mapper.NoticeLHBatchMapper;
import com.happyhome.openapi.LhOpenApiClient;
import com.happyhome.openapi.SampleData;
import com.happyhome.rental.dao.RentalNoticeMapper;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.dto.RentalSupply;

import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalService {

	private final LhOpenApiClient lhClient;
	private final RentalNoticeMapper mapper;
	private final NoticeLHBatchMapper noticeLHBatchMapper;

	public List<RentalNotice> notices(RentalSearchCondition condition) {
		List<RentalNotice> notices = lhClient.notices(condition);
		notices.forEach(this::cacheQuietly);
		return notices;
	}

	public RentalNoticeDetail detail(String noticeId) {
		RentalNotice notice = mapper.findById(noticeId)
				.orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다. noticeId=" + noticeId));

		RentalDetail detail = mapper.findDetailByNoticeId(noticeId)
				.filter(cachedDetail -> !isSampleFallbackDetail(cachedDetail))
				.orElseGet(() -> fetchAndCacheDetail(notice));

		List<RentalSupply> supplies = mapper.findSuppliesByNoticeId(noticeId);
		if (supplies.isEmpty()) {
			supplies = fetchAndCacheSupplies(notice);
		}

		return new RentalNoticeDetail(notice, detail, supplies);
	}

	private RentalDetail fetchAndCacheDetail(RentalNotice notice) {
		RentalDetail detail = lhClient.detail(notice);
		noticeLHBatchMapper.upsertDetail(NoticeLHDetail.from(notice.noticeId(), detail));
		return detail;
	}

	private boolean isSampleFallbackDetail(RentalDetail detail) {
		return "LH 서울지역본부".equals(detail.contractAddress())
				&& "서울특별시 강남구".equals(detail.contractDetailAddress())
				&& "2026.05.21".equals(detail.applyStartDate())
				&& "2026.05.28".equals(detail.applyEndDate());
	}

	private List<RentalSupply> fetchAndCacheSupplies(RentalNotice notice) {
		List<RentalSupply> supplies = lhClient.supplies(notice);
		noticeLHBatchMapper.deleteSuppliesByNoticeId(notice.noticeId());
		for (RentalSupply supply : supplies) {
			noticeLHBatchMapper.insertSupply(NoticeLHSupply.from(notice.noticeId(), supply));
		}
		return supplies;
	}

	private void cacheQuietly(RentalNotice notice) {
		try {
			mapper.upsert(notice);
		} catch (Exception ignored) {
			// API results should still be usable when local cache initialization is
			// unavailable.
		}
	}
}
