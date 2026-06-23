package com.happyhome.transfer.comment.service;

import com.happyhome.transfer.comment.dao.TransferCommentDao;
import com.happyhome.transfer.comment.dto.TransferCommentDto;
import com.happyhome.transfer.comment.dto.TransferCommentRequest;
import com.happyhome.transfer.service.TransferService;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class TransferCommentService {

    private final TransferCommentDao transferCommentDao;
    private final TransferService transferService;

    public TransferCommentService(TransferCommentDao transferCommentDao, TransferService transferService) {
        this.transferCommentDao = transferCommentDao;
        this.transferService = transferService;
    }

    public List<TransferCommentDto> findByTransferId(int transferId) {
        return transferCommentDao.findByTransferId(transferId);
    }

    public TransferCommentDto create(int transferId, TransferCommentRequest request, String writerId) {
        if (transferService.findById(transferId, false).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer post not found.");
        }
        TransferCommentDto comment = new TransferCommentDto();
        comment.setTransferId(transferId);
        comment.setWriterId(writerId);
        comment.setContent(content(request));
        return transferCommentDao.save(comment);
    }

    public Optional<TransferCommentDto> update(int commentId, TransferCommentRequest request, String writerId) {
        return transferCommentDao.findById(commentId).map(existing -> {
            assertWriter(existing, writerId);
            existing.setContent(content(request));
            transferCommentDao.update(existing);
            return transferCommentDao.findById(commentId).orElse(existing);
        });
    }

    public Optional<TransferCommentDto> delete(int commentId, String writerId) {
        return transferCommentDao.findById(commentId).map(existing -> {
            assertWriter(existing, writerId);
            transferCommentDao.deleteById(commentId);
            return existing;
        });
    }

    private String content(TransferCommentRequest request) {
        if (request == null || !StringUtils.hasText(request.getContent())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment content is required.");
        }
        return request.getContent().trim();
    }

    private void assertWriter(TransferCommentDto comment, String writerId) {
        if (!StringUtils.hasText(writerId) || !writerId.equals(comment.getWriterId())) {
            throw new AccessDeniedException("Only the writer can modify this comment.");
        }
    }
}
