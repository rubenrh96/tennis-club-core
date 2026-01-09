-- ============================================================================
-- MIGRACIÓN V2.1.0 - ELIMINACIÓN DE TABLAS OBSOLETAS DEL SISTEMA ANTIGUO
-- ============================================================================
-- 
-- Esta migración elimina las tablas, índices, foreign keys, enums y secuencias
-- del sistema antiguo que ya no se utilizan tras la refactorización al nuevo
-- modelo de gestión de club (ERP ligero).
--
-- Tablas obsoletas a eliminar:
--   - matches
--   - players
--   - player_subscriptions
--   - player_class_enrollments
--   - class_types
--   - class_instances
--   - class_consumptions
--   - quarters
--   - holidays
--
-- IMPORTANTE: No se modifica ninguna tabla del nuevo modelo:
--   - users
--   - sport_profiles
--   - services
--   - club_periods
--   - contracts
--   - payments
--   - calendar_events
--   - attendances
-- ============================================================================

-- ============================================================================
-- 1. ELIMINAR TABLAS CON DEPENDENCIAS (en orden inverso de creación)
-- ============================================================================

-- 1.1. Eliminar class_consumptions (depende de players, player_subscriptions, class_types)
DROP TABLE IF EXISTS class_consumptions CASCADE;

-- 1.2. Eliminar player_class_enrollments (depende de players, class_types, player_subscriptions, quarters)
DROP TABLE IF EXISTS player_class_enrollments CASCADE;

-- 1.3. Eliminar class_instances (depende de class_types, quarters)
DROP TABLE IF EXISTS class_instances CASCADE;

-- 1.4. Eliminar matches (depende de players)
DROP TABLE IF EXISTS matches CASCADE;

-- 1.5. Eliminar player_subscriptions (depende de players)
DROP TABLE IF EXISTS player_subscriptions CASCADE;

-- ============================================================================
-- 2. ELIMINAR TABLAS SIN DEPENDENCIAS DE OTRAS TABLAS OBSOLETAS
-- ============================================================================

-- 2.1. Eliminar class_types (no tiene dependencias de otras tablas obsoletas)
DROP TABLE IF EXISTS class_types CASCADE;

-- 2.2. Eliminar quarters (no tiene dependencias de otras tablas obsoletas)
DROP TABLE IF EXISTS quarters CASCADE;

-- 2.3. Eliminar holidays (no tiene dependencias)
DROP TABLE IF EXISTS holidays CASCADE;

-- ============================================================================
-- 3. ELIMINAR TABLA players (última, tiene FK a users del nuevo modelo)
-- ============================================================================
-- IMPORTANTE: players tiene una FK a users(license_number), pero users es del
-- nuevo modelo. Al usar CASCADE solo eliminamos la FK, no la tabla users.
DROP TABLE IF EXISTS players CASCADE;

-- ============================================================================
-- 4. ELIMINAR ENUMS OBSOLETOS
-- ============================================================================

-- 4.1. Eliminar enum subscription_type (usado en player_subscriptions)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'subscription_type') THEN
    DROP TYPE subscription_type CASCADE;
  END IF;
END$$;

-- 4.2. Eliminar enum class_instance_status (usado en class_instances)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'class_instance_status') THEN
    DROP TYPE class_instance_status CASCADE;
  END IF;
END$$;

-- NOTA: payment_type y payment_status NO se eliminan porque payments es del nuevo modelo
-- NOTA: user_role NO se elimina porque users es del nuevo modelo

-- ============================================================================
-- 5. LIMPIEZA DE ÍNDICES (ya eliminados automáticamente con CASCADE, pero
--    documentamos cuáles se eliminaron)
-- ============================================================================
-- Los siguientes índices se eliminaron automáticamente con DROP TABLE CASCADE:
--
-- Tabla: class_consumptions
--   - ix_consumptions_license
--   - ix_consumptions_subscription
--   - ix_consumptions_date
--
-- Tabla: player_class_enrollments
--   - ix_enrollments_license
--   - ix_enrollments_class_type
--   - ix_enrollments_subscription
--   - ix_enrollments_quarter
--
-- Tabla: class_instances
--   - ix_class_instances_date
--   - ix_class_instances_class_type
--   - ix_class_instances_quarter
--   - ix_class_instances_status
--
-- Tabla: matches
--   - ux_match_pair_month (índice único)
--   - ix_matches_month
--   - ix_matches_p1
--   - ix_matches_p2
--
-- Tabla: player_subscriptions
--   - ix_subscriptions_license
--   - ix_subscriptions_active
--
-- Tabla: class_types
--   - ix_class_types_day
--   - ix_class_types_active
--
-- Tabla: quarters
--   - ix_quarters_dates
--   - ix_quarters_active
--
-- Tabla: holidays
--   - ix_holidays_date
--   - ix_holidays_year
--   - ix_holidays_region
--
-- Tabla: players
--   - ux_players_month (índice único)
--
-- ============================================================================
-- FIN DE LA MIGRACIÓN
-- ============================================================================



