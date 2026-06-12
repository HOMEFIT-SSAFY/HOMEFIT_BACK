package com.happyhome.transport.dao;

import com.happyhome.transport.dto.BusCityCode;
import com.happyhome.transport.dto.BusStop;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BusStopMapper {

    void upsertCityCode(BusCityCode cityCode);

    void upsertBusStop(BusStop busStop);

    int countBusStops();

    List<BusStop> findNearby(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") int radiusMeters,
            @Param("limit") int limit
    );
}
