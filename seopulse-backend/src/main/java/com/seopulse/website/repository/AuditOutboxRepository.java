package com.seopulse.website.repository;

import com.seopulse.website.entity.AuditOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditOutboxRepository
        extends JpaRepository<AuditOutbox, Long> {

    List<AuditOutbox> findTop50ByPublishedFalseOrderByCreatedAtAsc();
}