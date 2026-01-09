-- Crear enum para estado de instancia de clase
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'class_instance_status') THEN
    CREATE TYPE class_instance_status AS ENUM ('SCHEDULED', 'COMPLETED', 'CANCELLED');
  END IF;
END$$;

-- Crear tabla class_instances
CREATE TABLE IF NOT EXISTS class_instances (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  class_type_id           UUID                NOT NULL,
  date                    DATE                NOT NULL,
  quarter_id              UUID                NOT NULL,
  is_holiday              BOOLEAN             NOT NULL DEFAULT FALSE,
  status                  class_instance_status NOT NULL DEFAULT 'SCHEDULED',
  cancellation_reason     VARCHAR(500),
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  
  CONSTRAINT fk_class_instance_type
    FOREIGN KEY (class_type_id)
    REFERENCES class_types(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    
  CONSTRAINT fk_class_instance_quarter
    FOREIGN KEY (quarter_id)
    REFERENCES quarters(id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

-- Crear índices
CREATE INDEX IF NOT EXISTS ix_class_instances_date ON class_instances(date);
CREATE INDEX IF NOT EXISTS ix_class_instances_class_type ON class_instances(class_type_id);
CREATE INDEX IF NOT EXISTS ix_class_instances_quarter ON class_instances(quarter_id);
CREATE INDEX IF NOT EXISTS ix_class_instances_status ON class_instances(status);

