package com.happyhome.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.house.dto.HouseDeal;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

class HappyHomeRagDocumentFactoryTest {

    private final HappyHomeRagDocumentFactory factory = new HappyHomeRagDocumentFactory();

    @Test
    void createsSearchableDocumentsFromHouseDeals() {
        HouseDeal deal = new HouseDeal();
        deal.setNo(7);
        deal.setAptName("Happy Palace");
        deal.setSidoName("Seoul");
        deal.setGugunName("Gangnam-gu");
        deal.setDongName("Yeoksam-dong");
        deal.setJibun("123-4");
        deal.setBuildYear(2018);
        deal.setDealYear(2026);
        deal.setDealMonth(5);
        deal.setDealDay(19);
        deal.setExclusiveArea(new BigDecimal("84.91"));
        deal.setDealAmount("120,000");
        deal.setFloor("12");

        List<Document> documents = factory.fromHouseDeals(List.of(deal));

        assertThat(documents).hasSize(1);
        Document document = documents.get(0);
        assertThat(document.getText()).contains("Happy Palace", "Seoul Gangnam-gu Yeoksam-dong 123-4", "120,000");
        assertThat(document.getMetadata())
                .containsEntry("source", "house_deal")
                .containsEntry("dealNo", 7)
                .containsEntry("aptName", "Happy Palace")
                .containsEntry("region", "Seoul Gangnam-gu Yeoksam-dong");
    }
}
