package com.happyhome.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happyhome.config.OpenApiProperties;
import com.happyhome.transport.dto.SubwayStation;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoLocalApiClient {

    private static final String SUBWAY_CATEGORY = "SW8";
    private static final String CATEGORY_SEARCH_URL = "https://dapi.kakao.com/v2/local/search/category.json";

    private final OpenApiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    public KakaoLocalApiClient(OpenApiProperties properties) {
        this.properties = properties;
    }

    public List<SubwayStation> subwayStations(double longitude, double latitude, int radiusMeters) {
        if (!OpenApiUri.hasText(properties.getKakao().getRestKey())) {
            return List.of();
        }
        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(CATEGORY_SEARCH_URL, Map.of(
                            "category_group_code", SUBWAY_CATEGORY,
                            "x", longitude,
                            "y", latitude,
                            "radius", Math.min(Math.max(radiusMeters, 100), 20000),
                            "sort", "distance",
                            "size", 15
                    )))
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.getKakao().getRestKey())
                    .retrieve()
                    .body(String.class);
            JsonNode documents = objectMapper.readTree(body).path("documents");
            if (!documents.isArray()) {
                return List.of();
            }
            java.util.ArrayList<SubwayStation> stations = new java.util.ArrayList<>();
            for (JsonNode node : documents) {
                stations.add(station(node));
            }
            return stations;
        } catch (Exception e) {
            return List.of();
        }
    }

    private SubwayStation station(JsonNode node) {
        return new SubwayStation(
                text(node, "id"),
                text(node, "place_name"),
                text(node, "road_address_name", text(node, "address_name")),
                number(node, "y"),
                number(node, "x"),
                integer(node, "distance")
        );
    }

    private String text(JsonNode node, String field) {
        return text(node, field, "");
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

    private int integer(JsonNode node, String field) {
        try {
            return Integer.parseInt(node.path(field).asText("0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
