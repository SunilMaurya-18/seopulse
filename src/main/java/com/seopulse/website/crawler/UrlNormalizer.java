package com.seopulse.website.crawler;

import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class UrlNormalizer {

    public String normalize(String rawUrl) {

        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }

        try {

            URI uri = URI.create(rawUrl.trim());

            String scheme = uri.getScheme();

            if (scheme == null) {
                return null;
            }

            String normalizedScheme =
                    scheme.toLowerCase();

            String host = uri.getHost();

            if (host == null || host.isBlank()) {
                return null;
            }

            String normalizedHost =
                    host.toLowerCase();

            int port = uri.getPort();

            String path = uri.getPath();

            if (path == null || path.isBlank()) {
                path = "/";
            }

            /*
             * Remove fragment.
             *
             * #section does not represent a different
             * HTML document.
             */
            String query = uri.getQuery();

            URI normalized = new URI(
                    normalizedScheme,
                    null,
                    normalizedHost,
                    port,
                    path,
                    query,
                    null
            );

            return normalized.toString();

        } catch (Exception ex) {
            return null;
        }
    }
}