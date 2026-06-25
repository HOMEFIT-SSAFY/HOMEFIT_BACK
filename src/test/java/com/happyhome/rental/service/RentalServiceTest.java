package com.happyhome.rental.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.happyhome.openapi.KakaoLocalApiClient;
import com.happyhome.openapi.LhOpenApiClient;
import com.happyhome.openapi.dto.GeoCoordinate;
import com.happyhome.rental.dao.RentalNoticeMapper;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.dto.RentalSupply;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private LhOpenApiClient lhClient;

    @Mock
    private RentalNoticeMapper mapper;

    @Mock
    private KakaoLocalApiClient kakaoLocalApiClient;

    @Test
    void returnsCachedRentalNoticesBeforeCallingLhApi() {
        RentalSearchCondition condition = new RentalSearchCondition("", "", "공고중", 1, 12);
        RentalNotice notice = new RentalNotice(
                "LH-001", "Cached notice", "Seoul", "rental", "public", "공고중",
                "2026.06.18", "2026.06.29", "https://apply.lh.or.kr",
                "03", "06", "10", "063", "api"
        );
        when(mapper.findByCondition(condition)).thenReturn(List.of(notice));

        List<RentalNotice> result = new RentalService(lhClient, mapper).notices(condition);

        assertThat(result).containsExactly(notice);
        verify(lhClient, never()).notices(condition);
    }

    @Test
    void refreshesConfiguredLhApiAndAddsApplicationDatesForCalendarRequests() {
        RentalSearchCondition condition = new RentalSearchCondition("", "", "", 1, 100);
        RentalNotice notice = new RentalNotice(
                "LH-NEW", "Future application notice", "Seoul", "rental", "public", "open",
                "2026.06.10", "2026.07.15", "https://apply.lh.or.kr",
                "03", "06", "10", "063", "api"
        );
        RentalDetail detail = new RentalDetail(
                "LH Seoul office", "Gangnam", "2026.06.26", "2026.06.30", "1600-1004"
        );
        when(lhClient.isConfigured()).thenReturn(true);
        when(lhClient.apiNotices(eq(condition), anyString(), eq("2099.12.31"))).thenReturn(List.of(notice));
        when(mapper.findByCondition(condition)).thenReturn(List.of(notice));
        when(mapper.findDetailByNoticeId("LH-NEW")).thenReturn(Optional.empty());
        when(lhClient.detail(notice)).thenReturn(detail);

        List<RentalNotice> result = new RentalService(lhClient, mapper).notices(condition);

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.noticeId()).isEqualTo("LH-NEW");
            assertThat(item.applyStartDate()).isEqualTo("2026.06.26");
            assertThat(item.applyEndDate()).isEqualTo("2026.06.30");
        });
        verify(mapper).upsert(notice);
    }

    @Test
    void returnsCurrentLhDetailAndSupplies() {
        RentalNotice notice = new RentalNotice(
                "LH-001", "Rental notice", "Seoul", "rental", "public", "open",
                "2026.06.18", "2026.06.29", "https://apply.lh.or.kr",
                "03", "06", "10", "063", "api"
        );
        RentalDetail detail = new RentalDetail(
                "LH Seoul office", "Gangnam", "2026.06.20", "2026.06.24", "1600-1004"
        );
        RentalSupply supply = new RentalSupply(
                "youth", "Seoul Gangnam", "", "26", "2,000,000", "2000000",
                "26A", "20", "available", "Seoul Gangnam", "https://map.example"
        );
        when(mapper.findById("LH-001")).thenReturn(Optional.of(notice));
        when(lhClient.detail(notice)).thenReturn(detail);
        when(lhClient.supplies(notice)).thenReturn(List.of(supply));

        RentalNoticeDetail result = new RentalService(lhClient, mapper).detail("LH-001");

        assertThat(result.detail()).isEqualTo(detail);
        assertThat(result.supplies()).containsExactly(supply);
    }

    @Test
    void attachesCoordinatesToLhSuppliesWhenGeocodingSucceeds() {
        RentalNotice notice = new RentalNotice(
                "LH-001", "Rental notice", "Seoul", "rental", "public", "open",
                "2026.06.18", "2026.06.29", "https://apply.lh.or.kr",
                "03", "06", "10", "063", "api"
        );
        RentalSupply supply = new RentalSupply(
                "youth", "서울특별시 강남구 개포로 310", "168",
                "26", "2,000,000", "2000000", "26A", "20",
                "available", "서울특별시 강남구 개포로 310 168", "https://map.example"
        );
        when(mapper.findById("LH-001")).thenReturn(Optional.of(notice));
        when(mapper.findDetailByNoticeId("LH-001")).thenReturn(Optional.of(new RentalDetail("", "", "", "", "")));
        when(mapper.findSuppliesByNoticeId("LH-001")).thenReturn(List.of(supply));
        when(kakaoLocalApiClient.geocode("서울특별시 강남구 개포로 310 168"))
                .thenReturn(Optional.of(new GeoCoordinate(37.4919, 127.0776)));

        RentalNoticeDetail result = new RentalService(lhClient, mapper, null, kakaoLocalApiClient).detail("LH-001");

        assertThat(result.supplies()).singleElement().satisfies(item -> {
            assertThat(item.latitude()).isEqualTo(37.4919);
            assertThat(item.longitude()).isEqualTo(127.0776);
        });
    }

    @Test
    void refreshesImpreciseCachedSuppliesBeforeMapping() {
        RentalNotice notice = new RentalNotice(
                "LH-001", "Rental notice", "Seoul", "rental", "public", "open",
                "2026.06.18", "2026.06.29", "https://apply.lh.or.kr",
                "03", "06", "10", "063", "api"
        );
        RentalSupply cachedDongOnlySupply = new RentalSupply(
                "youth", "인천광역시 중구 운남동", "", "26", "2,000,000", "2000000",
                "26A", "20", "", "인천광역시 중구 운남동", ""
        );
        RentalSupply fetchedPreciseSupply = new RentalSupply(
                "youth", "인천광역시 중구 운남동", "168", "26", "2,000,000", "2000000",
                "26A", "20", "available", "인천광역시 중구 운남동 168", "https://map.example"
        );
        when(mapper.findById("LH-001")).thenReturn(Optional.of(notice));
        when(mapper.findDetailByNoticeId("LH-001")).thenReturn(Optional.of(new RentalDetail("", "", "", "", "")));
        when(mapper.findSuppliesByNoticeId("LH-001")).thenReturn(List.of(cachedDongOnlySupply));
        when(lhClient.supplies(notice)).thenReturn(List.of(fetchedPreciseSupply));

        RentalNoticeDetail result = new RentalService(lhClient, mapper).detail("LH-001");

        assertThat(result.supplies()).containsExactly(fetchedPreciseSupply);
    }

    @Test
    void returnsCachedDetailBeforeCallingLhDetailApi() {
        RentalNotice notice = new RentalNotice(
                "LH-001", "Rental notice", "Seoul", "rental", "public", "open",
                "2026.06.18", "2026.06.29", "https://apply.lh.or.kr",
                "03", "06", "10", "063", "api"
        );
        RentalDetail cachedDetail = new RentalDetail(
                "Cached office", "Cached address", "2026.06.20", "2026.06.24", "1600-1004"
        );
        when(mapper.findById("LH-001")).thenReturn(Optional.of(notice));
        when(mapper.findDetailByNoticeId("LH-001")).thenReturn(Optional.of(cachedDetail));
        when(mapper.findSuppliesByNoticeId("LH-001")).thenReturn(List.of());
        when(lhClient.supplies(notice)).thenReturn(List.of());

        RentalNoticeDetail result = new RentalService(lhClient, mapper).detail("LH-001");

        assertThat(result.detail()).isEqualTo(cachedDetail);
        verify(lhClient, never()).detail(notice);
    }
}
