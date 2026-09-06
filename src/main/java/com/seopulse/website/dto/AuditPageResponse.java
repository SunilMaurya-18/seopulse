package com.seopulse.website.dto;

import com.seopulse.website.entity.AuditPageStatus;

import java.time.Instant;

public record AuditPageResponse(

        Long id,

        Long auditId,

        String url,

        AuditPageStatus status,

        Integer statusCode,

        String contentType,

        String title,

        String metaDescription,

        String canonicalUrl,

        Integer wordCount,

        Integer h1Count,

        Integer imageCount,

        Integer imagesWithoutAlt,

        Integer internalLinkCount,

        Integer externalLinkCount,

        Integer depth,

        Instant crawledAt,

        Instant createdAt
) {
}