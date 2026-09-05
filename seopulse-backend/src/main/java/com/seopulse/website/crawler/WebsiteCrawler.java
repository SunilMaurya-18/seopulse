package com.seopulse.website.crawler;

import com.seopulse.website.service.UrlValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebsiteCrawler {

    private final CrawlerProperties properties;
    private final UrlValidator urlValidator;
    private final UrlNormalizer urlNormalizer;
    private final HttpClient httpClient;

    private HttpClient createHttpClient() {

        return HttpClient.newBuilder()
                .connectTimeout(
                        Duration.ofMillis(
                                properties.getConnectTimeoutMs()
                        )
                )
                .followRedirects(
                        HttpClient.Redirect.NEVER
                )
                .build();
    }


    /**
     * Crawls a website starting from the supplied URL.
     */
    public List<CrawledPage> crawl(String startUrl) {

        String normalizedStartUrl =
                urlNormalizer.normalize(startUrl);

        if (normalizedStartUrl == null) {
            throw new IllegalArgumentException(
                    "Invalid starting URL"
            );
        }

        /*
         * Validate the seed URL before making
         * any network request.
         */
        urlValidator.validate(normalizedStartUrl);

        URI startUri =
                URI.create(normalizedStartUrl);

        String allowedHost =
                normalizeHost(startUri.getHost());

        Queue<CrawlTarget> queue =
                new ArrayDeque<>();

        Set<String> discoveredUrls =
                new HashSet<>();

        List<CrawledPage> crawledPages =
                new ArrayList<>();

        queue.offer(
                new CrawlTarget(
                        normalizedStartUrl,
                        0
                )
        );

        discoveredUrls.add(
                normalizedStartUrl
        );


        while (!queue.isEmpty()
                && crawledPages.size()
                < properties.getMaxPages()) {

            CrawlTarget target =
                    queue.poll();

            try {

                CrawledPage page =
                        crawlPage(
                                target,
                                allowedHost
                        );

                if (page == null) {
                    continue;
                }

                crawledPages.add(page);

                /*
                 * Don't discover more links when
                 * maximum depth has been reached.
                 */
                if (target.depth()
                        >= properties.getMaxDepth()) {

                    continue;
                }

                /*
                 * Add newly discovered links.
                 */
                for (String discoveredUrl
                        : page.discoveredUrls()) {

                    if (discoveredUrls.contains(
                            discoveredUrl
                    )) {
                        continue;
                    }

                    URI discoveredUri =
                            URI.create(discoveredUrl);

                    String discoveredHost =
                            normalizeHost(
                                    discoveredUri.getHost()
                            );

                    /*
                     * Same-origin restriction.
                     */
                    if (!allowedHost.equals(
                            discoveredHost
                    )) {
                        continue;
                    }

                    /*
                     * Validate URL again before putting
                     * it into the crawl queue.
                     */
                    try {
                        urlValidator.validate(
                                discoveredUrl
                        );
                    } catch (IllegalArgumentException ex) {
                        log.debug(
                                "Skipping unsafe URL: {}",
                                discoveredUrl
                        );
                        continue;
                    }

                    discoveredUrls.add(
                            discoveredUrl
                    );

                    queue.offer(
                            new CrawlTarget(
                                    discoveredUrl,
                                    target.depth() + 1
                            )
                    );
                }

            } catch (Exception ex) {

                log.warn(
                        "Failed to crawl URL: {}",
                        target.url(),
                        ex
                );
            }
        }

        log.info(
                "Crawl completed: startUrl={}, pages={}",
                normalizedStartUrl,
                crawledPages.size()
        );

        return crawledPages;
    }


    /**
     * Crawls one individual page.
     */
    private CrawledPage crawlPage(
            CrawlTarget target,
            String allowedHost
    ) throws Exception {

        URI uri =
                URI.create(target.url());

        /*
         * Validate URL immediately before making
         * the network request.
         */
        urlValidator.validate(
                target.url()
        );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(uri)
                        .timeout(
                                Duration.ofMillis(
                                        properties
                                                .getConnectTimeoutMs()
                                )
                        )
                        .header(
                                "User-Agent",
                                properties.getUserAgent()
                        )
                        .header(
                                "Accept",
                                "text/html,application/xhtml+xml"
                        )
                        .GET()
                        .build();

        HttpResponse<InputStream> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofInputStream()
                );

        int statusCode =
                response.statusCode();

        HttpHeaders headers =
                response.headers();

        String contentType =
                headers.firstValue("Content-Type")
                        .orElse("");

        /*
         * Handle redirects ourselves.
         *
         * This allows us to validate the redirect
         * destination before following it.
         */
        if (isRedirect(statusCode)) {

            String location =
                    headers.firstValue("Location")
                            .orElse(null);

            if (location == null) {
                return null;
            }

            URI redirectUri =
                    uri.resolve(location);

            String normalizedRedirect =
                    urlNormalizer.normalize(
                            redirectUri.toString()
                    );

            if (normalizedRedirect == null) {
                return null;
            }

            URI validatedRedirectUri =
                    URI.create(
                            normalizedRedirect
                    );

            String redirectHost =
                    normalizeHost(
                            validatedRedirectUri.getHost()
                    );

            if (!allowedHost.equals(
                    redirectHost
            )) {
                log.warn(
                        "Skipping cross-origin redirect: {} -> {}",
                        target.url(),
                        normalizedRedirect
                );

                return null;
            }

            /*
             * Validate redirect destination for SSRF.
             */
            urlValidator.validate(
                    normalizedRedirect
            );

            /*
             * We don't recursively follow redirects here.
             *
             * The redirected URL can instead be discovered
             * by the caller as a new crawl target.
             */
            return new CrawledPage(
                    target.url(),
                    statusCode,
                    contentType,
                    null,
                    null,
                    null,
                    0,
                    target.depth(),
                    Set.of(normalizedRedirect)
            );
        }

        /*
         * We only analyze successful responses.
         */
        if (statusCode < 200
                || statusCode >= 300) {

            return new CrawledPage(
                    target.url(),
                    statusCode,
                    contentType,
                    null,
                    null,
                    null,
                    0,
                    target.depth(),
                    Set.of()
            );
        }

        /*
         * Only process HTML documents.
         */
        if (!isHtml(contentType)) {

            return new CrawledPage(
                    target.url(),
                    statusCode,
                    contentType,
                    null,
                    null,
                    null,
                    0,
                    target.depth(),
                    Set.of()
            );
        }

        byte[] body =
                readLimitedBody(
                        response.body(),
                        properties.getMaxBodySizeBytes()
                );

        Document document =
                Jsoup.parse(
                        new String(
                                body,
                                java.nio.charset.StandardCharsets.UTF_8
                        ),
                        target.url()
                );

        String title =
                extractTitle(document);

        String metaDescription =
                extractMetaDescription(document);

        String canonicalUrl =
                extractCanonical(
                        document,
                        target.url()
                );

        int wordCount =
                countWords(
                        document
                                .body()
                                .text()
                );

        Set<String> links =
                extractLinks(
                        document,
                        target.url()
                );

        return new CrawledPage(
                target.url(),
                statusCode,
                contentType,
                title,
                metaDescription,
                canonicalUrl,
                wordCount,
                target.depth(),
                links
        );
    }


    /**
     * Extracts links from the page.
     */
    private Set<String> extractLinks(
            Document document,
            String baseUrl
    ) {

        Set<String> urls =
                new HashSet<>();

        Elements links =
                document.select("a[href]");

        for (Element link : links) {

            String href =
                    link.attr("href");

            if (href == null
                    || href.isBlank()) {
                continue;
            }

            try {

                URI base =
                        URI.create(baseUrl);

                URI resolved =
                        base.resolve(href);

                String normalized =
                        urlNormalizer.normalize(
                                resolved.toString()
                        );

                if (normalized != null) {
                    urls.add(normalized);
                }

            } catch (Exception ex) {

                log.debug(
                        "Invalid discovered link: {}",
                        href
                );
            }
        }

        return urls;
    }


    /**
     * Extracts the HTML title.
     */
    private String extractTitle(
            Document document
    ) {

        Element title =
                document.selectFirst("title");

        if (title == null) {
            return null;
        }

        String value =
                title.text().trim();

        return value.isBlank()
                ? null
                : value;
    }


    /**
     * Extracts meta description.
     */
    private String extractMetaDescription(
            Document document
    ) {

        Element element =
                document.selectFirst(
                        "meta[name=description]"
                );

        if (element == null) {
            return null;
        }

        String value =
                element.attr("content")
                        .trim();

        return value.isBlank()
                ? null
                : value;
    }


    /**
     * Extracts canonical URL.
     */
    private String extractCanonical(
            Document document,
            String baseUrl
    ) {

        Element element =
                document.selectFirst(
                        "link[rel=canonical]"
                );

        if (element == null) {
            return null;
        }

        String href =
                element.attr("href");

        if (href == null
                || href.isBlank()) {
            return null;
        }

        try {

            URI base =
                    URI.create(baseUrl);

            URI canonical =
                    base.resolve(href);

            return urlNormalizer.normalize(
                    canonical.toString()
            );

        } catch (Exception ex) {

            return null;
        }
    }


    /**
     * Counts visible words in the document body.
     */
    private int countWords(String text) {

        if (text == null
                || text.isBlank()) {
            return 0;
        }

        return text.trim()
                .split("\\s+")
                .length;
    }


    /**
     * Prevents responses larger than the configured limit
     * from being loaded into memory.
     */
    private byte[] readLimitedBody(
            InputStream inputStream,
            long maxBytes
    ) throws IOException {

        try (InputStream input =
                     inputStream) {

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            byte[] buffer =
                    new byte[8192];

            long total = 0;

            int bytesRead;

            while ((bytesRead =
                    input.read(buffer)) != -1) {

                total += bytesRead;

                if (total > maxBytes) {

                    throw new IOException(
                            "Response body exceeds maximum allowed size"
                    );
                }

                output.write(
                        buffer,
                        0,
                        bytesRead
                );
            }

            return output.toByteArray();
        }
    }


    private boolean isHtml(
            String contentType
    ) {

        String value =
                contentType.toLowerCase();

        return value.contains("text/html")
                || value.contains("application/xhtml+xml");
    }


    private boolean isRedirect(
            int statusCode
    ) {

        return statusCode == 301
                || statusCode == 302
                || statusCode == 303
                || statusCode == 307
                || statusCode == 308;
    }


    private String normalizeHost(
            String host
    ) {

        if (host == null) {
            return "";
        }

        return host
                .toLowerCase()
                .stripTrailing();
    }


    /**
     * Represents one URL waiting to be crawled.
     */
    private record CrawlTarget(
            String url,
            int depth
    ) {
    }
}