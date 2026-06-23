package com.happyhome.transfer.favorite.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteTransferMapper {

    Integer count(@Param("userId") String userId, @Param("transferId") int transferId);

    void save(@Param("userId") String userId, @Param("transferId") int transferId);

    void delete(@Param("userId") String userId, @Param("transferId") int transferId);

    List<Integer> findTransferIdsByUserId(@Param("userId") String userId, @Param("limit") int limit);
}
