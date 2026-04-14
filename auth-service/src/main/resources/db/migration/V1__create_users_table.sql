CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    spotify_id              VARCHAR(255) UNIQUE NOT NULL,
    display_name            VARCHAR(255) NOT NULL,
    avatar_url              TEXT,
    email                   VARCHAR(255),
    is_premium              BOOLEAN DEFAULT false,

    spotify_access_token    TEXT NOT NULL,
    spotify_refresh_token   TEXT NOT NULL,
    token_expires_at        TIMESTAMPTZ NOT NULL,
    is_active_host          BOOLEAN DEFAULT false,

    top_genres              TEXT[] DEFAULT '{}',

    created_at              TIMESTAMPTZ DEFAULT NOW(),
    updated_at              TIMESTAMPTZ DEFAULT NOW(),
    deleted_at              TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_users_spotify_id
    ON users(spotify_id);

CREATE INDEX idx_active_host_expiry
    ON users(token_expires_at)
    WHERE is_active_host = true;