-- ============================================================================
-- MIGRACIÓN V2.0.1: Corrección de Problemas Críticos
-- ============================================================================
-- Esta migración corrige los problemas críticos identificados en la review
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. CORREGIR MIGRACIÓN DE ENUM user_role
-- ----------------------------------------------------------------------------
-- Primero migrar datos existentes de PLAYER a ALUMNO (si existen)
-- Esto maneja el caso donde V2.0.0 no migró correctamente los datos
DO $$
DECLARE
  role_type text;
  has_player boolean;
BEGIN
  -- Obtener el tipo de dato actual de la columna role
  SELECT data_type INTO role_type
  FROM information_schema.columns
  WHERE table_name = 'users' AND column_name = 'role';
  
  -- Verificar si existe el enum user_role
  IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
    -- Verificar si el enum tiene el valor 'PLAYER'
    SELECT EXISTS (
      SELECT 1 FROM pg_enum 
      WHERE enumlabel = 'PLAYER' 
      AND enumtypid = (SELECT oid FROM pg_type WHERE typname = 'user_role')
    ) INTO has_player;
    
    -- Si tiene PLAYER, migrar datos
    IF has_player THEN
      -- Migrar datos primero (usar texto para evitar problemas de tipo)
      EXECUTE 'UPDATE users SET role = ''ALUMNO''::user_role WHERE role::text = ''PLAYER''';
      
      -- Cambiar a VARCHAR temporalmente
      ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(20);
      DROP TYPE user_role;
      CREATE TYPE user_role AS ENUM ('ADMIN', 'MONITOR', 'ALUMNO');
      ALTER TABLE users ALTER COLUMN role TYPE user_role USING role::text::user_role;
    END IF;
  ELSIF role_type = 'character varying' THEN
    -- Si ya es VARCHAR, migrar directamente
    UPDATE users SET role = 'ALUMNO' WHERE role::text = 'PLAYER';
    
    -- Crear el enum si no existe
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
      CREATE TYPE user_role AS ENUM ('ADMIN', 'MONITOR', 'ALUMNO');
      ALTER TABLE users ALTER COLUMN role TYPE user_role USING role::text::user_role;
    END IF;
  END IF;
END$$;

-- ----------------------------------------------------------------------------
-- 2. ELIMINAR CAMPO REDUNDANTE classes_remaining
-- ----------------------------------------------------------------------------
ALTER TABLE contracts DROP COLUMN IF EXISTS classes_remaining;

-- Nota: Se calcula en consulta como (total_classes - classes_used)

-- ----------------------------------------------------------------------------
-- 3. CORREGIR ÍNDICE DE CALENDARIO MENSUAL
-- ----------------------------------------------------------------------------
-- Eliminar índice funcional ineficiente (si existe)
DROP INDEX IF EXISTS ix_events_calendar_month;

-- Agregar índices eficientes para consultas mensuales
-- Este índice optimiza consultas de rango de fechas (más eficiente que función)
CREATE INDEX IF NOT EXISTS ix_events_calendar_range 
  ON calendar_events(start_datetime, event_type, status);

-- Índice parcial para eventos activos programados (optimiza vista mensual)
CREATE INDEX IF NOT EXISTS ix_events_active_scheduled
  ON calendar_events(start_datetime, event_type)
  WHERE status IN ('SCHEDULED', 'CONFIRMED');

-- ----------------------------------------------------------------------------
-- 4. AGREGAR ÍNDICES COMPUESTOS FALTANTES
-- ----------------------------------------------------------------------------

-- Contratos activos de usuario (consulta muy común)
-- Nota: Usa user_email por ahora. Si se migra a user_id, actualizar en V2.0.2
CREATE INDEX IF NOT EXISTS ix_contracts_user_active 
  ON contracts(user_email, status) 
  WHERE status = 'ACTIVE';

-- Contratos pendientes o activos de usuario
CREATE INDEX IF NOT EXISTS ix_contracts_user_pending_active
  ON contracts(user_email, status)
  WHERE status IN ('PENDING', 'ACTIVE');

-- Pagos pendientes ordenados por vencimiento (crítico para gestión)
CREATE INDEX IF NOT EXISTS ix_payments_pending_due 
  ON payments(status, due_date, payment_date)
  WHERE status = 'PENDING' AND due_date IS NOT NULL;

-- Bonos activos con clases disponibles
CREATE INDEX IF NOT EXISTS ix_contracts_active_packages
  ON contracts(service_id, status, total_classes, classes_used)
  WHERE total_classes IS NOT NULL AND status = 'ACTIVE';

