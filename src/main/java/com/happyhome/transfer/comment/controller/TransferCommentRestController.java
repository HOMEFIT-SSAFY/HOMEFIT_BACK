package com.happyhome.transfer.comment.controller;

import com.happyhome.transfer.comment.dto.TransferCommentDto;
import com.happyhome.transfer.comment.dto.TransferCommentRequest;
import com.happyhome.transfer.comment.service.TransferCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfer Comments", description = "Transfer post comment API")
public class TransferCommentRestController {

    private final TransferCommentService transferCommentService;

    public TransferCommentRestController(TransferCommentService transferCommentService) {
        this.transferCommentService = transferCommentService;
    }

    @Operation(summary = "List transfer comments")
    @GetMapping("/{transferId}/comments")
    public List<TransferCommentDto> comments(@PathVariable int transferId) {
        return transferCommentService.findByTransferId(transferId);
    }

    @Operation(summary = "Create transfer comment")
    @PostMapping("/{transferId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferCommentDto create(
            @PathVariable int transferId,
            @RequestBody TransferCommentRequest request,
            Authentication authentication
    ) {
        return transferCommentService.create(transferId, request, resolveWriterId(authentication));
    }

    @Operation(summary = "Update transfer comment")
    @PutMapping("/comments/{commentId}")
    public TransferCommentDto update(
            @PathVariable int commentId,
            @RequestBody TransferCommentRequest request,
            Authentication authentication
    ) {
        return transferCommentService.update(commentId, request, resolveWriterId(authentication))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found."));
    }

    @Operation(summary = "Delete transfer comment")
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int commentId, Authentication authentication) {
        transferCommentService.delete(commentId, resolveWriterId(authentication))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found."));
    }

    private String resolveWriterId(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        return authentication.getName();
    }
}
