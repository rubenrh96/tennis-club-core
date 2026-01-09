-- Crear tabla holidays
CREATE TABLE IF NOT EXISTS holidays (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  date                    DATE                NOT NULL UNIQUE,
  name                    VARCHAR(200)        NOT NULL,
  region                  VARCHAR(100),
  is_national             BOOLEAN             NOT NULL DEFAULT TRUE,
  year                    INTEGER,
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system'
);

-- Crear índices
CREATE INDEX IF NOT EXISTS ix_holidays_date ON holidays(date);
CREATE INDEX IF NOT EXISTS ix_holidays_year ON holidays(year);
CREATE INDEX IF NOT EXISTS ix_holidays_region ON holidays(region);

