-- Crear tabla quarters
CREATE TABLE IF NOT EXISTS quarters (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  name                    VARCHAR(50)         NOT NULL,
  start_date              DATE                NOT NULL,
  end_date                DATE                NOT NULL,
  is_active               BOOLEAN             NOT NULL DEFAULT FALSE,
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system'
);

-- Crear índices
CREATE INDEX IF NOT EXISTS ix_quarters_dates ON quarters(start_date, end_date);
CREATE INDEX IF NOT EXISTS ix_quarters_active ON quarters(is_active);

