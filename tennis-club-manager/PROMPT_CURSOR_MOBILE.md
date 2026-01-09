# 🎾 PROMPT DEFINITIVO — RESET + DASHBOARD ADMIN (REACT NATIVE)

---

## 📋 CONTEXTO GENERAL

Actúa como un **senior frontend engineer en React Native** con experiencia en apps administrativas en producción.

Estoy refactorizando una app mobile existente para un **sistema de gestión administrativa de un club de tenis** (ERP ligero).

**IMPORTANTE:** La app anterior tenía funcionalidades de ligas, partidos y rankings que **DEBEN ELIMINARSE COMPLETAMENTE**.

---

## 🚨 REGLA PRINCIPAL (NO NEGOCIABLE)

👉 **Eliminar todo el código, pantallas, navegación y estado global existente,**

**EXCEPTO:**
- ✅ Login
- ✅ Registro
- ✅ Gestión de sesión / token
- ✅ Cliente HTTP (api)
- ✅ Navegación base

**Todo lo demás debe considerarse obsoleto.**

---

## 🗄️ MODELO DE DATOS BACKEND (V2.0)

El backend ya está implementado con PostgreSQL. **NO debes modificar el backend**. Solo consume las APIs.

### Estructura Principal

#### 1. **USUARIOS** (`users`)
```typescript
{
  email: string;              // PK, único
  firstName: string;
  lastName: string;
  birthDate: string;         // ISO date
  phone: string;
  licenseNumber?: string;     // Opcional
  role: 'ADMIN' | 'MONITOR' | 'ALUMNO';
  // Campos de auditoría (no usar en frontend)
}
```

#### 2. **SERVICIOS** (`services`)
Catálogo de servicios/clases ofrecidos por el club.

```typescript
{
  id: string;                 // UUID
  code: string;               // Único, ej: "ESCUELA-LUNES-18H"
  name: string;                // Nombre del servicio
  description?: string;
  serviceType: 'QUARTERLY_GROUP_CLASS' | 'INDIVIDUAL_CLASS_PACKAGE' | 'SINGLE_INDIVIDUAL_CLASS';
  basePrice: number;          // Precio en euros
  currency: string;           // "EUR"
  
  // Para clases grupales trimestrales
  dayOfWeek?: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  startTime?: string;         // "18:00:00"
  endTime?: string;           // "19:30:00"
  maxCapacity?: number;
  minCapacity?: number;
  
  // Para bonos de clases
  classesInPackage?: number;  // Ej: 10
  packageValidityDays?: number; // Ej: 180
  
  isActive: boolean;
}
```

**Tipos de servicios:**
- `QUARTERLY_GROUP_CLASS`: Clase grupal trimestral (escuela) - tiene `dayOfWeek`, `startTime`, `endTime`
- `INDIVIDUAL_CLASS_PACKAGE`: Bono de clases individuales (ej: 10 clases) - tiene `classesInPackage`, `packageValidityDays`
- `SINGLE_INDIVIDUAL_CLASS`: Clase individual suelta - solo precio

#### 3. **PERIODOS DEL CLUB** (`club_periods`)
Trimestres, vacaciones, cierres.

```typescript
{
  id: string;                 // UUID
  name: string;               // "Trimestre 1 - 2025"
  periodType: 'QUARTER' | 'HOLIDAY' | 'CLOSURE' | 'SPECIAL';
  startDate: string;          // ISO date
  endDate: string;            // ISO date
  isActive: boolean;
  description?: string;
}
```

**Reglas de negocio:**
- Trimestres fijos: enero-marzo, abril-junio, septiembre-diciembre
- No hay escuela en verano (julio-agosto)

#### 4. **CONTRATOS** (`contracts`)
Contratos entre usuarios (alumnos) y servicios.

