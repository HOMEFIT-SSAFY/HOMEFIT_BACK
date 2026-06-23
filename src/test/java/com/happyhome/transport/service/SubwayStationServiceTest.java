package com.happyhome.transport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.happyhome.openapi.KakaoLocalApiClient;
import com.happyhome.transport.dto.SubwayStation;
import java.util.List;
import org.junit.jupiter.api.Test;

class SubwayStationServiceTest {

    private final KakaoLocalApiClient kakaoLocalApiClient = mock(KakaoLocalApiClient.class);
    private final SubwayStationService subwayStationService = new SubwayStationService(kakaoLocalApiClient);

    @Test
    void findNearbyReturnsKakaoSubwayStationsWithSafeRadius() {
        List<SubwayStation> expected = List.of(new SubwayStation(
                "SW8-1",
                "Gangnam Station",
                "Seoul Gangnam-daero",
                37.497952,
                127.027619,
                120
        ));
        when(kakaoLocalApiClient.subwayStations(127.027619, 37.497952, 1000)).thenReturn(expected);

        List<SubwayStation> stations = subwayStationService.findNearby(37.497952, 127.027619, 1000);

        assertThat(stations).isEqualTo(expected);
    }
}
