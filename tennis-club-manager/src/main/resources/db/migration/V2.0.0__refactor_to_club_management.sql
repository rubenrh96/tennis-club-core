-- ============================================================================
-- MIGRACIÓN V2.0.0: Refactorización a Sistema de Gestión Administrativa
-- ============================================================================
-- Este script refactoriza el sistema de ligas/partidos hacia un ERP ligero
-- para gestión administrativa del club (servicios, contratos, pagos, calendario)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. ACTUALIZAR ENUM DE ROLES
-- ----------------------------------------------------------------------------
-- Agregar nuevos roles: MONITOR y ALUMNO
DO $$
BEGIN
  -- Si el enum existe, necesitamos migrarlo correctamente
  IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
    -- Paso 1: Eliminar el valor por defecto (si existe)
    ALTER TABLE users ALTER COLUMN role DROP DEFAULT;
    
    -- Paso 2: Cambiar el tipo de la columna a VARCHAR temporalmente
    -- Esto automáticamente convierte los valores del enum a texto
    ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(20) USING role::text;
    
    -- Paso 3: Migrar datos existentes de PLAYER a ALUMNO (si existen)
    -- Ahora que es VARCHAR, podemos hacer el UPDATE sin problemas
    UPDATE users SET role = 'ALUMNO' WHERE role = 'PLAYER';
    
    -- Paso 4: Eliminar el enum antiguo (ahora sin dependencias)
    DROP TYPE user_role;
  END IF;
  
  -- Paso 5: Crear nuevo enum con roles actualizados
  -- Solo crear si no existe (por si acaso)
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
    CREATE TYPE user_role AS ENUM ('ADMIN', 'MONITOR', 'ALUMNO');
  END IF;
END$$;

-- Paso 6: Actualizar la columna role en users al nuevo enum
-- Solo si la columna es VARCHAR (si ya es enum, no hacer nada)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'users' 
    AND column_name = 'role' 
    AND data_type = 'character varying'
  ) THEN
    ALTER TABLE users ALTER COLUMN role TYPE user_role USING role::text::user_role;
  END IF;
END$$;

-- Paso 7: Restaurar el valor por defecto
ALTER TABLE users ALTER COLUMN role SET DEFAULT 'ALUMNO'::user_role;

-- ----------------------------------------------------------------------------
-- 2. REFACTORIZAR PLAYERS A SPORT_PROFILES (Perfil Deportivo Opcional)
-- ----------------------------------------------------------------------------
-- Crear nueva tabla sport_profiles (perfil deportivo opcional)
CREATE TABLE IF NOT EXISTS sport_profiles (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  user_email              VARCHAR(150)        NOT NULL UNIQUE,
  license_number          VARCHAR(50)         UNIQUE,
  forehand                VARCHAR(20),
  backhand                VARCHAR(20),
  level                   VARCHAR(50),        -- Nivel del jugador (principiante, intermedio, avanzado)
  notes                   VARCHAR(500),       -- Notas adicionales del perfil deportivo
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  
  CONSTRAINT fk_sport_profile_user
    FOREIGN KEY (user_email)
    REFERENCES users(email)
    ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_sport_profiles_user ON sport_profiles(user_email);
CREATE INDEX IF NOT EXISTS ix_sport_profiles_license ON sport_profiles(license_number);

-- ----------------------------------------------------------------------------
-- 3. CREAR TABLA DE SERVICIOS (Tipos de Servicios/Clases)
-- ----------------------------------------------------------------------------
-- Enum para tipo de servicio
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'service_type') THEN
    CREATE TYPE service_type AS ENUM (
      'QUARTERLY_GROUP_CLASS',    -- Clase grupal trimestral (escuela)
      'INDIVIDUAL_CLASS_PACKAGE', -- Bono de clases individuales (ej: 10 clases)
      'SINGLE_INDIVIDUAL_CLASS'   -- Clase individual suelta
    );
  END IF;
END$$;

