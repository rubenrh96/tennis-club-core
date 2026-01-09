-- Crear enums para tipos de pago y estado
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_type') THEN
    CREATE TYPE payment_type AS ENUM ('INDIVIDUAL_CLASS', 'CLASS_PACKAGE', 'QUARTERLY');
  END IF;
END$$;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_status') THEN
    CREATE TYPE payment_status AS ENUM ('PENDING', 'PAID');
  END IF;
END$$;

-- Crear tabla payments
CREATE TABLE IF NOT EXISTS payments (
  id                   UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
  license_number       VARCHAR(50)     NOT NULL,
  payment_type         payment_type    NOT NULL,
  amount               NUMERIC(10,2)   NOT NULL,
  payment_date         DATE            NOT NULL,
  status               payment_status  NOT NULL DEFAULT 'PENDING',
  class_session_id     UUID,
  class_date           DATE,
  classes_remaining    INTEGER,
  quarter_start_date   DATE,
  quarter_end_date     DATE,
  days_per_week        INTEGER,
  year                 INTEGER,
  quarter_number       INTEGER,
  notes                VARCHAR(500),
  
  created_date         TIMESTAMP       NOT NULL DEFAULT NOW(),
  created_by           VARCHAR(100)    NOT NULL DEFAULT 'system',
  updated_date         TIMESTAMP       NOT NULL DEFAULT NOW(),
  updated_by           VARCHAR(100)    NOT NULL DEFAULT 'system',
  
  CONSTRAINT fk_payment_player
    FOREIGN KEY (license_number)
    REFERENCES players(license_number)
    ON UPDATE CASCADE ON DELETE CASCADE
);

-- Crear índices
CREATE INDEX IF NOT EXISTS ix_payments_license ON payments(license_number);
CREATE INDEX IF NOT EXISTS ix_payments_date ON payments(payment_date);
CREATE INDEX IF NOT EXISTS ix_payments_status ON payments(status);




