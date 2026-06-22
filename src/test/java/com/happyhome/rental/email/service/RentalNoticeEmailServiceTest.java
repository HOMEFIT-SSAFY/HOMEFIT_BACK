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
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-22T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final RentalNoticeEmailService service = new RentalNoticeEmailService(
            favoriteService,
            memberService,
            emailLogDao,
            mailSenderProvider,
            clock
    );

    @Test
    void sendsClosingSoonEmailOncePerNoticeAndUser() {
        MemberDto member = new MemberDto();
        member.setUserId("ssafy");
        member.setEmail("ssafy@example.com");
        when(memberService.findByUserId("ssafy")).thenReturn(Optional.of(member));
        when(favoriteService.findFavorites("ssafy", 100)).thenReturn(List.of(detail("LH-001", "2026.06.20", "2026.06.24")));
        when(emailLogDao.exists("ssafy", "LH-001", "CLOSING_SOON")).thenReturn(false);
        when(mailSenderProvider.getIfAvailable()).thenReturn(Mockito.mock(JavaMailSender.class));

        RentalNoticeEmailService.EmailRunResult result = service.sendFavoriteNoticeEmails("ssafy");

        assertThat(result.sentCount()).isEqualTo(1);
        Mockito.verify(emailLogDao).save("ssafy", "LH-001", "CLOSING_SOON", "ssafy@example.com", "[HappyHome] LH 공고 마감 임박: 서울 행복주택");
    }

    @Test
    void skipsEmailWhenSameEventWasAlreadySent() {
        MemberDto member = new MemberDto();
        member.setUserId("ssafy");
        member.setEmail("ssafy@example.com");
        when(memberService.findByUserId("ssafy")).thenReturn(Optional.of(member));
        when(favoriteService.findFavorites("ssafy", 100)).thenReturn(List.of(detail("LH-001", "2026.06.20", "2026.06.24")));
        when(emailLogDao.exists("ssafy", "LH-001", "CLOSING_SOON")).thenReturn(true);

        RentalNoticeEmailService.EmailRunResult result = service.sendFavoriteNoticeEmails("ssafy");

        assertThat(result.sentCount()).isZero();
        Mockito.verify(emailLogDao, Mockito.never()).save(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    private RentalNoticeDetail detail(String noticeId, String startDate, String endDate) {
        RentalNotice notice = new RentalNotice(noticeId, "서울 행복주택", "서울", "임대", "행복주택", "공고중",
                "2026.06.01", "2026.06.30", "https://apply.lh.or.kr",
                "01", "01", "10", "010", "api");
        return new RentalNoticeDetail(
                notice,
                new RentalDetail("서울", "강남구", startDate, endDate, "1600-1004"),
                List.of()
        );
    }
}