-- Enum para frecuencia de servicio
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'service_frequency') THEN
    CREATE TYPE service_frequency AS ENUM (
      'WEEKLY',      -- Semanal
      'BIWEEKLY',    -- Quincenal
      'MONTHLY',     -- Mensual
      'QUARTERLY',   -- Trimestral
      'ONE_TIME'     -- Una sola vez
    );
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS services (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  code                    VARCHAR(50)         NOT NULL UNIQUE,  -- Código único del servicio (ej: "ESCUELA-LUNES-18H")
  name                    VARCHAR(200)         NOT NULL,         -- Nombre del servicio
  description             VARCHAR(1000),                        -- Descripción detallada
  service_type            service_type         NOT NULL,         -- Tipo de servicio
  frequency               service_frequency,                     -- Frecuencia (para clases grupales)
  
  -- Configuración de horario (para clases grupales)
  day_of_week             VARCHAR(20),                          -- Día de la semana (MONDAY, TUESDAY, etc.)
  start_time              TIME,                                  -- Hora de inicio
  end_time                TIME,                                  -- Hora de fin
  duration_minutes        INTEGER,                               -- Duración en minutos
  
  -- Configuración de precio
  base_price              NUMERIC(10,2)        NOT NULL,         -- Precio base
  currency                VARCHAR(3)           NOT NULL DEFAULT 'EUR',
  
  -- Configuración de capacidad (para clases grupales)
  max_capacity            INTEGER,                               -- Capacidad máxima
  min_capacity            INTEGER,                               -- Capacidad mínima
  
  -- Configuración de bonos (para bonos de clases)
  classes_in_package      INTEGER,                               -- Número de clases en el bono (ej: 10)
  package_validity_days   INTEGER,                               -- Días de validez del bono
  
  -- Configuración de trimestres (para clases trimestrales)
  applies_to_quarters     BOOLEAN             DEFAULT FALSE,     -- Si aplica a trimestres específicos
  
  -- Estado y configuración
  is_active               BOOLEAN             NOT NULL DEFAULT TRUE,
  requires_monitor        BOOLEAN             NOT NULL DEFAULT TRUE,  -- Si requiere monitor asignado
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  
  CONSTRAINT ck_service_price_positive CHECK (base_price >= 0),
  CONSTRAINT ck_service_capacity CHECK (max_capacity IS NULL OR min_capacity IS NULL OR max_capacity >= min_capacity),
  CONSTRAINT ck_service_package CHECK (
    (service_type = 'INDIVIDUAL_CLASS_PACKAGE' AND classes_in_package IS NOT NULL AND classes_in_package > 0) OR
    (service_type != 'INDIVIDUAL_CLASS_PACKAGE')
  )
);

CREATE INDEX IF NOT EXISTS ix_services_code ON services(code);
CREATE INDEX IF NOT EXISTS ix_services_type ON services(service_type);
CREATE INDEX IF NOT EXISTS ix_services_active ON services(is_active);
CREATE INDEX IF NOT EXISTS ix_services_day_time ON services(day_of_week, start_time) WHERE day_of_week IS NOT NULL;

-- ----------------------------------------------------------------------------
-- 4. CREAR TABLA DE PERIODOS DEL CLUB (Trimestres, Vacaciones, Cierres)
-- ----------------------------------------------------------------------------
-- Enum para tipo de periodo
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'period_type') THEN
    CREATE TYPE period_type AS ENUM (
      'QUARTER',        -- Trimestre (enero-marzo, abril-junio, septiembre-diciembre)
      'HOLIDAY',        -- Vacaciones (julio-agosto)
      'CLOSURE',        -- Cierre temporal
      'SPECIAL'         -- Periodo especial
    );
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS club_periods (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  name                    VARCHAR(200)         NOT NULL,         -- Nombre del periodo (ej: "Trimestre 1 - 2025")
  period_type             period_type          NOT NULL,
  start_date              DATE                 NOT NULL,
  end_date                DATE                 NOT NULL,
  is_active               BOOLEAN             NOT NULL DEFAULT TRUE,
  description             VARCHAR(500),                         -- Descripción del periodo
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  
  CONSTRAINT ck_period_dates CHECK (end_date >= start_date)
);

