package com.happyhome.transfer.service;

import com.happyhome.transfer.dao.TransferDao;
import com.happyhome.transfer.dto.TransferDto;
import com.happyhome.transfer.dto.TransferRequest;
import com.happyhome.transfer.dto.TransferSearchCondition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class TransferService {

    private final TransferDao transferDao;
    private final TransferImageStorage imageStorage;

    public TransferService(TransferDao transferDao, TransferImageStorage imageStorage) {
        this.transferDao = transferDao;
        this.imageStorage = imageStorage;
    }

    public List<TransferDto> findAll(TransferSearchCondition condition) {
        TransferSearchCondition normalized = condition == null ? new TransferSearchCondition() : condition;
        normalized.setKeyword(trimToNull(normalized.getKeyword()));
        normalized.setStatus(trimToNull(normalized.getStatus()));
        normalized.setLimit(normalizeLimit(normalized.getLimit()));
        return transferDao.findAll(normalized);
    }

    public Optional<TransferDto> findById(int transferId, boolean increaseViewCount) {
        if (increaseViewCount) {
            transferDao.increaseViewCount(transferId);
        }
        return transferDao.findById(transferId);
    }

    @Transactional
    public TransferDto create(TransferRequest request, String writerId) {
        TransferDto transfer = toTransfer(new TransferDto(), request, writerId);
        return transferDao.save(transfer);
    }

    @Transactional
    public Optional<TransferDto> update(int transferId, TransferRequest request, String writerId) {
        return transferDao.findById(transferId).map(existing -> {
            assertWriter(existing, writerId);
            TransferDto transfer = toTransfer(existing, request, writerId);
            transfer.setTransferId(transferId);
            transferDao.update(transfer);
            return transferDao.findById(transferId).orElse(transfer);
        });
    }

    @Transactional
    public void delete(int transferId) {
        transferDao.deleteById(transferId);
    }

    @Transactional
    public Optional<TransferDto> delete(int transferId, String writerId) {
        return transferDao.findById(transferId).map(existing -> {
            assertWriter(existing, writerId);
            transferDao.deleteById(transferId);
            return existing;
        });
    }

    private TransferDto toTransfer(TransferDto transfer, TransferRequest request, String writerId) {
        if (request == null) {
            throw new IllegalArgumentException("양도글 정보를 입력해 주세요.");
        }
        if (!StringUtils.hasText(request.getTitle()) || !StringUtils.hasText(request.getContent())
                || !StringUtils.hasText(request.getAddress())) {
            throw new IllegalArgumentException("제목, 내용, 주소는 필수입니다.");
        }

        transfer.setWriterId(StringUtils.hasText(transfer.getWriterId()) ? transfer.getWriterId() : writerId);
        transfer.setTitle(request.getTitle().trim());
        transfer.setContent(request.getContent().trim());
        transfer.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "양도가능");
        transfer.setAddress(request.getAddress().trim());
        transfer.setDetailAddress(trimToNull(request.getDetailAddress()));
        transfer.setFloor(trimToNull(request.getFloor()));
        transfer.setExclusiveArea(request.getExclusiveArea());
        transfer.setDepositAmount(defaultNumber(request.getDepositAmount()));
        transfer.setMonthlyRentAmount(defaultNumber(request.getMonthlyRentAmount()));
        transfer.setMaintenanceFee(defaultNumber(request.getMaintenanceFee()));
        transfer.setTransferFee(defaultNumber(request.getTransferFee()));
        transfer.setContractEndDate(request.getContractEndDate());
        transfer.setMoveInDate(request.getMoveInDate());
        transfer.setContactPhone(trimToNull(request.getContactPhone()));
        transfer.setImageUrls(resolveImageUrls(request));
        return transfer;
    }

    private List<String> resolveImageUrls(TransferRequest request) {
        List<String> imageUrls = new ArrayList<>();
        if (request.getImageUrls() != null) {
            request.getImageUrls().stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .forEach(imageUrls::add);
        }
        if (request.getImages() != null) {
            request.getImages().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(imageStorage::store)
                    .forEach(imageUrls::add);
        }
        return imageUrls;
    }

    private Integer defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 100;
        }
        return Math.min(limit, 200);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void assertWriter(TransferDto transfer, String writerId) {
        if (!StringUtils.hasText(writerId) || !writerId.equals(transfer.getWriterId())) {
            throw new AccessDeniedException("Only the writer can modify this transfer.");
        }
    }
}
