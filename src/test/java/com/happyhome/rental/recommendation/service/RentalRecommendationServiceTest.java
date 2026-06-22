package com.happyhome.rental.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.happyhome.member.dto.FinancialProfile;
import com.happyhome.member.service.FinancialProfileService;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.dto.RentalSupply;
import com.happyhome.rental.recommendation.dto.RentalRecommendation;
import com.happyhome.rental.service.RentalService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RentalRecommendationServiceTest {

    private final RentalService rentalService = Mockito.mock(RentalService.class);
    private final FinancialProfileService financialProfileService = Mockito.mock(FinancialProfileService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-22T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final RentalRecommendationService service = new RentalRecommendationService(
            rentalService,
            financialProfileService,
            clock
    );

    @Test
    void recommendsAffordableActiveNoticesBeforeUnaffordableClosedNotices() {
        when(financialProfileService.findByUserId("ssafy")).thenReturn(Optional.of(profile("100000000")));
        RentalNotice affordable = notice("LH-001", "서울 행복주택", "공고중");
        RentalNotice expensive = notice("LH-002", "부산 국민임대", "마감");
        when(rentalService.notices(any(RentalSearchCondition.class))).thenReturn(List.of(expensive, affordable));
        when(rentalService.detail("LH-001")).thenReturn(detail(affordable, "2026.06.20", "2026.06.24", "보증금 5,000만원"));
        when(rentalService.detail("LH-002")).thenReturn(detail(expensive, "2026.06.01", "2026.06.10", "보증금 3억원"));

        List<RentalRecommendation> recommendations = service.recommend("ssafy", 10);

        assertThat(recommendations).extracting(item -> item.notice().noticeId())
                .containsExactly("LH-001", "LH-002");
        assertThat(recommendations.get(0).score()).isGreaterThan(recommendations.get(1).score());
        assertThat(recommendations.get(0).reasons()).contains("자산 범위 안의 예상 보증금입니다.");
    }

    private FinancialProfile profile(String assets) {
        return new FinancialProfile(
                "ssafy",
                new BigDecimal(assets),
                new BigDecimal("60000000"),
                new BigDecimal("1500000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    private RentalNotice notice(String noticeId, String title, String status) {
        return new RentalNotice(noticeId, title, "서울", "임대", "행복주택", status,
                "2026.06.01", "2026.06.30", "https://apply.lh.or.kr",
                "01", "01", "10", "010", "api");
    }

    private RentalNoticeDetail detail(RentalNotice notice, String startDate, String endDate, String amount) {
        return new RentalNoticeDetail(
                notice,
                new RentalDetail("서울", "강남구", startDate, endDate, "1600-1004"),
                List.of(new RentalSupply("주택", "서울 강남구", "", "59", amount, amount, "59A", "10", "가능", "서울 강남구", ""))
        );
    }
}
