package com.happyhome.favorite.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteDealMapper {

    Integer count(@Param("userId") String userId, @Param("dealNo") int dealNo);

    void save(@Param("userId") String userId, @Param("dealNo") int dealNo);

    void delete(@Param("userId") String userId, @Param("dealNo") int dealNo);

    List<Integer> findDealNosByUserId(@Param("userId") String userId, @Param("limit") int limit);
}
