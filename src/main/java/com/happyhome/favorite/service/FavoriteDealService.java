package com.happyhome.favorite.service;

import com.happyhome.favorite.dao.FavoriteDealDao;
import com.happyhome.house.dao.HouseDealDao;
import com.happyhome.house.dto.HouseDeal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FavoriteDealService {

    private final FavoriteDealDao favoriteDealDao;
    private final HouseDealDao houseDealDao;

    public FavoriteDealService(FavoriteDealDao favoriteDealDao, HouseDealDao houseDealDao) {
        this.favoriteDealDao = favoriteDealDao;
        this.houseDealDao = houseDealDao;
    }

    public boolean toggle(String userId, int dealNo) {
        if (favoriteDealDao.exists(userId, dealNo)) {
            favoriteDealDao.delete(userId, dealNo);
            return false;
        }
        favoriteDealDao.save(userId, dealNo);
        return true;
    }

    public boolean isFavorite(String userId, int dealNo) {
        return favoriteDealDao.exists(userId, dealNo);
    }

    public List<Integer> findFavoriteDealNos(String userId, int limit) {
        return favoriteDealDao.findDealNosByUserId(userId, limit);
    }

    public List<HouseDeal> findFavoriteDeals(String userId, int limit) {
        List<Integer> dealNos = favoriteDealDao.findDealNosByUserId(userId, limit);
        return houseDealDao.findByNos(dealNos);
    }
}
