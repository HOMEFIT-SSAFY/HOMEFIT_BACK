package com.happyhome.favorite.controller;

import com.happyhome.favorite.service.FavoriteDealService;
import com.happyhome.house.dto.HouseDeal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/favorites")
@Tag(name = "Favorite Deals", description = "Favorite house deal API")
public class FavoriteDealRestController {

    private final FavoriteDealService favoriteDealService;

    public FavoriteDealRestController(FavoriteDealService favoriteDealService) {
        this.favoriteDealService = favoriteDealService;
    }

    @Operation(summary = "List current member favorites")
    @GetMapping
    public ResponseEntity<List<HouseDeal>> favorites(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(favoriteDealService.findFavoriteDeals(authentication.getName(), 50));
    }

    @Operation(summary = "Toggle favorite deal")
    @PostMapping("/{dealNo}/toggle")
    public ResponseEntity<Map<String, Object>> toggle(@PathVariable int dealNo, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean favorite = favoriteDealService.toggle(authentication.getName(), dealNo);
        return ResponseEntity.ok(Map.of("dealNo", dealNo, "favorite", favorite));
    }
}
