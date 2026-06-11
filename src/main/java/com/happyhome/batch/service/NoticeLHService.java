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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeLHService {

    private static final String JOB_NAME = "LH_NOTICE_SYNC";
    private static final String API_NAME = "LH_OPEN_API";

    private final LhOpenApiClient lhOpenApiClient;
    private final RentalNoticeMapper rentalNoticeMapper;
    private final NoticeLHBatchMapper noticeLHBatchMapper;

    public NoticeLHResult runSync() {
        LocalDateTime startedAt = LocalDateTime.now();

        int fetchedCount = 0;
        int savedCount = 0;
        List<String> errors = new ArrayList<>();

        try {
            RentalSearchCondition condition = new RentalSearchCondition(
                    "",
                    "",
                    "",
                    1,
                    100
            );

            List<RentalNotice> notices = lhOpenApiClient.notices(condition);
            fetchedCount = notices.size();

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

            String status = errors.isEmpty() ? "SUCCESS" : "PARTIAL_FAILED";

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
}