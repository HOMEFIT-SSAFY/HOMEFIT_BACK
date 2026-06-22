package com.happyhome.rental.favorite.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteRentalNoticeMapper {

    Integer count(@Param("userId") String userId, @Param("noticeId") String noticeId);

    void save(@Param("userId") String userId, @Param("noticeId") String noticeId);

    void delete(@Param("userId") String userId, @Param("noticeId") String noticeId);

    List<String> findNoticeIdsByUserId(@Param("userId") String userId, @Param("limit") int limit);

    List<String> findUserIdsWithFavorites();
}
