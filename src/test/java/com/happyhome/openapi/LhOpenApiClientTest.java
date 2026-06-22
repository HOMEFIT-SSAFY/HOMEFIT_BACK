package com.happyhome.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happyhome.config.OpenApiProperties;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalSupply;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class LhOpenApiClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LhOpenApiClient client = new LhOpenApiClient(new OpenApiProperties());

    @Test
    void mapsActualLhSupplyFields() throws Exception {
        JsonNode node = objectMapper.readTree("""
                {
                  "SBD_LGO_NM": "인천새시장 마을정비형 공공주택",
                  "DDO_AR": "26.95",
                  "HTY_NNA": "26A 고령자",
                  "LS_GMY": "공고문 참조",
                  "RFE": "월임대료 공고문 참조",
                  "NOW_HSH_CNT": "2"
                }
                """);

        RentalSupply supply = invokeSupply(node);

        assertThat(supply).isEqualTo(new RentalSupply(
                "26A 고령자",
                "인천새시장 마을정비형 공공주택",
                "26.95",
                "보증금 공고문 참조 / 월 월임대료 공고문 참조",
                "26A 고령자",
                "2"
        ));
    }

    @Test
    void mapsActualLhDetailFields() throws Exception {
        JsonNode node = objectMapper.readTree("""
                {
                  "SBSC_ACP_ST_DT": "2026.06.29",
                  "SBSC_ACP_CLSG_DT": "2026.06.29",
                  "LGDN_ADR": "인천광역시 강화군",
                  "LGDN_DTL_ADR": "남문안길",
                  "ETC_CTS": "공고문 확인"
                }
                """);

        RentalDetail detail = invokeDetail(node);

        assertThat(detail).isEqualTo(new RentalDetail(
                "인천광역시 강화군",
                "남문안길",
                "2026.06.29",
                "2026.06.29",
                "1600-1004"
        ));
    }

    private RentalSupply invokeSupply(JsonNode node) throws Exception {
        Method method = LhOpenApiClient.class.getDeclaredMethod("supply", JsonNode.class);
        method.setAccessible(true);
        return (RentalSupply) method.invoke(client, node);
    }

    private RentalDetail invokeDetail(JsonNode node) throws Exception {
        Method method = LhOpenApiClient.class.getDeclaredMethod("detail", JsonNode.class);
        method.setAccessible(true);
        return (RentalDetail) method.invoke(client, node);
    }
}