```typescript
{
  id: string;                 // UUID
  contractNumber: string;      // Único, ej: "CT-2025-001"
  userEmail: string;          // FK a users.email (alumno)
  serviceId: string;          // FK a services.id
  monitorEmail?: string;      // FK a users.email (monitor asignado)
  
  status: 'ACTIVE' | 'SUSPENDED' | 'CANCELLED' | 'EXPIRED' | 'PENDING';
  startDate: string;          // ISO date
  endDate?: string;           // ISO date (NULL para contratos sin fin)
  signedDate?: string;        // ISO date
  
  price: number;              // Precio acordado (puede diferir del base_price)
  currency: string;           // "EUR"
  
  // Para bonos de clases
  totalClasses?: number;      // Total de clases del bono
  classesUsed: number;        // Clases consumidas (default: 0)
  // classesRemaining se calcula: totalClasses - classesUsed
  
  // Para clases trimestrales
  periodId?: string;           // FK a club_periods.id
  daysPerWeek?: number;       // Días por semana
  
  autoRenew: boolean;
  notes?: string;
}
```

**Reglas importantes:**
- Un usuario puede tener múltiples contratos activos
- `classesRemaining` = `totalClasses - classesUsed` (calcular en frontend)
- Solo contratos con `status = 'ACTIVE'` permiten asistir a clases

#### 5. **PAGOS** (`payments`)
Pagos asociados a contratos.

```typescript
{
  id: string;                 // UUID
  paymentNumber: string;      // Único, ej: "PAY-2025-001"
  contractId: string;         // FK a contracts.id
  userEmail: string;          // FK a users.email
  
  amount: number;             // Monto del pago
  currency: string;           // "EUR"
  paymentDate: string;        // ISO date
  dueDate?: string;           // ISO date (fecha de vencimiento)
  paymentMethod?: 'CASH' | 'CARD' | 'BANK_TRANSFER' | 'CHECK' | 'OTHER';
  status: 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED' | 'CANCELLED';
  
  referenceNumber?: string;  // Número de referencia (transferencia, cheque)
  notes?: string;
  invoiceNumber?: string;
  invoiceDate?: string;
}
```

#### 6. **EVENTOS DE CALENDARIO** (`calendar_events`)
Clases, reservas, eventos del calendario.

```typescript
{
  id: string;                 // UUID
  eventType: 'CLASS' | 'RESERVATION' | 'TOURNAMENT' | 'LEAGUE' | 'HOLIDAY' | 'CLOSURE' | 'SPECIAL_EVENT';
  title: string;              // Título del evento
  description?: string;
  
  startDatetime: string;      // ISO datetime (TIMESTAMPTZ)
  endDatetime?: string;       // ISO datetime
  isAllDay: boolean;
  
  // Relaciones
  contractId?: string;        // FK a contracts.id (para clases)
  serviceId?: string;         // FK a services.id (para clases)
  periodId?: string;          // FK a club_periods.id
  monitorEmail?: string;      // FK a users.email (monitor asignado)
  
  participantsCount?: number; // Número de participantes
  
  status: 'SCHEDULED' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'POSTPONED';
  cancellationReason?: string;
  notes?: string;
}
```

**Para el dashboard admin:**
- Filtrar por `eventType = 'CLASS'` y `status IN ('SCHEDULED', 'CONFIRMED')`
- Ordenar por `startDatetime`
- Filtrar por fecha de hoy

#### 7. **ASISTENCIAS** (`attendances`)
Registro de asistencia a clases/eventos.

```typescript
{
  id: string;                 // UUID
  eventId: string;            // FK a calendar_events.id
  userEmail: string;          // FK a users.email (alumno)
  contractId: string;         // FK a contracts.id
  
  attendanceStatus: 'PRESENT' | 'ABSENT' | 'EXCUSED' | 'LATE';
  arrivalTime?: string;       // ISO datetime
  departureTime?: string;     // ISO datetime
  notes?: string;
}
```

---

## 🔌 APIs NECESARIAS PARA DASHBOARD ADMIN

### Base URL
```
/api/v1
```

### Autenticación
Todas las peticiones (excepto login/registro) requieren:
```
Authorization: Bearer <token>
```

### Endpoints Requeridos

#### 1. **Login**
```http
POST /api/v1/users/log-in
Content-Type: application/json

{
  "email": "admin@club.com",
  "password": "password123"
}

Response 200:
{
  "email": "admin@club.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "role": "ADMIN",              // ← IMPORTANTE: Determina redirección
  "token": "jwt_token_here"
}
```

