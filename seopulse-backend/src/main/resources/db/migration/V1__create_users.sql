CREATE TABLE users(
    id BEGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPZ NOT NULL ,
    updated_at TIMESTAMPZ NOT NULL ,
    CONSTRAINT uk_users_email UNIQUE (email)
);
CREATE INDEX idx_users_email
 ON users(email);