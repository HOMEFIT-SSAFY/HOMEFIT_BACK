package com.happyhome.rental.email.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "rental.notice.email.enabled", havingValue = "true", matchIfMissing = true)
public class RentalNoticeEmailScheduler {

    private final RentalNoticeEmailService rentalNoticeEmailService;

    public RentalNoticeEmailScheduler(RentalNoticeEmailService rentalNoticeEmailService) {
        this.rentalNoticeEmailService = rentalNoticeEmailService;
    }

    @Scheduled(cron = "${rental.notice.email.cron}")
    public void sendFavoriteNoticeEmails() {
        rentalNoticeEmailService.sendAllFavoriteNoticeEmails();
    }
}
