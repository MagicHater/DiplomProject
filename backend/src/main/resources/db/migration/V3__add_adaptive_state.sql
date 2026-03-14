ALTER TABLE test_sessions
    ADD COLUMN adaptive_state_json TEXT NOT NULL DEFAULT '{}';
