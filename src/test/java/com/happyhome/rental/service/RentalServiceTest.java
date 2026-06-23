package com.happyhome.rental.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.happyhome.openapi.LhOpenApiClient;
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