#### 2. **Dashboard Admin - Resumen del Día**
```http
GET /api/v1/admin/dashboard/today
Authorization: Bearer <token>

Response 200:
{
  "date": "2025-01-15",
  "summary": {
    "classesToday": 5,           // Número de clases programadas hoy
    "pendingPayments": 12,       // Pagos pendientes (total)
    "activeContracts": 45        // Contratos activos (total)
  },
  "events": [                    // Eventos de hoy ordenados por hora
    {
      "id": "uuid",
      "eventType": "CLASS",
      "title": "Escuela Lunes 18:00",
      "startDatetime": "2025-01-15T18:00:00Z",
      "endDatetime": "2025-01-15T19:30:00Z",
      "service": {
        "id": "uuid",
        "name": "Escuela Lunes 18:00-19:30",
        "serviceType": "QUARTERLY_GROUP_CLASS",
        "code": "ESCUELA-LUNES-18H"
      },
      "monitor": {
        "email": "monitor@club.com",
        "firstName": "Carlos",
        "lastName": "García"
      },
      "participantsCount": 8,
      "status": "CONFIRMED",
      "contractId": "uuid"
    }
    // ... más eventos
  ],
  "clubStatus": "OPEN"           // "OPEN" | "CLOSED" | "HOLIDAY"
}
```

#### 3. **Calendario Mensual**
```http
GET /api/v1/admin/calendar/month?year=2025&month=1
Authorization: Bearer <token>

Response 200:
{
  "year": 2025,
  "month": 1,
  "events": [
    {
      "id": "uuid",
      "eventType": "CLASS",
      "title": "Escuela Lunes 18:00",
      "startDatetime": "2025-01-06T18:00:00Z",
      "endDatetime": "2025-01-06T19:30:00Z",
      "service": {
        "id": "uuid",
        "name": "Escuela Lunes 18:00-19:30",
        "code": "ESCUELA-LUNES-18H"
      },
      "monitor": {
        "email": "monitor@club.com",
        "firstName": "Carlos",
        "lastName": "García"
      },
      "status": "CONFIRMED"
    }
    // ... eventos del mes
  ],
  "periods": [                   // Periodos activos en el mes
    {
      "id": "uuid",
      "name": "Trimestre 1 - 2025",
      "periodType": "QUARTER",
      "startDate": "2025-01-01",
      "endDate": "2025-03-31"
    }
  ]
}
```

#### 4. **Detalle de Evento**
```http
GET /api/v1/admin/events/{eventId}
Authorization: Bearer <token>

Response 200:
{
  "id": "uuid",
  "eventType": "CLASS",
  "title": "Escuela Lunes 18:00",
  "description": "Clase grupal trimestral",
  "startDatetime": "2025-01-15T18:00:00Z",
  "endDatetime": "2025-01-15T19:30:00Z",
  "service": {
    "id": "uuid",
    "name": "Escuela Lunes 18:00-19:30",
    "code": "ESCUELA-LUNES-18H",
    "serviceType": "QUARTERLY_GROUP_CLASS",
    "maxCapacity": 12
  },
  "monitor": {
    "email": "monitor@club.com",
    "firstName": "Carlos",
    "lastName": "García",
    "phone": "+34 600 123 456"
  },
  "contract": {
    "id": "uuid",
    "contractNumber": "CT-2025-001",
    "userEmail": "alumno@club.com",
    "status": "ACTIVE"
  },
  "participantsCount": 8,
  "attendances": [              // Lista de asistencias
    {
      "id": "uuid",
      "user": {
        "email": "alumno1@club.com",
        "firstName": "Ana",
        "lastName": "López"
      },
      "attendanceStatus": "PRESENT",
      "arrivalTime": "2025-01-15T18:05:00Z"
    }
    // ... más asistencias
  ],
  "status": "CONFIRMED"
}
```

#### 5. **Pagos Pendientes**
```http
GET /api/v1/admin/payments/pending
Authorization: Bearer <token>

Response 200:
[
  {
    "id": "uuid",
    "paymentNumber": "PAY-2025-001",
    "contract": {
      "id": "uuid",
      "contractNumber": "CT-2025-001",
      "user": {
        "email": "alumno@club.com",
        "firstName": "Ana",
        "lastName": "López"
      },
      "service": {
        "name": "Escuela Lunes 18:00-19:30"
      }
    },
    "amount": 150.00,
    "currency": "EUR",
    "dueDate": "2025-01-20",
    "status": "PENDING",
    "paymentDate": "2025-01-15"
  }
  // ... más pagos
]
```

