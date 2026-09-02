package com.seopulse.website.controller;

import com.seopulse.common.dto.PageResponse;
import com.seopulse.website.dto.AuditResponse;
import com.seopulse.website.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public PageResponse<AuditResponse> getAudits(Pageable pageable) {
        return auditService.getAudits(pageable);
    }
}