CREATE TABLE seo_issues
(
    id             BIGSERIAL PRIMARY KEY,

    audit_page_id  BIGINT                   NOT NULL,

    rule_code      VARCHAR(100)             NOT NULL,

    severity       VARCHAR(20)              NOT NULL,

    message        VARCHAR(500)             NOT NULL,

    recommendation VARCHAR(1000),

    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_seo_issues_audit_page
        FOREIGN KEY (audit_page_id)
            REFERENCES audit_pages (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_seo_issues_audit_page_id
    ON seo_issues (audit_page_id);

CREATE INDEX idx_seo_issues_rule_code
    ON seo_issues (rule_code);

CREATE INDEX idx_seo_issues_severity
    ON seo_issues (severity);