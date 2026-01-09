
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
    CREATE TYPE user_role AS ENUM ('PLAYER','ADMIN');
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS users (
  email              VARCHAR(150)  PRIMARY KEY,
  first_name         VARCHAR(100)  NOT NULL,
  last_name          VARCHAR(100)  NOT NULL,
  birth_date         DATE          NOT NULL,
  license_number     VARCHAR(50)   UNIQUE,
  password_hash      TEXT          NOT NULL,
  role               user_role     NOT NULL DEFAULT 'PLAYER',
  created_date       TIMESTAMP     NOT NULL DEFAULT NOW(),
  created_by         VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_date       TIMESTAMP     NOT NULL DEFAULT NOW(),
  updated_by         VARCHAR(100)  NOT NULL DEFAULT 'system'
);

CREATE TABLE IF NOT EXISTS players (
  license_number     VARCHAR(50)   PRIMARY KEY,
  forehand           VARCHAR(20),
  backhand           VARCHAR(20),
  group_no           INTEGER,
  phase_month        DATE,

  created_date       TIMESTAMP     NOT NULL DEFAULT NOW(),
  created_by         VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_date       TIMESTAMP     NOT NULL DEFAULT NOW(),
  updated_by         VARCHAR(100)  NOT NULL DEFAULT 'system',

  CONSTRAINT fk_players_user
    FOREIGN KEY (license_number)
    REFERENCES users(license_number)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_players_month
  ON players (license_number, phase_month);

CREATE TABLE IF NOT EXISTS matches (
  id                 UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
  phase_month        DATE          NOT NULL,
  scheduled_at       TIMESTAMPTZ,
  played_at          TIMESTAMPTZ,
  player1_license    VARCHAR(50)   NOT NULL,
  player2_license    VARCHAR(50)   NOT NULL,
  set1_p1            SMALLINT,
  set1_p2            SMALLINT,
  set2_p1            SMALLINT,
  set2_p2            SMALLINT,
  set3_p1            SMALLINT,
  set3_p2            SMALLINT,
  winner_license     VARCHAR(50),
  created_date       TIMESTAMP     NOT NULL DEFAULT NOW(),
  created_by         VARCHAR(100)  NOT NULL DEFAULT 'system',
  updated_date       TIMESTAMP     NOT NULL DEFAULT NOW(),
  updated_by         VARCHAR(100)  NOT NULL DEFAULT 'system',

  CONSTRAINT fk_match_p1 FOREIGN KEY (player1_license)
    REFERENCES players(license_number)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT fk_match_p2 FOREIGN KEY (player2_license)
    REFERENCES players(license_number)
    ON UPDATE CASCADE ON DELETE RESTRICT,

  CONSTRAINT fk_match_winner FOREIGN KEY (winner_license)
    REFERENCES players(license_number)
    ON UPDATE CASCADE ON DELETE SET NULL,

  CONSTRAINT ck_match_distinct_players
    CHECK (player1_license <> player2_license)
);

-- Índice único conmutativo para evitar duplicar el emparejamiento en el mismo mes
-- (independiente del orden P1/P2)
CREATE UNIQUE INDEX IF NOT EXISTS ux_match_pair_month
  ON matches (
    LEAST(player1_license, player2_license),
    GREATEST(player1_license, player2_license),
    phase_month
  );

-- Índices de ayuda
CREATE INDEX IF NOT EXISTS ix_matches_month ON matches (phase_month);
CREATE INDEX IF NOT EXISTS ix_matches_p1 ON matches (player1_license);
CREATE INDEX IF NOT EXISTS ix_matches_p2 ON matches (player2_license);
