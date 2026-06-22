package com.happyhome.rental.favorite.controller;

import com.happyhome.rental.dto.RentalNoticeDetail;
import com.happyhome.rental.favorite.service.FavoriteRentalNoticeService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals")
public class FavoriteRentalNoticeRestController {

    private final FavoriteRentalNoticeService favoriteRentalNoticeService;

    public FavoriteRentalNoticeRestController(FavoriteRentalNoticeService favoriteRentalNoticeService) {
        this.favoriteRentalNoticeService = favoriteRentalNoticeService;
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<RentalNoticeDetail>> favorites(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(favoriteRentalNoticeService.findFavorites(authentication.getName(), 100));
    }

    @PostMapping("/{noticeId}/favorite/toggle")
    public ResponseEntity<Map<String, Object>> toggle(
            @PathVariable String noticeId,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean favorite = favoriteRentalNoticeService.toggle(authentication.getName(), noticeId);
        return ResponseEntity.ok(Map.of("noticeId", noticeId, "favorite", favorite));
    }
}
