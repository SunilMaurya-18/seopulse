package com.seopulse.website.controller;

import com.seopulse.common.dto.PageResponse;
import com.seopulse.website.dto.AuditPageResponse;
import com.seopulse.website.dto.AuditResponse;
import com.seopulse.website.dto.SeoIssueResponse;
import com.seopulse.website.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;


    // ============================================================
    // CREATE AUDIT
    // ============================================================

    @PostMapping("/websites/{websiteId}/audits")
    public ResponseEntity<AuditResponse> createAudit(

            @PathVariable Long projectId,

            @PathVariable Long websiteId

    ) {

        AuditResponse response =
                auditService.createAudit(
                        projectId,
                        websiteId
                );

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // GET WEBSITE AUDITS
    // ============================================================

    @GetMapping("/websites/{websiteId}/audits")
    public ResponseEntity<PageResponse<AuditResponse>> getAudits(

            @PathVariable Long projectId,

            @PathVariable Long websiteId,

            @PageableDefault(size = 20)
            Pageable pageable

    ) {

        PageResponse<AuditResponse> response =
                auditService.getAudits(
                        projectId,
                        websiteId,
                        pageable
                );

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // GET SINGLE AUDIT
    // ============================================================

    @GetMapping("/audits/{auditId}")
    public ResponseEntity<AuditResponse> getAudit(

            @PathVariable Long projectId,

            @PathVariable Long auditId

    ) {

        AuditResponse response =
                auditService.getAudit(
                        projectId,
                        auditId
                );

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // GET AUDIT PAGES
    // ============================================================

    @GetMapping("/audits/{auditId}/pages")
    public ResponseEntity<PageResponse<AuditPageResponse>> getAuditPages(

            @PathVariable Long projectId,

            @PathVariable Long auditId,

            @PageableDefault(size = 20)
            Pageable pageable

    ) {

        PageResponse<AuditPageResponse> response =
                auditService.getAuditPages(
                        projectId,
                        auditId,
                        pageable
                );

        return ResponseEntity.ok(response);
    }


    // ============================================================
    // GET SEO ISSUES
    // ============================================================

    @GetMapping("/audits/{auditId}/issues")
    public ResponseEntity<PageResponse<SeoIssueResponse>> getAuditIssues(

            @PathVariable Long projectId,

            @PathVariable Long auditId,

            @RequestParam(required = false)
            String severity,

            @RequestParam(required = false)
            String ruleCode,

            @PageableDefault(size = 20)
            Pageable pageable

    ) {

        PageResponse<SeoIssueResponse> response =
                auditService.getAuditIssues(
                        projectId,
                        auditId,
                        severity,
                        ruleCode,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

}