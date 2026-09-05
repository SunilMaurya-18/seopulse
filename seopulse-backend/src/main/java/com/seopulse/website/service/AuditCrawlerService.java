package com.seopulse.website.service;

import com.seopulse.website.crawler.CrawledPage;
import com.seopulse.website.crawler.WebsiteCrawler;
import com.seopulse.website.entity.Audit;
import com.seopulse.website.entity.AuditPage;
import com.seopulse.website.entity.AuditPageStatus;
import com.seopulse.website.entity.AuditStatus;
import com.seopulse.website.repository.AuditPageRepository;
import com.seopulse.website.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditCrawlerService {

    private final AuditRepository auditRepository;
    private final AuditPageRepository auditPageRepository;
    private final WebsiteCrawler websiteCrawler;


    public void crawlAudit(Long auditId) {

        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Audit not found: " + auditId
                        )
                );

        if (audit.getStatus() != AuditStatus.CRAWLING) {
            log.warn(
                    "Skipping crawl for audit {} because status is {}",
                    auditId,
                    audit.getStatus()
            );
            return;
        }

        try {

            log.info(
                    "Starting website crawl: auditId={}, url={}",
                    auditId,
                    audit.getWebsite().getUrl()
            );

            List<CrawledPage> pages =
                    websiteCrawler.crawl(
                            audit.getWebsite().getUrl()
                    );

            log.info(
                    "Crawl completed: auditId={}, pages={}",
                    auditId,
                    pages.size()
            );

            savePages(audit, pages);

            audit.setPagesCrawled(pages.size());
            audit.setStatus(AuditStatus.ANALYZING);

            auditRepository.save(audit);

            log.info(
                    "Audit {} moved to ANALYZING",
                    auditId
            );

        } catch (Exception ex) {

            log.error(
                    "Website crawl failed: auditId={}",
                    auditId,
                    ex
            );

            audit.setStatus(AuditStatus.FAILED);
            audit.setCompletedAt(Instant.now());
            audit.setErrorMessage(
                    buildErrorMessage(ex)
            );

            auditRepository.save(audit);

            throw ex;
        }
    }

    private void savePages(
            Audit audit,
            List<CrawledPage> pages
    ) {

        for (CrawledPage page : pages) {

            if (auditPageRepository.existsByAuditIdAndUrl(
                    audit.getId(),
                    page.url()
            )) {
                continue;
            }

            AuditPage auditPage =
                    AuditPage.builder()
                            .audit(audit)
                            .url(page.url())
                            .status(AuditPageStatus.CRAWLED)
                            .statusCode(page.status())
                            .contentType(page.contentType())
                            .title(page.title())
                            .metaDescription(page.metaDescription())
                            .canonicalUrl(page.canonicalUrl())
                            .wordCount(page.wordCount())
                            .depth(page.depth())
                            .crawledAt(Instant.now())
                            .build();

            auditPageRepository.save(auditPage);
        }
    }

    private String buildErrorMessage(Exception ex) {

        String message = ex.getMessage();

        if (message == null || message.isBlank()) {
            return "Website crawl failed";
        }

        return message.length() > 1000
                ? message.substring(0, 1000)
                : message;
    }
}