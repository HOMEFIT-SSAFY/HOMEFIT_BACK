package com.happyhome.transfer.comment.dao;

import com.happyhome.transfer.comment.dto.TransferCommentDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransferCommentMapper {

    List<TransferCommentDto> findByTransferId(int transferId);

    TransferCommentDto findById(int commentId);

    void save(TransferCommentDto comment);

    void update(TransferCommentDto comment);

    void deleteById(int commentId);
}
