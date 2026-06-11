package com.happyhome.favorite.controller;

import com.happyhome.house.dto.HouseDeal;
import com.happyhome.member.dto.MemberDto;
import com.happyhome.favorite.service.FavoriteDealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favorite Deals", description = "관심매물 API")
public class FavoriteDealRestController {

    private final FavoriteDealService favoriteDealService;

    public FavoriteDealRestController(FavoriteDealService favoriteDealService) {
        this.favoriteDealService = favoriteDealService;
    }

    @Operation(summary = "내 관심매물 목록", description = "현재 로그인한 회원의 관심매물 실거래 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<HouseDeal>> favorites(HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(favoriteDealService.findFavoriteDeals(loginMember.getUserId(), 50));
    }

    @Operation(summary = "관심매물 토글", description = "거래 번호를 관심매물에 추가하거나 이미 있으면 삭제합니다.")
    @PostMapping("/{dealNo}/toggle")
    public ResponseEntity<Map<String, Object>> toggle(@PathVariable int dealNo, HttpSession session) {
        MemberDto loginMember = (MemberDto) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean favorite = favoriteDealService.toggle(loginMember.getUserId(), dealNo);
        return ResponseEntity.ok(Map.of("dealNo", dealNo, "favorite", favorite));
    }
}
