package com.happyhome.openapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.happyhome.config.OpenApiProperties;
import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.dto.RentalSupply;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LhOpenApiClient {

    private final OpenApiProperties properties;
    private final OpenApiJsonParser parser;
    private final RestClient restClient;

    public LhOpenApiClient(OpenApiProperties properties) {
        this.properties = properties;
        this.parser = new OpenApiJsonParser();
        this.restClient = RestClient.create();
    }

    public List<RentalNotice> notices(RentalSearchCondition condition) {
        if (!OpenApiUri.hasText(properties.getData().getServiceKey())) {
            return SampleData.rentalNotices();
        }
        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(properties.getLh().getNoticeUrl(), Map.of(
                            "ServiceKey", properties.getData().getServiceKey(),
                            "PG_SZ", condition.size(),
                            "PAGE", condition.page(),
                            "PAN_NM", blankToEmpty(condition.keyword()),
                            "CNP_CD", blankToEmpty(condition.regionCode()),
                            "PAN_SS", blankToEmpty(condition.status()),
                            "PAN_NT_ST_DT", "2020.01.01",
                            "CLSG_DT", "2099.12.31"
                    )))
                    .retrieve()
                    .body(String.class);
            List<RentalNotice> notices = parser.items(body).stream().map(this::notice).toList();
            return notices.isEmpty() ? SampleData.rentalNotices() : notices;
        } catch (Exception e) {
            return SampleData.rentalNotices();
        }
    }

    public List<RentalSupply> supplies(RentalNotice notice) {
        if (!OpenApiUri.hasText(properties.getData().getServiceKey())) {
            throw new IllegalStateException("怨듦났?곗씠???쒕퉬???ㅺ? ?놁뒿?덈떎.");
        }

        if ("sample".equals(notice.source())) {
            return List.of();
        }

        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(
                            properties.getLh().getSupplyUrl(),
                            detailParams(notice)
                    ))
                    .retrieve()
                    .body(String.class);

            List<RentalSupply> supplies = parser.items(body)
                    .stream()
                    .map(this::supply)
                    .toList();

            return supplies;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "LH 怨듦툒?뺣낫 議고쉶 ?ㅽ뙣: noticeId=" + notice.noticeId(),
                    e
            );
        }
    }

    public RentalDetail detail(RentalNotice notice) {
        if (!OpenApiUri.hasText(properties.getData().getServiceKey()) || "sample".equals(notice.source())) {
            return SampleData.detail();
        }
        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(properties.getLh().getDetailUrl(), detailParams(notice)))
                    .retrieve()
                    .body(String.class);
            return parser.items(body).stream().findFirst().map(this::detail).orElseGet(SampleData::detail);
        } catch (Exception e) {
            return SampleData.detail();
        }
    }

    private Map<String, ?> detailParams(RentalNotice notice) {
        return Map.of(
                "ServiceKey", properties.getData().getServiceKey(),
                "serviceKey", properties.getData().getServiceKey(),
                "SPL_INF_TP_CD", firstText(notice.splInfTpCd(), "010"),
                "CCR_CNNT_SYS_DS_CD", firstText(notice.ccrCnntSysDsCd(), "01"),
                "PAN_ID", notice.noticeId(),
                "UPP_AIS_TP_CD", firstText(notice.uppAisTpCd(), "01"),
                "AIS_TP_CD", blankToEmpty(notice.aisTpCd())
        );
    }

    private RentalNotice notice(JsonNode node) {
        return new RentalNotice(
                text(node, "PAN_ID", "id"),
                text(node, "PAN_NM", "공공임대 모집공고"),
                text(node, "CNP_CD_NM", "전국"),
                text(node, "UPP_AIS_TP_NM", "임대주택"),
                text(node, "AIS_TP_CD_NM", "공공임대"),
                text(node, "PAN_SS", "공고중"),
                text(node, "PAN_NT_ST_DT", ""),
                text(node, "CLSG_DT", ""),
                text(node, "DTL_URL", ""),
                text(node, "CCR_CNNT_SYS_DS_CD", "01"),
                text(node, "UPP_AIS_TP_CD", "01"),
                text(node, "AIS_TP_CD", ""),
                text(node, "SPL_INF_TP_CD", "010"),
                "api"
        );
    }

    private RentalSupply supply(JsonNode node) {
        String houseType = text(node, "HTY_NNA", text(node, "HTYPE", text(node, "HOUSE_TY_NM", "")));
        String deposit = text(node, "LS_GMY", "");
        String rent = text(node, "RFE", "");
        String expectedAmount = deposit.isBlank() ? rent : "보증금 " + deposit + (rent.isBlank() ? "" : " / 월 " + rent);
        return new RentalSupply(
                text(node, "LND_US_DS_CD_NM", text(node, "SPL_INF_TP_NM", houseType)),
                text(node, "LGDN_DTL_ADR", text(node, "ADDR", text(node, "SBD_LGO_NM", ""))),
                text(node, "AR", text(node, "DDO_AR", "")),
                text(node, "SPL_XPC_AMT", expectedAmount),
                houseType,
                text(node, "NOW_HSH_CNT", text(node, "HSH_CNT", text(node, "SPL_HSH_CNT", "")))
        );
    }

    private RentalDetail detail(JsonNode node) {
        return new RentalDetail(
                text(node, "CTRT_PLC_ADR", text(node, "LGDN_ADR", "")),
                text(node, "CTRT_PLC_DTL_ADR", text(node, "LGDN_DTL_ADR", "")),
                text(node, "SBSC_ACP_ST_DT", ""),
                text(node, "SBSC_ACP_CLSG_DT", ""),
                text(node, "SIL_OFC_TLNO", "1600-1004")
        );
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText("");
        return value.isBlank() ? fallback : value;
    }

    private String firstText(String value, String fallback) {
        return OpenApiUri.hasText(value) ? value : fallback;
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
