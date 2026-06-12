package com.happyhome.transport.service;

import com.happyhome.transport.dao.BusStopMapper;
import com.happyhome.transport.dto.BusStop;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BusStopService {

    private final BusStopMapper busStopMapper;

    public BusStopService(BusStopMapper busStopMapper) {
        this.busStopMapper = busStopMapper;
    }

    public List<BusStop> findNearby(double latitude, double longitude, int radiusMeters, int limit) {
        int safeRadius = Math.min(Math.max(radiusMeters, 100), 3000);
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return busStopMapper.findNearby(latitude, longitude, safeRadius, safeLimit);
    }
}
