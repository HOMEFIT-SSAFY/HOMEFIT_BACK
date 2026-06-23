package com.happyhome.transfer.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.transfer.dao.TransferDao;
import com.happyhome.transfer.dto.TransferDto;
import com.happyhome.transfer.favorite.dao.FavoriteTransferDao;
import com.happyhome.transfer.service.TransferService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FavoriteTransferServiceTest {

    @Test
    void toggleAddsAndRemovesFavoriteTransfer() {
        FakeFavoriteTransferDao favoriteDao = new FakeFavoriteTransferDao();
        FavoriteTransferService service = new FavoriteTransferService(
                favoriteDao,
                new TransferService(new FakeTransferDao())
        );

        assertThat(service.toggle("ssafy", 7)).isTrue();
        assertThat(service.isFavorite("ssafy", 7)).isTrue();

        assertThat(service.toggle("ssafy", 7)).isFalse();
        assertThat(service.isFavorite("ssafy", 7)).isFalse();
    }

    @Test
    void findFavoritesReturnsExistingTransferDtos() {
        FakeFavoriteTransferDao favoriteDao = new FakeFavoriteTransferDao();
        favoriteDao.save("ssafy", 7);
        favoriteDao.save("ssafy", 8);
        FakeTransferDao transferDao = new FakeTransferDao();
        transferDao.transfers.put(7, transfer(7, "First"));

        FavoriteTransferService service = new FavoriteTransferService(
                favoriteDao,
                new TransferService(transferDao)
        );

        List<TransferDto> favorites = service.findFavorites("ssafy", 100);

        assertThat(favorites).extracting(TransferDto::getTransferId).containsExactly(7);
    }

    private static TransferDto transfer(int transferId, String title) {
        TransferDto transfer = new TransferDto();
        transfer.setTransferId(transferId);
        transfer.setTitle(title);
        return transfer;
    }

    private static class FakeFavoriteTransferDao extends FavoriteTransferDao {

        private final Map<String, Set<Integer>> favorites = new HashMap<>();

        FakeFavoriteTransferDao() {
            super(null);
        }

        @Override
        public boolean exists(String userId, int transferId) {
            return favorites.getOrDefault(userId, Set.of()).contains(transferId);
        }

        @Override
        public void save(String userId, int transferId) {
            favorites.computeIfAbsent(userId, ignored -> new HashSet<>()).add(transferId);
        }

        @Override
        public void delete(String userId, int transferId) {
            favorites.getOrDefault(userId, Set.of()).remove(transferId);
        }

        @Override
        public List<Integer> findTransferIdsByUserId(String userId, int limit) {
            return new ArrayList<>(favorites.getOrDefault(userId, Set.of()));
        }
    }

    private static class FakeTransferDao extends TransferDao {

        private final Map<Integer, TransferDto> transfers = new HashMap<>();

        FakeTransferDao() {
            super(null);
        }

        @Override
        public Optional<TransferDto> findById(int transferId) {
            return Optional.ofNullable(transfers.get(transferId));
        }
    }
}
