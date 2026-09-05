package com.seopulse.website.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "audit_pages",
        indexes = {
                @Index(
                        name = "idx_audit_pages_audit_id",
                        columnList = "audit_id"
                ),
                @Index(
                        name = "idx_audit_pages_audit_url",
                        columnList = "audit_id,url"
                ),
                @Index(
                        name = "idx_audit_pages_status_code",
                        columnList = "status_code"
                ),
                @Index(
                        name = "idx_audit_pages_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "audit_id", nullable = false)
    private Audit audit;

    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditPageStatus status;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(length = 500)
    private String title;
    @Column(name = "meta_description", length = 1000)
    private String metaDescription;

    @Column(name = "canonical_url", length = 2048)
    private String canonicalUrl;

    @Column(name = "word_count")
    private Integer wordCount;
    @Column(nullable = false)
    private Integer depth;

    @Column(name = "crawled_at")
    private Instant crawledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {

        createdAt = Instant.now();

        if (depth == null) {
            depth = 0;
        }

        if (status == null) {
            status = AuditPageStatus.QUEUED;
        }
    }
}
