package com.happyhome.transfer.favorite.dao;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class FavoriteTransferDao {

    private final FavoriteTransferMapper mapper;

    public FavoriteTransferDao(FavoriteTransferMapper mapper) {
        this.mapper = mapper;
    }

    public boolean exists(String userId, int transferId) {
        Integer count = mapper.count(userId, transferId);
        return count != null && count > 0;
    }

    public void save(String userId, int transferId) {
        mapper.save(userId, transferId);
    }

    public void delete(String userId, int transferId) {
        mapper.delete(userId, transferId);
    }

    public List<Integer> findTransferIdsByUserId(String userId, int limit) {
        return mapper.findTransferIdsByUserId(userId, safeLimit(limit));
    }

    private int safeLimit(int limit) {
        if (limit <= 0) {
            return 5;
        }
        return Math.min(limit, 100);
    }
}
