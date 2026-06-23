package com.happyhome.transfer.favorite.service;

import com.happyhome.transfer.dto.TransferDto;
import com.happyhome.transfer.favorite.dao.FavoriteTransferDao;
import com.happyhome.transfer.service.TransferService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FavoriteTransferService {

    private final FavoriteTransferDao favoriteTransferDao;
    private final TransferService transferService;

    public FavoriteTransferService(FavoriteTransferDao favoriteTransferDao, TransferService transferService) {
        this.favoriteTransferDao = favoriteTransferDao;
        this.transferService = transferService;
    }

    public boolean toggle(String userId, int transferId) {
        if (favoriteTransferDao.exists(userId, transferId)) {
            favoriteTransferDao.delete(userId, transferId);
            return false;
        }
        favoriteTransferDao.save(userId, transferId);
        return true;
    }

    public boolean isFavorite(String userId, int transferId) {
        return favoriteTransferDao.exists(userId, transferId);
    }

    public List<TransferDto> findFavorites(String userId, int limit) {
        return favoriteTransferDao.findTransferIdsByUserId(userId, limit).stream()
                .map(transferId -> transferService.findById(transferId, false))
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}
