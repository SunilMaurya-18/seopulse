CREATE TABLE audit_pages
(
    id               BIGSERIAL PRIMARY KEY,

    audit_id         BIGINT                   NOT NULL,

    url              VARCHAR(2048)            NOT NULL,

    status           VARCHAR(20)              NOT NULL DEFAULT 'QUEUED',

    status_code      INTEGER,

    content_type     VARCHAR(100),

    title            VARCHAR(500),

    meta_description VARCHAR(1000),

    canonical_url    VARCHAR(2048),

    word_count       INTEGER,

    depth            INTEGER                  NOT NULL DEFAULT 0,

    crawled_at       TIMESTAMP WITH TIME ZONE,

    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_audit_pages_audit
        FOREIGN KEY (audit_id)
            REFERENCES audits (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_audit_pages_audit_id
    ON audit_pages (audit_id);

CREATE INDEX idx_audit_pages_audit_url
    ON audit_pages (audit_id, url);

CREATE INDEX idx_audit_pages_status_code
    ON audit_pages (status_code);

CREATE INDEX idx_audit_pages_status
    ON audit_pages (status);