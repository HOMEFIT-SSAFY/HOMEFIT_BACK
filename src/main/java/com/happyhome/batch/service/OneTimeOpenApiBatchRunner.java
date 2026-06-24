package com.happyhome.batch.service;

import com.happyhome.batch.dto.NoticeLHResult;
import com.happyhome.property.service.PropertyDealSyncService;
import com.happyhome.transport.dto.BusStopSyncResult;
import com.happyhome.transport.service.BusStopSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "batch.once", name = "enabled", havingValue = "true")
public class OneTimeOpenApiBatchRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OneTimeOpenApiBatchRunner.class);

    private final NoticeLHService noticeLHService;
    private final LoanProductBatchService loanProductBatchService;
    private final BusStopSyncService busStopSyncService;
    private final PropertyDealSyncService propertyDealSyncService;
    private final ConfigurableApplicationContext context;
    private final String job;
    private final int propertyMonths;
    private final String propertyLawdCodes;

    public OneTimeOpenApiBatchRunner(
            NoticeLHService noticeLHService,
            LoanProductBatchService loanProductBatchService,
            BusStopSyncService busStopSyncService,
            PropertyDealSyncService propertyDealSyncService,
            ConfigurableApplicationContext context,
            @Value("${batch.once.job:all}") String job,
            @Value("${batch.property.months:3}") int propertyMonths,
            @Value("${batch.property.lawd-codes:}") String propertyLawdCodes
    ) {
        this.noticeLHService = noticeLHService;
        this.loanProductBatchService = loanProductBatchService;
        this.busStopSyncService = busStopSyncService;
        this.propertyDealSyncService = propertyDealSyncService;
        this.context = context;
        this.job = job;
        this.propertyMonths = propertyMonths;
        this.propertyLawdCodes = propertyLawdCodes;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("One-time Open API batch started. job={}", job);
        if ("lh".equalsIgnoreCase(job)) {
            NoticeLHResult lhResult = noticeLHService.runSync();
            log.info("One-time Open API batch finished. LH={}", summarize(lhResult));
        } else if ("loan".equalsIgnoreCase(job)) {
            NoticeLHResult loanResult = loanProductBatchService.runSync();
            log.info("One-time Open API batch finished. Loan={}", summarize(loanResult));
        } else if ("bus".equalsIgnoreCase(job)) {
            BusStopSyncResult busResult = busStopSyncService.syncAll(null);
            log.info("One-time Open API batch finished. Bus={}", summarize(busResult));
        } else if ("property".equalsIgnoreCase(job)) {
            NoticeLHResult propertyResult = propertyDealSyncService.syncRecent(propertyLawdCodes(), propertyMonths);
            log.info("One-time Open API batch finished. Property={}", summarize(propertyResult));
        } else {
            NoticeLHResult lhResult = noticeLHService.runSync();
            NoticeLHResult loanResult = loanProductBatchService.runSync();
            NoticeLHResult propertyResult = propertyDealSyncService.syncRecent(propertyLawdCodes(), propertyMonths);
            BusStopSyncResult busResult = busStopSyncService.syncAll(null);
            log.info("One-time Open API batch finished. LH={}, Loan={}, Property={}, Bus={}",
                    summarize(lhResult), summarize(loanResult), summarize(propertyResult), summarize(busResult));
        }
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }

    private String summarize(NoticeLHResult result) {
        return "status=" + result.status()
                + ", fetched=" + result.fetchedCount()
                + ", saved=" + result.savedCount()
                + ", errors=" + result.errors().size();
    }

    private List<String> propertyLawdCodes() {
        if (propertyLawdCodes == null || propertyLawdCodes.isBlank()) {
            return List.of();
        }
        return Arrays.stream(propertyLawdCodes.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private String summarize(BusStopSyncResult result) {
        return "status=" + result.status()
                + ", cities=" + result.cityCount()
                + ", fetched=" + result.fetchedCount()
                + ", saved=" + result.savedCount()
                + ", errors=" + result.errors().size();
    }
}