-- ----------------------------------------------------------------------------
-- 5. SIMPLIFICAR services PARA V1 (Eliminar sobreingeniería)
-- ----------------------------------------------------------------------------
-- Eliminar campos innecesarios para V1
-- Nota: Estos campos se pueden agregar en el futuro si se necesitan
DO $$
BEGIN
  -- Eliminar columnas solo si existen (evita errores si ya se eliminaron)
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'services' AND column_name = 'frequency') THEN
    ALTER TABLE services DROP COLUMN frequency;
  END IF;
  
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'services' AND column_name = 'duration_minutes') THEN
    ALTER TABLE services DROP COLUMN duration_minutes;
  END IF;
  
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'services' AND column_name = 'applies_to_quarters') THEN
    ALTER TABLE services DROP COLUMN applies_to_quarters;
  END IF;
  
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'services' AND column_name = 'requires_monitor') THEN
    ALTER TABLE services DROP COLUMN requires_monitor;
  END IF;
END$$;

-- Agregar check constraint para day_of_week (validación)
ALTER TABLE services 
  DROP CONSTRAINT IF EXISTS ck_service_day_of_week,
  ADD CONSTRAINT ck_service_day_of_week
    CHECK (
      day_of_week IS NULL OR 
      day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')
    );

-- ----------------------------------------------------------------------------
-- 6. SIMPLIFICAR calendar_events PARA V1
-- ----------------------------------------------------------------------------
-- Eliminar campos de recurrencia (para futuras funcionalidades)
-- Nota: Estos campos se pueden agregar cuando se implemente la funcionalidad
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'calendar_events' AND column_name = 'is_recurring') THEN
    ALTER TABLE calendar_events DROP COLUMN is_recurring;
  END IF;
  
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'calendar_events' AND column_name = 'recurrence_pattern') THEN
    ALTER TABLE calendar_events DROP COLUMN recurrence_pattern;
  END IF;
END$$;

-- ----------------------------------------------------------------------------
-- 7. AGREGAR VALIDACIÓN: day_of_week debe ser consistente con service_type
-- ----------------------------------------------------------------------------
-- Para clases grupales trimestrales, day_of_week debe estar definido
ALTER TABLE services 
  DROP CONSTRAINT IF EXISTS ck_service_day_required,
  ADD CONSTRAINT ck_service_day_required
    CHECK (
      (service_type = 'QUARTERLY_GROUP_CLASS' AND day_of_week IS NOT NULL) OR
      (service_type != 'QUARTERLY_GROUP_CLASS')
    );

-- ----------------------------------------------------------------------------
-- 8. MEJORAR RESTRICCIÓN DE BONOS
-- ----------------------------------------------------------------------------
-- Para bonos, package_validity_days debe estar definido
ALTER TABLE services 
  DROP CONSTRAINT IF EXISTS ck_service_package_validity,
  ADD CONSTRAINT ck_service_package_validity
    CHECK (
      (service_type = 'INDIVIDUAL_CLASS_PACKAGE' AND package_validity_days IS NOT NULL AND package_validity_days > 0) OR
      (service_type != 'INDIVIDUAL_CLASS_PACKAGE')
    );

-- ----------------------------------------------------------------------------
-- 9. AGREGAR COMENTARIOS ADICIONALES
-- ----------------------------------------------------------------------------
COMMENT ON COLUMN contracts.classes_used IS 'Clases consumidas. Clases restantes = total_classes - classes_used (calculado)';
COMMENT ON COLUMN contracts.total_classes IS 'Total de clases del bono. NULL para contratos que no son bonos.';

-- Comentarios de índices
DO $$
BEGIN
  -- Comentarios se agregan después de crear los índices
  IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'ix_contracts_user_active') THEN
    EXECUTE 'COMMENT ON INDEX ix_contracts_user_active IS ''Optimiza consultas de contratos activos por usuario''';
  END IF;
  
  IF EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'ix_payments_pending_due') THEN
    EXECUTE 'COMMENT ON INDEX ix_payments_pending_due IS ''Optimiza consultas de pagos pendientes ordenados por vencimiento''';
  END IF;
END$$;

-- ============================================================================
-- FIN DE LA MIGRACIÓN V2.0.1
-- ============================================================================
-- NOTAS:
-- 1. Esta migración corrige problemas críticos identificados en la review
-- 2. El cambio de email PK a id UUID se debe hacer en una migración separada
--    (V2.0.2) debido a su complejidad y necesidad de migración de datos
-- 3. Los índices agregados mejoran significativamente el rendimiento de
--    consultas comunes del sistema
-- ============================================================================

