package com.happyhome.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.happyhome.config.OpenApiProperties;
import com.happyhome.commercial.dto.CommercialPlace;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CommercialOpenApiClient {

    private final OpenApiProperties properties;
    private final OpenApiJsonParser parser = new OpenApiJsonParser();
    private final RestClient restClient = RestClient.create();

    public CommercialOpenApiClient(OpenApiProperties properties) {
        this.properties = properties;
    }

    public List<CommercialPlace> places(double longitude, double latitude, int radiusMeters) {
        if (!OpenApiUri.hasText(properties.getData().getServiceKey())) {
            return SampleData.places(longitude, latitude);
        }
        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(properties.getCommercial().getRadiusUrl(), Map.of(
                            "serviceKey", properties.getData().getServiceKey(),
                            "type", "json",
                            "cx", longitude,
                            "cy", latitude,
                            "radius", radiusMeters,
                            "numOfRows", 50,
                            "pageNo", 1
                    )))
                    .retrieve()
                    .body(String.class);
            List<CommercialPlace> places = parser.items(body).stream().map(this::place).toList();
            return places.isEmpty() ? SampleData.places(longitude, latitude) : places;
        } catch (Exception e) {
            return SampleData.places(longitude, latitude);
        }
    }

    private CommercialPlace place(JsonNode node) {
        return new CommercialPlace(
                text(node, "bizesNm", "상호명 없음"),
                text(node, "indsLclsNm", ""),
                text(node, "indsMclsNm", ""),
                text(node, "rdnmAdr", text(node, "lnoAdr", "")),
                number(node, "lon"),
                number(node, "lat")
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
