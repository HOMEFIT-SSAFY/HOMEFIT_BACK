package com.happyhome.property.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.happyhome.batch.dto.NoticeLHResult;
import com.happyhome.openapi.RtmsOpenApiClient;
import com.happyhome.property.dao.PropertyDealMapper;
import com.happyhome.property.dto.PropertyDeal;
import com.happyhome.property.dto.PropertyDealType;
import com.happyhome.property.dto.PropertyType;
import java.util.List;
import org.junit.jupiter.api.Test;

class PropertyDealSyncServiceTest {

    @Test
    void syncsOfficetelAndOneroomDealsIntoDatabase() {
        RtmsOpenApiClient client = mock(RtmsOpenApiClient.class);
        PropertyDealMapper mapper = mock(PropertyDealMapper.class);
        PropertyDeal officetel = deal(PropertyType.OFFICETEL, PropertyDealType.TRADE, "테스트오피스텔");
        PropertyDeal oneroom = deal(PropertyType.ONEROOM, PropertyDealType.RENT, "단독다가구");
        when(client.isConfigured()).thenReturn(true);
        when(client.fetch(PropertyType.OFFICETEL, PropertyDealType.TRADE, "11680", "202606"))
                .thenReturn(List.of(officetel));
        when(client.fetch(PropertyType.OFFICETEL, PropertyDealType.RENT, "11680", "202606"))
                .thenReturn(List.of());
        when(client.fetch(PropertyType.ONEROOM, PropertyDealType.TRADE, "11680", "202606"))
                .thenReturn(List.of());
        when(client.fetch(PropertyType.ONEROOM, PropertyDealType.RENT, "11680", "202606"))
                .thenReturn(List.of(oneroom));

        PropertyDealSyncService service = new PropertyDealSyncService(client, mapper);

        NoticeLHResult result = service.sync(List.of("11680"), List.of("202606"));

        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.fetchedCount()).isEqualTo(2);
        assertThat(result.savedCount()).isEqualTo(2);
    }

    private PropertyDeal deal(PropertyType propertyType, PropertyDealType dealType, String name) {
        return new PropertyDeal(
                null,
                propertyType,
                dealType,
                propertyType + "-" + dealType + "-11680-202606-1",
                "11680",
                "서울특별시",
                "강남구",
                "역삼동",
                name,
                "2026-06-01",
                "120,000",
                "",
                "",
                "42.10",
                "10",
                "2020",
                "",
                null,
                null,
                "api"
        );
    }
}
