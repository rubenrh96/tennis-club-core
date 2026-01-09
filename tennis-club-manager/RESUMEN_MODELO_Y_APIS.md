# 📊 Resumen Ejecutivo: Modelo de Datos y APIs

## 🗄️ Modelo de Datos - Tablas Principales

### 1. `users` - Usuarios del Sistema
- **PK:** `email` (string)
- **Campos clave:** `firstName`, `lastName`, `role` ('ADMIN' | 'MONITOR' | 'ALUMNO')
- **Uso:** Autenticación y roles

### 2. `services` - Catálogo de Servicios
- **PK:** `id` (UUID)
- **Tipos:** 
  - `QUARTERLY_GROUP_CLASS` - Clase grupal trimestral
  - `INDIVIDUAL_CLASS_PACKAGE` - Bono de clases (ej: 10 clases)
  - `SINGLE_INDIVIDUAL_CLASS` - Clase individual suelta
- **Campos clave:** `name`, `code`, `serviceType`, `basePrice`, `dayOfWeek`, `startTime`, `endTime`

### 3. `contracts` - Contratos Usuario-Servicio
- **PK:** `id` (UUID)
- **FKs:** `userEmail` → users, `serviceId` → services, `monitorEmail` → users
- **Estados:** 'ACTIVE' | 'SUSPENDED' | 'CANCELLED' | 'EXPIRED' | 'PENDING'
- **Campos clave:** `contractNumber`, `status`, `startDate`, `endDate`, `price`, `totalClasses`, `classesUsed`

### 4. `payments` - Pagos
- **PK:** `id` (UUID)
- **FKs:** `contractId` → contracts, `userEmail` → users
- **Estados:** 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED' | 'CANCELLED'
- **Campos clave:** `paymentNumber`, `amount`, `dueDate`, `status`, `paymentMethod`

### 5. `calendar_events` - Eventos del Calendario
- **PK:** `id` (UUID)
- **FKs:** `contractId` → contracts, `serviceId` → services, `monitorEmail` → users
- **Tipos:** 'CLASS' | 'RESERVATION' | 'TOURNAMENT' | 'LEAGUE' | 'HOLIDAY' | 'CLOSURE' | 'SPECIAL_EVENT'
- **Estados:** 'SCHEDULED' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'POSTPONED'
- **Campos clave:** `title`, `startDatetime`, `endDatetime`, `eventType`, `status`

### 6. `attendances` - Asistencias
- **PK:** `id` (UUID)
- **FKs:** `eventId` → calendar_events, `userEmail` → users, `contractId` → contracts
- **Estados:** 'PRESENT' | 'ABSENT' | 'EXCUSED' | 'LATE'

### 7. `club_periods` - Periodos del Club
- **PK:** `id` (UUID)
- **Tipos:** 'QUARTER' | 'HOLIDAY' | 'CLOSURE' | 'SPECIAL'
- **Campos clave:** `name`, `startDate`, `endDate`, `periodType`

---

## 🔌 APIs Principales para Dashboard Admin

### Base URL
```
/api/v1
```

### Autenticación
```
Authorization: Bearer <token>
```

### Endpoints Críticos

#### 1. Login
```
POST /api/v1/users/log-in
Body: { email, password }
Response: { email, firstName, lastName, role, token }
```

#### 2. Dashboard - Resumen del Día
```
GET /api/v1/admin/dashboard/today
Response: {
  date: string,
  summary: {
    classesToday: number,
    pendingPayments: number,
    activeContracts: number
  },
  events: Event[],
  clubStatus: 'OPEN' | 'CLOSED' | 'HOLIDAY'
}
```

#### 3. Calendario Mensual
```
GET /api/v1/admin/calendar/month?year=2025&month=1
Response: {
  year: number,
  month: number,
  events: Event[],
  periods: ClubPeriod[]
}
```

#### 4. Detalle de Evento
```
GET /api/v1/admin/events/{eventId}
Response: {
  id, eventType, title, description,
  startDatetime, endDatetime,
  service: Service,
  monitor: User,
  contract: Contract,
  participantsCount: number,
  attendances: Attendance[],
  status: string
}
```

#### 5. Pagos Pendientes
```
GET /api/v1/admin/payments/pending
Response: Payment[]
```

#### 6. Contratos Activos
```
GET /api/v1/admin/contracts/active
Response: Contract[]
```

---

## 📋 Tipos TypeScript de Referencia

```typescript
// User
interface User {
  email: string;
  firstName: string;
  lastName: string;
  role: 'ADMIN' | 'MONITOR' | 'ALUMNO';
  phone?: string;
}

// Service
interface Service {
  id: string;
  code: string;
  name: string;
  serviceType: 'QUARTERLY_GROUP_CLASS' | 'INDIVIDUAL_CLASS_PACKAGE' | 'SINGLE_INDIVIDUAL_CLASS';
  basePrice: number;
  dayOfWeek?: string;
  startTime?: string;
  endTime?: string;
  maxCapacity?: number;
}

// Contract
interface Contract {
  id: string;
  contractNumber: string;
  userEmail: string;
  serviceId: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'CANCELLED' | 'EXPIRED' | 'PENDING';
  startDate: string;
  endDate?: string;
  price: number;
  totalClasses?: number;
  classesUsed: number;
}

// Payment
interface Payment {
  id: string;
  paymentNumber: string;
  contractId: string;
  amount: number;
  dueDate?: string;
  status: 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED' | 'CANCELLED';
  paymentMethod?: string;
}

// Calendar Event
interface CalendarEvent {
  id: string;
  eventType: 'CLASS' | 'RESERVATION' | 'TOURNAMENT' | 'LEAGUE' | 'HOLIDAY' | 'CLOSURE' | 'SPECIAL_EVENT';
  title: string;
  startDatetime: string;
  endDatetime?: string;
  service?: Service;
  monitor?: User;
  participantsCount?: number;
  status: 'SCHEDULED' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'POSTPONED';
}

// Dashboard Response
interface DashboardResponse {
  date: string;
  summary: {
    classesToday: number;
    pendingPayments: number;
    activeContracts: number;
  };
  events: CalendarEvent[];
  clubStatus: 'OPEN' | 'CLOSED' | 'HOLIDAY';
}
```

---

## 🎯 Reglas de Negocio Clave

1. **Un usuario puede tener múltiples contratos activos**
2. **Solo contratos con `status = 'ACTIVE'` permiten asistir a clases**
3. **`classesRemaining` = `totalClasses - classesUsed`** (calcular en frontend)
4. **No hay escuela en verano** (julio-agosto)
5. **Trimestres fijos:** enero-marzo, abril-junio, septiembre-diciembre
6. **Eventos de tipo `CLASS` deben tener `contractId` y `serviceId`**

---

## 📝 Notas para Desarrollo Mobile

- **Email es PK en users** - no cambiar emails en producción
- **UUIDs son strings** en las respuestas JSON
- **Fechas en formato ISO 8601** (ej: "2025-01-15T18:00:00Z")
- **Moneda siempre EUR** por ahora
- **Todos los endpoints requieren autenticación** excepto login/registro

