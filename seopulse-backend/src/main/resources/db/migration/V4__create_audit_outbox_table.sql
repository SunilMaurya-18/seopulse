CREATE TABLE audit_outbox
(
    id           BIGSERIAL PRIMARY KEY,
    audit_id     BIGINT                   NOT NULL,
    event_type   VARCHAR(50)              NOT NULL,
    published    BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_audit_outbox_audit
        FOREIGN KEY (audit_id)
            REFERENCES audits (id)
            ON DELETE CASCADE
);
CREATE INDEX idx_audit_outbox_unpublished
    ON audit_outbox (published, created_at);

CREATE INDEX idx_audit_outbox_audit_id
    ON audit_outbox (audit_id);