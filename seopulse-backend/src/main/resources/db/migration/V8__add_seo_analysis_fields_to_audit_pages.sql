ALTER TABLE audit_pages
    ADD COLUMN h1_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE audit_pages
    ADD COLUMN image_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE audit_pages
    ADD COLUMN images_without_alt INTEGER NOT NULL DEFAULT 0;

ALTER TABLE audit_pages
    ADD COLUMN internal_link_count INTEGER NOT NULL DEFAULT 0;

ALTER TABLE audit_pages
    ADD COLUMN external_link_count INTEGER NOT NULL DEFAULT 0;