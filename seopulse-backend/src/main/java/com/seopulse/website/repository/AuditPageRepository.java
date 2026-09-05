package com.seopulse.website.repository;

import com.seopulse.website.entity.AuditPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditPageRepository
        extends JpaRepository<AuditPage, Long> {

    List<AuditPage> findByAuditId(Long auditId);

    boolean existsByAuditIdAndUrl(
            Long auditId,
            String url
    );

    long countByAuditId(Long auditId);
}