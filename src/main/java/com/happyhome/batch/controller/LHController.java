package com.happyhome.batch.controller;

import com.happyhome.batch.dto.NoticeLHResult;
import com.happyhome.batch.dto.OpenApiBatchResult;
import com.happyhome.batch.service.LoanProductBatchService;
import com.happyhome.batch.service.NoticeLHService;
import com.happyhome.transport.dto.BusStopSyncResult;
import com.happyhome.transport.service.BusStopSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/batch")
@Tag(name = "Admin Batch", description = "Open API batch manual execution")
public class LHController {

    private final NoticeLHService noticeLHService;
    private final LoanProductBatchService loanProductBatchService;
    private final BusStopSyncService busStopSyncService;

    public LHController(
            NoticeLHService noticeLHService,
            LoanProductBatchService loanProductBatchService,
            BusStopSyncService busStopSyncService
    ) {
        this.noticeLHService = noticeLHService;
        this.loanProductBatchService = loanProductBatchService;
        this.busStopSyncService = busStopSyncService;
    }

    @Operation(summary = "Run LH notice sync batch manually")
    @PostMapping("/lh-notices")
    public NoticeLHResult syncNoticeLH() {
        return noticeLHService.runSync();
    }

    @Operation(summary = "Run FinLife loan product sync batch manually")
    @PostMapping("/loan-products")
    public NoticeLHResult syncLoanProducts() {
        return loanProductBatchService.runSync();
    }

    @Operation(summary = "Run all Open API sync batches manually")
    @PostMapping("/all-openapi")
    public OpenApiBatchResult syncAllOpenApi() {
        NoticeLHResult lhResult = noticeLHService.runSync();
        NoticeLHResult loanResult = loanProductBatchService.runSync();
        BusStopSyncResult busResult = busStopSyncService.syncAll(null);
        return new OpenApiBatchResult(lhResult, loanResult, busResult);
    }
}
