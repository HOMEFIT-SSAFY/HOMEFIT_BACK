package com.happyhome.transfer.comment.dao;

import com.happyhome.transfer.comment.dto.TransferCommentDto;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TransferCommentDao {

    private final TransferCommentMapper mapper;

    public TransferCommentDao(TransferCommentMapper mapper) {
        this.mapper = mapper;
    }

    public List<TransferCommentDto> findByTransferId(int transferId) {
        return mapper.findByTransferId(transferId);
    }

    public Optional<TransferCommentDto> findById(int commentId) {
        return Optional.ofNullable(mapper.findById(commentId));
    }

    public TransferCommentDto save(TransferCommentDto comment) {
        mapper.save(comment);
        return findById(comment.getCommentId()).orElse(comment);
    }

    public void update(TransferCommentDto comment) {
        mapper.update(comment);
    }

    public void deleteById(int commentId) {
        mapper.deleteById(commentId);
    }
}
