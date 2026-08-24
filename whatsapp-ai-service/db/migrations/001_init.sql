CREATE TABLE IF NOT EXISTS sessions (
    id          SERIAL PRIMARY KEY,
    phone_number TEXT NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS messages (
    id         SERIAL PRIMARY KEY,
    session_id INTEGER NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    role       TEXT NOT NULL CHECK (role IN ('user', 'assistant', 'system')),
    content    TEXT NOT NULL,
    timestamp  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS messages_session_id_timestamp_idx
    ON messages (session_id, timestamp DESC);

CREATE TABLE IF NOT EXISTS leads (
    id               SERIAL PRIMARY KEY,
    phone_number     TEXT NOT NULL,
    name             TEXT,
    service_interest TEXT,
    metadata         JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS leads_phone_number_idx ON leads (phone_number);
