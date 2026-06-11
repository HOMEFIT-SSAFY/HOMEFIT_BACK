package com.happyhome.transfer.dao;

import com.happyhome.transfer.dto.TransferDto;
import com.happyhome.transfer.dto.TransferSearchCondition;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TransferDao {

    private final TransferMapper mapper;

    public TransferDao(TransferMapper mapper) {
        this.mapper = mapper;
    }

    public List<TransferDto> findAll(TransferSearchCondition condition) {
        List<TransferDto> transfers = mapper.findAll(condition);
        transfers.forEach(this::attachImages);
        return transfers;
    }

    public Optional<TransferDto> findById(int transferId) {
        return Optional.ofNullable(mapper.findById(transferId)).map(this::attachImages);
    }

    public TransferDto save(TransferDto transfer) {
        mapper.save(transfer);
        saveImages(transfer);
        return findById(transfer.getTransferId()).orElse(transfer);
    }

    public void update(TransferDto transfer) {
        mapper.update(transfer);
        mapper.deleteImagesByTransferId(transfer.getTransferId());
        saveImages(transfer);
    }

    public void increaseViewCount(int transferId) {
        mapper.increaseViewCount(transferId);
    }

    public void deleteById(int transferId) {
        mapper.deleteById(transferId);
    }

    private TransferDto attachImages(TransferDto transfer) {
        transfer.setImageUrls(mapper.findImageUrls(transfer.getTransferId()));
        return transfer;
    }

    private void saveImages(TransferDto transfer) {
        List<String> imageUrls = transfer.getImageUrls();
        if (imageUrls == null || transfer.getTransferId() == null) {
            return;
        }
        for (int i = 0; i < imageUrls.size(); i++) {
            mapper.saveImage(transfer.getTransferId(), imageUrls.get(i), i);
        }
    }
}
