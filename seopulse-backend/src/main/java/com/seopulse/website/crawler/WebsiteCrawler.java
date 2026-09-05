package com.seopulse.website.crawler;

import com.seopulse.website.service.UrlValidator;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
@Slf4j
public class WebsiteCrawler {

    private final CrawlerProperties properties;
    private final UrlValidator urlValidator;
    private final UrlNormalizer urlNormalizer;
    private final HttpClient httpClient;

    public WebsiteCrawler(
            CrawlerProperties properties,
            UrlValidator urlValidator,
            UrlNormalizer urlNormalizer
    ) {
        this.properties = properties;
        this.urlValidator = urlValidator;
        this.urlNormalizer = urlNormalizer;

        this.httpClient =
                HttpClient.newBuilder()
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
     *
     * @param startUrl website URL
     * @return list of successfully crawled pages
     */
    public List<CrawledPage> crawl(String startUrl) {

        URI validatedStartUrl =
                urlValidator.validate(startUrl);

        String normalizedStartUrl =
                urlNormalizer.normalize(
                        validatedStartUrl.toString()
                );

        if (normalizedStartUrl == null) {
            throw new IllegalArgumentException(
                    "Unable to normalize website URL"
            );
        }

        String allowedHost =
                normalizeHost(
                        validatedStartUrl.getHost()
                );

        Queue<CrawlTarget> queue =
                new ArrayDeque<>();

        Set<String> discoveredUrls =
                new HashSet<>();

        List<CrawledPage> crawledPages =
                new ArrayList<>();

        queue.add(
                new CrawlTarget(
                        normalizedStartUrl,
                        0
                )
        );

        discoveredUrls.add(
                normalizedStartUrl
        );

        log.info(
                "Starting website crawl: url={}, maxPages={}, maxDepth={}",
                normalizedStartUrl,
                properties.getMaxPages(),
                properties.getMaxDepth()
        );

        while (
                !queue.isEmpty()
                        && crawledPages.size()
                        < properties.getMaxPages()
        ) {

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

                log.debug(
                        "Page crawled: url={}, status={}, depth={}",
                        page.url(),
                        page.status(),
                        page.depth()
                );

                if (
                        target.depth()
                                >= properties.getMaxDepth()
                ) {
                    continue;
                }

                for (String discoveredUrl :
                        page.discoveredUrls()) {

                    if (
                            discoveredUrls.size()
                                    >= properties.getMaxPages()
                    ) {
                        break;
                    }

                    String normalizedUrl =
                            urlNormalizer.normalize(
                                    discoveredUrl
                            );

                    if (normalizedUrl == null) {
                        continue;
                    }

                    if (
                            discoveredUrls.contains(
                                    normalizedUrl
                            )
                    ) {
                        continue;
                    }

                    if (
                            !isSameOrigin(
                                    normalizedUrl,
                                    allowedHost
                            )
                    ) {
                        continue;
                    }

                    try {

                        urlValidator.validate(
                                normalizedUrl
                        );

                    } catch (IllegalArgumentException ex) {

                        log.debug(
                                "Skipping unsafe URL: {}",
                                normalizedUrl
                        );

                        continue;
                    }

                    discoveredUrls.add(
                            normalizedUrl
                    );

                    queue.add(
                            new CrawlTarget(
                                    normalizedUrl,
                                    target.depth() + 1
                            )
                    );
                }

            } catch (Exception ex) {

                log.warn(
                        "Failed to crawl page: url={}, depth={}, error={}",
                        target.url(),
                        target.depth(),
                        ex.getMessage()
                );
            }
        }

        log.info(
                "Website crawl finished: startUrl={}, pagesCrawled={}",
                normalizedStartUrl,
                crawledPages.size()
        );

        return crawledPages;
    }

