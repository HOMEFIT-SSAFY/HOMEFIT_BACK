package com.happyhome.favorite.dao;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteDealDao {

    private final FavoriteDealMapper mapper;

    public FavoriteDealDao(FavoriteDealMapper mapper) {
        this.mapper = mapper;
    }

    public boolean exists(String userId, int dealNo) {
        Integer count = mapper.count(userId, dealNo);
        return count != null && count > 0;
    }

    public void save(String userId, int dealNo) {
        mapper.save(userId, dealNo);
    }

    public void delete(String userId, int dealNo) {
        mapper.delete(userId, dealNo);
    }

    public List<Integer> findDealNosByUserId(String userId, int limit) {
        return mapper.findDealNosByUserId(userId, safeLimit(limit));
    }

    private int safeLimit(int limit) {
        if (limit <= 0) {
            return 5;
        }
        return Math.min(limit, 50);
    }
}
