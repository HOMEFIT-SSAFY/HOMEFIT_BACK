package com.happyhome.house.service;

import com.happyhome.house.dao.HouseDealDao;
import com.happyhome.house.dto.HouseDeal;
import com.happyhome.house.dto.HouseSearchCondition;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class HouseDealService {

    private final HouseDealDao houseDealDao;

    public HouseDealService(HouseDealDao houseDealDao) {
        this.houseDealDao = houseDealDao;
    }

    public List<HouseDeal> search(HouseSearchCondition condition) {
        return houseDealDao.search(condition);
    }

    public List<HouseDeal> findRecent(int limit) {
        return houseDealDao.findRecent(limit);
    }

    public Optional<HouseDeal> findByNo(int no) {
        return houseDealDao.findByNo(no);
    }
}

