-- Crear tabla class_types
CREATE TABLE IF NOT EXISTS class_types (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  day_of_week             VARCHAR(20)         NOT NULL,  -- MONDAY, TUESDAY, etc.
  start_time              TIME                NOT NULL,
  end_time                TIME,
  name                    VARCHAR(100),
  description             VARCHAR(500),
  max_capacity            INTEGER,
  is_active               BOOLEAN             NOT NULL DEFAULT TRUE,
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system'
);

-- Crear índices
CREATE INDEX IF NOT EXISTS ix_class_types_day ON class_types(day_of_week);
CREATE INDEX IF NOT EXISTS ix_class_types_active ON class_types(is_active);

