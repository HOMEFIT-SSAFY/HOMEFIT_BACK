package com.happyhome.batch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "batch.lh",
        name = "enabled",
        havingValue = "true"
)
public class NoticeLHScheduler {

    private final NoticeLHService noticeLHService;

    @Scheduled(cron = "${batch.lh.cron}")
    public void syncNoticeLH() {
        noticeLHService.runSync();
    }
}