CREATE INDEX IF NOT EXISTS ix_periods_type ON club_periods(period_type);
CREATE INDEX IF NOT EXISTS ix_periods_dates ON club_periods(start_date, end_date);
CREATE INDEX IF NOT EXISTS ix_periods_active ON club_periods(is_active);

-- ----------------------------------------------------------------------------
-- 5. CREAR TABLA DE CONTRATOS (Relación Usuario-Servicio)
-- ----------------------------------------------------------------------------
-- Enum para estado de contrato
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'contract_status') THEN
    CREATE TYPE contract_status AS ENUM (
      'ACTIVE',         -- Contrato activo
      'SUSPENDED',      -- Contrato suspendido temporalmente
      'CANCELLED',      -- Contrato cancelado
      'EXPIRED',        -- Contrato expirado
      'PENDING'         -- Contrato pendiente de activación
    );
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS contracts (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  contract_number         VARCHAR(50)         NOT NULL UNIQUE,   -- Número de contrato único
  user_email              VARCHAR(150)        NOT NULL,          -- Usuario (alumno)
  service_id              UUID                NOT NULL,          -- Servicio contratado
  monitor_email           VARCHAR(150),                          -- Monitor asignado (opcional)
  
  -- Estado del contrato
  status                  contract_status     NOT NULL DEFAULT 'PENDING',
  
  -- Fechas del contrato
  start_date              DATE                 NOT NULL,
  end_date                DATE,                                  -- NULL para contratos sin fecha de fin
  signed_date             DATE,                                  -- Fecha de firma
  
  -- Configuración específica del contrato
  price                   NUMERIC(10,2)       NOT NULL,          -- Precio acordado (puede diferir del precio base)
  currency                VARCHAR(3)           NOT NULL DEFAULT 'EUR',
  
  -- Para bonos de clases
  total_classes           INTEGER,                               -- Total de clases (para bonos)
  classes_used            INTEGER             NOT NULL DEFAULT 0, -- Clases consumidas
  classes_remaining       INTEGER,                               -- Clases restantes (calculado)
  
  -- Para clases trimestrales
  period_id               UUID,                                  -- Periodo asociado (trimestre)
  days_per_week           INTEGER,                               -- Días por semana (para clases trimestrales)
  
  -- Renovación automática
  auto_renew              BOOLEAN             NOT NULL DEFAULT FALSE,
  
  -- Notas y observaciones
  notes                   VARCHAR(1000),                         -- Notas del contrato
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  
  CONSTRAINT fk_contract_user
    FOREIGN KEY (user_email)
    REFERENCES users(email)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    
  CONSTRAINT fk_contract_service
    FOREIGN KEY (service_id)
    REFERENCES services(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    
  CONSTRAINT fk_contract_monitor
    FOREIGN KEY (monitor_email)
    REFERENCES users(email)
    ON UPDATE CASCADE ON DELETE SET NULL,
    
  CONSTRAINT fk_contract_period
    FOREIGN KEY (period_id)
    REFERENCES club_periods(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
    
  CONSTRAINT ck_contract_price_positive CHECK (price >= 0),
  CONSTRAINT ck_contract_dates CHECK (end_date IS NULL OR end_date >= start_date),
  CONSTRAINT ck_contract_classes CHECK (
    (total_classes IS NULL) OR 
    (total_classes IS NOT NULL AND classes_used <= total_classes)
  )
);

CREATE INDEX IF NOT EXISTS ix_contracts_number ON contracts(contract_number);
CREATE INDEX IF NOT EXISTS ix_contracts_user ON contracts(user_email);
CREATE INDEX IF NOT EXISTS ix_contracts_service ON contracts(service_id);
CREATE INDEX IF NOT EXISTS ix_contracts_monitor ON contracts(monitor_email);
CREATE INDEX IF NOT EXISTS ix_contracts_status ON contracts(status);
CREATE INDEX IF NOT EXISTS ix_contracts_dates ON contracts(start_date, end_date);
CREATE INDEX IF NOT EXISTS ix_contracts_period ON contracts(period_id);

-- ----------------------------------------------------------------------------
-- 6. CREAR TABLA DE PAGOS (Pagos asociados a Contratos)
-- ----------------------------------------------------------------------------
-- Actualizar enum de estado de pago
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_status') THEN
    CREATE TYPE payment_status AS ENUM ('PENDING', 'PAID', 'FAILED', 'REFUNDED', 'CANCELLED');
  ELSIF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'FAILED' AND enumtypid = (SELECT oid FROM pg_type WHERE typname = 'payment_status')) THEN
    -- Agregar nuevos estados si el enum ya existe
    ALTER TYPE payment_status ADD VALUE 'FAILED';
    ALTER TYPE payment_status ADD VALUE 'REFUNDED';
    ALTER TYPE payment_status ADD VALUE 'CANCELLED';
  END IF;
END$$;

-- Enum para método de pago
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_method') THEN
    CREATE TYPE payment_method AS ENUM (
      'CASH',           -- Efectivo
      'CARD',           -- Tarjeta
      'BANK_TRANSFER',  -- Transferencia bancaria
      'CHECK',          -- Cheque
      'OTHER'           -- Otro
    );
  END IF;
END$$;

-- Modificar tabla payments si ya existe (de migración anterior)
DO $$
BEGIN
  -- Verificar si la tabla payments existe con la estructura antigua
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_name = 'payments'
  ) AND EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'payments' AND column_name = 'license_number'
  ) THEN
    -- La tabla existe con estructura antigua, necesitamos modificarla
    
    -- Eliminar índices antiguos
    DROP INDEX IF EXISTS ix_payments_license;
    
    -- Eliminar constraint antiguo
    ALTER TABLE payments DROP CONSTRAINT IF EXISTS fk_payment_player;
    
    -- Eliminar columnas antiguas que no se necesitan
    ALTER TABLE payments DROP COLUMN IF EXISTS license_number;
    ALTER TABLE payments DROP COLUMN IF EXISTS payment_type;
    ALTER TABLE payments DROP COLUMN IF EXISTS class_session_id;
    ALTER TABLE payments DROP COLUMN IF EXISTS class_date;
    ALTER TABLE payments DROP COLUMN IF EXISTS classes_remaining;
    ALTER TABLE payments DROP COLUMN IF EXISTS quarter_start_date;
    ALTER TABLE payments DROP COLUMN IF EXISTS quarter_end_date;
    ALTER TABLE payments DROP COLUMN IF EXISTS days_per_week;
    ALTER TABLE payments DROP COLUMN IF EXISTS year;
    ALTER TABLE payments DROP COLUMN IF EXISTS quarter_number;
    
    -- Agregar nuevas columnas
    ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_number VARCHAR(50);
    ALTER TABLE payments ADD COLUMN IF NOT EXISTS contract_id UUID;
    ALTER TABLE payments ADD COLUMN IF NOT EXISTS user_email VARCHAR(150);
    ALTER TABLE payments ADD COLUMN IF NOT EXISTS currency VARCHAR(3) DEFAULT 'EUR';
    ALTER TABLE payments ADD COLUMN IF NOT EXISTS due_date DATE;
    ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_method payment_method;
    ALTER TABLE payments ADD COLUMN IF NOT EXISTS reference_number VARCHAR(100);
    ALTER TABLE payments ADD COLUMN IF NOT EXISTS invoice_number VARCHAR(50);
    ALTER TABLE payments ADD COLUMN IF NOT EXISTS invoice_date DATE;
    
    -- Generar payment_number para registros existentes (si no tienen)
    UPDATE payments 
    SET payment_number = 'PAY-' || TO_CHAR(payment_date, 'YYYY-MM-DD') || '-' || SUBSTRING(id::text, 1, 8)
    WHERE payment_number IS NULL;
    
    -- Hacer payment_number NOT NULL después de poblarlo
    ALTER TABLE payments ALTER COLUMN payment_number SET NOT NULL;
    
    -- NOTA: contract_id y user_email se dejan nullable temporalmente
    -- porque los pagos antiguos no tienen esta información.
    -- Se harán NOT NULL en una migración posterior cuando se migren los datos
    -- o cuando se creen nuevos pagos con contratos.
    
    -- Agregar constraint único para payment_number
    CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_number ON payments(payment_number);
    
    -- Agregar constraints de check
    ALTER TABLE payments DROP CONSTRAINT IF EXISTS ck_payment_amount_positive;
    ALTER TABLE payments ADD CONSTRAINT ck_payment_amount_positive CHECK (amount > 0);
    
    ALTER TABLE payments DROP CONSTRAINT IF EXISTS ck_payment_due_date;
    ALTER TABLE payments ADD CONSTRAINT ck_payment_due_date CHECK (due_date IS NULL OR due_date >= payment_date);
    
  ELSIF NOT EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_name = 'payments'
  ) THEN
    -- La tabla no existe, crearla nueva (sin FKs por ahora, se agregarán al final)
    CREATE TABLE payments (
      id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
      payment_number          VARCHAR(50)         NOT NULL UNIQUE,
      contract_id             UUID,                -- Nullable temporalmente para compatibilidad
      user_email              VARCHAR(150),         -- Nullable temporalmente para compatibilidad
      amount                  NUMERIC(10,2)       NOT NULL,
      currency                VARCHAR(3)           NOT NULL DEFAULT 'EUR',
      payment_date            DATE                 NOT NULL,
      due_date                DATE,
      payment_method          payment_method,
      status                  payment_status       NOT NULL DEFAULT 'PENDING',
      reference_number        VARCHAR(100),
      notes                   VARCHAR(500),
      invoice_number          VARCHAR(50),
      invoice_date            DATE,
      created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
      created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
      updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
      updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
      CONSTRAINT ck_payment_amount_positive CHECK (amount > 0),
      CONSTRAINT ck_payment_due_date CHECK (due_date IS NULL OR due_date >= payment_date)
    );
  END IF;
