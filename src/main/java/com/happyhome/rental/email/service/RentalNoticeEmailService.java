package com.happyhome.rental.email.service;

import com.happyhome.member.dto.MemberDto;
import com.happyhome.member.service.MemberService;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.email.dao.RentalNoticeEmailLogDao;
import com.happyhome.rental.email.dto.RentalNoticeEmailEventType;
import com.happyhome.rental.favorite.service.FavoriteRentalNoticeService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

@Service
public class RentalNoticeEmailService {

    private final FavoriteRentalNoticeService favoriteService;
    private final MemberService memberService;
    private final RentalNoticeEmailLogDao emailLogDao;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final Clock clock;
    private final int closingSoonDays;
    private final String frontendOrigin;

    @Autowired
    public RentalNoticeEmailService(
            FavoriteRentalNoticeService favoriteService,
            MemberService memberService,
            RentalNoticeEmailLogDao emailLogDao,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${rental.notice.email.closing-soon-days:3}") int closingSoonDays,
            @Value("${happyhome.frontend.origin:http://localhost:8080}") String frontendOrigin
    ) {
        this(
                favoriteService,
                memberService,
                emailLogDao,
                mailSenderProvider,
                Clock.systemDefaultZone(),
                closingSoonDays,
                frontendOrigin
        );
    }

    public RentalNoticeEmailService(
            FavoriteRentalNoticeService favoriteService,
            MemberService memberService,
            RentalNoticeEmailLogDao emailLogDao,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            Clock clock
    ) {
        this(favoriteService, memberService, emailLogDao, mailSenderProvider, clock, 3, "http://localhost:8080");
    }

    public RentalNoticeEmailService(
            FavoriteRentalNoticeService favoriteService,
            MemberService memberService,
            RentalNoticeEmailLogDao emailLogDao,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            Clock clock,
            int closingSoonDays
    ) {
        this(favoriteService, memberService, emailLogDao, mailSenderProvider, clock, closingSoonDays, "http://localhost:8080");
    }

    public RentalNoticeEmailService(
            FavoriteRentalNoticeService favoriteService,
            MemberService memberService,
            RentalNoticeEmailLogDao emailLogDao,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            Clock clock,
            int closingSoonDays,
            String frontendOrigin
    ) {
        this.favoriteService = favoriteService;
        this.memberService = memberService;
        this.emailLogDao = emailLogDao;
        this.mailSenderProvider = mailSenderProvider;
        this.clock = clock;
        this.closingSoonDays = closingSoonDays;
        this.frontendOrigin = frontendOrigin;
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
            String subject = "[HomeFit] " + eventType.get().label() + ": " + favorite.notice().title();
            mailSender.send(message(mailSender, member.get().getEmail(), subject, favorite, eventType.get()));
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
        Optional<LocalDate> startDate = RentalNoticeDateParser.parse(detail.detail().applyStartDate())
                .or(() -> RentalNoticeDateParser.parse(detail.notice().noticeDate()));
        Optional<LocalDate> endDate = RentalNoticeDateParser.parse(detail.detail().applyEndDate())
                .or(() -> RentalNoticeDateParser.parse(detail.notice().closeDate()));
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

    private MimeMessage message(
            JavaMailSender mailSender,
            String recipient,
            String subject,
            RentalNoticeDetail detail,
            RentalNoticeEmailEventType eventType
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(plainBody(detail, eventType), htmlBody(detail, eventType));
            return message;
        } catch (MessagingException exception) {
            throw new IllegalStateException("Failed to create rental notice email.", exception);
        }
    }

