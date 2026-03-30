CREATE TABLE questions (
    id UUID PRIMARY KEY,
    text TEXT NOT NULL,
    scale_weights_json TEXT NOT NULL,
    difficulty SMALLINT NOT NULL DEFAULT 1,
    priority INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE question_options (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL,
    option_order SMALLINT NOT NULL,
    option_text VARCHAR(500) NOT NULL,
    contribution_value NUMERIC(6,2) NOT NULL,
    scale_contributions_json TEXT,
    CONSTRAINT fk_question_options_question_id FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE,
    CONSTRAINT uq_question_options_order UNIQUE (question_id, option_order)
);

CREATE INDEX idx_question_options_question_id ON question_options (question_id);

CREATE TABLE test_sessions (
    id UUID PRIMARY KEY,
    candidate_user_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    CONSTRAINT fk_test_sessions_candidate_user_id FOREIGN KEY (candidate_user_id) REFERENCES users (id),
    CONSTRAINT chk_test_sessions_status CHECK (status IN ('created', 'in_progress', 'completed', 'cancelled'))
);

CREATE INDEX idx_test_sessions_candidate_user_id ON test_sessions (candidate_user_id);
CREATE INDEX idx_test_sessions_status ON test_sessions (status);

CREATE TABLE question_snapshots (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    question_id UUID,
    question_order INTEGER NOT NULL,
    question_text TEXT NOT NULL,
    scale_weights_json TEXT NOT NULL,
    difficulty SMALLINT NOT NULL,
    priority INTEGER NOT NULL,
    option_snapshots_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_question_snapshots_session_id FOREIGN KEY (session_id) REFERENCES test_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_question_snapshots_question_id FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE SET NULL,
    CONSTRAINT uq_question_snapshots_order UNIQUE (session_id, question_order)
);

CREATE INDEX idx_question_snapshots_session_id ON question_snapshots (session_id);

CREATE TABLE answers (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    question_snapshot_id UUID NOT NULL,
    selected_option_id UUID,
    answer_value NUMERIC(6,2),
    answered_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_answers_session_id FOREIGN KEY (session_id) REFERENCES test_sessions (id) ON DELETE CASCADE,
    CONSTRAINT fk_answers_question_snapshot_id FOREIGN KEY (question_snapshot_id) REFERENCES question_snapshots (id) ON DELETE CASCADE,
    CONSTRAINT fk_answers_selected_option_id FOREIGN KEY (selected_option_id) REFERENCES question_options (id) ON DELETE SET NULL,
    CONSTRAINT uq_answers_question_snapshot_id UNIQUE (question_snapshot_id)
);

CREATE INDEX idx_answers_session_id ON answers (session_id);

CREATE TABLE result_profiles (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL UNIQUE,
    attention_score NUMERIC(5,2) NOT NULL,
    stress_resistance_score NUMERIC(5,2) NOT NULL,
    responsibility_score NUMERIC(5,2) NOT NULL,
    adaptability_score NUMERIC(5,2) NOT NULL,
    decision_speed_accuracy_score NUMERIC(5,2) NOT NULL,
    summary TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_result_profiles_session_id FOREIGN KEY (session_id) REFERENCES test_sessions (id) ON DELETE CASCADE
);

INSERT INTO questions (id, text, scale_weights_json, difficulty, priority, is_active)
VALUES
    ('30000000-0000-0000-0000-000000000001', 'Как вы действуете, если одновременно поступают несколько срочных задач?', '{"attention": 0.35, "stress_resistance": 0.20, "responsibility": 0.20, "adaptability": 0.15, "decision_speed_accuracy": 0.10}', 2, 10, TRUE),
    ('30000000-0000-0000-0000-000000000002', 'Как вы обычно реагируете на резкое изменение рабочих требований?', '{"attention": 0.10, "stress_resistance": 0.20, "responsibility": 0.10, "adaptability": 0.45, "decision_speed_accuracy": 0.15}', 2, 9, TRUE),
    ('30000000-0000-0000-0000-000000000003', 'Я сохраняю концентрацию даже при длительной монотонной работе.', '{"attention": 0.55, "stress_resistance": 0.10, "responsibility": 0.20, "adaptability": 0.05, "decision_speed_accuracy": 0.10}', 1, 8, TRUE),
    ('30000000-0000-0000-0000-000000000004', 'Что вы делаете, если замечаете свою ошибку после отправки результата?', '{"attention": 0.15, "stress_resistance": 0.10, "responsibility": 0.55, "adaptability": 0.10, "decision_speed_accuracy": 0.10}', 3, 10, TRUE),
    ('30000000-0000-0000-0000-000000000005', 'Как вы принимаете решение в условиях ограниченного времени?', '{"attention": 0.15, "stress_resistance": 0.20, "responsibility": 0.10, "adaptability": 0.15, "decision_speed_accuracy": 0.40}', 3, 10, TRUE),
    ('30000000-0000-0000-0000-000000000006', 'Как вы действуете при высоком уровне стресса в течение смены?', '{"attention": 0.10, "stress_resistance": 0.55, "responsibility": 0.15, "adaptability": 0.10, "decision_speed_accuracy": 0.10}', 2, 9, TRUE),
    ('30000000-0000-0000-0000-000000000007', 'Как вы проверяете свою работу перед финальной отправкой?', '{"attention": 0.30, "stress_resistance": 0.05, "responsibility": 0.45, "adaptability": 0.05, "decision_speed_accuracy": 0.15}', 2, 8, TRUE),
    ('30000000-0000-0000-0000-000000000008', 'Как вы адаптируетесь к новой инструкции, полученной во время выполнения задачи?', '{"attention": 0.10, "stress_resistance": 0.10, "responsibility": 0.15, "adaptability": 0.50, "decision_speed_accuracy": 0.15}', 2, 7, TRUE),
    ('30000000-0000-0000-0000-000000000009', 'Как вы действуете, если нужно быстро выбрать между двумя похожими вариантами?', '{"attention": 0.15, "stress_resistance": 0.15, "responsibility": 0.10, "adaptability": 0.10, "decision_speed_accuracy": 0.50}', 3, 9, TRUE),
    ('30000000-0000-0000-0000-000000000010', 'К концу длительной смены я сохраняю рабочую продуктивность и точность.', '{"attention": 0.20, "stress_resistance": 0.30, "responsibility": 0.20, "adaptability": 0.20, "decision_speed_accuracy": 0.10}', 2, 6, TRUE);

INSERT INTO question_options (id, question_id, option_order, option_text, contribution_value)
SELECT
    ('40000000-0000-0000-0000-' || LPAD((row_number() OVER (ORDER BY v.question_id, v.option_order))::text, 12, '0'))::UUID,
    v.question_id,
    v.option_order,
    v.option_text,
    v.contribution_value
FROM (
    VALUES
        ('30000000-0000-0000-0000-000000000001'::UUID, 1::SMALLINT, 'Теряю приоритеты, начинаю делать задачи хаотично и часто ошибаюсь.', -2.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000001'::UUID, 2::SMALLINT, 'Беру первую попавшуюся задачу, остальные откладываю без оценки срочности.', -1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000001'::UUID, 3::SMALLINT, 'Пытаюсь быстро расставить приоритеты, но не всегда учитываю риски и зависимости.', 0.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000001'::UUID, 4::SMALLINT, 'Оцениваю срочность и влияние задач, выполняю их по приоритету и держу коллег в курсе.', 1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000001'::UUID, 5::SMALLINT, 'Быстро структурирую очередь по критичности, подтверждаю приоритеты с командой и контролирую качество выполнения.', 2.00::NUMERIC(6,2)),

        ('30000000-0000-0000-0000-000000000002'::UUID, 1::SMALLINT, 'Игнорирую изменения и продолжаю работать по старому плану.', -2.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000002'::UUID, 2::SMALLINT, 'Раздражаюсь и адаптируюсь только после повторного напоминания.', -1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000002'::UUID, 3::SMALLINT, 'Принимаю изменение, но трачу много времени на перестройку и прошу постоянную помощь.', 0.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000002'::UUID, 4::SMALLINT, 'Быстро уточняю новые требования, корректирую действия и продолжаю работу без потери качества.', 1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000002'::UUID, 5::SMALLINT, 'Оперативно перестраиваю план, проверяю критичные точки и помогаю коллегам синхронизироваться с новыми требованиями.', 2.00::NUMERIC(6,2)),

        ('30000000-0000-0000-0000-000000000003'::UUID, 1::SMALLINT, 'Совершенно не про меня', -2.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000003'::UUID, 2::SMALLINT, 'Скорее не про меня', -1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000003'::UUID, 3::SMALLINT, 'Частично про меня', 0.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000003'::UUID, 4::SMALLINT, 'В основном про меня', 1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000003'::UUID, 5::SMALLINT, 'Полностью про меня', 2.00::NUMERIC(6,2)),

        ('30000000-0000-0000-0000-000000000004'::UUID, 1::SMALLINT, 'Ничего не делаю: если уже отправлено, значит исправлять поздно.', -2.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000004'::UUID, 2::SMALLINT, 'Сообщаю об ошибке только если о ней спросят.', -1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000004'::UUID, 3::SMALLINT, 'Исправляю ошибку, но уведомляю не всех, кого это может затронуть.', 0.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000004'::UUID, 4::SMALLINT, 'Сразу сообщаю ответственным, отправляю корректировку и проверяю связанные данные.', 1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000004'::UUID, 5::SMALLINT, 'Немедленно фиксирую ошибку, уведомляю всех заинтересованных, документирую причину и предлагаю меры профилактики.', 2.00::NUMERIC(6,2)),

        ('30000000-0000-0000-0000-000000000005'::UUID, 1::SMALLINT, 'Принимаю решение наугад, чтобы быстрее закончить.', -2.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000005'::UUID, 2::SMALLINT, 'Сильно сомневаюсь и часто пропускаю дедлайн.', -1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000005'::UUID, 3::SMALLINT, 'Выбираю приемлемый вариант по основным признакам, но не всегда проверяю последствия.', 0.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000005'::UUID, 4::SMALLINT, 'Оцениваю ключевые риски, выбираю оптимальный вариант и контролирую результат.', 1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000005'::UUID, 5::SMALLINT, 'Использую четкий алгоритм приоритизации, быстро принимаю обоснованное решение и при необходимости эскалирую.', 2.00::NUMERIC(6,2)),

        ('30000000-0000-0000-0000-000000000006'::UUID, 1::SMALLINT, 'Под стрессом теряю самообладание и начинаю допускать критичные ошибки.', -2.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000006'::UUID, 2::SMALLINT, 'Работаю медленнее и часто откладываю сложные действия до конца смены.', -1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000006'::UUID, 3::SMALLINT, 'Сохраняю базовую работоспособность, но периодически теряю фокус.', 0.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000006'::UUID, 4::SMALLINT, 'Использую короткие техники самоконтроля, держу темп и качество на стабильном уровне.', 1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000006'::UUID, 5::SMALLINT, 'Системно управляю нагрузкой, сохраняю точность и при необходимости перераспределяю задачи с командой.', 2.00::NUMERIC(6,2)),

        ('30000000-0000-0000-0000-000000000007'::UUID, 1::SMALLINT, 'Отправляю результат без проверки, если времени мало.', -2.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000007'::UUID, 2::SMALLINT, 'Проверяю только очевидные ошибки, детали часто пропускаю.', -1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000007'::UUID, 3::SMALLINT, 'Делаю стандартную проверку, но не всегда сверяюсь с чек-листом.', 0.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000007'::UUID, 4::SMALLINT, 'Проверяю результат по чек-листу и при необходимости запрашиваю выборочную верификацию.', 1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000007'::UUID, 5::SMALLINT, 'Применяю многоступенчатую проверку: чек-лист, контроль критичных параметров и финальную самооценку рисков.', 2.00::NUMERIC(6,2)),

        ('30000000-0000-0000-0000-000000000008'::UUID, 1::SMALLINT, 'Продолжаю работать по старой инструкции, пока не заставят изменить подход.', -2.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000008'::UUID, 2::SMALLINT, 'Пытаюсь применить новую инструкцию, но часто путаюсь и допускаю повторные ошибки.', -1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000008'::UUID, 3::SMALLINT, 'Переключаюсь на новую инструкцию после дополнительного разъяснения.', 0.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000008'::UUID, 4::SMALLINT, 'Быстро уточняю спорные пункты и корректно применяю новую инструкцию в текущей задаче.', 1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000008'::UUID, 5::SMALLINT, 'Оперативно внедряю новую инструкцию, проверяю влияние на смежные шаги и фиксирую изменения для команды.', 2.00::NUMERIC(6,2)),

        ('30000000-0000-0000-0000-000000000009'::UUID, 1::SMALLINT, 'Выбираю случайно, не анализируя отличия между вариантами.', -2.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000009'::UUID, 2::SMALLINT, 'Долго колеблюсь и принимаю решение с задержкой.', -1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000009'::UUID, 3::SMALLINT, 'Сравниваю основные параметры и выбираю один вариант без глубокой проверки.', 0.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000009'::UUID, 4::SMALLINT, 'Быстро сравниваю критичные критерии и выбираю более надежный вариант.', 1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000009'::UUID, 5::SMALLINT, 'Использую понятные критерии выбора, при равенстве рисков запрашиваю подтверждение и фиксирую обоснование.', 2.00::NUMERIC(6,2)),

        ('30000000-0000-0000-0000-000000000010'::UUID, 1::SMALLINT, 'Совершенно не про меня', -2.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000010'::UUID, 2::SMALLINT, 'Скорее не про меня', -1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000010'::UUID, 3::SMALLINT, 'Частично про меня', 0.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000010'::UUID, 4::SMALLINT, 'В основном про меня', 1.00::NUMERIC(6,2)),
        ('30000000-0000-0000-0000-000000000010'::UUID, 5::SMALLINT, 'Полностью про меня', 2.00::NUMERIC(6,2))
) AS v(question_id, option_order, option_text, contribution_value);
