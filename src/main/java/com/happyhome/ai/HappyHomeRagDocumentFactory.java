package com.happyhome.ai;

import com.happyhome.house.dto.HouseDeal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

@Component
public class HappyHomeRagDocumentFactory {

    public List<Document> fromHouseDeals(List<HouseDeal> deals) {
        if (deals == null || deals.isEmpty()) {
            return List.of();
        }

        List<Document> documents = new ArrayList<>();
        for (HouseDeal deal : deals) {
            if (deal == null) {
                continue;
            }
            documents.add(toDocument(deal));
        }
        return documents;
    }

    private Document toDocument(HouseDeal deal) {
        String region = join(" ", deal.getSidoName(), deal.getGugunName(),
                deal.getDongName() != null ? deal.getDongName() : deal.getUmdName());
        String text = """
                HappyHome apartment transaction document.
                Apartment: %s
                Address: %s
                Region: %s
                Deal date: %s
                Deal amount: %s
                Exclusive area: %s square meters
                Floor: %s
                Build year: %s
                Latitude: %s
                Longitude: %s
                """.formatted(
                value(deal.getAptName()),
                value(deal.getAddress()),
                value(region),
                value(deal.getDealDate()),
                value(deal.getDealAmount()),
                value(deal.getExclusiveArea()),
                value(deal.getFloor()),
                value(deal.getBuildYear()),
                value(deal.getLatitude()),
                value(deal.getLongitude())
        );

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "house_deal");
        metadata.put("dealNo", deal.getNo());
        metadata.put("aptName", value(deal.getAptName()));
        metadata.put("region", value(region));
        metadata.put("dealDate", value(deal.getDealDate()));

        return Document.builder()
                .text(text)
                .metadata(metadata)
                .build();
    }

    private String join(String delimiter, String... values) {
        List<String> parts = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                parts.add(value.trim());
            }
        }
        return String.join(delimiter, parts);
    }

    private String value(Object value) {
        if (value == null) {
            return "unknown";
        }
        String text = value.toString();
        return text.isBlank() ? "unknown" : text;
    }
}
