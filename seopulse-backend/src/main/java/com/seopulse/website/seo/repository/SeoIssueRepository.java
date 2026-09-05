package com.seopulse.website.seo.repository;

import com.seopulse.website.seo.entity.SeoIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeoIssueRepository
        extends JpaRepository<SeoIssue, Long> {

    List<SeoIssue> findByAuditPageId(Long auditPageId);

    long countByAuditPageId(Long auditPageId);

    long countByAuditPageAuditId(Long auditId);
}