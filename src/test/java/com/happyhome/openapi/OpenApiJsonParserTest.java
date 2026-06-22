package com.happyhome.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpenApiJsonParserTest {

    private final OpenApiJsonParser parser = new OpenApiJsonParser();

    @Test
    void readsItemsFromLhNumberedListKeys() {
        String json = """
                [
                  {"dsSch":[{"PAN_ID":"LH-001"}]},
                  {"dsList01":[
                    {"HTY_NNA":"26A","NOW_HSH_CNT":"2"},
                    {"HTY_NNA":"26B","NOW_HSH_CNT":"4"}
                  ]}
                ]
                """;

        List<JsonNode> items = parser.items(json);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).path("HTY_NNA").asText()).isEqualTo("26A");
        assertThat(items.get(1).path("NOW_HSH_CNT").asText()).isEqualTo("4");
    }

    @Test
    void readsItemsFromLhDetailScheduleKeys() {
        String json = """
                [
                  {"dsSplScdl":[{"SBSC_ACP_ST_DT":"2026.06.29","SBSC_ACP_CLSG_DT":"2026.06.29"}]},
                  {"dsSbd":[{"LGDN_ADR":"인천광역시 강화군","LGDN_DTL_ADR":"남문안길"}]}
                ]
                """;

        List<JsonNode> items = parser.items(json);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).path("SBSC_ACP_ST_DT").asText()).isEqualTo("2026.06.29");
        assertThat(items.get(0).path("LGDN_ADR").asText()).isEqualTo("인천광역시 강화군");
    }
}
