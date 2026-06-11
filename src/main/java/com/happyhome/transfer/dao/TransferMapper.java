package com.happyhome.transfer.dao;

import com.happyhome.transfer.dto.TransferDto;
import com.happyhome.transfer.dto.TransferSearchCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TransferMapper {

    List<TransferDto> findAll(TransferSearchCondition condition);

    TransferDto findById(int transferId);

    List<String> findImageUrls(int transferId);

    void save(TransferDto transfer);

    void update(TransferDto transfer);

    void increaseViewCount(int transferId);

    void deleteById(int transferId);

    void deleteImagesByTransferId(int transferId);

    void saveImage(@Param("transferId") int transferId, @Param("imageUrl") String imageUrl, @Param("sortOrder") int sortOrder);
}
