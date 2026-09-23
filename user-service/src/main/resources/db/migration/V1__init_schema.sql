CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 10000) PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    surname    VARCHAR(100) NOT NULL,
    username   VARCHAR(50)  NOT NULL,
    birth_date DATE         NOT NULL,
    residence  VARCHAR(150) NOT NULL,
    created_at timestamptz  NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username
    ON users (username);

CREATE INDEX IF NOT EXISTS idx_users_created_at_desc
    ON users (created_at DESC);

CREATE TABLE IF NOT EXISTS friendships
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 10000) PRIMARY KEY,
    user_id
               BIGINT
        REFERENCES
            users
                (
                 id
                    ) ON DELETE CASCADE,
    id_friend  BIGINT REFERENCES users
        (
         id
            )
        ON DELETE CASCADE,
    created_at timestamptz NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_friendships_user_id
    ON friendships (user_id);

CREATE INDEX IF NOT EXISTS idx_friendships_id_friend
    ON friendships (id_friend DESC);

CREATE INDEX IF NOT EXISTS idx_friends_created_at_desc
    ON friendships (created_at DESC);

CREATE TABLE IF NOT EXISTS users_outbox
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_topic        VARCHAR(50) NOT NULL,
    event_id           VARCHAR(50) NOT NULL,
    event_payload      TEXT        NOT NULL,
    tracingspancontext TEXT        NOT NULL,
    event_type         VARCHAR(20) NOT NULL,
    event_group        VARCHAR(20) NOT NULL,
    event_status       VARCHAR(20) NOT NULL
);
ALTER TABLE users_outbox
    REPLICA IDENTITY DEFAULT;
CREATE TABLE users_outbox_heartbeat
(
    id INT PRIMARY KEY,
    ts TIMESTAMP NOT NULL
);
CREATE PUBLICATION users_outbox_publication FOR TABLE public.users_outbox, public.users_outbox_heartbeat;
SELECT pg_create_logical_replication_slot('users_outbox_slot', 'pgoutput');

CREATE INDEX IF NOT EXISTS idx_users_outbox_completed
    ON users_outbox (event_status)
    WHERE event_status = 'COMPLETED';

CREATE TABLE IF NOT EXISTS friendships_outbox
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_topic        VARCHAR(50) NOT NULL,
    event_id           VARCHAR(50) NOT NULL,
    event_payload      TEXT        NOT NULL,
    tracingspancontext TEXT        NOT NULL,
    event_type         VARCHAR(20) NOT NULL,
    event_group        VARCHAR(20) NOT NULL,
    event_status       VARCHAR(20) NOT NULL
);
ALTER TABLE friendships_outbox
    REPLICA IDENTITY DEFAULT;
CREATE TABLE friendships_outbox_heartbeat
(
    id INT PRIMARY KEY,
    ts TIMESTAMP NOT NULL
);
CREATE PUBLICATION friendships_outbox_publication FOR TABLE public.friendships_outbox, public.friendships_outbox_heartbeat;
SELECT pg_create_logical_replication_slot('friendships_outbox_slot', 'pgoutput');

CREATE INDEX IF NOT EXISTS idx_friendships_outbox_completed
    ON friendships_outbox (event_status)
    WHERE event_status = 'COMPLETED';
