package com.seopulse.website.controller;

import com.seopulse.common.dto.PageResponse;
import com.seopulse.common.security.CurrentUserService;
import com.seopulse.website.dto.AuditResponse;
import com.seopulse.website.service.AuditService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        "/api/v1/projects/{projectId}/websites/{websiteId}/audits"
)
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<AuditResponse> createAudit(
            @PathVariable Long projectId,
            @PathVariable Long websiteId,
            Authentication authentication
    ) {

        Long userId =
                currentUserService.getUserId(authentication);

        AuditResponse response =
                auditService.createAudit(
                        projectId,
                        websiteId,
                        userId
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public PageResponse<AuditResponse> getAudits(
            @PathVariable Long projectId,
            @PathVariable Long websiteId,
            Pageable pageable,
            Authentication authentication
    ) {

        Long userId =
                currentUserService.getUserId(authentication);

        return auditService.getAudits(
                projectId,
                websiteId,
                userId,
                pageable
        );
    }
}