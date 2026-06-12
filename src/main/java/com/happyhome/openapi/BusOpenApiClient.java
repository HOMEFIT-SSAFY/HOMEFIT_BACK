package com.happyhome.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happyhome.config.OpenApiProperties;
import com.happyhome.transport.dto.BusCityCode;
import com.happyhome.transport.dto.BusStop;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BusOpenApiClient {

    private final OpenApiProperties properties;
    private final OpenApiJsonParser parser = new OpenApiJsonParser();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    public BusOpenApiClient(OpenApiProperties properties) {
        this.properties = properties;
    }

    public List<BusCityCode> cityCodes() {
        if (!configured()) {
            return List.of();
        }
        String body = get("/getCtyCodeList", Map.of("numOfRows", 1000, "pageNo", 1));
        return parser.items(body).stream()
                .map(this::cityCode)
                .filter(cityCode -> OpenApiUri.hasText(cityCode.cityCode()))
                .toList();
    }

    public List<BusStop> stops(String cityCode) {
        if (!configured() || !OpenApiUri.hasText(cityCode)) {
            return List.of();
        }
        int totalCount = totalCount(get("/getSttnNoList", Map.of(
                "cityCode", cityCode,
                "numOfRows", 1,
                "pageNo", 1
        )));
        if (totalCount <= 0) {
            return List.of();
        }

        String body = get("/getSttnNoList", Map.of(
                "cityCode", cityCode,
                "numOfRows", totalCount,
                "pageNo", 1
        ));
        return parser.items(body).stream()
                .map(node -> busStop(cityCode, node))
                .filter(stop -> OpenApiUri.hasText(stop.nodeId()))
                .toList();
    }

    private String get(String path, Map<String, ?> params) {
        Map<String, Object> requestParams = new java.util.LinkedHashMap<>();
        requestParams.put("serviceKey", properties.getBus().getServiceKey());
        requestParams.put("_type", "json");
        requestParams.putAll(params);

        return restClient.get()
                .uri(OpenApiUri.build(properties.getBus().getBaseUrl() + path, requestParams))
                .retrieve()
                .body(String.class);
    }

    private boolean configured() {
        return OpenApiUri.hasText(properties.getBus().getServiceKey())
                && OpenApiUri.hasText(properties.getBus().getBaseUrl());
    }

    private BusCityCode cityCode(JsonNode node) {
        return new BusCityCode(
                text(node, "citycode"),
                text(node, "cityname")
        );
    }

    private BusStop busStop(String cityCode, JsonNode node) {
        return new BusStop(
                text(node, "nodeid"),
                text(node, "nodenm"),
                text(node, "nodeno"),
                text(node, "citycode", cityCode),
                number(node, "gpslati"),
                number(node, "gpslong")
        );
    }

    private int totalCount(String body) {
        try {
            JsonNode root = objectMapper.readTree(body);
            return root.path("response").path("body").path("totalCount").asInt(0);
        } catch (Exception e) {
            return 0;
        }
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
}
