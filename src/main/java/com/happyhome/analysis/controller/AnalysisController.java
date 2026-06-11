package com.happyhome.analysis.controller;

import com.happyhome.config.OpenApiProperties;
import com.happyhome.analysis.service.AnalysisService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AnalysisController {

    private final AnalysisService analysisService;
    private final OpenApiProperties properties;

    public AnalysisController(AnalysisService analysisService, OpenApiProperties properties) {
        this.analysisService = analysisService;
        this.properties = properties;
    }

    @GetMapping("/analysis")
    public String analysis(
            @RequestParam(defaultValue = "관악구 봉천동") String label,
            @RequestParam(defaultValue = "126.9413") double longitude,
            @RequestParam(defaultValue = "37.4826") double latitude,
            @RequestParam(defaultValue = "1000") int radius,
            Model model
    ) {
        model.addAttribute("analysis", analysisService.analyze(label, longitude, latitude, radius));
        model.addAttribute("kakaoKey", properties.getKakao().getJavascriptKey());
        return "analysis";
    }
}
