package com.happyhome.notice.controller;

import com.happyhome.notice.dto.NoticeDto;
import com.happyhome.notice.dto.NoticeRequest;
import com.happyhome.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notices")
@Tag(name = "Notices", description = "공지사항 API")
public class NoticeRestController {

    private final NoticeService noticeService;

    public NoticeRestController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Operation(summary = "공지사항 목록", description = "노출 중인 공지사항을 최신순으로 조회합니다.")
    @GetMapping
    public List<NoticeDto> notices(@RequestParam(defaultValue = "50") int limit) {
        return noticeService.findVisible(limit);
    }

    @Operation(summary = "팝업 공지 목록", description = "메인 팝업으로 표시할 공지사항을 조회합니다.")
    @GetMapping("/popups")
    public List<NoticeDto> popups(@RequestParam(defaultValue = "3") int limit) {
        return noticeService.findPopupNotices(limit);
    }

    @Operation(summary = "공지사항 상세")
    @GetMapping("/{noticeId}")
    public NoticeDto notice(@PathVariable int noticeId) {
        return noticeService.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."));
    }

    @Operation(summary = "공지사항 작성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoticeDto create(@RequestBody NoticeRequest request) {
        return noticeService.create(request, "api");
    }

    @Operation(summary = "공지사항 수정")
    @PutMapping("/{noticeId}")
    public NoticeDto update(@PathVariable int noticeId, @RequestBody NoticeRequest request) {
        return noticeService.update(noticeId, request, "api")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공지사항을 찾을 수 없습니다."));
    }

    @Operation(summary = "공지사항 삭제")
    @DeleteMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int noticeId) {
        noticeService.delete(noticeId);
    }
}
