package com.happyhome.transfer.favorite.controller;

import com.happyhome.transfer.dto.TransferDto;
import com.happyhome.transfer.favorite.service.FavoriteTransferService;
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
@RequestMapping("/api/transfers")
public class FavoriteTransferRestController {

    private final FavoriteTransferService favoriteTransferService;

    public FavoriteTransferRestController(FavoriteTransferService favoriteTransferService) {
        this.favoriteTransferService = favoriteTransferService;
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<TransferDto>> favorites(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(favoriteTransferService.findFavorites(authentication.getName(), 100));
    }

    @PostMapping("/{transferId}/favorite/toggle")
    public ResponseEntity<Map<String, Object>> toggle(
            @PathVariable int transferId,
            Authentication authentication
    ) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean favorite = favoriteTransferService.toggle(authentication.getName(), transferId);
        return ResponseEntity.ok(Map.of("transferId", transferId, "favorite", favorite));
    }
}
