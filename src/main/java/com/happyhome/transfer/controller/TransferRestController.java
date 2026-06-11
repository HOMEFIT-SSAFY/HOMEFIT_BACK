package com.happyhome.transfer.controller;

import com.happyhome.member.dto.MemberDto;
import com.happyhome.transfer.dto.TransferDto;
import com.happyhome.transfer.dto.TransferRequest;
import com.happyhome.transfer.dto.TransferSearchCondition;
import com.happyhome.transfer.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
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
@Tag(name = "Transfers", description = "양도 게시판 API")
public class TransferRestController {

    private final TransferService transferService;

    public TransferRestController(TransferService transferService) {
        this.transferService = transferService;
    }

    @Operation(summary = "양도글 목록")
    @GetMapping
    public List<TransferDto> transfers(@ModelAttribute TransferSearchCondition condition) {
        return transferService.findAll(condition);
    }

    @Operation(summary = "양도글 상세")
    @GetMapping("/{transferId}")
    public TransferDto transfer(@PathVariable int transferId) {
        return transferService.findById(transferId, true)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "양도글을 찾을 수 없습니다."));
    }

    @Operation(summary = "양도글 작성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferDto create(@ModelAttribute TransferRequest request, HttpSession session) {
        return transferService.create(request, resolveWriterId(session));
    }

    @Operation(summary = "양도글 수정")
    @PutMapping("/{transferId}")
    public TransferDto update(@PathVariable int transferId, @ModelAttribute TransferRequest request, HttpSession session) {
        return transferService.update(transferId, request, resolveWriterId(session))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "양도글을 찾을 수 없습니다."));
    }

    @Operation(summary = "양도글 삭제")
    @DeleteMapping("/{transferId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int transferId) {
        transferService.delete(transferId);
    }

    private String resolveWriterId(HttpSession session) {
        MemberDto member = (MemberDto) session.getAttribute("loginMember");
        return member == null ? "api" : member.getUserId();
    }
}
