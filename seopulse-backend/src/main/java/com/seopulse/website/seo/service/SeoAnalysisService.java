package com.seopulse.website.seo.service;

import com.seopulse.website.entity.AuditPage;
import com.seopulse.website.seo.analyzer.SeoAnalyzer;
import com.seopulse.website.seo.model.SeoIssueResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SeoAnalysisService {

    private final List<SeoAnalyzer> analyzers;

    public SeoAnalysisService(
            List<SeoAnalyzer> analyzers
    ) {
        this.analyzers = analyzers;
    }

    public List<SeoIssueResult> analyze(
            AuditPage page
    ) {

        List<SeoIssueResult> issues =
                new ArrayList<>();

        for (SeoAnalyzer analyzer : analyzers) {

            log.debug(
                    "Running analyzer: {} for page {}",
                    analyzer.getName(),
                    page.getUrl()
            );

            List<SeoIssueResult> results =
                    analyzer.analyze(page);

            if (results != null) {
                issues.addAll(results);
            }
        }

        return issues;
    }
}