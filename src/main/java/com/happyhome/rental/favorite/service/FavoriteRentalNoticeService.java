package com.happyhome.rental.favorite.service;

import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.favorite.dao.FavoriteRentalNoticeDao;
import com.happyhome.rental.service.RentalService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class FavoriteRentalNoticeService {

    private final FavoriteRentalNoticeDao favoriteDao;
    private final RentalService rentalService;

    public FavoriteRentalNoticeService(FavoriteRentalNoticeDao favoriteDao, RentalService rentalService) {
        this.favoriteDao = favoriteDao;
        this.rentalService = rentalService;
    }

    public boolean toggle(String userId, String noticeId) {
        if (!StringUtils.hasText(noticeId)) {
            throw new IllegalArgumentException("noticeId is required");
        }
        if (favoriteDao.exists(userId, noticeId)) {
            favoriteDao.delete(userId, noticeId);
            return false;
        }
        favoriteDao.save(userId, noticeId);
        return true;
    }

    public boolean isFavorite(String userId, String noticeId) {
        return favoriteDao.exists(userId, noticeId);
    }

    public List<RentalNoticeDetail> findFavorites(String userId, int limit) {
        return favoriteDao.findNoticeIdsByUserId(userId, limit).stream()
                .map(rentalService::detail)
                .toList();
    }

    public List<String> findUserIdsWithFavorites() {
        return favoriteDao.findUserIdsWithFavorites();
    }
}
