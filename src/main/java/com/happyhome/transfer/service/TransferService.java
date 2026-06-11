package com.happyhome.transfer.service;

import com.happyhome.transfer.dao.TransferDao;
import com.happyhome.transfer.dto.TransferDto;
import com.happyhome.transfer.dto.TransferRequest;
import com.happyhome.transfer.dto.TransferSearchCondition;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TransferService {

    private final TransferDao transferDao;
    private final Path uploadRoot = Paths.get("uploads", "transfers").toAbsolutePath().normalize();

    public TransferService(TransferDao transferDao) {
        this.transferDao = transferDao;
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

    public TransferDto create(TransferRequest request, String writerId) {
        TransferDto transfer = toTransfer(new TransferDto(), request, writerId);
        return transferDao.save(transfer);
    }

    public Optional<TransferDto> update(int transferId, TransferRequest request, String writerId) {
        return transferDao.findById(transferId).map(existing -> {
            TransferDto transfer = toTransfer(existing, request, writerId);
            transfer.setTransferId(transferId);
            transferDao.update(transfer);
            return transferDao.findById(transferId).orElse(transfer);
        });
    }

    public void delete(int transferId) {
        transferDao.deleteById(transferId);
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
                    .map(this::storeImage)
                    .forEach(imageUrls::add);
        }
        return imageUrls;
    }

    private String storeImage(MultipartFile file) {
        try {
            Files.createDirectories(uploadRoot);
            String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }
            String filename = UUID.randomUUID() + extension;
            Path target = uploadRoot.resolve(filename).normalize();
            file.transferTo(target);
            return "/uploads/transfers/" + filename;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
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
}
