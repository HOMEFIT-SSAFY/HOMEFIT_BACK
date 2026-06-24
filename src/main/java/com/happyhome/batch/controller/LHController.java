package com.happyhome.batch.controller;

import com.happyhome.batch.dto.NoticeLHResult;
import com.happyhome.batch.dto.OpenApiBatchResult;
import com.happyhome.batch.service.LoanProductBatchService;
import com.happyhome.batch.service.NoticeLHService;
import com.happyhome.property.service.PropertyDealSyncService;
import com.happyhome.transport.dto.BusStopSyncResult;
import com.happyhome.transport.service.BusStopSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/batch")
@Tag(name = "Admin Batch", description = "Open API batch manual execution")
public class LHController {

    private final NoticeLHService noticeLHService;
    private final LoanProductBatchService loanProductBatchService;
    private final BusStopSyncService busStopSyncService;
    private final PropertyDealSyncService propertyDealSyncService;

    public LHController(
            NoticeLHService noticeLHService,
            LoanProductBatchService loanProductBatchService,
            BusStopSyncService busStopSyncService,
            PropertyDealSyncService propertyDealSyncService
    ) {
        this.noticeLHService = noticeLHService;
        this.loanProductBatchService = loanProductBatchService;
        this.busStopSyncService = busStopSyncService;
        this.propertyDealSyncService = propertyDealSyncService;
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

    @Operation(summary = "Run MOLIT officetel/oneroom deal sync batch manually")
    @PostMapping("/property-deals")
    public NoticeLHResult syncPropertyDeals(
            @RequestParam(defaultValue = "3") int months,
            @RequestParam(required = false) java.util.List<String> lawdCodes
    ) {
        return propertyDealSyncService.syncRecent(lawdCodes, months);
    }

    @Operation(summary = "Run all Open API sync batches manually")
    @PostMapping("/all-openapi")
    public OpenApiBatchResult syncAllOpenApi() {
        NoticeLHResult lhResult = noticeLHService.runSync();
        NoticeLHResult loanResult = loanProductBatchService.runSync();
        BusStopSyncResult busResult = busStopSyncService.syncAll(null);
        NoticeLHResult propertyDealResult = propertyDealSyncService.syncRecent(3);
        return new OpenApiBatchResult(lhResult, loanResult, busResult, propertyDealResult);
    }
}
