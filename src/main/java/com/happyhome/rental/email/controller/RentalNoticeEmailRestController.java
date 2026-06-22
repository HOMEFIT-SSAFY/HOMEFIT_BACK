package com.happyhome.rental.email.controller;

import com.happyhome.rental.email.service.RentalNoticeEmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rentals/favorites/emails")
public class RentalNoticeEmailRestController {

    private final RentalNoticeEmailService rentalNoticeEmailService;

    public RentalNoticeEmailRestController(RentalNoticeEmailService rentalNoticeEmailService) {
        this.rentalNoticeEmailService = rentalNoticeEmailService;
    }

    @PostMapping("/send")
    public ResponseEntity<RentalNoticeEmailService.EmailRunResult> send(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(rentalNoticeEmailService.sendFavoriteNoticeEmails(authentication.getName()));
    }
}
