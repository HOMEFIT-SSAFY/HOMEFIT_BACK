package com.happyhome.rental.controller;

import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.service.RentalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping("/rentals")
    public String rentals(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String regionCode,
            @RequestParam(defaultValue = "공고중") String status,
            Model model
    ) {
        RentalSearchCondition condition = new RentalSearchCondition(keyword, regionCode, status, 1, 12);
        model.addAttribute("condition", condition);
        model.addAttribute("notices", rentalService.notices(condition));
        return "rentals";
    }

    @GetMapping("/rentals/{noticeId}")
    public String detail(@PathVariable String noticeId, Model model) {
        RentalNoticeDetail detail = rentalService.detail(noticeId);
        model.addAttribute("detail", detail);
        return "rental-detail";
    }
}
