package com.seopulse.website.crawler;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "seopulse.crawler")
@Getter
@Setter
public class CrawlerProperties {
    private int maxPages = 500;
    private int maxDepth = 5;

    private int connectTimeoutMs = 10000;
    private long maxBodySizeBytes = 5_000_000;
    private String userAgent = "SEOPulseBot/1.0(+https://seopulse.example.com/bot)";
    private boolean respectRobotsTxt = true;
}
