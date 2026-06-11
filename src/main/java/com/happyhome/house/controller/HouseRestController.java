package com.happyhome.house.controller;

import com.happyhome.house.dto.HouseDeal;
import com.happyhome.house.dto.HouseSearchCondition;
import com.happyhome.region.dto.RegionOptionDto;
import com.happyhome.house.service.HouseDealService;
import com.happyhome.region.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Housing Deals", description = "아파트 실거래가 API")
public class HouseRestController {

    private final HouseDealService houseDealService;
    private final RegionService regionService;

    public HouseRestController(HouseDealService houseDealService, RegionService regionService) {
        this.houseDealService = houseDealService;
        this.regionService = regionService;
    }

    @Operation(summary = "실거래 검색", description = "지역, 아파트명, 거래연도 조건으로 실거래를 검색합니다.")
    @GetMapping({"/houses", "/prices", "/trades"})
    public List<HouseDeal> houses(@ModelAttribute HouseSearchCondition condition) {
        return houseDealService.search(condition);
    }

    @Operation(summary = "최근 실거래", description = "최근 등록된 아파트 실거래 목록을 조회합니다.")
    @GetMapping("/houses/recent")
    public List<HouseDeal> recentHouses(
            @Parameter(description = "조회 개수") @RequestParam(defaultValue = "5") int limit
    ) {
        return houseDealService.findRecent(limit);
    }

    @Operation(summary = "실거래 단건 조회", description = "거래 번호로 실거래 상세 정보를 조회합니다.")
    @GetMapping({"/houses/{no}", "/prices/{no}", "/trades/{no}"})
    public ResponseEntity<HouseDeal> house(@PathVariable int no) {
        return houseDealService.findByNo(no)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "시도 목록", description = "실거래 검색에 사용할 시도 목록을 조회합니다.")
    @GetMapping("/regions/sidos")
    public List<RegionOptionDto> sidos() {
        return regionService.findSidos();
    }

    @Operation(summary = "구군 목록", description = "선택한 시도에 속한 구군 목록을 조회합니다.")
    @GetMapping("/regions/guguns")
    public List<RegionOptionDto> guguns(@RequestParam String sidoName) {
        return regionService.findGuguns(sidoName);
    }

    @Operation(summary = "동 목록", description = "선택한 시도/구군에 속한 동 목록을 조회합니다.")
    @GetMapping("/regions/dongs")
    public List<RegionOptionDto> dongs(@RequestParam String sidoName, @RequestParam String gugunName) {
        return regionService.findDongs(sidoName, gugunName);
    }
}
