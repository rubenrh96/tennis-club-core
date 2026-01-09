-- Crear tabla class_consumptions
CREATE TABLE IF NOT EXISTS class_consumptions (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  license_number          VARCHAR(50)         NOT NULL,
  subscription_id         UUID                NOT NULL,
  class_date              DATE                NOT NULL,
  class_time              TIME,
  class_type_id           UUID,
  consumed_by             VARCHAR(50)          NOT NULL DEFAULT 'ADMIN',
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  
  CONSTRAINT fk_consumption_player
    FOREIGN KEY (license_number)
    REFERENCES players(license_number)
    ON UPDATE CASCADE ON DELETE CASCADE,
    
  CONSTRAINT fk_consumption_subscription
    FOREIGN KEY (subscription_id)
    REFERENCES player_subscriptions(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    
  CONSTRAINT fk_consumption_class_type
    FOREIGN KEY (class_type_id)
    REFERENCES class_types(id)
    ON UPDATE CASCADE ON DELETE SET NULL
);

-- Crear índices
CREATE INDEX IF NOT EXISTS ix_consumptions_license ON class_consumptions(license_number);
CREATE INDEX IF NOT EXISTS ix_consumptions_subscription ON class_consumptions(subscription_id);
CREATE INDEX IF NOT EXISTS ix_consumptions_date ON class_consumptions(class_date);

