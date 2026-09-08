-- Add composite index for efficient project-level audit queries
CREATE INDEX idx_audits_website_status
    ON audits (website_id, status);

-- Add composite index for efficient audit-level issue counts
CREATE INDEX idx_seo_issues_audit_page_severity
    ON seo_issues (audit_page_id, severity);