    /**
     * Crawls one page.
     */
    private CrawledPage crawlPage(
            CrawlTarget target,
            String allowedHost
    ) throws IOException, InterruptedException {

        URI currentUri =
                urlValidator.validate(target.url());

        String currentUrl =
                urlNormalizer.normalize(currentUri.toString());

        if (currentUrl == null) {
            return null;
        }

        if (!isSameOrigin(currentUrl, allowedHost)) {
            return null;
        }

        HttpResponse<InputStream> response =
                sendRequest(currentUri);

        try (InputStream body = response.body()) {

            int statusCode =
                    response.statusCode();

            HttpHeaders headers =
                    response.headers();

            String contentType =
                    headers.firstValue("Content-Type")
                            .orElse(null);

            /*
             * Handle redirects manually.
             */
            if (isRedirect(statusCode)) {

                String location =
                        headers.firstValue("Location")
                                .orElse(null);

                if (location == null || location.isBlank()) {
                    return null;
                }

                URI redirectUri;

                try {
                    redirectUri =
                            currentUri.resolve(location);
                } catch (IllegalArgumentException ex) {

                    log.debug(
                            "Invalid redirect URL: {}",
                            location
                    );

                    return null;
                }

                String redirectUrl =
                        urlNormalizer.normalize(
                                redirectUri.toString()
                        );

                if (redirectUrl == null) {
                    return null;
                }

                /*
                 * Never follow redirects outside the website.
                 */
                if (!isSameOrigin(
                        redirectUrl,
                        allowedHost
                )) {

                    log.debug(
                            "Skipping cross-origin redirect: {} -> {}",
                            currentUrl,
                            redirectUrl
                    );

                    return null;
                }

                /*
                 * Revalidate redirect destination
                 * for SSRF protection.
                 */
                try {

                    urlValidator.validate(
                            redirectUrl
                    );

                } catch (IllegalArgumentException ex) {

                    log.warn(
                            "Blocked unsafe redirect: {} -> {}",
                            currentUrl,
                            redirectUrl
                    );

                    return null;
                }

                /*
                 * We currently do not follow redirects.
                 */
                return null;
            }

            /*
             * Non-success HTTP responses.
             */
            if (statusCode < 200 || statusCode >= 300) {

                return new CrawledPage(
                        currentUrl,
                        statusCode,
                        contentType,
                        null,
                        null,
                        null,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        target.depth(),
                        Set.of()
                );
            }

            /*
             * Only HTML pages are analyzed.
             */
            if (!isHtml(contentType)) {
                return null;
            }

            String html =
                    readLimitedBody(
                            body,
                            properties.getMaxBodySizeBytes()
                    );

            if (html == null) {
                return null;
            }

            Document document =
                    Jsoup.parse(
                            html,
                            currentUrl
                    );

            String title =
                    extractTitle(document);

            String metaDescription =
                    extractMetaDescription(document);

            String canonicalUrl =
                    extractCanonical(
                            document,
                            currentUrl
                    );

            int wordCount =
                    countWords(document);

            int h1Count =
                    document.select("h1").size();

            int imageCount =
                    document.select("img").size();

            int imagesWithoutAlt =
                    countImagesWithoutAlt(
                            document
                    );

            int internalLinkCount =
                    countInternalLinks(
                            document,
                            allowedHost
                    );

            int externalLinkCount =
                    countExternalLinks(
                            document,
                            allowedHost
                    );

            Set<String> links =
                    extractLinks(
                            document,
                            allowedHost
                    );

            return new CrawledPage(
                    currentUrl,
                    statusCode,
                    contentType,
                    title,
                    metaDescription,
                    canonicalUrl,
                    wordCount,
                    h1Count,
                    imageCount,
                    imagesWithoutAlt,
                    internalLinkCount,
                    externalLinkCount,
                    target.depth(),
                    links
            );
        }
    }

