package com.happyhome.transport.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.happyhome.transport.dto.BusStop;
import com.happyhome.transport.service.BusStopService;
import com.happyhome.transport.service.BusStopSyncService;
import java.util.List;
import org.junit.jupiter.api.Test;

class BusStopRestControllerTest {

    private final BusStopService busStopService = mock(BusStopService.class);
    private final BusStopSyncService busStopSyncService = mock(BusStopSyncService.class);
    private final BusStopRestController controller = new BusStopRestController(busStopService, busStopSyncService);

    @Test
    void nearbyReturnsCoordinateApiBackedStops() {
        List<BusStop> expected = List.of(new BusStop(
                "ICB123000001",
                "api stop",
                "39001",
                "23",
                37.503,
                127.047
        ));
        when(busStopService.findNearby(37.503, 127.047, 500, 20)).thenReturn(expected);

        List<BusStop> stops = controller.nearby(37.503, 127.047, 500, 20);

        assertThat(stops).isEqualTo(expected);
    }
}