END$$;

-- Crear índices (solo si las columnas existen)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'payment_number') THEN
    CREATE INDEX IF NOT EXISTS ix_payments_number ON payments(payment_number);
  END IF;
  
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'contract_id') THEN
    CREATE INDEX IF NOT EXISTS ix_payments_contract ON payments(contract_id);
  END IF;
  
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'user_email') THEN
    CREATE INDEX IF NOT EXISTS ix_payments_user ON payments(user_email);
  END IF;
  
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'due_date') THEN
    CREATE INDEX IF NOT EXISTS ix_payments_due_date ON payments(due_date) WHERE due_date IS NOT NULL;
  END IF;
END$$;

CREATE INDEX IF NOT EXISTS ix_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS ix_payments_date ON payments(payment_date);


-- ----------------------------------------------------------------------------
-- 7. CREAR TABLA DE EVENTOS DE CALENDARIO
-- ----------------------------------------------------------------------------
-- Enum para tipo de evento
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'calendar_event_type') THEN
    CREATE TYPE calendar_event_type AS ENUM (
      'CLASS',           -- Clase programada
      'RESERVATION',     -- Reserva de pista
      'TOURNAMENT',      -- Torneo
      'LEAGUE',          -- Liga
      'HOLIDAY',         -- Día festivo
      'CLOSURE',         -- Cierre del club
      'SPECIAL_EVENT'    -- Evento especial
    );
  END IF;
