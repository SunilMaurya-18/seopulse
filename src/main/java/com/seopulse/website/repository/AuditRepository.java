package com.seopulse.website.repository;

import com.seopulse.website.entity.Audit;
import com.seopulse.website.entity.AuditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuditRepository
        extends JpaRepository<Audit, Long> {

    Page<Audit> findByWebsiteId(
            Long websiteId,
            Pageable pageable
    );

    boolean existsByWebsiteIdAndStatusIn(
            Long websiteId,
            Iterable<AuditStatus> statuses
    );

    Optional<Audit> findByIdAndWebsiteProjectId(
            Long auditId,
            Long projectId
    );
}