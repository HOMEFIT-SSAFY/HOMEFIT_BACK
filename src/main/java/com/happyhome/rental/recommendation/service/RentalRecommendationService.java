package com.happyhome.rental.recommendation.service;

import com.happyhome.member.dto.FinancialProfile;
import com.happyhome.member.service.FinancialProfileService;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.dto.RentalSupply;
import com.happyhome.rental.email.service.RentalNoticeDateParser;
import com.happyhome.rental.recommendation.dto.RentalRecommendation;
import com.happyhome.rental.service.RentalService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RentalRecommendationService {

    private static final Pattern KOREAN_AMOUNT_PATTERN = Pattern.compile("([0-9,]+(?:\\.[0-9]+)?)(\\s*)(억|만원|원)?");

    private final RentalService rentalService;
    private final FinancialProfileService financialProfileService;
    private final Clock clock;

    @Autowired
    public RentalRecommendationService(
            RentalService rentalService,
            FinancialProfileService financialProfileService
    ) {
        this(rentalService, financialProfileService, Clock.systemDefaultZone());
    }

    public RentalRecommendationService(
            RentalService rentalService,
            FinancialProfileService financialProfileService,
            Clock clock
    ) {
        this.rentalService = rentalService;
        this.financialProfileService = financialProfileService;
        this.clock = clock;
    }

    public List<RentalRecommendation> recommend(String userId, int limit) {
        FinancialProfile profile = financialProfileService.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Financial profile is required for LH recommendations."));
        List<RentalNotice> notices = rentalService.notices(new RentalSearchCondition("", "", "", 1, Math.max(limit * 3, 30)));
        return notices.stream()
                .map(notice -> score(notice, profile))
                .sorted(Comparator.comparingInt(RentalRecommendation::score).reversed())
                .limit(limit)
                .toList();
    }

    private RentalRecommendation score(RentalNotice notice, FinancialProfile profile) {
        RentalNoticeDetail detail = rentalService.detail(notice.noticeId());
        List<String> reasons = new ArrayList<>();
        int score = 20;

        String status = lower(notice.status());
        if (status.contains("공고중") || status.contains("접수") || status.contains("open")) {
            score += 30;
            reasons.add("현재 신청 가능한 공고입니다.");
        } else if (status.contains("예정")) {
            score += 20;
            reasons.add("곧 신청이 시작될 공고입니다.");
        } else if (status.contains("마감") || status.contains("closed")) {
            score -= 30;
            reasons.add("이미 마감된 공고일 수 있습니다.");
        }

        LocalDate today = LocalDate.now(clock);
        Optional<LocalDate> startDate = RentalNoticeDateParser.parse(detail.detail().applyStartDate());
        Optional<LocalDate> endDate = RentalNoticeDateParser.parse(detail.detail().applyEndDate());
        if (startDate.isPresent() && endDate.isPresent()) {
            if ((today.isEqual(startDate.get()) || today.isAfter(startDate.get()))
                    && (today.isEqual(endDate.get()) || today.isBefore(endDate.get()))) {
                score += 25;
                reasons.add("접수 기간 안에 있습니다.");
            } else if (today.isBefore(startDate.get())) {
                score += 10;
                reasons.add("접수 시작 전이라 준비 시간이 있습니다.");
            } else {
                score -= 35;
                reasons.add("접수 기간이 지난 공고입니다.");
            }
        }

        Optional<BigDecimal> expectedAmount = maxExpectedAmount(detail.supplies());
        if (expectedAmount.isPresent()) {
            BigDecimal affordableLimit = profile.availableAssets().multiply(new BigDecimal("1.10"));
            if (expectedAmount.get().compareTo(affordableLimit) <= 0) {
                score += 30;
                reasons.add("자산 범위 안의 예상 보증금입니다.");
            } else {
                score -= 25;
                reasons.add("예상 보증금이 현재 자산보다 높을 수 있습니다.");
            }
        } else {
            score += 5;
            reasons.add("공급 금액 정보 확인이 필요합니다.");
        }

        if (containsAny(notice.title(), "청년", "신혼", "행복주택")) {
            score += 8;
            reasons.add("실수요자 선호도가 높은 유형입니다.");
        }

        return new RentalRecommendation(notice, Math.max(score, 0), List.copyOf(reasons), detail.supplies());
    }

    private Optional<BigDecimal> maxExpectedAmount(List<RentalSupply> supplies) {
        return supplies.stream()
                .map(supply -> parseAmount(supply.expectedAmountRaw() + " " + supply.expectedAmount()))
                .flatMap(Optional::stream)
                .max(Comparator.naturalOrder());
    }

    private Optional<BigDecimal> parseAmount(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = KOREAN_AMOUNT_PATTERN.matcher(text);
        BigDecimal max = null;
        while (matcher.find()) {
            String rawNumber = matcher.group(1);
            if (rawNumber == null || rawNumber.isBlank()) {
                continue;
            }
            BigDecimal value = new BigDecimal(rawNumber.replace(",", ""));
            String unit = matcher.group(3);
            if ("억".equals(unit)) {
                value = value.multiply(new BigDecimal("100000000"));
            } else if ("만원".equals(unit)) {
                value = value.multiply(new BigDecimal("10000"));
            }
            if (max == null || value.compareTo(max) > 0) {
                max = value;
            }
        }
        return Optional.ofNullable(max);
    }

    private boolean containsAny(String text, String... needles) {
        String haystack = lower(text);
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
