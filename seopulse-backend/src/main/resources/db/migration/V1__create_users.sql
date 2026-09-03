CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,

    name       VARCHAR(100)             NOT NULL,

    email      VARCHAR(255)             NOT NULL UNIQUE,

    password   VARCHAR(255)             NOT NULL,

    role       VARCHAR(20)              NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_users_email
    ON users (email);


CREATE TABLE projects
(
    id          BIGSERIAL PRIMARY KEY,

    name        VARCHAR(150)             NOT NULL,

    description VARCHAR(500),

    user_id     BIGINT                   NOT NULL,

    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_projects_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_projects_user_id
    ON projects (user_id);