package com.happyhome.common.controller;

import com.happyhome.house.dto.HouseSearchCondition;
import com.happyhome.house.service.HouseDealService;
import com.happyhome.notice.service.NoticeService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HomeController {

    private final HouseDealService houseDealService;
    private final NoticeService noticeService;

    public HomeController(HouseDealService houseDealService, NoticeService noticeService) {
        this.houseDealService = houseDealService;
        this.noticeService = noticeService;
    }

    @GetMapping("/")
    public String root(Model model) {
        return home(new HouseSearchCondition(), model);
    }

    @GetMapping("/home")
    public String home(@ModelAttribute("condition") HouseSearchCondition condition, Model model) {
        List<?> trades = houseDealService.findRecent(5);
        model.addAttribute("condition", condition);
        model.addAttribute("trades", trades);
        model.addAttribute("popupNotices", noticeService.findPopupNotices(3));
        model.addAttribute("showingSearchResult", false);
        return "main";
    }
}
