package com.happyhome.property.dao;

import com.happyhome.property.dto.PropertyDeal;
import com.happyhome.property.dto.PropertyDealSearchCondition;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PropertyDealMapper {

    List<PropertyDeal> search(PropertyDealSearchCondition condition);

    void upsert(PropertyDeal deal);

    List<String> findLawdCodes();
}
