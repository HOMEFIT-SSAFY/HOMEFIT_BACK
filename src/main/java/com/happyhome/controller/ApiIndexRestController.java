package com.happyhome.controller;

import com.happyhome.config.OpenApiProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "API Index", description = "REST API entry point")
public class ApiIndexRestController {

    private final OpenApiProperties openApiProperties;

    public ApiIndexRestController(OpenApiProperties openApiProperties) {
        this.openApiProperties = openApiProperties;
    }

    @Operation(summary = "API link list", description = "Returns primary REST API and Swagger documentation URLs.")
    @GetMapping
    public Map<String, String> index() {
        return Map.of(
                "houses", "/api/houses",
                "recentHouses", "/api/houses/recent",
                "regions", "/api/regions/sidos",
                "rentals", "/api/rentals",
                "analysis", "/api/analysis",
                "notices", "/api/notices",
                "noticePopups", "/api/notices/popups",
                "config", "/api/config",
                "swagger", "/swagger-ui.html",
                "openapiJson", "/v3/api-docs"
        );
    }

    @Operation(summary = "Frontend config", description = "Returns public browser configuration values.")
    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("kakaoJavascriptKey", openApiProperties.getKakao().getJavascriptKey());
    }
}
