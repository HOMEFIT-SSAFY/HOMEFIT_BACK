package com.happyhome.rental.service;

import com.happyhome.openapi.LhOpenApiClient;
import com.happyhome.openapi.SampleData;
import com.happyhome.rental.dao.RentalNoticeMapper;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.dto.RentalSearchCondition;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RentalService {

    private final LhOpenApiClient lhClient;
    private final RentalNoticeMapper mapper;

    public RentalService(LhOpenApiClient lhClient, RentalNoticeMapper mapper) {
        this.lhClient = lhClient;
        this.mapper = mapper;
    }

    public List<RentalNotice> notices(RentalSearchCondition condition) {
        List<RentalNotice> notices = lhClient.notices(condition);
        notices.forEach(this::cacheQuietly);
        return notices;
    }

    public RentalNoticeDetail detail(String noticeId) {
        RentalNotice notice = mapper.findById(noticeId)
                .orElseGet(() -> SampleData.rentalNotices().stream()
                        .filter(item -> item.noticeId().equals(noticeId))
                        .findFirst()
                        .orElseGet(() -> uncachedNotice(noticeId)));
        return new RentalNoticeDetail(notice, lhClient.detail(notice), lhClient.supplies(notice));
    }

    private RentalNotice uncachedNotice(String noticeId) {
        return new RentalNotice(
                noticeId,
                "공고 정보를 찾을 수 없습니다.",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "api"
        );
    }

    private void cacheQuietly(RentalNotice notice) {
        try {
            mapper.upsert(notice);
        } catch (Exception ignored) {
            // API results should still be usable when local cache initialization is unavailable.
        }
    }
}

