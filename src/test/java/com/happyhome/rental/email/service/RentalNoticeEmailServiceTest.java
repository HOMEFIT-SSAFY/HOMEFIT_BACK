package com.happyhome.rental.email.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.happyhome.member.dto.MemberDto;
import com.happyhome.member.service.MemberService;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.email.dao.RentalNoticeEmailLogDao;
import com.happyhome.rental.favorite.service.FavoriteRentalNoticeService;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;

class RentalNoticeEmailServiceTest {

    private final FavoriteRentalNoticeService favoriteService = Mockito.mock(FavoriteRentalNoticeService.class);
    private final MemberService memberService = Mockito.mock(MemberService.class);
    private final RentalNoticeEmailLogDao emailLogDao = Mockito.mock(RentalNoticeEmailLogDao.class);
    private final ObjectProvider<JavaMailSender> mailSenderProvider = Mockito.mock(ObjectProvider.class);
    private final JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-22T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final RentalNoticeEmailService service = new RentalNoticeEmailService(
            favoriteService,
            memberService,
            emailLogDao,
            mailSenderProvider,
            clock,
            3,
            "https://homefit.example.com"
    );

    @Test
    void sendsClosingSoonEmailOncePerNoticeAndUser() {
        MemberDto member = member("ssafy", "ssafy@example.com");
        when(memberService.findByUserId("ssafy")).thenReturn(Optional.of(member));
        when(favoriteService.findFavorites("ssafy", 100))
                .thenReturn(List.of(detail("LH-001", "LH notice", "2026.06.01", "2026.06.30", "2026.06.20", "2026.06.24")));
        when(emailLogDao.exists("ssafy", "LH-001", "CLOSING_SOON")).thenReturn(false);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        RentalNoticeEmailService.EmailRunResult result = service.sendFavoriteNoticeEmails("ssafy");

        assertThat(result.sentCount()).isEqualTo(1);
        Mockito.verify(emailLogDao).save(
                Mockito.eq("ssafy"),
                Mockito.eq("LH-001"),
                Mockito.eq("CLOSING_SOON"),
                Mockito.eq("ssafy@example.com"),
                Mockito.argThat(subject -> subject.endsWith(": LH notice"))
        );
    }

    @Test
    void skipsEmailWhenSameEventWasAlreadySent() {
        MemberDto member = member("ssafy", "ssafy@example.com");
        when(memberService.findByUserId("ssafy")).thenReturn(Optional.of(member));
        when(favoriteService.findFavorites("ssafy", 100))
                .thenReturn(List.of(detail("LH-001", "LH notice", "2026.06.01", "2026.06.30", "2026.06.20", "2026.06.24")));
        when(emailLogDao.exists("ssafy", "LH-001", "CLOSING_SOON")).thenReturn(true);

        RentalNoticeEmailService.EmailRunResult result = service.sendFavoriteNoticeEmails("ssafy");

        assertThat(result.sentCount()).isZero();
        Mockito.verify(emailLogDao, Mockito.never()).save(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void usesNoticeDatesWhenDetailApplicationDatesAreMissing() {
        MemberDto member = member("ssafy", "ssafy@example.com");
        when(memberService.findByUserId("ssafy")).thenReturn(Optional.of(member));
        when(favoriteService.findFavorites("ssafy", 100))
                .thenReturn(List.of(detail("BN-0007676", "LH notice", "2026.06.22", "2026.07.09", "", "")));
        when(emailLogDao.exists("ssafy", "BN-0007676", "APPLY_OPEN")).thenReturn(false);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        RentalNoticeEmailService.EmailRunResult result = service.sendFavoriteNoticeEmails("ssafy");

        assertThat(result.sentCount()).isEqualTo(1);
        Mockito.verify(emailLogDao).save(
                Mockito.eq("ssafy"),
                Mockito.eq("BN-0007676"),
                Mockito.eq("APPLY_OPEN"),
                Mockito.eq("ssafy@example.com"),
                Mockito.argThat(subject -> subject.endsWith(": LH notice"))
        );
    }

    @Test
    void sendsHomeFitHtmlEmailWithBulletsAndHomepageButton() throws Exception {
        MemberDto member = member("ssafy", "ssafy@example.com");
        MimeMessage message = new MimeMessage((Session) null);
        when(memberService.findByUserId("ssafy")).thenReturn(Optional.of(member));
        when(favoriteService.findFavorites("ssafy", 100))
                .thenReturn(List.of(detail("BN-0007676", "Yeongjong notice", "2026.06.22", "2026.07.09", "", "")));
        when(emailLogDao.exists("ssafy", "BN-0007676", "APPLY_OPEN")).thenReturn(false);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(message);

        RentalNoticeEmailService.EmailRunResult result = service.sendFavoriteNoticeEmails("ssafy");

        assertThat(result.sentCount()).isEqualTo(1);
        assertThat(extractText(message.getContent()))
                .contains("HomeFit")
                .contains("● 알림 유형: LH 공고 접수 시작")
                .contains("● 공고명")
                .contains("HomeFit 홈페이지 바로가기")
                .contains("https://homefit.example.com/home");
        Mockito.verify(mailSender).send(message);
    }

    private MemberDto member(String userId, String email) {
        MemberDto member = new MemberDto();
        member.setUserId(userId);
        member.setEmail(email);
        return member;
    }

    private RentalNoticeDetail detail(
            String noticeId,
            String title,
            String noticeDate,
            String closeDate,
            String applyStartDate,
            String applyEndDate
    ) {
        RentalNotice notice = new RentalNotice(
                noticeId,
                title,
                "Seoul",
                "rental",
                "happy",
                "open",
                noticeDate,
                closeDate,
                "https://apply.lh.or.kr",
                "01",
                "01",
                "10",
                "010",
                "api"
        );
        return new RentalNoticeDetail(
                notice,
                new RentalDetail("Seoul", "Gangnam", applyStartDate, applyEndDate, "1600-1004"),
                List.of()
        );
    }

    private String extractText(Object content) throws Exception {
        if (content instanceof String text) {
            return text;
        }
        if (content instanceof Multipart multipart) {
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < multipart.getCount(); index++) {
                BodyPart part = multipart.getBodyPart(index);
                builder.append(extractText(part.getContent()));
            }
            return builder.toString();
        }
        return String.valueOf(content);
    }
}
