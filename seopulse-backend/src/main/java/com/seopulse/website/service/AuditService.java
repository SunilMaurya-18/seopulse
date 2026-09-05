package com.seopulse.website.service;

import com.seopulse.common.dto.PageResponse;
import com.seopulse.common.exception.DuplicateResourceException;
import com.seopulse.common.exception.ResourceNotFoundException;
import com.seopulse.project.entity.Project;
import com.seopulse.project.repository.ProjectRepository;
import com.seopulse.website.dto.AuditResponse;
import com.seopulse.website.entity.Audit;
import com.seopulse.website.entity.AuditOutbox;
import com.seopulse.website.entity.AuditStatus;
import com.seopulse.website.entity.Website;
import com.seopulse.website.job.AuditQueue;
import com.seopulse.website.repository.AuditOutboxRepository;
import com.seopulse.website.repository.AuditRepository;
import com.seopulse.website.repository.WebsiteRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {

    private final AuditRepository auditRepository;
    private final WebsiteRepository websiteRepository;
    private final ProjectRepository projectRepository;
    private final UrlValidator urlValidator;

    private final AuditOutboxRepository auditOutboxRepository;

    public AuditResponse createAudit(
            Long projectId,
            Long websiteId,
            Long userId
    ) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found"
                        )
                );

        // Ownership check
        if (!project.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Project not found"
            );
        }

        Website website = websiteRepository.findById(websiteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Website not found"
                        )
                );

        // Make sure website belongs to this project
        if (!website.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException(
                    "Website not found"
            );
        }

        // Website must be active
        if (website.getStatus() !=
                com.seopulse.website.entity.WebsiteStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Website is not active"
            );
        }

        // Validate URL again before crawling
        urlValidator.validate(website.getUrl());

        // Prevent multiple active audits
        boolean activeAuditExists =
                auditRepository.existsByWebsiteIdAndStatusIn(
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

        Audit audit = Audit.builder()
                .website(website)
                .status(AuditStatus.QUEUED)
                .pagesCrawled(0)
                .pagesAnalyzed(0)
                .retryCount(0)
                .maxRetries(3)
                .build();
        Audit savedAudit = auditRepository.save(audit);

        AuditOutbox outbox = AuditOutbox.builder()
                .audit(savedAudit)
                .eventType("AUDIT_CREATED")
                .published(false)
                .build();

        auditOutboxRepository.save(outbox);

        return mapToResponse(savedAudit);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditResponse> getAudits(
            Long projectId,
            Long websiteId,
            Long userId,
            Pageable pageable
    ) {

        verifyWebsiteOwnership(
                projectId,
                websiteId,
                userId
        );

        Page<AuditResponse> page =
                auditRepository
                        .findByWebsiteId(websiteId, pageable)
                        .map(this::mapToResponse);

        return PageResponse.from(page);
    }

    private Website verifyWebsiteOwnership(
            Long projectId,
            Long websiteId,
            Long userId
    ) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found"
                        )
                );

        if (!project.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException(
                    "Project not found"
            );
        }

        Website website = websiteRepository.findById(websiteId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Website not found"
                        )
                );

        if (!website.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException(
                    "Website not found"
            );
        }

        return website;
    }

    private AuditResponse mapToResponse(Audit audit) {

        return new AuditResponse(
                audit.getId(),
                audit.getWebsite().getId(),
                audit.getWebsite().getUrl(),
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
}