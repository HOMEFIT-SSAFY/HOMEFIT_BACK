package com.happyhome.rental.service;

import com.happyhome.batch.dto.NoticeLHDetail;
import com.happyhome.batch.dto.NoticeLHSupply;
import com.happyhome.batch.mapper.NoticeLHBatchMapper;
import com.happyhome.openapi.KakaoLocalApiClient;
import com.happyhome.openapi.LhOpenApiClient;
import com.happyhome.openapi.OpenApiUri;
import com.happyhome.openapi.SampleData;
import com.happyhome.openapi.dto.GeoCoordinate;
import com.happyhome.rental.dao.RentalNoticeMapper;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.dto.RentalSupply;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RentalService {

    private static final int CALENDAR_ENRICHMENT_SIZE = 50;
    private static final int LH_NOTICE_LOOKBACK_DAYS = 90;
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final LhOpenApiClient lhClient;
    private final RentalNoticeMapper mapper;
    private final NoticeLHBatchMapper noticeLHBatchMapper;
    private final KakaoLocalApiClient kakaoLocalApiClient;

    @Autowired
    public RentalService(
            LhOpenApiClient lhClient,
            RentalNoticeMapper mapper,
            NoticeLHBatchMapper noticeLHBatchMapper,
            KakaoLocalApiClient kakaoLocalApiClient
    ) {
        this.lhClient = lhClient;
        this.mapper = mapper;
        this.noticeLHBatchMapper = noticeLHBatchMapper;
        this.kakaoLocalApiClient = kakaoLocalApiClient;
    }

    public RentalService(LhOpenApiClient lhClient, RentalNoticeMapper mapper, NoticeLHBatchMapper noticeLHBatchMapper) {
        this(lhClient, mapper, noticeLHBatchMapper, null);
    }

    public RentalService(LhOpenApiClient lhClient, RentalNoticeMapper mapper) {
        this(lhClient, mapper, null);
    }

    public List<RentalNotice> notices(RentalSearchCondition condition) {
        refreshLhCacheQuietly(condition);

        List<RentalNotice> cachedNotices = mapper.findByCondition(condition);
        if (!cachedNotices.isEmpty()) {
            return enrichCalendarDatesIfNeeded(condition, cachedNotices);
        }

        List<RentalNotice> notices = lhClient.notices(condition);
        notices.forEach(this::cacheQuietly);
        return enrichCalendarDatesIfNeeded(condition, notices);
    }

    public RentalNoticeDetail detail(String noticeId) {
        RentalNotice notice = mapper.findById(noticeId)
                .orElseGet(() -> SampleData.rentalNotices().stream()
                        .filter(item -> item.noticeId().equals(noticeId))
                        .findFirst()
                        .orElseGet(() -> uncachedNotice(noticeId)));
        RentalDetail detail = detailFor(notice);
        List<RentalSupply> supplies = suppliesFor(notice);
        return new RentalNoticeDetail(notice, detail, suppliesWithCoordinates(supplies));
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
        if (!cachedSupplies.isEmpty() && hasPreciseMapTarget(cachedSupplies)) {
            return cachedSupplies;
        }

        List<RentalSupply> fetchedSupplies = lhClient.supplies(notice);
        if (fetchedSupplies.isEmpty() && !cachedSupplies.isEmpty()) {
            return cachedSupplies;
        }
        cacheSuppliesQuietly(notice.noticeId(), fetchedSupplies);
        return fetchedSupplies;
    }

    private boolean hasPreciseMapTarget(List<RentalSupply> supplies) {
        return supplies.stream().anyMatch(supply ->
                OpenApiUri.hasText(supply.lotNumber())
                        || hasAddressNumber(supply.mapAddress())
                        || hasAddressNumber(supply.address())
        );
    }

    private boolean hasAddressNumber(String address) {
        return OpenApiUri.hasText(address) && address.matches(".*\\d+.*");
    }

    private List<RentalSupply> suppliesWithCoordinates(List<RentalSupply> supplies) {
        if (kakaoLocalApiClient == null || supplies == null || supplies.isEmpty()) {
            return supplies;
        }
        return supplies.stream().map(this::supplyWithCoordinate).toList();
    }

    private RentalSupply supplyWithCoordinate(RentalSupply supply) {
        if (supply.latitude() != null && supply.longitude() != null) {
            return supply;
        }
        return coordinateFor(supply)
                .map(coordinate -> new RentalSupply(
                        supply.usage(),
                        supply.address(),
                        supply.lotNumber(),
                        supply.area(),
                        supply.expectedAmount(),
                        supply.expectedAmountRaw(),
                        supply.houseType(),
                        supply.householdCount(),
                        supply.internetApplyStatus(),
                        supply.mapAddress(),
                        supply.mapUrl(),
                        coordinate.latitude(),
                        coordinate.longitude()
                ))
                .orElse(supply);
    }

    private void refreshLhCacheQuietly(RentalSearchCondition condition) {
        if (!lhClient.isConfigured()) {
            return;
        }
        try {
            String noticeStartDate = LocalDate.now().minusDays(LH_NOTICE_LOOKBACK_DAYS).format(API_DATE_FORMAT);
            List<RentalNotice> notices = lhClient.apiNotices(condition, noticeStartDate, "2099.12.31");
            notices.forEach(this::cacheQuietly);
        } catch (Exception ignored) {
            // Cached data is still usable when the live LH list refresh is unavailable.
        }
    }

    private List<RentalNotice> enrichCalendarDatesIfNeeded(RentalSearchCondition condition, List<RentalNotice> notices) {
        if (condition.size() < CALENDAR_ENRICHMENT_SIZE || notices.isEmpty() || !lhClient.isConfigured()) {
            return notices;
        }
        return notices.stream()
                .map(this::noticeWithApplicationDates)
                .toList();
    }

    private RentalNotice noticeWithApplicationDates(RentalNotice notice) {
        if (OpenApiUri.hasText(notice.applyStartDate()) || OpenApiUri.hasText(notice.applyEndDate())) {
            return notice;
        }
        RentalDetail detail = detailFor(notice);
        return new RentalNotice(
                notice.noticeId(),
                notice.title(),
                notice.regionName(),
                notice.noticeType(),
                notice.detailType(),
                notice.status(),
                notice.noticeDate(),
                notice.closeDate(),
                detail.applyStartDate(),
                detail.applyEndDate(),
                notice.detailUrl(),
                notice.ccrCnntSysDsCd(),
                notice.uppAisTpCd(),
                notice.aisTpCd(),
                notice.splInfTpCd(),
                notice.source()
        );
    }

    private Optional<GeoCoordinate> coordinateFor(RentalSupply supply) {
        for (String query : Arrays.asList(
                combineAddress(supply.address(), supply.lotNumber()),
                supply.mapAddress(),
                supply.address()
        )) {
            if (!OpenApiUri.hasText(query)) {
                continue;
            }
            Optional<GeoCoordinate> coordinate = kakaoLocalApiClient.geocode(query);
            if (coordinate.isPresent()) {
                return coordinate;
            }
        }
        return Optional.empty();
    }

    private String combineAddress(String address, String lotNumber) {
        if (!OpenApiUri.hasText(address)) {
            return OpenApiUri.hasText(lotNumber) ? lotNumber : "";
        }
        if (!OpenApiUri.hasText(lotNumber) || address.contains(lotNumber)) {
            return address;
        }
        return address.trim() + " " + lotNumber.trim();
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

