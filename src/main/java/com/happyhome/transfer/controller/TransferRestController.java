package com.happyhome.transfer.controller;

import com.happyhome.transfer.dto.TransferDto;
import com.happyhome.transfer.dto.TransferRequest;
import com.happyhome.transfer.dto.TransferSearchCondition;
import com.happyhome.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Transfer post API")
public class TransferRestController {

    private final TransferService transferService;

    public TransferRestController(TransferService transferService) {
        this.transferService = transferService;
    }

    @Operation(summary = "List transfer posts")
    @GetMapping
    public List<TransferDto> transfers(@ModelAttribute TransferSearchCondition condition) {
        return transferService.findAll(condition);
    }

    @Operation(summary = "Get transfer post")
    @GetMapping("/{transferId}")
    public TransferDto transfer(@PathVariable int transferId) {
        return transferService.findById(transferId, true)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer post not found."));
    }

    @Operation(summary = "Create transfer post")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferDto create(@ModelAttribute TransferRequest request, Authentication authentication) {
        return transferService.create(request, resolveWriterId(authentication));
    }

    @Operation(summary = "Update transfer post")
    @PutMapping("/{transferId}")
    public TransferDto update(
            @PathVariable int transferId,
            @ModelAttribute TransferRequest request,
            Authentication authentication
    ) {
        return transferService.update(transferId, request, resolveWriterId(authentication))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer post not found."));
    }

    @Operation(summary = "Delete transfer post")
    @DeleteMapping("/{transferId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int transferId, Authentication authentication) {
        transferService.delete(transferId, resolveWriterId(authentication))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer post not found."));
    }

    private String resolveWriterId(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        return authentication.getName();
    }
}
