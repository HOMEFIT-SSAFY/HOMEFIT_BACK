package com.happyhome.batch.service;

import com.happyhome.batch.dto.NoticeLHResult;
import com.happyhome.batch.dto.OpenApiLog;
import com.happyhome.batch.mapper.LoanProductBatchMapper;
import com.happyhome.batch.mapper.NoticeLHBatchMapper;
import com.happyhome.loan.dto.LoanProduct;
import com.happyhome.loan.dto.LoanRateOption;
import com.happyhome.loan.dto.LoanType;
import com.happyhome.openapi.FinLifeLoanApiClient;
import com.happyhome.openapi.FinLifeLoanApiClient.LoanProductPage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanProductBatchService {

    private static final String JOB_NAME = "LOAN_PRODUCT_SYNC";
    private static final String API_NAME = "FINLIFE_OPEN_API";

    private final FinLifeLoanApiClient finLifeLoanApiClient;
    private final LoanProductBatchMapper loanProductBatchMapper;
    private final NoticeLHBatchMapper noticeLHBatchMapper;

    public LoanProductBatchService(
            FinLifeLoanApiClient finLifeLoanApiClient,
            LoanProductBatchMapper loanProductBatchMapper,
            NoticeLHBatchMapper noticeLHBatchMapper
    ) {
        this.finLifeLoanApiClient = finLifeLoanApiClient;
        this.loanProductBatchMapper = loanProductBatchMapper;
        this.noticeLHBatchMapper = noticeLHBatchMapper;
    }

    @Transactional
    public NoticeLHResult runSync() {
        LocalDateTime startedAt = LocalDateTime.now();
        int fetchedCount = 0;
        int savedCount = 0;
        List<String> errors = new ArrayList<>();

        try {
            if (!finLifeLoanApiClient.isConfigured()) {
                errors.add("OPENAPI_FINLIFE_AUTH is not configured.");
            } else {
                for (LoanType type : LoanType.values()) {
                    int page = 1;
                    int maxPage = 1;
                    do {
                        LoanProductPage productPage = finLifeLoanApiClient.productPage(type, page);
                        maxPage = Math.max(productPage.maxPageNo(), page);
                        List<LoanProduct> products = productPage.products();
                        fetchedCount += products.size();

                        for (LoanProduct product : products) {
                            try {
                                loanProductBatchMapper.upsertProduct(product);
                                savedCount++;

                                loanProductBatchMapper.deleteOptionsByProductCode(product.productCode());
                                for (LoanRateOption option : product.options()) {
                                    loanProductBatchMapper.insertOption(option);
                                    savedCount++;
                                }
                            } catch (Exception e) {
                                errors.add(type + " " + product.productCode() + ": " + e.getMessage());
                            }
                        }
                        page++;
                    } while (page <= maxPage);
                }
            }

            String status = errors.isEmpty() ? "SUCCESS" : (fetchedCount == 0 ? "FAILED" : "PARTIAL_FAILED");
            saveLogQuietly(status, startedAt, LocalDateTime.now(), fetchedCount, savedCount, errors);
            return new NoticeLHResult(status, fetchedCount, savedCount, errors);
        } catch (Exception e) {
            errors.add(e.getMessage());
            saveLogQuietly("FAILED", startedAt, LocalDateTime.now(), fetchedCount, savedCount, errors);
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
            String errorMessage = errors.isEmpty() ? null : String.join("\n", errors);
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
