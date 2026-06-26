ALTER TABLE users
    ALTER COLUMN password DROP NOT NULL,
    ADD COLUMN provider    VARCHAR(20),
    ADD COLUMN provider_id VARCHAR(255);

CREATE UNIQUE INDEX idx_users_provider_provider_id ON users(provider, provider_id)
    WHERE provider IS NOT NULL;