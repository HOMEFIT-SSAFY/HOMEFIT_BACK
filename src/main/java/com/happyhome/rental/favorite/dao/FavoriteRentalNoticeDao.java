package com.happyhome.rental.favorite.dao;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteRentalNoticeDao {

    private final FavoriteRentalNoticeMapper mapper;

    public FavoriteRentalNoticeDao(FavoriteRentalNoticeMapper mapper) {
        this.mapper = mapper;
    }

    public boolean exists(String userId, String noticeId) {
        Integer count = mapper.count(userId, noticeId);
        return count != null && count > 0;
    }

    public void save(String userId, String noticeId) {
        mapper.save(userId, noticeId);
    }

    public void delete(String userId, String noticeId) {
        mapper.delete(userId, noticeId);
    }

    public List<String> findNoticeIdsByUserId(String userId, int limit) {
        return mapper.findNoticeIdsByUserId(userId, limit);
    }

    public List<String> findUserIdsWithFavorites() {
        return mapper.findUserIdsWithFavorites();
    }
}
