package com.happyhome.rental.email.service;

import com.happyhome.member.dto.MemberDto;
import com.happyhome.member.service.MemberService;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.email.dao.RentalNoticeEmailLogDao;
import com.happyhome.rental.email.dto.RentalNoticeEmailEventType;
import com.happyhome.rental.favorite.service.FavoriteRentalNoticeService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RentalNoticeEmailService {

    private final FavoriteRentalNoticeService favoriteService;
    private final MemberService memberService;
    private final RentalNoticeEmailLogDao emailLogDao;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final Clock clock;
    private final int closingSoonDays;

    @Autowired
    public RentalNoticeEmailService(
            FavoriteRentalNoticeService favoriteService,
            MemberService memberService,
            RentalNoticeEmailLogDao emailLogDao,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${rental.notice.email.closing-soon-days:3}") int closingSoonDays
    ) {
        this(favoriteService, memberService, emailLogDao, mailSenderProvider, Clock.systemDefaultZone(), closingSoonDays);
    }

    public RentalNoticeEmailService(
            FavoriteRentalNoticeService favoriteService,
            MemberService memberService,
            RentalNoticeEmailLogDao emailLogDao,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            Clock clock
    ) {
        this(favoriteService, memberService, emailLogDao, mailSenderProvider, clock, 3);
    }

    public RentalNoticeEmailService(
            FavoriteRentalNoticeService favoriteService,
            MemberService memberService,
            RentalNoticeEmailLogDao emailLogDao,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            Clock clock,
            int closingSoonDays
    ) {
        this.favoriteService = favoriteService;
        this.memberService = memberService;
        this.emailLogDao = emailLogDao;
        this.mailSenderProvider = mailSenderProvider;
        this.clock = clock;
        this.closingSoonDays = closingSoonDays;
    }

    public EmailRunResult sendFavoriteNoticeEmails(String userId) {
        Optional<MemberDto> member = memberService.findByUserId(userId);
        if (member.isEmpty() || !StringUtils.hasText(member.get().getEmail())) {
            return new EmailRunResult(0, 0, 1);
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        List<RentalNoticeDetail> favorites = favoriteService.findFavorites(userId, 100);
        int sent = 0;
        int skipped = 0;
        for (RentalNoticeDetail favorite : favorites) {
            Optional<RentalNoticeEmailEventType> eventType = eventTypeFor(favorite);
            if (eventType.isEmpty()) {
                skipped++;
                continue;
            }
            String eventName = eventType.get().name();
            if (emailLogDao.exists(userId, favorite.notice().noticeId(), eventName)) {
                skipped++;
                continue;
            }
            if (mailSender == null) {
                skipped++;
                continue;
            }
            String subject = "[HappyHome] " + eventType.get().label() + ": " + favorite.notice().title();
            mailSender.send(message(member.get().getEmail(), subject, body(favorite, eventType.get())));
            emailLogDao.save(userId, favorite.notice().noticeId(), eventName, member.get().getEmail(), subject);
            sent++;
        }
        return new EmailRunResult(sent, skipped, 0);
    }

    public List<EmailRunResult> sendAllFavoriteNoticeEmails() {
        return favoriteService.findUserIdsWithFavorites().stream()
                .map(this::sendFavoriteNoticeEmails)
                .toList();
    }

    private Optional<RentalNoticeEmailEventType> eventTypeFor(RentalNoticeDetail detail) {
        Optional<LocalDate> startDate = RentalNoticeDateParser.parse(detail.detail().applyStartDate());
        Optional<LocalDate> endDate = RentalNoticeDateParser.parse(detail.detail().applyEndDate());
        if (startDate.isEmpty() || endDate.isEmpty()) {
            return Optional.empty();
        }
        LocalDate today = LocalDate.now(clock);
        if (today.isEqual(startDate.get())) {
            return Optional.of(RentalNoticeEmailEventType.APPLY_OPEN);
        }
        long daysUntilClose = ChronoUnit.DAYS.between(today, endDate.get());
        if (daysUntilClose >= 0 && daysUntilClose <= closingSoonDays) {
            return Optional.of(RentalNoticeEmailEventType.CLOSING_SOON);
        }
        if (today.isAfter(startDate.get()) && today.isBefore(endDate.get())) {
            return Optional.of(RentalNoticeEmailEventType.APPLY_ACTIVE);
        }
        return Optional.empty();
    }

    private SimpleMailMessage message(String recipient, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        return message;
    }

    private String body(RentalNoticeDetail detail, RentalNoticeEmailEventType eventType) {
        return """
                관심 등록한 LH 공고 알림입니다.

                알림 유형: %s
                공고명: %s
                지역: %s
                접수 기간: %s ~ %s
                문의: %s
                상세 링크: %s
                """.formatted(
                eventType.label(),
                detail.notice().title(),
                detail.notice().regionName(),
                detail.detail().applyStartDate(),
                detail.detail().applyEndDate(),
                detail.detail().contact(),
                detail.notice().detailUrl()
        );
    }

    public record EmailRunResult(int sentCount, int skippedCount, int missingMemberCount) {
    }
}
