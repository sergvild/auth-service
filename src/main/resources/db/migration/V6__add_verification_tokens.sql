CREATE TABLE verification_tokens (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    token      VARCHAR(36) NOT NULL UNIQUE,
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL
);