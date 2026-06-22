package com.happyhome.rental.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.happyhome.batch.dto.NoticeLHDetail;
import com.happyhome.batch.dto.NoticeLHSupply;
import com.happyhome.batch.mapper.NoticeLHBatchMapper;
import com.happyhome.openapi.LhOpenApiClient;
import com.happyhome.rental.dao.RentalNoticeMapper;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.dto.RentalSupply;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private LhOpenApiClient lhClient;

    @Mock
    private RentalNoticeMapper mapper;

    @Mock
    private NoticeLHBatchMapper noticeLHBatchMapper;

    @Test
    void fetchesAndCachesDetailAndSuppliesWhenLocalCacheIsEmpty() {
        RentalNotice notice = new RentalNotice(
                "LH-001",
                "행복주택 공고",
                "서울특별시",
                "임대주택",
                "행복주택",
                "공고중",
                "2026.06.18",
                "2026.06.29",
                "https://apply.lh.or.kr",
                "03",
                "06",
                "10",
                "063",
                "api"
        );
        RentalDetail fetchedDetail = new RentalDetail(
                "LH 서울지역본부",
                "강남구",
                "2026.06.20",
                "2026.06.24",
                "1600-1004"
        );
        RentalSupply fetchedSupply = new RentalSupply(
                "청년",
                "서울 강남구",
                "26㎡",
                "보증금 200만원 / 월 18만원",
                "26A",
                "20"
        );

        when(mapper.findById("LH-001")).thenReturn(Optional.of(notice));
        when(mapper.findDetailByNoticeId("LH-001")).thenReturn(Optional.empty());
        when(mapper.findSuppliesByNoticeId("LH-001")).thenReturn(List.of());
        when(lhClient.detail(notice)).thenReturn(fetchedDetail);
        when(lhClient.supplies(notice)).thenReturn(List.of(fetchedSupply));

        RentalService rentalService = new RentalService(lhClient, mapper, noticeLHBatchMapper);

        RentalNoticeDetail result = rentalService.detail("LH-001");

        assertThat(result.detail()).isEqualTo(fetchedDetail);
        assertThat(result.supplies()).containsExactly(fetchedSupply);
        verify(noticeLHBatchMapper).upsertDetail(NoticeLHDetail.from("LH-001", fetchedDetail));
        verify(noticeLHBatchMapper).deleteSuppliesByNoticeId("LH-001");
        verify(noticeLHBatchMapper).insertSupply(NoticeLHSupply.from("LH-001", fetchedSupply));
    }

    @Test
    void refreshesPreviouslyCachedSampleDetail() {
        RentalNotice notice = new RentalNotice(
                "LH-002",
                "행복주택 공고",
                "인천광역시",
                "임대주택",
                "행복주택",
                "공고중",
                "2026.06.18",
                "2026.06.29",
                "https://apply.lh.or.kr",
                "03",
                "06",
                "10",
                "063",
                "api"
        );
        RentalDetail staleSampleDetail = new RentalDetail(
                "LH 서울지역본부",
                "서울특별시 강남구",
                "2026.05.21",
                "2026.05.28",
                "1600-1004"
        );
        RentalDetail fetchedDetail = new RentalDetail(
                "인천광역시 강화군",
                "남문안길",
                "2026.06.29",
                "2026.06.29",
                "1600-1004"
        );

        when(mapper.findById("LH-002")).thenReturn(Optional.of(notice));
        when(mapper.findDetailByNoticeId("LH-002")).thenReturn(Optional.of(staleSampleDetail));
        when(mapper.findSuppliesByNoticeId("LH-002")).thenReturn(List.of(new RentalSupply(
                "26A",
                "인천새시장",
                "26.95",
                "공고문 참조",
                "26A",
                "2"
        )));
        when(lhClient.detail(notice)).thenReturn(fetchedDetail);

        RentalService rentalService = new RentalService(lhClient, mapper, noticeLHBatchMapper);

        RentalNoticeDetail result = rentalService.detail("LH-002");

        assertThat(result.detail()).isEqualTo(fetchedDetail);
        verify(noticeLHBatchMapper).upsertDetail(NoticeLHDetail.from("LH-002", fetchedDetail));
    }
}