#### 6. **Contratos Activos**
```http
GET /api/v1/admin/contracts/active
Authorization: Bearer <token>

Response 200:
[
  {
    "id": "uuid",
    "contractNumber": "CT-2025-001",
    "user": {
      "email": "alumno@club.com",
      "firstName": "Ana",
      "lastName": "López"
    },
    "service": {
      "id": "uuid",
      "name": "Escuela Lunes 18:00-19:30",
      "serviceType": "QUARTERLY_GROUP_CLASS"
    },
    "status": "ACTIVE",
    "startDate": "2025-01-01",
    "endDate": "2025-03-31",
    "price": 150.00,
    "classesRemaining": null,   // null para clases trimestrales
    "monitor": {
      "email": "monitor@club.com",
      "firstName": "Carlos",
      "lastName": "García"
    }
  }
  // ... más contratos
]
```

---

## 🎯 OBJETIVO DEL DASHBOARD ADMIN

Ser la **primera pantalla tras el login** para el rol `ADMIN`.

**Uso diario, rápido, diseñado para reemplazar Excel.**

El administrador debe entender el estado del club en **menos de 10 segundos**.

---

## 🎨 DISEÑO FUNCIONAL — ADMIN DASHBOARD

### 1️⃣ **Header Fijo**
- **Título:** "Hoy"
- **Subtítulo:** Fecha actual formateada (ej: "Miércoles, 15 de enero de 2025")
- **Icono calendario** → Navega a `CalendarScreen` (vista mensual)

### 2️⃣ **Resumen Rápido (Cards)**
Mostrar exactamente **3 cards**:

1. **Clases hoy**
   - Número: `summary.classesToday`
   - Icono: 📚
   - Color: Azul

2. **Pagos pendientes**
   - Número: `summary.pendingPayments`
   - Icono: 💰
   - Color: Naranja

3. **Contratos activos**
   - Número: `summary.activeContracts`
   - Icono: 📄
   - Color: Verde

**Sin gráficas. Sin tendencias. Solo números.**

### 3️⃣ **Lista Principal — Eventos de Hoy**
Lista vertical ordenada por hora (`startDatetime`):

Cada evento muestra:
- **Hora inicio** (ej: "18:00")
- **Tipo de evento** (clase grupal / individual / reserva)
- **Nombre del servicio** (ej: "Escuela Lunes 18:00-19:30")
- **Monitor asignado** (ej: "Carlos García")
- **Nº asistentes esperados** (ej: "8 alumnos")

**Tap** → `EventDetailScreen`

### 4️⃣ **Estados Especiales**
- **No hay eventos hoy** → Mensaje claro: "No hay clases programadas para hoy"
- **Club cerrado** → Aviso destacado en la parte superior: "⚠️ El club está cerrado hoy"

---

## 🚫 RESTRICCIONES EXPLÍCITAS

❌ **Nada de ligas**  
❌ **Nada de rankings**  
❌ **Nada de partidos**  
❌ **Nada de gráficos**  
❌ **Nada de edición masiva**  
❌ **Nada de lógica de negocio en frontend**

---

## 🔐 AUTENTICACIÓN Y ROLES

Tras el login:

El backend devuelve el `role` del usuario.

La app redirige automáticamente según rol:

| Rol | Pantalla inicial |
|-----|------------------|
| `ADMIN` | `AdminDashboardScreen` |
| `ALUMNO` | `UserDashboardScreen` |
| `MONITOR` | (fuera de alcance por ahora) |

👉 **ADMIN y ALUMNO tienen dashboards completamente distintos.**

---

## 🛠️ TECNOLOGÍA

- **React Native**
- **Componentes funcionales**
- **Hooks** (`useState`, `useEffect`, `useCallback`, `useMemo`)
- **Código claro, mantenible, orientado a producción**
- **TypeScript** (recomendado)

---

## 📦 LO QUE DEBES ENTREGAR

### Estructura del Proyecto