    private String plainBody(RentalNoticeDetail detail, RentalNoticeEmailEventType eventType) {
        return """
                HomeFit 관심 공고 알림입니다.

                ● 알림 유형: %s
                ● 공고명: %s
                ● 지역: %s
                ● 접수 기간: %s ~ %s
                ● 문의: %s

                HomeFit 홈페이지: %s
                LH 상세 링크: %s
                """.formatted(
                eventType.label(),
                detail.notice().title(),
                detail.notice().regionName(),
                displayStartDate(detail),
                displayEndDate(detail),
                displayText(detail.detail().contact()),
                homeUrl(),
                displayText(detail.notice().detailUrl())
        );
    }

    private String htmlBody(RentalNoticeDetail detail, RentalNoticeEmailEventType eventType) {
        String homeUrl = escape(homeUrl());
        String detailUrl = escape(detail.notice().detailUrl());
        return """
                <!doctype html>
                <html lang="ko">
                <body style="margin:0;background:#f5f7fb;padding:32px 0;font-family:Arial,'Apple SD Gothic Neo','Malgun Gothic',sans-serif;color:#111827;">
                  <div style="width:100%%;max-width:640px;margin:0 auto;background:#ffffff;border-radius:18px;padding:42px 44px;box-sizing:border-box;">
                    <div style="font-weight:900;font-size:22px;color:#2563eb;margin-bottom:36px;">HomeFit</div>
                    <h1 style="margin:0 0 20px;font-size:28px;line-height:1.35;font-weight:900;letter-spacing:-0.02em;color:#111827;">관심 공고 접수 알림드립니다.</h1>
                    <p style="margin:0 0 28px;font-size:15px;line-height:1.8;color:#374151;">
                      안녕하세요.<br>
                      HomeFit에서 관심 등록한 LH 공고의 접수 상태를 안내드립니다.
                    </p>
                    <div style="margin:0 0 30px;padding:22px 24px;background:#f8fafc;border:1px solid #e5e7eb;border-radius:14px;">
                      %s
                      %s
                      %s
                      %s
                      %s
                    </div>
                    <p style="margin:0 0 24px;font-size:14px;line-height:1.8;color:#4b5563;">
                      공고 상세 정보와 관심 목록은 HomeFit 홈페이지에서 다시 확인하실 수 있습니다.
                    </p>
                    <a href="%s" style="display:inline-block;background:#315bff;color:#ffffff;text-decoration:none;font-size:15px;font-weight:900;padding:14px 24px;border-radius:18px;">HomeFit 홈페이지 바로가기</a>
                    <p style="margin:28px 0 0;font-size:13px;line-height:1.7;color:#6b7280;">
                      LH 상세 링크: <a href="%s" style="color:#315bff;text-decoration:underline;">공고 원문 보기</a>
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(
                bullet("알림 유형", eventType.label()),
                bullet("공고명", detail.notice().title()),
                bullet("지역", detail.notice().regionName()),
                bullet("접수 기간", displayStartDate(detail) + " ~ " + displayEndDate(detail)),
                bullet("문의", displayText(detail.detail().contact())),
                homeUrl,
                detailUrl
        );
    }

    private String bullet(String label, String value) {
        return """
                <p style="margin:0 0 12px;font-size:15px;line-height:1.7;color:#111827;"><strong>● %s</strong>: %s</p>
                """.formatted(escape(label), escape(value));
    }

    private String displayStartDate(RentalNoticeDetail detail) {
        return displayText(StringUtils.hasText(detail.detail().applyStartDate())
                ? detail.detail().applyStartDate()
                : detail.notice().noticeDate());
    }

    private String displayEndDate(RentalNoticeDetail detail) {
        return displayText(StringUtils.hasText(detail.detail().applyEndDate())
                ? detail.detail().applyEndDate()
                : detail.notice().closeDate());
    }

    private String displayText(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private String homeUrl() {
        String origin = frontendOrigin == null || frontendOrigin.isBlank()
                ? "http://localhost:8080"
                : frontendOrigin.trim();
        return origin.replaceAll("/+$", "") + "/home";
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(displayText(value));
    }

    public record EmailRunResult(int sentCount, int skippedCount, int missingMemberCount) {
    }
}
