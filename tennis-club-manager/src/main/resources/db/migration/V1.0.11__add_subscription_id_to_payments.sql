-- Añadir columna subscription_id a la tabla payments para vincular pagos con suscripciones
ALTER TABLE payments
ADD COLUMN subscription_id UUID;

-- Crear índice para mejorar las consultas
CREATE INDEX IF NOT EXISTS ix_payments_subscription ON payments(subscription_id);

-- Añadir foreign key constraint
ALTER TABLE payments
ADD CONSTRAINT fk_payment_subscription
    FOREIGN KEY (subscription_id)
    REFERENCES player_subscriptions(id)
    ON UPDATE CASCADE ON DELETE SET NULL;

