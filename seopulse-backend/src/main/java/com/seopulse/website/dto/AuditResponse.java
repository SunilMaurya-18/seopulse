package com.seopulse.website.dto;

public record AuditResponse(
        Long id,
        String url,
        Integer score
) {
}
