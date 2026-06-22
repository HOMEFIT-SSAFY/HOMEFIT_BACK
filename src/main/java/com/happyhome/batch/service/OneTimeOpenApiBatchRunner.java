package com.happyhome.batch.service;

import com.happyhome.batch.dto.NoticeLHResult;
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

@Component
@ConditionalOnProperty(prefix = "batch.once", name = "enabled", havingValue = "true")
public class OneTimeOpenApiBatchRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OneTimeOpenApiBatchRunner.class);

    private final NoticeLHService noticeLHService;
    private final LoanProductBatchService loanProductBatchService;
    private final BusStopSyncService busStopSyncService;
    private final ConfigurableApplicationContext context;
    private final String job;

    public OneTimeOpenApiBatchRunner(
            NoticeLHService noticeLHService,
            LoanProductBatchService loanProductBatchService,
            BusStopSyncService busStopSyncService,
            ConfigurableApplicationContext context,
            @Value("${batch.once.job:all}") String job
    ) {
        this.noticeLHService = noticeLHService;
        this.loanProductBatchService = loanProductBatchService;
        this.busStopSyncService = busStopSyncService;
        this.context = context;
        this.job = job;
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
        } else {
            NoticeLHResult lhResult = noticeLHService.runSync();
            NoticeLHResult loanResult = loanProductBatchService.runSync();
            BusStopSyncResult busResult = busStopSyncService.syncAll(null);
            log.info("One-time Open API batch finished. LH={}, Loan={}, Bus={}",
                    summarize(lhResult), summarize(loanResult), summarize(busResult));
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

    private String summarize(BusStopSyncResult result) {
        return "status=" + result.status()
                + ", cities=" + result.cityCount()
                + ", fetched=" + result.fetchedCount()
                + ", saved=" + result.savedCount()
                + ", errors=" + result.errors().size();
    }
}
