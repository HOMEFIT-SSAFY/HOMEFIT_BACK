package com.happyhome.dao;

import com.happyhome.dto.HouseDeal;
import com.happyhome.dto.HouseSearchCondition;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class HouseDealDao {

    private final HouseDealMapper mapper;

    public HouseDealDao(HouseDealMapper mapper) {
        this.mapper = mapper;
    }

    public List<HouseDeal> search(HouseSearchCondition condition) {
        return mapper.search(condition == null ? new HouseSearchCondition() : condition);
    }

    public Optional<HouseDeal> findByNo(int no) {
        return Optional.ofNullable(mapper.findByNo(no));
    }

    public List<HouseDeal> findByNos(List<Integer> dealNos) {
        if (dealNos == null || dealNos.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.findByNos(dealNos);
    }

    public List<HouseDeal> findRecent(int limit) {
        return mapper.findRecent(Math.min(Math.max(limit, 1), 20));
    }
}
