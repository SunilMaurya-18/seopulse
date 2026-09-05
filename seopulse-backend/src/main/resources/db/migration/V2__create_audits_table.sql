CREATE TABLE audits
(
    id             BIGSERIAL PRIMARY KEY,
    website_id     BIGINT                   NOT NULL,
    status         VARCHAR(20)              NOT NULL,
    score          INTEGER,
    pages_crawled  INTEGER                  NOT NULL DEFAULT 0,
    pages_analyzed INTEGER                  NOT NULL DEFAULT 0,
    started_at     TIMESTAMP WITH TIME ZONE,
    completed_at   TIMESTAMP WITH TIME ZONE,
    error_message  VARCHAR(1000),
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_audits_website
        FOREIGN KEY (website_id)
            REFERENCES website (id)
            ON DELETE CASCADE
);
CREATE INDEX idx_audits_website_id
    ON audits (website_id);

CREATE INDEX idx_audits_status
    ON audits (status);

CREATE INDEX idx_audits_created_at
    ON audits (created_at);