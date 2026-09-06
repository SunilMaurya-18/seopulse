package com.seopulse.website.crawler;

import java.util.Set;

public record CrawledPage(
        String url,
        int status,
        String contentType,
        String title,
        String metaDescription,
        String canonicalUrl,
        int wordCount,
        int h1Count,
        int imageCount,
        int imagesWithoutAlt,
        int internalLinkCount,
        int externalLinkCount,
        int depth,
        Set<String> discoveredUrls
) {
}