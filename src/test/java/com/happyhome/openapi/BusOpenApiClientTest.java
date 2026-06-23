package com.happyhome.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.config.OpenApiProperties;
import com.happyhome.transport.dto.BusStop;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BusOpenApiClientTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void nearbyStopsRequestsCoordinateApiAndParsesBusStops() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        CapturedRequest captured = new CapturedRequest();
        server.createContext("/getCrdntPrxmtSttnList", exchange -> {
            captured.query = parseQuery(exchange.getRequestURI().getRawQuery());
            byte[] body = (
                    "{"
                            + "\"response\":{"
                            + "\"body\":{"
                            + "\"items\":{"
                            + "\"item\":[{"
                            + "\"nodeid\":\"ICB123000001\","
                            + "\"nodenm\":\"테스트정류장\","
                            + "\"nodeno\":\"39001\","
                            + "\"citycode\":\"23\","
                            + "\"gpslati\":\"37.503\","
                            + "\"gpslong\":\"127.047\""
                            + "}]"
                            + "}"
                            + "}"
                            + "}"
                            + "}"
            ).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        BusOpenApiClient client = new BusOpenApiClient(properties(server));

        List<BusStop> stops = client.nearbyStops(37.503, 127.047);

        assertThat(captured.query)
                .containsEntry("serviceKey", "test-service-key")
                .containsEntry("_type", "json")
                .containsEntry("gpsLati", "37.503")
                .containsEntry("gpsLong", "127.047");
        assertThat(stops).containsExactly(new BusStop(
                "ICB123000001",
                "테스트정류장",
                "39001",
                "23",
                37.503,
                127.047
        ));
    }

    private OpenApiProperties properties(HttpServer server) {
        OpenApiProperties properties = new OpenApiProperties();
        properties.getBus().setServiceKey("test-service-key");
        properties.getBus().setBaseUrl("http://localhost:" + server.getAddress().getPort());
        return properties;
    }

    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> result = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return result;
        }
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }

    private static class CapturedRequest {
        private Map<String, String> query = Map.of();
    }
}