END$$;

-- Enum para estado de evento
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'event_status') THEN
    CREATE TYPE event_status AS ENUM (
      'SCHEDULED',       -- Programado
      'CONFIRMED',       -- Confirmado
      'IN_PROGRESS',     -- En curso
      'COMPLETED',       -- Completado
      'CANCELLED',       -- Cancelado
      'POSTPONED'        -- Aplazado
    );
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS calendar_events (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  event_type              calendar_event_type NOT NULL,
  title                   VARCHAR(200)        NOT NULL,          -- Título del evento
  description             VARCHAR(1000),                         -- Descripción
  
  -- Fechas y horarios
  start_datetime          TIMESTAMPTZ          NOT NULL,          -- Fecha y hora de inicio
  end_datetime            TIMESTAMPTZ,                           -- Fecha y hora de fin
  is_all_day              BOOLEAN             NOT NULL DEFAULT FALSE,
  
  -- Relaciones
  contract_id             UUID,                                  -- Contrato asociado (para clases)
  service_id              UUID,                                  -- Servicio asociado (para clases)
  period_id               UUID,                                  -- Periodo asociado
  
  -- Participantes
  monitor_email           VARCHAR(150),                          -- Monitor asignado
  participants_count      INTEGER,                               -- Número de participantes
  
  -- Estado
  status                  event_status         NOT NULL DEFAULT 'SCHEDULED',
  cancellation_reason     VARCHAR(500),                          -- Razón de cancelación
  
  -- Configuración de repetición (para futuras funcionalidades)
  is_recurring            BOOLEAN             NOT NULL DEFAULT FALSE,
  recurrence_pattern      VARCHAR(200),                         -- Patrón de repetición (ej: "WEEKLY", "MONTHLY")
  
  -- Notas
  notes                   VARCHAR(1000),                         -- Notas adicionales
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  
  CONSTRAINT fk_event_contract
    FOREIGN KEY (contract_id)
    REFERENCES contracts(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
    
  CONSTRAINT fk_event_service
    FOREIGN KEY (service_id)
    REFERENCES services(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
    
  CONSTRAINT fk_event_period
    FOREIGN KEY (period_id)
    REFERENCES club_periods(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
    
  CONSTRAINT fk_event_monitor
    FOREIGN KEY (monitor_email)
    REFERENCES users(email)
    ON UPDATE CASCADE ON DELETE SET NULL,
    
  CONSTRAINT ck_event_datetime CHECK (end_datetime IS NULL OR end_datetime >= start_datetime)
);

CREATE INDEX IF NOT EXISTS ix_events_type ON calendar_events(event_type);
CREATE INDEX IF NOT EXISTS ix_events_start ON calendar_events(start_datetime);
CREATE INDEX IF NOT EXISTS ix_events_status ON calendar_events(status);
CREATE INDEX IF NOT EXISTS ix_events_contract ON calendar_events(contract_id) WHERE contract_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_events_service ON calendar_events(service_id) WHERE service_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_events_monitor ON calendar_events(monitor_email) WHERE monitor_email IS NOT NULL;
CREATE INDEX IF NOT EXISTS ix_events_period ON calendar_events(period_id) WHERE period_id IS NOT NULL;

-- Función inmutable para truncar a mes (necesaria para índices funcionales)
CREATE OR REPLACE FUNCTION trunc_month_immutable(timestamptz)
RETURNS timestamp WITHOUT time zone
LANGUAGE sql
IMMUTABLE
AS $$
  SELECT date_trunc('month', $1 AT TIME ZONE 'UTC')::timestamp;
$$;

-- Índice compuesto para consultas de calendario mensual
CREATE INDEX IF NOT EXISTS ix_events_calendar_month 
  ON calendar_events(trunc_month_immutable(start_datetime), event_type, status);

-- ----------------------------------------------------------------------------
-- 8. CREAR TABLA DE ASISTENCIAS (Registro de asistencia a clases)
-- ----------------------------------------------------------------------------
-- Enum para estado de asistencia
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'attendance_status') THEN
    CREATE TYPE attendance_status AS ENUM (
      'PRESENT',         -- Presente
      'ABSENT',          -- Ausente
      'EXCUSED',         -- Ausente justificado
      'LATE'             -- Llegó tarde
    );
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS attendances (
  id                      UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  event_id                UUID                NOT NULL,          -- Evento de calendario (clase)
  user_email              VARCHAR(150)        NOT NULL,          -- Usuario (alumno)
  contract_id             UUID                NOT NULL,          -- Contrato asociado
  
  attendance_status       attendance_status   NOT NULL DEFAULT 'PRESENT',
  arrival_time            TIMESTAMPTZ,                           -- Hora de llegada
  departure_time          TIMESTAMPTZ,                           -- Hora de salida
  notes                   VARCHAR(500),                          -- Notas de asistencia
  
  created_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  created_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  updated_date            TIMESTAMP           NOT NULL DEFAULT NOW(),
  updated_by              VARCHAR(100)        NOT NULL DEFAULT 'system',
  
  CONSTRAINT fk_attendance_event
    FOREIGN KEY (event_id)
    REFERENCES calendar_events(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
    
  CONSTRAINT fk_attendance_user
    FOREIGN KEY (user_email)
    REFERENCES users(email)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    
  CONSTRAINT fk_attendance_contract
    FOREIGN KEY (contract_id)
    REFERENCES contracts(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
    
  CONSTRAINT uq_attendance_user_event UNIQUE (event_id, user_email)
);

CREATE INDEX IF NOT EXISTS ix_attendances_event ON attendances(event_id);
CREATE INDEX IF NOT EXISTS ix_attendances_user ON attendances(user_email);
CREATE INDEX IF NOT EXISTS ix_attendances_contract ON attendances(contract_id);
CREATE INDEX IF NOT EXISTS ix_attendances_status ON attendances(attendance_status);

-- ----------------------------------------------------------------------------
-- 9. AGREGAR FKs DE PAYMENTS (después de crear todas las tablas)
-- ----------------------------------------------------------------------------
-- Agregar FKs de payments si no existen (solo si las columnas y tablas referenciadas existen)
DO $$
BEGIN
  -- FK a contracts
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'contract_id')
     AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'contracts') THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints 
      WHERE table_name = 'payments' AND constraint_name = 'fk_payment_contract'
    ) THEN
      ALTER TABLE payments ADD CONSTRAINT fk_payment_contract
        FOREIGN KEY (contract_id)
        REFERENCES contracts(id)
        ON UPDATE CASCADE ON DELETE RESTRICT;
    END IF;
  END IF;
  
  -- FK a users
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'payments' AND column_name = 'user_email') THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.table_constraints 
      WHERE table_name = 'payments' AND constraint_name = 'fk_payment_user'
    ) THEN
      ALTER TABLE payments ADD CONSTRAINT fk_payment_user
        FOREIGN KEY (user_email)
        REFERENCES users(email)
        ON UPDATE CASCADE ON DELETE RESTRICT;
    END IF;
  END IF;
