-- Crear enum para tipo de suscripción
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'subscription_type') THEN
    CREATE TYPE subscription_type AS ENUM ('INDIVIDUAL_CLASSES', 'CLASS_PACKAGE', 'QUARTERLY');
  END IF;
END$$;

-- Crear tabla player_subscriptions
CREATE TABLE IF NOT EXISTS player_subscriptions (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  license_number          VARCHAR(50)         NOT NULL,
  subscription_type       subscription_type    NOT NULL,
  is_active               BOOLEAN             NOT NULL DEFAULT TRUE,
  classes_remaining       INTEGER,
  package_purchase_date    DATE,
  days_per_week            INTEGER,
  current_quarter_start   DATE,
  current_quarter_end      DATE,
  auto_renew              BOOLEAN             NOT NULL DEFAULT FALSE,
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  
  CONSTRAINT fk_subscription_player
    FOREIGN KEY (license_number)
    REFERENCES players(license_number)
    ON UPDATE CASCADE ON DELETE CASCADE
);

-- Crear índices
CREATE INDEX IF NOT EXISTS ix_subscriptions_license ON player_subscriptions(license_number);
CREATE INDEX IF NOT EXISTS ix_subscriptions_active ON player_subscriptions(is_active);




