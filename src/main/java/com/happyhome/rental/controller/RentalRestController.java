package com.happyhome.rental.controller;

import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals")
@Tag(name = "Public Rentals", description = "LH 공공임대 API")
public class RentalRestController {

    private final RentalService rentalService;

    public RentalRestController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @Operation(summary = "공공임대 공고 검색", description = "키워드, 지역코드, 공고 상태로 LH 공공임대 공고를 조회합니다.")
    @GetMapping
    public List<RentalNotice> rentals(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String regionCode,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return rentalService.notices(new RentalSearchCondition(keyword, regionCode, status, page, size));
    }

    @Operation(summary = "공공임대 공고 상세", description = "공고 ID로 상세 신청 정보와 공급 정보를 조회합니다.")
    @GetMapping("/{noticeId}")
    public RentalNoticeDetail rental(@PathVariable String noticeId) {
        return rentalService.detail(noticeId);
    }
}
