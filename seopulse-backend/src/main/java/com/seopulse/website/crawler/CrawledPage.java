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
        int depth,
        Set<String> discoveredUrls
) {

}