    /**
     * Sends a GET request.
     * <p>
     * Redirects are disabled at HttpClient level because redirects
     * are validated manually.
     */
    private HttpResponse<InputStream> sendRequest(
            URI uri
    ) throws IOException, InterruptedException {

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(uri)
                        .timeout(
                                Duration.ofMillis(
                                        properties.getConnectTimeoutMs()
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

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
        );
    }

    /**
     * Extracts links from the page.
     * <p>
     * Only HTTP/HTTPS URLs belonging to the same host are returned.
     */
    private Set<String> extractLinks(
            Document document,
            String allowedHost
    ) {

        Set<String> links =
                new HashSet<>();

        for (Element element :
                document.select("a[href]")) {

            String href =
                    element.absUrl("href");

            if (href == null || href.isBlank()) {
                continue;
            }

            String normalized =
                    urlNormalizer.normalize(href);

            if (normalized == null) {
                continue;
            }

            if (
                    !isSameOrigin(
                            normalized,
                            allowedHost
                    )
            ) {
                continue;
            }

            links.add(normalized);
        }

        return links;
    }

    /**
     * Extracts the page title.
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
     * Extracts the meta description.
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
                element.attr("content").trim();

        return value.isBlank()
                ? null
                : value;
    }

    /**
     * Extracts the canonical URL.
     */
    private String extractCanonical(
            Document document,
            String currentUrl
    ) {

        Element canonical =
                document.selectFirst(
                        "link[rel=canonical]"
                );

        if (canonical == null) {
            return null;
        }

        String href =
                canonical.absUrl("href");

        if (href == null || href.isBlank()) {
            return null;
        }

        String normalized =
                urlNormalizer.normalize(href);

        if (normalized == null) {
            return null;
        }

        return normalized;
    }

    /**
     * Counts textual words on the page.
     */
    private int countWords(
            Document document
    ) {

        String text =
                document.body() != null
                        ? document.body().text()
                        : document.text();

        if (text == null || text.isBlank()) {
            return 0;
        }

        return text.trim()
                .split("\\s+")
                .length;
    }

    /**
     * Counts images that do not have alt text.
     * <p>
     * An empty alt attribute is considered missing for our current
     * SEO audit implementation.
     */
    private int countImagesWithoutAlt(
            Document document
    ) {

        int count = 0;

        for (Element image :
                document.select("img")) {

            String alt =
                    image.attr("alt");

            if (alt == null || alt.isBlank()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Counts links pointing to the same website host.
     */
    private int countInternalLinks(
            Document document,
            String allowedHost
    ) {

        int count = 0;

        for (Element link :
                document.select("a[href]")) {

            String href =
                    link.absUrl("href");

            if (href == null || href.isBlank()) {
                continue;
            }

            try {

                URI uri =
                        URI.create(href);

                String host =
                        normalizeHost(
                                uri.getHost()
                        );

                if (
                        host != null
                                && allowedHost.equals(host)
                ) {
                    count++;
                }

            } catch (IllegalArgumentException ignored) {
            }
        }

        return count;
    }

    /**
     * Counts links pointing outside the website.
     */
    private int countExternalLinks(
            Document document,
            String allowedHost
    ) {

        int count = 0;

        for (Element link :
                document.select("a[href]")) {

            String href =
                    link.absUrl("href");

            if (href == null || href.isBlank()) {
                continue;
            }

            try {

                URI uri =
                        URI.create(href);

                String host =
                        normalizeHost(
                                uri.getHost()
                        );

                if (
                        host != null
                                && !allowedHost.equals(host)
                ) {
                    count++;
                }

            } catch (IllegalArgumentException ignored) {
            }
        }

        return count;
    }

    /**
     * Reads the HTTP response while enforcing the configured
     * maximum response size.
     */
    private String readLimitedBody(
            InputStream inputStream,
            long maxBytes
    ) {

        if (inputStream == null) {
            return null;
        }

        try (
                InputStream input =
                        inputStream;

                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            byte[] buffer =
                    new byte[8192];

            long totalBytes = 0;

            int bytesRead;

            while (
                    (bytesRead =
                            input.read(buffer))
                            != -1
            ) {

                totalBytes += bytesRead;

                if (totalBytes > maxBytes) {

                    log.warn(
                            "Response exceeded maximum body size: {} bytes",
                            maxBytes
                    );

                    return null;
                }

                output.write(
                        buffer,
                        0,
                        bytesRead
                );
            }

            return output.toString(
                    java.nio.charset.StandardCharsets.UTF_8
            );

        } catch (IOException ex) {

            log.warn(
                    "Failed to read HTTP response body: {}",
                    ex.getMessage()
            );

            return null;
        }
    }

    /**
     * Determines whether a response is HTML.
     */
    private boolean isHtml(
            String contentType
    ) {

        if (contentType == null) {
            return false;
        }

        String normalized =
                contentType.toLowerCase();

        return normalized.contains(
                "text/html"
        )
                || normalized.contains(
                "application/xhtml+xml"
        );
    }

    /**
     * Determines whether the status code represents a redirect.
     */
    private boolean isRedirect(
            int statusCode
    ) {

        return statusCode == 301
                || statusCode == 302
                || statusCode == 303
                || statusCode == 307
                || statusCode == 308;
    }

    /**
     * Checks whether a URL belongs to the same host as the website.
     */
    private boolean isSameOrigin(
            String url,
            String allowedHost
    ) {

        try {

            URI uri =
                    URI.create(url);

            String host =
                    normalizeHost(
                            uri.getHost()
                    );

            return host != null
                    && host.equals(
                    allowedHost
            );

        } catch (IllegalArgumentException ex) {

            return false;
        }
    }

    /**
     * Normalizes host names for comparison.
     */
    private String normalizeHost(
            String host
    ) {

        if (host == null || host.isBlank()) {
            return null;
        }

        return host
                .trim()
                .toLowerCase();
    }

    /**
     * Safely closes an HTTP response body.
     */
    private void closeBody(
            InputStream inputStream
    ) {

        if (inputStream == null) {
            return;
        }

        try {
            inputStream.close();
        } catch (IOException ignored) {
        }
    }

    private record CrawlTarget(
            String url,
            int depth
    ) {
    }
}