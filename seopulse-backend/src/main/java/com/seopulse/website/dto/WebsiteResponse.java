package com.seopulse.website.dto;

import java.time.Instant;

public record WebsiteResponse(
        Long id,
        String name,
        String url,
        Instant createdAt,
        Instant updatedAt
) {
}
