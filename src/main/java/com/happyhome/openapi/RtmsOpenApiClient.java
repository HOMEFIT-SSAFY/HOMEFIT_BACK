package com.happyhome.openapi;

import com.happyhome.config.OpenApiProperties;
import com.happyhome.property.dto.PropertyDeal;
import com.happyhome.property.dto.PropertyDealType;
import com.happyhome.property.dto.PropertyType;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class RtmsOpenApiClient {

    private static final Logger log = LoggerFactory.getLogger(RtmsOpenApiClient.class);

    private final OpenApiProperties properties;
    private final RestClient restClient;

    public RtmsOpenApiClient(OpenApiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.create();
    }

    public boolean isConfigured() {
        return OpenApiUri.hasText(properties.getData().getServiceKey());
    }

    public List<PropertyDeal> fetch(PropertyType propertyType, PropertyDealType dealType, String lawdCd, String dealYmd) {
        if (!isConfigured()) {
            return List.of();
        }
        String url = url(propertyType, dealType);
        if (!OpenApiUri.hasText(url)) {
            return List.of();
        }
        try {
            String body = restClient.get()
                    .uri(OpenApiUri.build(url, Map.of(
                            "serviceKey", properties.getData().getServiceKey(),
                            "LAWD_CD", lawdCd,
                            "DEAL_YMD", dealYmd
                    )))
                    .retrieve()
                    .body(String.class);
            return parse(body, propertyType, dealType, lawdCd, dealYmd);
        } catch (Exception exception) {
            log.warn("RTMS API failed. propertyType={}, dealType={}, lawdCd={}, dealYmd={}, message={}",
                    propertyType, dealType, lawdCd, dealYmd, exception.getMessage());
            return List.of();
        }
    }

    List<PropertyDeal> parse(String xml, PropertyType propertyType, PropertyDealType dealType, String lawdCd, String dealYmd) {
        if (!OpenApiUri.hasText(xml)) {
            return List.of();
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            NodeList nodes = document.getElementsByTagName("item");
            List<PropertyDeal> deals = new ArrayList<>();
            for (int index = 0; index < nodes.getLength(); index++) {
                Node node = nodes.item(index);
                if (node instanceof Element element) {
                    deals.add(toDeal(element, propertyType, dealType, lawdCd, dealYmd, index));
                }
            }
            return deals;
        } catch (Exception exception) {
            log.warn("RTMS XML parse failed: {}", exception.getMessage());
            return List.of();
        }
    }

    private PropertyDeal toDeal(
            Element element,
            PropertyType propertyType,
            PropertyDealType dealType,
            String lawdCd,
            String dealYmd,
            int index
    ) {
        String year = firstText(element, "년", "dealYear");
        String month = firstText(element, "월", "dealMonth");
        String day = firstText(element, "일", "dealDay");
        String dealDate = formatDealDate(firstText(year, dealYmd.substring(0, 4)), month, day, dealYmd);
        String dongName = normalize(firstText(element, "법정동", "umdNm", "dongName"));
        String propertyName = normalize(firstText(
                element,
                "단지명",
                "단지",
                "오피스텔명",
                "offiNm",
                "offiName",
                "officetelName",
                "오피스텔",
                "건물명",
                "건물명칭",
                "주택명",
                "연립다세대명",
                "연립다세대",
                "아파트",
                "aptNm",
                "houseNm",
                "buildingName"
        ));
        if (!OpenApiUri.hasText(propertyName)) {
            propertyName = fallbackPropertyName(propertyType, dongName, normalize(firstText(element, "지번", "jibun")));
        }
        String sourceId = String.join("|",
                propertyType.name(),
                dealType.name(),
                lawdCd,
                dealYmd,
                value(dongName),
                value(propertyName),
                value(firstText(element, "지번", "jibun")),
                value(dealDate),
                value(firstText(element, "전용면적", "건물면적", "연면적", "excluUseAr")),
                value(firstText(element, "층", "floor")),
                String.valueOf(index)
        );
        return new PropertyDeal(
                null,
                propertyType,
                dealType,
                sourceId,
                lawdCd,
                "",
                "",
                dongName,
                propertyName,
                dealDate,
                normalize(firstText(element, "거래금액", "dealAmount")),
                normalize(firstText(element, "보증금액", "deposit", "depositAmount")),
                normalize(firstText(element, "월세금액", "monthlyRent", "monthlyRentAmount")),
                normalize(firstText(element, "전용면적", "건물면적", "연면적", "excluUseAr")),
                normalize(firstText(element, "층", "floor")),
                normalize(firstText(element, "건축년도", "건축년", "buildYear")),
                normalize(firstText(element, "지번", "jibun")),
                null,
                null,
                "api"
        );
    }

    private String url(PropertyType propertyType, PropertyDealType dealType) {
        if (propertyType == PropertyType.OFFICETEL && dealType == PropertyDealType.TRADE) {
            return properties.getRtms().getOfficetelTradeUrl();
        }
        if (propertyType == PropertyType.OFFICETEL && dealType == PropertyDealType.RENT) {
            return properties.getRtms().getOfficetelRentUrl();
        }
        if (propertyType == PropertyType.ONEROOM && dealType == PropertyDealType.TRADE) {
            return properties.getRtms().getOneroomTradeUrl();
        }
        if (propertyType == PropertyType.ONEROOM && dealType == PropertyDealType.RENT) {
            return properties.getRtms().getOneroomRentUrl();
        }
        return "";
    }

    private String firstText(Element element, String... names) {
        for (String name : names) {
            NodeList nodes = element.getElementsByTagName(name);
            if (nodes.getLength() > 0) {
                String value = nodes.item(0).getTextContent();
                if (OpenApiUri.hasText(value)) {
                    return value;
                }
            }
        }
        return "";
    }

    private String firstText(String value, String fallback) {
        return OpenApiUri.hasText(value) ? value : fallback;
    }

    private String formatDealDate(String year, String month, String day, String dealYmd) {
        if (!OpenApiUri.hasText(month) || !OpenApiUri.hasText(day)) {
            return dealYmd.substring(0, 4) + "-" + dealYmd.substring(4, 6) + "-01";
        }
        return "%04d-%02d-%02d".formatted(
                integer(year, Integer.parseInt(dealYmd.substring(0, 4))),
                integer(month, Integer.parseInt(dealYmd.substring(4, 6))),
                integer(day, 1)
        );
    }

    private int integer(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception exception) {
            return fallback;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String fallbackPropertyName(PropertyType propertyType, String dongName, String jibun) {
        String typeLabel = propertyType == PropertyType.ONEROOM ? "원룸" : "오피스텔";
        String location = String.join(" ", List.of(dongName, jibun).stream()
                .filter(OpenApiUri::hasText)
                .map(String::trim)
                .toList());
        if (OpenApiUri.hasText(location)) {
            return location + " " + typeLabel;
        }
        return typeLabel;
    }

    private String value(String value) {
        return OpenApiUri.hasText(value) ? value.trim() : "-";
    }
}
