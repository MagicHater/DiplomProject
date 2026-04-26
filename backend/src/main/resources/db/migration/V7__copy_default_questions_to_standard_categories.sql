WITH category_source AS (
    SELECT
        c.id AS category_id,
        c.code AS category_code,
        ROW_NUMBER() OVER (ORDER BY c.code) AS category_number
    FROM test_categories c
    WHERE c.code IN ('SMM', 'DESIGN', 'SAFETY')
), source_questions AS (
    SELECT
        q.*,
        ROW_NUMBER() OVER (ORDER BY q.priority DESC, q.difficulty ASC, q.created_at ASC, q.id ASC) AS question_number
    FROM questions q
    JOIN test_categories c ON c.id = q.category_id
    WHERE c.code = 'MARKETING'
), question_mapping AS (
    SELECT
        sq.id AS old_question_id,
        cs.category_id,
        cs.category_code,
        (
            '3' || cs.category_number::text || '000000-0000-0000-0000-' ||
            LPAD(sq.question_number::text, 12, '0')
        )::UUID AS new_question_id,
        sq.text,
        sq.scale_weights_json,
        sq.difficulty,
        sq.priority,
        sq.is_active,
        sq.created_at,
        sq.updated_at
    FROM category_source cs
    CROSS JOIN source_questions sq
)
INSERT INTO questions (
    id,
    text,
    scale_weights_json,
    difficulty,
    priority,
    is_active,
    created_at,
    updated_at,
    category_id
)
SELECT
    qm.new_question_id,
    qm.text,
    qm.scale_weights_json,
    qm.difficulty,
    qm.priority,
    qm.is_active,
    qm.created_at,
    qm.updated_at,
    qm.category_id
FROM question_mapping qm
WHERE NOT EXISTS (
    SELECT 1
    FROM questions existing
    WHERE existing.category_id = qm.category_id
      AND existing.text = qm.text
);

WITH category_source AS (
    SELECT
        c.id AS category_id,
        c.code AS category_code,
        ROW_NUMBER() OVER (ORDER BY c.code) AS category_number
    FROM test_categories c
    WHERE c.code IN ('SMM', 'DESIGN', 'SAFETY')
), source_questions AS (
    SELECT
        q.*,
        ROW_NUMBER() OVER (ORDER BY q.priority DESC, q.difficulty ASC, q.created_at ASC, q.id ASC) AS question_number
    FROM questions q
    JOIN test_categories c ON c.id = q.category_id
    WHERE c.code = 'MARKETING'
), question_mapping AS (
    SELECT
        sq.id AS old_question_id,
        cs.category_id,
        cs.category_code,
        (
            '3' || cs.category_number::text || '000000-0000-0000-0000-' ||
            LPAD(sq.question_number::text, 12, '0')
        )::UUID AS new_question_id
    FROM category_source cs
    CROSS JOIN source_questions sq
), option_mapping AS (
    SELECT
        qo.*,
        qm.new_question_id,
        ROW_NUMBER() OVER (ORDER BY qm.category_code, qm.new_question_id, qo.option_order) AS option_number
    FROM question_mapping qm
    JOIN question_options qo ON qo.question_id = qm.old_question_id
)
INSERT INTO question_options (
    id,
    question_id,
    option_order,
    option_text,
    contribution_value,
    scale_contributions_json
)
SELECT
    (
        '4' ||
        LPAD(((1000 + option_number)::text), 7, '0') ||
        '-0000-0000-0000-' ||
        LPAD(option_number::text, 12, '0')
    )::UUID,
    new_question_id,
    option_order,
    option_text,
    contribution_value,
    scale_contributions_json
FROM option_mapping om
WHERE EXISTS (
    SELECT 1 FROM questions q WHERE q.id = om.new_question_id
)
AND NOT EXISTS (
    SELECT 1
    FROM question_options existing
    WHERE existing.question_id = om.new_question_id
      AND existing.option_order = om.option_order
);
