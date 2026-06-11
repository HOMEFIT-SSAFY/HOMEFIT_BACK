package com.happyhome.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.favorite.dao.FavoriteDealDao;
import com.happyhome.favorite.service.FavoriteDealService;
import com.happyhome.house.dao.HouseDealDao;
import com.happyhome.house.dto.HouseDeal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FavoriteDealServiceTest {

    @Test
    void toggleAddsDealWhenItIsNotFavorite() {
        FakeFavoriteDealDao favoriteDealDao = new FakeFavoriteDealDao();
        FavoriteDealService service = new FavoriteDealService(favoriteDealDao, new FakeHouseDealDao());

        boolean added = service.toggle("ssafy", 10);

        assertThat(added).isTrue();
        assertThat(favoriteDealDao.exists("ssafy", 10)).isTrue();
    }

    @Test
    void toggleRemovesDealWhenItAlreadyExists() {
        FakeFavoriteDealDao favoriteDealDao = new FakeFavoriteDealDao();
        favoriteDealDao.save("ssafy", 10);
        FavoriteDealService service = new FavoriteDealService(favoriteDealDao, new FakeHouseDealDao());

        boolean added = service.toggle("ssafy", 10);

        assertThat(added).isFalse();
        assertThat(favoriteDealDao.exists("ssafy", 10)).isFalse();
    }

    @Test
    void findFavoriteDealsLoadsHouseDealDetailsInSavedOrder() {
        FakeFavoriteDealDao favoriteDealDao = new FakeFavoriteDealDao();
        favoriteDealDao.save("ssafy", 7);
        favoriteDealDao.save("ssafy", 3);
        FavoriteDealService service = new FavoriteDealService(favoriteDealDao, new FakeHouseDealDao());

        List<HouseDeal> deals = service.findFavoriteDeals("ssafy", 10);

        assertThat(deals).extracting(HouseDeal::getNo).containsExactly(7, 3);
    }

    private static class FakeFavoriteDealDao extends FavoriteDealDao {

        private final Set<String> saved = new HashSet<>();
        private final List<Integer> order = new ArrayList<>();

        FakeFavoriteDealDao() {
            super(null);
        }

        @Override
        public boolean exists(String userId, int dealNo) {
            return saved.contains(userId + ":" + dealNo);
        }

        @Override
        public void save(String userId, int dealNo) {
            String key = userId + ":" + dealNo;
            if (saved.add(key)) {
                order.add(dealNo);
            }
        }

        @Override
        public void delete(String userId, int dealNo) {
            saved.remove(userId + ":" + dealNo);
            order.remove(Integer.valueOf(dealNo));
        }

        @Override
        public List<Integer> findDealNosByUserId(String userId, int limit) {
            return order.stream().limit(limit).toList();
        }
    }

    private static class FakeHouseDealDao extends HouseDealDao {

        FakeHouseDealDao() {
            super(null);
        }

        @Override
        public List<HouseDeal> findByNos(List<Integer> dealNos) {
            return dealNos.stream()
                    .map(no -> {
                        HouseDeal deal = new HouseDeal();
                        deal.setNo(no);
                        deal.setAptName("apt-" + no);
                        return deal;
                    })
                    .toList();
        }
    }
}
