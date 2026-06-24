package com.happyhome.property.controller;

import com.happyhome.property.dto.PropertyDeal;
import com.happyhome.property.dto.PropertyDealSearchCondition;
import com.happyhome.property.dto.PropertyDealType;
import com.happyhome.property.dto.PropertyType;
import com.happyhome.property.service.PropertyDealService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/property-deals")
public class PropertyDealRestController {

    private final PropertyDealService propertyDealService;

    public PropertyDealRestController(PropertyDealService propertyDealService) {
        this.propertyDealService = propertyDealService;
    }

    @GetMapping
    public List<PropertyDeal> deals(
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) PropertyDealType dealType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String lawdCd,
            @RequestParam(required = false) String sidoName,
            @RequestParam(required = false) String gugunName,
            @RequestParam(required = false) String dongName,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return propertyDealService.search(new PropertyDealSearchCondition(
                propertyType,
                dealType,
                keyword,
                lawdCd,
                sidoName,
                gugunName,
                dongName,
                limit
        ));
    }
}