END$$;

-- ----------------------------------------------------------------------------
-- 10. COMENTARIOS Y DOCUMENTACIÓN
-- ----------------------------------------------------------------------------
COMMENT ON TABLE users IS 'Usuarios del sistema (administradores, monitores y alumnos)';
COMMENT ON TABLE sport_profiles IS 'Perfil deportivo opcional para usuarios que participan en competiciones';
COMMENT ON TABLE services IS 'Catálogo de servicios/clases ofrecidos por el club';
COMMENT ON TABLE club_periods IS 'Periodos del club (trimestres, vacaciones, cierres)';
COMMENT ON TABLE contracts IS 'Contratos entre usuarios y servicios del club';
COMMENT ON TABLE payments IS 'Pagos asociados a contratos';
COMMENT ON TABLE calendar_events IS 'Eventos del calendario (clases, reservas, torneos, etc.)';
COMMENT ON TABLE attendances IS 'Registro de asistencia a clases/eventos';

-- ============================================================================
-- FIN DE LA MIGRACIÓN
-- ============================================================================
-- NOTAS IMPORTANTES:
-- 1. Esta migración NO elimina las tablas antiguas (matches, players, etc.)
--    para permitir migración de datos si es necesario.
-- 2. Las tablas antiguas se pueden eliminar en una migración posterior
--    una vez que se haya migrado la información relevante.
-- 3. El modelo está diseñado para ser extensible y permitir futuras
--    funcionalidades como reservas de pistas, ligas o torneos.
-- ============================================================================

