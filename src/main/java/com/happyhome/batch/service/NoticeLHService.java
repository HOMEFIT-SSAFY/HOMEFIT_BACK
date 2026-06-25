package com.happyhome.batch.service;

import com.happyhome.batch.dto.NoticeLHDetail;
import com.happyhome.batch.dto.NoticeLHResult;
import com.happyhome.batch.dto.NoticeLHSupply;
import com.happyhome.batch.dto.OpenApiLog;
import com.happyhome.batch.mapper.NoticeLHBatchMapper;
import com.happyhome.rental.dao.RentalNoticeMapper;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.dto.RentalSupply;
import com.happyhome.openapi.LhOpenApiClient;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeLHService {

    private static final String JOB_NAME = "LH_NOTICE_SYNC";
    private static final String API_NAME = "LH_OPEN_API";
    private static final int PAGE_SIZE = 100;
    private static final int NOTICE_LOOKBACK_DAYS = 90;
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final LhOpenApiClient lhOpenApiClient;
    private final RentalNoticeMapper rentalNoticeMapper;
    private final NoticeLHBatchMapper noticeLHBatchMapper;

    public NoticeLHResult runSync() {
        LocalDateTime startedAt = LocalDateTime.now();

        int fetchedCount = 0;
        int savedCount = 0;
        List<String> errors = new ArrayList<>();

        try {
            if (!lhOpenApiClient.isConfigured()) {
                errors.add("OPENAPI_DATA_SERVICE_KEY is not configured.");
            } else {
                LocalDate today = LocalDate.now();
                String noticeStartDate = today.minusDays(NOTICE_LOOKBACK_DAYS).format(API_DATE_FORMAT);
                rentalNoticeMapper.deleteApiNoticesBefore(today.format(API_DATE_FORMAT));

                int page = 1;
                while (true) {
                    RentalSearchCondition condition = new RentalSearchCondition(
                            "",
                            "",
                            "",
                            page,
                            PAGE_SIZE
                    );

                    List<RentalNotice> notices = lhOpenApiClient.apiNotices(condition, noticeStartDate, "2099.12.31")
                            .stream()
                            .filter(notice -> isRelevantForCalendar(notice, noticeStartDate, today.format(API_DATE_FORMAT)))
                            .toList();
                    if (notices.isEmpty()) {
                        break;
                    }

                    fetchedCount += notices.size();
                    for (RentalNotice notice : notices) {
                        try {
                            rentalNoticeMapper.upsert(notice);
                            savedCount++;

                            RentalDetail detail = lhOpenApiClient.detail(notice);
                            noticeLHBatchMapper.upsertDetail(
                                    NoticeLHDetail.from(notice.noticeId(), detail)
                            );
                            savedCount++;

                            List<RentalSupply> supplies = lhOpenApiClient.supplies(notice);

                            noticeLHBatchMapper.deleteSuppliesByNoticeId(notice.noticeId());

                            for (RentalSupply supply : supplies) {
                                noticeLHBatchMapper.insertSupply(
                                        NoticeLHSupply.from(notice.noticeId(), supply)
                                );
                                savedCount++;
                            }
                        } catch (Exception e) {
                            errors.add(notice.noticeId() + ": " + e.getMessage());
                        }
                    }

                    if (notices.size() < PAGE_SIZE) {
                        break;
                    }
                    page++;
                }
            }

            String status = errors.isEmpty() ? "SUCCESS" : (fetchedCount == 0 ? "FAILED" : "PARTIAL_FAILED");

            saveLogQuietly(
                    status,
                    startedAt,
                    LocalDateTime.now(),
                    fetchedCount,
                    savedCount,
                    errors
            );

            return new NoticeLHResult(status, fetchedCount, savedCount, errors);
        } catch (Exception e) {
            errors.add(e.getMessage());

            saveLogQuietly(
                    "FAILED",
                    startedAt,
                    LocalDateTime.now(),
                    fetchedCount,
                    savedCount,
                    errors
            );

            return new NoticeLHResult("FAILED", fetchedCount, savedCount, errors);
        }
    }

    private void saveLogQuietly(
            String status,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            int fetchedCount,
            int savedCount,
            List<String> errors
    ) {
        try {
            String errorMessage = errors.isEmpty()
                    ? null
                    : String.join("\n", errors);

            noticeLHBatchMapper.insertLog(new OpenApiLog(
                    JOB_NAME,
                    API_NAME,
                    status,
                    startedAt,
                    finishedAt,
                    fetchedCount,
                    savedCount,
                    errorMessage
            ));
        } catch (Exception ignored) {
        }
    }

    private boolean isRelevantForCalendar(RentalNotice notice, String noticeStartDate, String today) {
        return isOnOrAfter(notice.noticeDate(), noticeStartDate)
                || isOnOrAfter(notice.closeDate(), today);
    }

    private boolean isOnOrAfter(String date, String cutoffDate) {
        return date != null && date.compareTo(cutoffDate) >= 0;
    }
}
