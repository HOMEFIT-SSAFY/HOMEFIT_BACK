package com.happyhome.rental.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.favorite.dao.FavoriteRentalNoticeDao;
import com.happyhome.rental.service.RentalService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FavoriteRentalNoticeServiceTest {

    private final FavoriteRentalNoticeDao favoriteDao = Mockito.mock(FavoriteRentalNoticeDao.class);
    private final RentalService rentalService = Mockito.mock(RentalService.class);
    private final FavoriteRentalNoticeService service = new FavoriteRentalNoticeService(favoriteDao, rentalService);

    @Test
    void toggleAddsNoticeWhenItIsNotFavorite() {
        when(favoriteDao.exists("ssafy", "LH-001")).thenReturn(false);

        boolean favorite = service.toggle("ssafy", "LH-001");

        assertThat(favorite).isTrue();
        Mockito.verify(favoriteDao).save("ssafy", "LH-001");
    }

    @Test
    void toggleRemovesNoticeWhenItIsAlreadyFavorite() {
        when(favoriteDao.exists("ssafy", "LH-001")).thenReturn(true);

        boolean favorite = service.toggle("ssafy", "LH-001");

        assertThat(favorite).isFalse();
        Mockito.verify(favoriteDao).delete("ssafy", "LH-001");
    }

    @Test
    void findFavoritesReturnsNoticeDetailsInFavoriteOrder() {
        when(favoriteDao.findNoticeIdsByUserId("ssafy", 20)).thenReturn(List.of("LH-001", "LH-002"));
        when(rentalService.detail("LH-001")).thenReturn(detail("LH-001"));
        when(rentalService.detail("LH-002")).thenReturn(detail("LH-002"));

        List<RentalNoticeDetail> favorites = service.findFavorites("ssafy", 20);

        assertThat(favorites).extracting(item -> item.notice().noticeId())
                .containsExactly("LH-001", "LH-002");
    }

    private RentalNoticeDetail detail(String noticeId) {
        return new RentalNoticeDetail(
                new RentalNotice(noticeId, "서울 행복주택", "서울", "임대", "행복주택", "공고중",
                        "2026.06.01", "2026.06.30", "https://apply.lh.or.kr",
                        "01", "01", "10", "010", "api"),
                new RentalDetail("서울", "강남구", "2026.06.20", "2026.06.24", "1600-1004"),
                List.of()
        );
    }
}
