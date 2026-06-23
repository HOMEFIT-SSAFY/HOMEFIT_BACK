package com.happyhome.transport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.happyhome.openapi.BusOpenApiClient;
import com.happyhome.openapi.KakaoLocalApiClient;
import com.happyhome.transport.dao.BusStopMapper;
import com.happyhome.transport.dto.BusStop;
import java.util.List;
import org.junit.jupiter.api.Test;

class BusStopServiceTest {

    private final BusStopMapper busStopMapper = mock(BusStopMapper.class);
    private final BusOpenApiClient busOpenApiClient = mock(BusOpenApiClient.class);
    private final KakaoLocalApiClient kakaoLocalApiClient = mock(KakaoLocalApiClient.class);
    private final BusStopService busStopService = new BusStopService(
            busStopMapper,
            busOpenApiClient,
            kakaoLocalApiClient
    );

    @Test
    void findNearbyReturnsCoordinateApiStopsFirst() {
        List<BusStop> expected = List.of(new BusStop(
                "ICB123000001",
                "api stop",
                "39001",
                "23",
                37.503,
                127.047
        ));
        when(busOpenApiClient.nearbyStops(37.503, 127.047)).thenReturn(expected);

        List<BusStop> stops = busStopService.findNearby(37.503, 127.047, 500, 20);

        assertThat(stops).isEqualTo(expected);
        verify(busStopMapper, never()).findNearby(37.503, 127.047, 500, 20);
    }

    @Test
    void findNearbyFallsBackToLocalDataWhenCoordinateApiIsEmpty() {
        List<BusStop> fallback = List.of(new BusStop(
                "LOCAL123",
                "cached stop",
                "39002",
                "23",
                37.504,
                127.048
        ));
        when(busOpenApiClient.nearbyStops(37.503, 127.047)).thenReturn(List.of());
        when(busStopMapper.findNearby(37.503, 127.047, 500, 20)).thenReturn(fallback);

        List<BusStop> stops = busStopService.findNearby(37.503, 127.047, 500, 20);

        assertThat(stops).isEqualTo(fallback);
    }

    @Test
    void findNearbyFallsBackToKakaoBusStopsWhenCoordinateApiIsEmpty() {
        List<BusStop> kakaoStops = List.of(new BusStop(
                "kakao-123",
                "Seonjeongneung bus stop",
                "",
                "kakao",
                37.5105,
                127.044
        ));
        when(busOpenApiClient.nearbyStops(37.51098, 127.043594)).thenReturn(List.of());
        when(kakaoLocalApiClient.busStops(127.043594, 37.51098, 500)).thenReturn(kakaoStops);

        List<BusStop> stops = busStopService.findNearby(37.51098, 127.043594, 500, 20);

        assertThat(stops).isEqualTo(kakaoStops);
        verify(busStopMapper, never()).findNearby(37.51098, 127.043594, 500, 20);
    }
}
