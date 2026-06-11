package com.happyhome.house.dao;

import com.happyhome.house.dto.HouseDeal;
import com.happyhome.house.dto.HouseSearchCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HouseDealMapper {

    List<HouseDeal> search(HouseSearchCondition condition);

    HouseDeal findByNo(int no);

    List<HouseDeal> findByNos(@Param("dealNos") List<Integer> dealNos);

    List<HouseDeal> findRecent(int limit);
}
