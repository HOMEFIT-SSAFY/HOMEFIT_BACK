package com.happyhome.property.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.happyhome.house.service.HouseDealService;
import com.happyhome.openapi.KakaoLocalApiClient;
import com.happyhome.openapi.dto.GeoCoordinate;
import com.happyhome.property.dao.PropertyDealMapper;
import com.happyhome.property.dto.PropertyDeal;
import com.happyhome.property.dto.PropertyDealSearchCondition;
import com.happyhome.property.dto.PropertyDealType;
import com.happyhome.property.dto.PropertyType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PropertyDealServiceTest {

    @Test
    void fillsMissingCoordinatesForOfficetelDealsBeforeReturningSearchResults() {
        PropertyDealMapper propertyDealMapper = mock(PropertyDealMapper.class);
        HouseDealService houseDealService = mock(HouseDealService.class);
        KakaoLocalApiClient kakaoLocalApiClient = mock(KakaoLocalApiClient.class);
        PropertyDeal dealWithoutCoordinate = new PropertyDeal(
                22L,
                PropertyType.OFFICETEL,
                PropertyDealType.RENT,
                "OFFICETEL|RENT|11680|202606|1",
                "11680",
                "서울특별시",
                "강남구",
                "역삼동",
                "역삼동하나빌",
                "2026-06-23",
                "",
                "500",
                "39",
                "30.73",
                "4",
                "2020",
                "1008",
                null,
                null,
                "api"
        );
        PropertyDealSearchCondition condition = new PropertyDealSearchCondition(
                PropertyType.OFFICETEL,
                null,
                null,
                null,
                null,
                null,
                null,
                20
        );
        when(propertyDealMapper.search(condition)).thenReturn(List.of(dealWithoutCoordinate));
        when(kakaoLocalApiClient.geocode("서울특별시 강남구 역삼동 1008 역삼동하나빌"))
                .thenReturn(Optional.of(new GeoCoordinate(37.5006, 127.0365)));

        PropertyDealService service = new PropertyDealService(
                propertyDealMapper,
                houseDealService,
                kakaoLocalApiClient
        );

        List<PropertyDeal> results = service.search(condition);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).latitude()).isEqualTo(37.5006);
        assertThat(results.get(0).longitude()).isEqualTo(127.0365);
    }
}
