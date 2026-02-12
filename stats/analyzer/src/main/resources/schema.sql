-- 1. Таблица для хранения коэффициентов сходства мероприятий
-- Обновляется из топика stats.events-similarity.v1
CREATE TABLE IF NOT EXISTS event_similarity
(
    id         BIGSERIAL PRIMARY KEY,
    event_a_id INTEGER          NOT NULL,
    event_b_id INTEGER          NOT NULL,
    score      DOUBLE PRECISION NOT NULL CHECK (score >= 0 AND score <= 1),
    updated_at TIMESTAMP        NOT NULL DEFAULT NOW(),
    CONSTRAINT event_pair_unique UNIQUE (event_a_id, event_b_id),
    CONSTRAINT event_pair_order CHECK (event_a_id < event_b_id)
);

CREATE INDEX IF NOT EXISTS idx_event_similarity_event_a_id ON event_similarity (event_a_id);
CREATE INDEX IF NOT EXISTS idx_event_similarity_event_b_id ON event_similarity (event_b_id);
CREATE INDEX IF NOT EXISTS idx_event_similarity_score ON event_similarity (score DESC);

-- 2. Таблица для хранения истории взаимодействий пользователей с мероприятиями
-- Обновляется из топика stats.user-actions.v1
CREATE TABLE IF NOT EXISTS user_event_interaction
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     INTEGER          NOT NULL,
    event_id    INTEGER          NOT NULL,
    action_type VARCHAR(20)      NOT NULL, -- VIEW, REGISTER, LIKE
    weight      DOUBLE PRECISION NOT NULL, -- 0.4, 0.8, 1.0
    timestamp   TIMESTAMP        NOT NULL,
    CONSTRAINT user_event_unique UNIQUE (user_id, event_id)
);

CREATE INDEX IF NOT EXISTS idx_user_event_interaction_user_id ON user_event_interaction (user_id);
CREATE INDEX IF NOT EXISTS idx_user_event_interaction_event_id ON user_event_interaction (event_id);
CREATE INDEX IF NOT EXISTS idx_user_event_interaction_timestamp ON user_event_interaction (timestamp DESC);

-- 3. Таблица для весов действий (кэш маппинга)
CREATE TABLE IF NOT EXISTS action_weight
(
    action_type VARCHAR(20) PRIMARY KEY,
    weight      DOUBLE PRECISION NOT NULL
);

-- Инициализация весов
INSERT INTO action_weight (action_type, weight)
VALUES ('VIEW', 0.4),
       ('REGISTER', 0.8),
       ('LIKE', 1.0)
ON CONFLICT (action_type) DO UPDATE SET weight = EXCLUDED.weight;