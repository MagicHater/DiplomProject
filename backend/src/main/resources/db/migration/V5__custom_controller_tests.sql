CREATE TABLE custom_tests (
    id UUID PRIMARY KEY,
    controller_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_custom_tests_controller FOREIGN KEY (controller_id) REFERENCES users (id)
);

CREATE INDEX idx_custom_tests_controller_id ON custom_tests (controller_id);

CREATE TABLE custom_test_questions (
    id UUID PRIMARY KEY,
    test_id UUID NOT NULL,
    question_order INT NOT NULL,
    text TEXT NOT NULL,
    CONSTRAINT fk_custom_test_questions_test FOREIGN KEY (test_id) REFERENCES custom_tests (id) ON DELETE CASCADE,
    CONSTRAINT uq_custom_test_question_order UNIQUE (test_id, question_order)
);

CREATE TABLE custom_test_options (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL,
    option_order INT NOT NULL,
    text TEXT NOT NULL,
    CONSTRAINT fk_custom_test_options_question FOREIGN KEY (question_id) REFERENCES custom_test_questions (id) ON DELETE CASCADE,
    CONSTRAINT uq_custom_test_option_order UNIQUE (question_id, option_order)
);

CREATE TABLE custom_test_allowed_emails (
    id UUID PRIMARY KEY,
    test_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT fk_custom_test_allowed_emails_test FOREIGN KEY (test_id) REFERENCES custom_tests (id) ON DELETE CASCADE,
    CONSTRAINT uq_custom_test_allowed_email UNIQUE (test_id, email)
);

CREATE INDEX idx_custom_test_allowed_email ON custom_test_allowed_emails (email);

CREATE TABLE custom_test_submissions (
    id UUID PRIMARY KEY,
    test_id UUID NOT NULL,
    user_id UUID NOT NULL,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_custom_test_submissions_test FOREIGN KEY (test_id) REFERENCES custom_tests (id) ON DELETE CASCADE,
    CONSTRAINT fk_custom_test_submissions_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_custom_test_submissions_test_id ON custom_test_submissions (test_id);

CREATE TABLE custom_test_submission_answers (
    id UUID PRIMARY KEY,
    submission_id UUID NOT NULL,
    question_id UUID NOT NULL,
    option_id UUID NOT NULL,
    CONSTRAINT fk_custom_test_submission_answers_submission FOREIGN KEY (submission_id) REFERENCES custom_test_submissions (id) ON DELETE CASCADE,
    CONSTRAINT fk_custom_test_submission_answers_question FOREIGN KEY (question_id) REFERENCES custom_test_questions (id),
    CONSTRAINT fk_custom_test_submission_answers_option FOREIGN KEY (option_id) REFERENCES custom_test_options (id),
    CONSTRAINT uq_custom_submission_question UNIQUE (submission_id, question_id)
);
