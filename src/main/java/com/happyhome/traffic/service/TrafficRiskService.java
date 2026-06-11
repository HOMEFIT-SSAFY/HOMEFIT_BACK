package com.happyhome.traffic.service;

import com.happyhome.traffic.dto.TrafficEvent;
import com.happyhome.traffic.dto.TrafficRiskSummary;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TrafficRiskService {

    public TrafficRiskSummary summarize(List<TrafficEvent> events) {
        int roadworks = (int) events.stream().filter(TrafficEvent::roadwork).count();
        String level = events.size() >= 5 || roadworks >= 3 ? "높음"
                : events.size() >= 2 || roadworks >= 1 ? "보통"
                : "낮음";
        return new TrafficRiskSummary(events.size(), roadworks, level);
    }
}
