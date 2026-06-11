package com.happyhome.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.happyhome.config.OpenApiProperties;
import com.happyhome.traffic.dto.TrafficEvent;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ItsOpenApiClient {

    private final OpenApiProperties properties;
    private final OpenApiJsonParser parser = new OpenApiJsonParser();
    private final RestClient restClient = RestClient.create();

    public ItsOpenApiClient(OpenApiProperties properties) {
        this.properties = properties;
    }

    public List<TrafficEvent> events(double longitude, double latitude) {
        if (!OpenApiUri.hasText(properties.getIts().getApiKey())) {
            return SampleData.trafficEvents(longitude, latitude);
        }
        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(
                            properties.getIts().getEventUrl(),
                            requestParams(properties.getIts().getApiKey(), longitude, latitude)
                    ))
                    .retrieve()
                    .body(String.class);
            List<TrafficEvent> events = parser.items(body).stream().map(this::event).toList();
            return events.isEmpty() ? SampleData.trafficEvents(longitude, latitude) : events;
        } catch (Exception e) {
            return SampleData.trafficEvents(longitude, latitude);
        }
    }

    static Map<String, Object> requestParams(String apiKey, double longitude, double latitude) {
        double delta = 0.03;
        return Map.of(
                "apiKey", apiKey,
                "type", "all",
                "eventType", "all",
                "minX", longitude - delta,
                "maxX", longitude + delta,
                "minY", latitude - delta,
                "maxY", latitude + delta,
                "getType", "json"
        );
    }

    private TrafficEvent event(JsonNode node) {
        return new TrafficEvent(
                text(node, "eventType", text(node, "type", "돌발")),
                text(node, "message", text(node, "eventDetailType", "교통 정보")),
                text(node, "roadName", text(node, "road", "")),
                number(node, "coordX"),
                number(node, "coordY")
        );
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText("");
        return value.isBlank() ? fallback : value;
    }

    private double number(JsonNode node, String field) {
        try {
            return Double.parseDouble(node.path(field).asText("0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
