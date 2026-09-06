package com.seopulse.website.seo.repository;

import com.seopulse.website.seo.entity.SeoIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeoIssueRepository
        extends JpaRepository<SeoIssue, Long> {

    List<SeoIssue> findByAuditPageId(Long auditPageId);

    long countByAuditPageId(Long auditPageId);

    long countByAuditPageAuditId(Long auditId);

    void deleteByAuditPageId(Long auditPageId);

    Page<SeoIssue> findByAuditPageAuditId(
            Long auditId,
            Pageable pageable
    );

    Page<SeoIssue> findByAuditPageAuditIdAndSeverityIgnoreCase(
            Long auditId,
            String severity,
            Pageable pageable
    );

    Page<SeoIssue> findByAuditPageAuditIdAndRuleCode(
            Long auditId,
            String ruleCode,
            Pageable pageable
    );

    Page<SeoIssue> findByAuditPageAuditIdAndSeverityIgnoreCaseAndRuleCode(
            Long auditId,
            String severity,
            String ruleCode,
            Pageable pageable
    );
}