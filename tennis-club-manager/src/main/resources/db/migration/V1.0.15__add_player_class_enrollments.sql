-- Crear tabla player_class_enrollments
CREATE TABLE IF NOT EXISTS player_class_enrollments (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  license_number          VARCHAR(50)         NOT NULL,
  class_type_id           UUID                NOT NULL,
  subscription_id         UUID,
  quarter_id              UUID                NOT NULL,
  is_active               BOOLEAN             NOT NULL DEFAULT TRUE,
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  
  CONSTRAINT fk_enrollment_player
    FOREIGN KEY (license_number)
    REFERENCES players(license_number)
    ON UPDATE CASCADE ON DELETE CASCADE,
    
  CONSTRAINT fk_enrollment_class_type
    FOREIGN KEY (class_type_id)
    REFERENCES class_types(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    
  CONSTRAINT fk_enrollment_subscription
    FOREIGN KEY (subscription_id)
    REFERENCES player_subscriptions(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
    
  CONSTRAINT fk_enrollment_quarter
    FOREIGN KEY (quarter_id)
    REFERENCES quarters(id)
    ON UPDATE CASCADE ON DELETE CASCADE
);

-- Crear índices
CREATE INDEX IF NOT EXISTS ix_enrollments_license ON player_class_enrollments(license_number);
CREATE INDEX IF NOT EXISTS ix_enrollments_class_type ON player_class_enrollments(class_type_id);
CREATE INDEX IF NOT EXISTS ix_enrollments_subscription ON player_class_enrollments(subscription_id);
CREATE INDEX IF NOT EXISTS ix_enrollments_quarter ON player_class_enrollments(quarter_id);

