package com.seopulse.website.service;

import com.seopulse.common.dto.PageResponse;
import com.seopulse.website.dto.AuditResponse;
import com.seopulse.website.entity.Audit;
import com.seopulse.website.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository auditRepository;

    public PageResponse<AuditResponse> getAudits(Pageable pageable) {

        Page<AuditResponse> page=auditRepository.findAll(pageable).map(this::mapToResponse);
        return PageResponse.from(page);
    }

    private AuditResponse mapToResponse(Audit audit) {

        return new AuditResponse(
                audit.getId(),
                audit.getUrl(),
                audit.getScore()
        );
    }
}