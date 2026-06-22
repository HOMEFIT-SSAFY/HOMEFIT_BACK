package com.happyhome.rental.recommendation.controller;

import com.happyhome.rental.recommendation.service.RentalRecommendationService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals/recommendations")
public class RentalRecommendationRestController {

    private final RentalRecommendationService rentalRecommendationService;

    public RentalRecommendationRestController(RentalRecommendationService rentalRecommendationService) {
        this.rentalRecommendationService = rentalRecommendationService;
    }

    @GetMapping
    public ResponseEntity<?> recommendations(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(rentalRecommendationService.recommend(authentication.getName(), Math.min(limit, 30)));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
