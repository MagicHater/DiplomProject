CREATE TABLE test_categories (
    id UUID PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_test_categories_code UNIQUE (code)
);

INSERT INTO test_categories (id, code, name, description, is_active)
VALUES
    ('50000000-0000-0000-0000-000000000001', 'MARKETING', 'Маркетолог', 'Категория для маркетинга', TRUE),
    ('50000000-0000-0000-0000-000000000002', 'SMM', 'SMM', 'Категория для SMM', TRUE),
    ('50000000-0000-0000-0000-000000000003', 'DESIGN', 'Дизайнер', 'Категория для дизайнеров', TRUE),
    ('50000000-0000-0000-0000-000000000004', 'SAFETY', 'Техника безопасности', 'Категория для тестов по технике безопасности', TRUE);

ALTER TABLE questions ADD COLUMN category_id UUID;
UPDATE questions SET category_id = '50000000-0000-0000-0000-000000000001';
ALTER TABLE questions ALTER COLUMN category_id SET NOT NULL;
ALTER TABLE questions ADD CONSTRAINT fk_questions_category_id FOREIGN KEY (category_id) REFERENCES test_categories(id);
CREATE INDEX idx_questions_category_id ON questions(category_id);

CREATE TABLE test_access_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(128) NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_user_id UUID NOT NULL,
    category_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMPTZ,
    used_by_user_id UUID,
    used_by_guest_display_name VARCHAR(255),
    test_session_id UUID,
    CONSTRAINT uq_test_access_tokens_token UNIQUE (token),
    CONSTRAINT fk_test_access_tokens_created_by_user_id FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_test_access_tokens_category_id FOREIGN KEY (category_id) REFERENCES test_categories(id),
    CONSTRAINT fk_test_access_tokens_used_by_user_id FOREIGN KEY (used_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_test_access_tokens_created_by_user_id ON test_access_tokens(created_by_user_id);
CREATE INDEX idx_test_access_tokens_category_id ON test_access_tokens(category_id);

ALTER TABLE test_sessions ALTER COLUMN candidate_user_id DROP NOT NULL;
ALTER TABLE test_sessions ADD COLUMN category_id UUID;
ALTER TABLE test_sessions ADD COLUMN access_token_id UUID;
ALTER TABLE test_sessions ADD COLUMN guest_identifier VARCHAR(255);
ALTER TABLE test_sessions ADD COLUMN guest_session_key VARCHAR(128);

UPDATE test_sessions SET category_id = '50000000-0000-0000-0000-000000000001' WHERE category_id IS NULL;
ALTER TABLE test_sessions ALTER COLUMN category_id SET NOT NULL;
ALTER TABLE test_sessions ADD CONSTRAINT fk_test_sessions_category_id FOREIGN KEY (category_id) REFERENCES test_categories(id);
ALTER TABLE test_sessions ADD CONSTRAINT fk_test_sessions_access_token_id FOREIGN KEY (access_token_id) REFERENCES test_access_tokens(id);

CREATE INDEX idx_test_sessions_category_id ON test_sessions(category_id);
CREATE INDEX idx_test_sessions_access_token_id ON test_sessions(access_token_id);
CREATE INDEX idx_test_sessions_guest_session_key ON test_sessions(guest_session_key);

ALTER TABLE test_access_tokens
    ADD CONSTRAINT fk_test_access_tokens_test_session_id FOREIGN KEY (test_session_id) REFERENCES test_sessions(id);
