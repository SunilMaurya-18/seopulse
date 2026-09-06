package com.seopulse.website.service;

import com.seopulse.common.dto.PageResponse;
import com.seopulse.common.exception.DuplicateResourceException;
import com.seopulse.common.exception.ResourceNotFoundException;
import com.seopulse.project.entity.Project;
import com.seopulse.project.repository.ProjectRepository;
import com.seopulse.website.dto.AuditPageResponse;
import com.seopulse.website.dto.AuditResponse;
import com.seopulse.website.dto.SeoIssueResponse;
import com.seopulse.website.entity.Audit;
import com.seopulse.website.entity.AuditOutbox;
import com.seopulse.website.entity.AuditPage;
import com.seopulse.website.entity.AuditStatus;
import com.seopulse.website.entity.Website;
import com.seopulse.website.entity.WebsiteStatus;
import com.seopulse.website.repository.AuditOutboxRepository;
import com.seopulse.website.repository.AuditPageRepository;
import com.seopulse.website.repository.AuditRepository;
import com.seopulse.website.repository.WebsiteRepository;
import com.seopulse.website.seo.entity.SeoIssue;
import com.seopulse.website.seo.repository.SeoIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {

    private final AuditRepository auditRepository;
    private final AuditPageRepository auditPageRepository;
    private final AuditOutboxRepository auditOutboxRepository;
    private final SeoIssueRepository seoIssueRepository;
    private final WebsiteRepository websiteRepository;
    private final ProjectRepository projectRepository;
    private final UrlValidator urlValidator;


    // ============================================================
    // CREATE AUDIT
    // ============================================================

    public AuditResponse createAudit(
            Long projectId,
            Long websiteId
    ) {

        Project project =
                projectRepository
                        .findById(projectId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Project not found"
                                )
                        );


        Website website =
                websiteRepository
                        .findById(websiteId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Website not found"
                                )
                        );


        if (!website.getProject()
                .getId()
                .equals(project.getId())) {

            throw new ResourceNotFoundException(
                    "Website not found"
            );
        }


        if (website.getStatus() != WebsiteStatus.ACTIVE) {

            throw new IllegalStateException(
                    "Website is not active"
            );
        }


        URI validatedUrl =
                urlValidator.validate(
                        website.getUrl()
                );


        boolean activeAuditExists =
                auditRepository
                        .existsByWebsiteIdAndStatusIn(
                                websiteId,
                                List.of(
                                        AuditStatus.QUEUED,
                                        AuditStatus.CRAWLING,
                                        AuditStatus.ANALYZING
                                )
                        );


        if (activeAuditExists) {

            throw new DuplicateResourceException(
                    "An audit is already running for this website"
            );
        }


        Audit audit =
                Audit.builder()
                        .website(website)
                        .status(AuditStatus.QUEUED)
                        .score(null)
                        .pagesCrawled(0)
                        .pagesAnalyzed(0)
                        .retryCount(0)
                        .maxRetries(3)
                        .startedAt(null)
                        .completedAt(null)
                        .errorMessage(null)
                        .build();


        Audit savedAudit =
                auditRepository.save(audit);


        AuditOutbox outbox =
                AuditOutbox.builder()
                        .audit(savedAudit)
                        .eventType("AUDIT_CREATED")
                        .published(false)
                        .build();


        auditOutboxRepository.save(outbox);


        return mapToResponse(savedAudit);
    }


    // ============================================================
    // GET AUDITS
    // ============================================================

    @Transactional(readOnly = true)
    public PageResponse<AuditResponse> getAudits(
            Long projectId,
            Long websiteId,
            Pageable pageable
    ) {

        projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found"
                        )
                );


        Website website =
                websiteRepository
                        .findById(websiteId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Website not found"
                                )
                        );


        if (!website.getProject()
                .getId()
                .equals(projectId)) {

            throw new ResourceNotFoundException(
                    "Website not found"
            );
        }


        Page<Audit> audits =
                auditRepository
                        .findByWebsiteId(
                                websiteId,
                                pageable
                        );


        Page<AuditResponse> response =
                audits.map(this::mapToResponse);


        return PageResponse.from(response);
    }


    // ============================================================
    // GET SINGLE AUDIT
    // ============================================================

    @Transactional(readOnly = true)
    public AuditResponse getAudit(
            Long projectId,
            Long auditId
    ) {

        Audit audit =
                auditRepository
                        .findByIdAndWebsiteProjectId(
                                auditId,
                                projectId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Audit not found"
                                )
                        );


        return mapToResponse(audit);
    }


    // ============================================================
    // GET AUDIT PAGES
    // ============================================================

    @Transactional(readOnly = true)
    public PageResponse<AuditPageResponse> getAuditPages(
            Long projectId,
            Long auditId,
            Pageable pageable
    ) {

        auditRepository
                .findByIdAndWebsiteProjectId(
                        auditId,
                        projectId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Audit not found"
                        )
                );


        Page<AuditPage> pages =
                auditPageRepository
                        .findByAuditId(
                                auditId,
                                pageable
                        );


        Page<AuditPageResponse> response =
                pages.map(
                        this::mapToAuditPageResponse
                );


        return PageResponse.from(response);
    }


    // ============================================================
    // GET SEO ISSUES
    // ============================================================

    @Transactional(readOnly = true)
    public PageResponse<SeoIssueResponse> getAuditIssues(
            Long projectId,
            Long auditId,
            String severity,
            String ruleCode,
            Pageable pageable
    ) {

        auditRepository
                .findByIdAndWebsiteProjectId(
                        auditId,
                        projectId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Audit not found"
                        )
                );


        String normalizedSeverity =
                normalizeSeverity(severity);


        String normalizedRuleCode =
                normalizeRuleCode(ruleCode);


        Page<SeoIssue> issues;


        // Both filters
        if (normalizedSeverity != null
                && normalizedRuleCode != null) {

            issues =
                    seoIssueRepository
                            .findByAuditPageAuditIdAndSeverityIgnoreCaseAndRuleCode(
                                    auditId,
                                    normalizedSeverity,
                                    normalizedRuleCode,
                                    pageable
                            );
        }

        // Severity only
        else if (normalizedSeverity != null) {

            issues =
                    seoIssueRepository
                            .findByAuditPageAuditIdAndSeverityIgnoreCase(
                                    auditId,
                                    normalizedSeverity,
                                    pageable
                            );
        }

        // Rule code only
        else if (normalizedRuleCode != null) {

            issues =
                    seoIssueRepository
                            .findByAuditPageAuditIdAndRuleCode(
                                    auditId,
                                    normalizedRuleCode,
                                    pageable
                            );
        }

        // No filters
        else {

            issues =
                    seoIssueRepository
                            .findByAuditPageAuditId(
                                    auditId,
                                    pageable
                            );
        }


        Page<SeoIssueResponse> response =
                issues.map(
                        this::mapToSeoIssueResponse
                );


        return PageResponse.from(response);
    }


    // ============================================================
    // NORMALIZE SEVERITY
    // ============================================================

    private String normalizeSeverity(
            String severity
    ) {

        if (severity == null
                || severity.isBlank()) {

            return null;
        }


        String value =
                severity
                        .trim()
                        .toUpperCase();


        if (!value.equals("ERROR")
                && !value.equals("WARNING")
                && !value.equals("INFO")) {

            throw new IllegalArgumentException(
                    "Invalid severity. Allowed values: ERROR, WARNING, INFO"
            );
        }


        return value;
    }


    // ============================================================
    // NORMALIZE RULE CODE
    // ============================================================

    private String normalizeRuleCode(
            String ruleCode
    ) {

        if (ruleCode == null
                || ruleCode.isBlank()) {

            return null;
        }


        String value =
                ruleCode.trim();


        if (value.length() > 100) {

            throw new IllegalArgumentException(
                    "Rule code must not exceed 100 characters"
            );
        }


        return value;
    }


    // ============================================================
    // AUDIT → DTO
    // ============================================================

    private AuditResponse mapToResponse(
            Audit audit
    ) {

        return new AuditResponse(

                audit.getId(),

                audit.getWebsite()
                        .getId(),

                audit.getWebsite()
                        .getUrl(),

                audit.getStatus(),

                audit.getScore(),

                audit.getPagesCrawled(),

                audit.getPagesAnalyzed(),

                audit.getStartedAt(),

                audit.getCompletedAt(),

                audit.getErrorMessage(),

                audit.getCreatedAt()
        );
    }


    // ============================================================
    // AUDIT PAGE → DTO
    // ============================================================

    private AuditPageResponse mapToAuditPageResponse(
            AuditPage page
    ) {

        return new AuditPageResponse(

                page.getId(),

                page.getAudit()
                        .getId(),

                page.getUrl(),

                page.getStatus(),

                page.getStatusCode(),

                page.getContentType(),

                page.getTitle(),

                page.getMetaDescription(),

                page.getCanonicalUrl(),

                page.getWordCount(),

                page.getH1Count(),

                page.getImageCount(),

                page.getImagesWithoutAlt(),

                page.getInternalLinkCount(),

                page.getExternalLinkCount(),

                page.getDepth(),

                page.getCrawledAt(),

                page.getCreatedAt()
        );
    }


    // ============================================================
    // SEO ISSUE → DTO
    // ============================================================

    private SeoIssueResponse mapToSeoIssueResponse(
            SeoIssue issue
    ) {

        return new SeoIssueResponse(

                issue.getId(),

                issue.getAuditPage()
                        .getAudit()
                        .getId(),

                issue.getAuditPage()
                        .getId(),

                issue.getAuditPage()
                        .getUrl(),

                issue.getRuleCode(),

                issue.getSeverity(),

                issue.getMessage(),

                issue.getRecommendations(),

                issue.getCreatedAt()
        );
    }
}