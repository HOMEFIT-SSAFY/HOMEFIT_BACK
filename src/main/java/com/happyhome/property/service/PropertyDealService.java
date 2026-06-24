package com.happyhome.property.service;

import com.happyhome.house.dto.HouseDeal;
import com.happyhome.house.dto.HouseSearchCondition;
import com.happyhome.house.service.HouseDealService;
import com.happyhome.openapi.KakaoLocalApiClient;
import com.happyhome.openapi.dto.GeoCoordinate;
import com.happyhome.property.dao.PropertyDealMapper;
import com.happyhome.property.dto.PropertyDeal;
import com.happyhome.property.dto.PropertyDealType;
import com.happyhome.property.dto.PropertyDealSearchCondition;
import com.happyhome.property.dto.PropertyType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PropertyDealService {

    private final PropertyDealMapper propertyDealMapper;
    private final HouseDealService houseDealService;
    private final KakaoLocalApiClient kakaoLocalApiClient;

    public PropertyDealService(
            PropertyDealMapper propertyDealMapper,
            HouseDealService houseDealService,
            KakaoLocalApiClient kakaoLocalApiClient
    ) {
        this.propertyDealMapper = propertyDealMapper;
        this.houseDealService = houseDealService;
        this.kakaoLocalApiClient = kakaoLocalApiClient;
    }

    public List<PropertyDeal> search(PropertyDealSearchCondition condition) {
        if (condition.propertyType() == PropertyType.APARTMENT) {
            return apartmentDeals(condition);
        }
        if (condition.propertyType() == null) {
            List<PropertyDeal> combined = new ArrayList<>(apartmentDeals(condition));
            combined.addAll(enrichMissingCoordinates(propertyDealMapper.search(condition)));
            return combined.stream()
                    .sorted((left, right) -> value(right.dealDate()).compareTo(value(left.dealDate())))
                    .limit(condition.limit())
                    .toList();
        }
        return enrichMissingCoordinates(propertyDealMapper.search(condition));
    }

    private List<PropertyDeal> enrichMissingCoordinates(List<PropertyDeal> deals) {
        return deals.stream()
                .map(this::enrichMissingCoordinate)
                .toList();
    }

    private PropertyDeal enrichMissingCoordinate(PropertyDeal deal) {
        if (deal.latitude() != null && deal.longitude() != null) {
            return deal;
        }
        if (deal.propertyType() == PropertyType.APARTMENT) {
            return deal;
        }
        return kakaoLocalApiClient.geocode(geocodeQuery(deal))
                .map(coordinate -> withCoordinate(deal, coordinate))
                .orElse(deal);
    }

    private PropertyDeal withCoordinate(PropertyDeal deal, GeoCoordinate coordinate) {
        return new PropertyDeal(
                deal.propertyDealId(),
                deal.propertyType(),
                deal.dealType(),
                deal.sourceId(),
                deal.lawdCd(),
                deal.sidoName(),
                deal.gugunName(),
                deal.dongName(),
                deal.propertyName(),
                deal.dealDate(),
                deal.dealAmount(),
                deal.depositAmount(),
                deal.monthlyRentAmount(),
                deal.exclusiveArea(),
                deal.floor(),
                deal.buildYear(),
                deal.jibun(),
                coordinate.latitude(),
                coordinate.longitude(),
                deal.source()
        );
    }

    private String geocodeQuery(PropertyDeal deal) {
        return List.of(deal.sidoName(), deal.gugunName(), deal.dongName(), deal.jibun(), deal.propertyName()).stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .distinct()
                .reduce((left, right) -> left + " " + right)
                .orElse("");
    }

    private List<PropertyDeal> apartmentDeals(PropertyDealSearchCondition condition) {
        HouseSearchCondition houseCondition = new HouseSearchCondition();
        houseCondition.setKeyword(condition.keyword());
        houseCondition.setLawdCd(condition.lawdCd());
        houseCondition.setSidoName(condition.sidoName());
        houseCondition.setGugunName(condition.gugunName());
        houseCondition.setDongName(condition.dongName());
        houseCondition.setLimit(condition.limit());
        return houseDealService.search(houseCondition).stream()
                .map(this::fromHouseDeal)
                .toList();
    }

    private PropertyDeal fromHouseDeal(HouseDeal deal) {
        return new PropertyDeal(
                (long) deal.getNo(),
                PropertyType.APARTMENT,
                PropertyDealType.TRADE,
                "APARTMENT|" + deal.getNo(),
                "",
                deal.getSidoName(),
                deal.getGugunName(),
                deal.getDongName() != null ? deal.getDongName() : deal.getUmdName(),
                deal.getAptName(),
                deal.getDealDate(),
                deal.getDealAmount(),
                "",
                "",
                deal.getExclusiveArea() == null ? "" : deal.getExclusiveArea().toPlainString(),
                deal.getFloor(),
                deal.getBuildYear() == null ? "" : deal.getBuildYear().toString(),
                deal.getJibun(),
                parseDouble(deal.getLatitude()),
                parseDouble(deal.getLongitude()),
                "db"
        );
    }

    private Double parseDouble(String value) {
        try {
            return value == null || value.isBlank() ? null : Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
