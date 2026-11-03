CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL CHECK (role IN ('USER', 'AGENT'))
);
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    description VARCHAR(2000) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    status VARCHAR(20) NOT NULL,
    creator_id BIGINT NOT NULL REFERENCES app_users(id),
    assignee_id BIGINT REFERENCES app_users(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_tickets_creator_date ON tickets(creator_id, created_at DESC);
CREATE INDEX idx_tickets_status_date ON tickets(status, created_at DESC);
CREATE TABLE ticket_comments (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES app_users(id),
    body VARCHAR(1000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_ticket_comments_ticket_date ON ticket_comments(ticket_id, created_at ASC);