```
src/
├── screens/
│   ├── auth/
│   │   ├── LoginScreen.tsx          # ✅ MANTENER (si existe)
│   │   └── RegisterScreen.tsx       # ✅ MANTENER (si existe)
│   ├── admin/
│   │   ├── AdminDashboardScreen.tsx # 🆕 NUEVO
│   │   ├── CalendarScreen.tsx       # 🆕 NUEVO (vista mensual)
│   │   └── EventDetailScreen.tsx    # 🆕 NUEVO
│   └── user/
│       └── UserDashboardScreen.tsx  # 🆕 NUEVO (placeholder por ahora)
├── components/
│   ├── common/
│   │   ├── Card.tsx                 # 🆕 Componente reutilizable
│   │   └── EventItem.tsx            # 🆕 Item de evento
│   └── admin/
│       └── SummaryCard.tsx          # 🆕 Card de resumen
├── navigation/
│   ├── AppNavigator.tsx             # ✅ MANTENER/ACTUALIZAR
│   └── AdminNavigator.tsx           # 🆕 Navegación admin
├── services/
│   ├── api.ts                       # ✅ MANTENER (cliente HTTP)
│   └── auth.ts                      # ✅ MANTENER (gestión de sesión)
├── types/
│   └── models.ts                    # 🆕 Tipos TypeScript del modelo
└── utils/
    └── dateUtils.ts                  # 🆕 Utilidades de fecha
```

### Componentes Requeridos

1. **AdminDashboardScreen**
   - Header con fecha y botón calendario
   - 3 cards de resumen
   - Lista de eventos de hoy
   - Estados especiales (sin eventos, club cerrado)

2. **SummaryCard** (reutilizable)
   - Props: `title`, `value`, `icon`, `color`
   - Diseño simple y claro

3. **EventItem** (reutilizable)
   - Props: `event` (objeto completo del evento)
   - Muestra: hora, tipo, servicio, monitor, participantes
   - `onPress` → navega a detalle

4. **CalendarScreen**
   - Vista mensual del calendario
   - Muestra eventos del mes
   - Navegación entre meses

5. **EventDetailScreen**
   - Detalle completo del evento
   - Información del servicio, monitor, contrato
   - Lista de asistencias

### Mock de Datos Temporal

Crear un archivo `src/mocks/dashboardMock.ts` con datos de ejemplo para desarrollo:

```typescript
export const mockDashboardData = {
  date: "2025-01-15",
  summary: {
    classesToday: 5,
    pendingPayments: 12,
    activeContracts: 45
  },
  events: [
    // ... eventos de ejemplo
  ],
  clubStatus: "OPEN"
};
```

### Comentarios en el Código

- Explicar lógica compleja
- Documentar props de componentes
- Comentar decisiones de diseño importantes

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

- [ ] Eliminar código obsoleto (ligas, rankings, partidos)
- [ ] Mantener solo: Login, Registro, Sesión, API client, Navegación base
- [ ] Crear estructura de carpetas nueva
- [ ] Implementar redirección por roles tras login
- [ ] Crear `AdminDashboardScreen` completa
- [ ] Crear componentes reutilizables (`Card`, `EventItem`)
- [ ] Implementar llamadas a API (o mocks temporales)
- [ ] Manejar estados especiales (sin eventos, club cerrado)
- [ ] Navegación a `CalendarScreen` y `EventDetailScreen`
- [ ] Formateo de fechas y horas
- [ ] Comentarios claros en el código

---

## 📝 NOTAS IMPORTANTES

1. **No reutilices pantallas antiguas** salvo Login y Registro (adaptada a lo nuevo).
2. **Si algo no encaja con el nuevo dominio, elimínalo.**
3. **El backend ya está implementado** - solo consume las APIs.
4. **Prioriza simplicidad** - el dashboard debe ser rápido y claro.
5. **Diseño funcional** - no necesita ser "bonito", necesita ser útil.

---

## 🎯 PRIORIDADES

1. **Alta:** AdminDashboardScreen funcional con datos reales o mocks
2. **Media:** Navegación completa y redirección por roles
3. **Baja:** UserDashboardScreen (placeholder por ahora)

---

**¡Empieza por limpiar el código obsoleto y crear la estructura base!**

