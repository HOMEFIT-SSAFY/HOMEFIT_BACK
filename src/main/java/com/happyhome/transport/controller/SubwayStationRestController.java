package com.happyhome.transport.controller;

import com.happyhome.transport.dto.SubwayStation;
import com.happyhome.transport.service.SubwayStationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subway-stations")
public class SubwayStationRestController {

    private final SubwayStationService subwayStationService;

    public SubwayStationRestController(SubwayStationService subwayStationService) {
        this.subwayStationService = subwayStationService;
    }

    @GetMapping("/nearby")
    public List<SubwayStation> nearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "1000") int radiusMeters
    ) {
        return subwayStationService.findNearby(latitude, longitude, radiusMeters);
    }
}
