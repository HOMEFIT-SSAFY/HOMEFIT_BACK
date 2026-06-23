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
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RentalService {

    private final LhOpenApiClient lhClient;
    private final RentalNoticeMapper mapper;
    private final NoticeLHBatchMapper noticeLHBatchMapper;

    @Autowired
    public RentalService(LhOpenApiClient lhClient, RentalNoticeMapper mapper, NoticeLHBatchMapper noticeLHBatchMapper) {
        this.lhClient = lhClient;
        this.mapper = mapper;
        this.noticeLHBatchMapper = noticeLHBatchMapper;
    }

    public RentalService(LhOpenApiClient lhClient, RentalNoticeMapper mapper) {
        this(lhClient, mapper, null);
    }

    public List<RentalNotice> notices(RentalSearchCondition condition) {
        List<RentalNotice> cachedNotices = mapper.findByCondition(condition);
        if (!cachedNotices.isEmpty()) {
            return cachedNotices;
        }

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
        RentalDetail detail = detailFor(notice);
        List<RentalSupply> supplies = suppliesFor(notice);
        return new RentalNoticeDetail(notice, detail, supplies);
    }

    private RentalDetail detailFor(RentalNotice notice) {
        Optional<RentalDetail> cachedDetail = mapper.findDetailByNoticeId(notice.noticeId());
        if (cachedDetail.isPresent()) {
            return cachedDetail.get();
        }

        RentalDetail fetchedDetail = lhClient.detail(notice);
        cacheDetailQuietly(notice.noticeId(), fetchedDetail);
        return fetchedDetail;
    }

    private List<RentalSupply> suppliesFor(RentalNotice notice) {
        List<RentalSupply> cachedSupplies = mapper.findSuppliesByNoticeId(notice.noticeId());
        if (!cachedSupplies.isEmpty()) {
            return cachedSupplies;
        }

        List<RentalSupply> fetchedSupplies = lhClient.supplies(notice);
        cacheSuppliesQuietly(notice.noticeId(), fetchedSupplies);
        return fetchedSupplies;
    }

    private void cacheDetailQuietly(String noticeId, RentalDetail detail) {
        if (noticeLHBatchMapper == null || detail == null) {
            return;
        }
        try {
            noticeLHBatchMapper.upsertDetail(NoticeLHDetail.from(noticeId, detail));
        } catch (Exception ignored) {
            // Live API detail should still be returned when cache persistence fails.
        }
    }

    private void cacheSuppliesQuietly(String noticeId, List<RentalSupply> supplies) {
        if (noticeLHBatchMapper == null || supplies == null) {
            return;
        }
        try {
            noticeLHBatchMapper.deleteSuppliesByNoticeId(noticeId);
            for (RentalSupply supply : supplies) {
                noticeLHBatchMapper.insertSupply(NoticeLHSupply.from(noticeId, supply));
            }
        } catch (Exception ignored) {
            // Live API supplies should still be returned when cache persistence fails.
        }
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

