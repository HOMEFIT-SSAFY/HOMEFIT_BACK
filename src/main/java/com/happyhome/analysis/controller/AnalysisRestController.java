package com.happyhome.analysis.controller;

import com.happyhome.analysis.dto.HousingAnalysis;
import com.happyhome.analysis.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@Tag(name = "Life Analysis", description = "생활권 분석 API")
public class AnalysisRestController {

    private final AnalysisService analysisService;

    public AnalysisRestController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @Operation(summary = "생활권 분석", description = "좌표와 반경을 기준으로 상권 편의성과 교통 리스크를 분석합니다.")
    @GetMapping
    public HousingAnalysis analyze(
            @RequestParam(defaultValue = "관악구 봉천동") String label,
            @RequestParam(defaultValue = "126.9413") double longitude,
            @RequestParam(defaultValue = "37.4826") double latitude,
            @RequestParam(defaultValue = "1000") int radius
    ) {
        return analysisService.analyze(label, longitude, latitude, radius);
    }
}
