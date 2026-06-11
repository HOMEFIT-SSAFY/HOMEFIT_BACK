package com.happyhome.batch.controller;

import com.happyhome.batch.dto.NoticeLHResult;
import com.happyhome.batch.service.NoticeLHService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
@Tag(name = "Admin Batch", description = "Open API batch manual execution")
public class LHController {

    private final NoticeLHService noticeLHService;

    @Operation(summary = "Run LH notice sync batch manually")
    @PostMapping("/lh-notices")
    public NoticeLHResult syncNoticeLH() {
        return noticeLHService.runSync();
    }
}