package com.happyhome.transport.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.transport.dto.BusCityCode;
import com.happyhome.transport.dto.BusStop;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:happyhome-bus-stop-mapper-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
class BusStopMapperTest {

    @Autowired
    private BusStopMapper busStopMapper;

    @Test
    void findNearbyMapsDatabaseDoubleColumnsToBusStop() {
        busStopMapper.upsertCityCode(new BusCityCode("23", "Seoul"));
        busStopMapper.upsertBusStop(new BusStop(
                "ICB122000115",
                "Seolleung Station",
                "23218",
                "23",
                37.505151,
                127.050493
        ));

        List<BusStop> stops = busStopMapper.findNearby(37.505151, 127.050493, 100, 10);

        assertThat(stops).containsExactly(new BusStop(
                "ICB122000115",
                "Seolleung Station",
                "23218",
                "23",
                37.505151,
                127.050493
        ));
    }
}
