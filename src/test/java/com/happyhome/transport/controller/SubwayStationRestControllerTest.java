package com.happyhome.transport.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.happyhome.transport.dto.SubwayStation;
import com.happyhome.transport.service.SubwayStationService;
import java.util.List;
import org.junit.jupiter.api.Test;

class SubwayStationRestControllerTest {

    private final SubwayStationService subwayStationService = mock(SubwayStationService.class);
    private final SubwayStationRestController controller = new SubwayStationRestController(subwayStationService);

    @Test
    void nearbyReturnsSubwayStationDetails() {
        List<SubwayStation> expected = List.of(new SubwayStation(
                "SW8-1",
                "Gangnam Station",
                "Seoul Gangnam-daero",
                37.497952,
                127.027619,
                120
        ));
        when(subwayStationService.findNearby(37.497952, 127.027619, 1000)).thenReturn(expected);

        List<SubwayStation> stations = controller.nearby(37.497952, 127.027619, 1000);

        assertThat(stations).isEqualTo(expected);
    }
}
