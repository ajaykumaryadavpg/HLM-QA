# Rulebook: Architecture Standards

> Module boundaries, dependency flow, and scalability patterns for the IMS Gen2 platform.
> Enforced during code review, CI, and Claude Code skills.

---

## Folder Structure (Feature-Based)

### Required Layout

```
src/
├── app/
│   ├── components/
│   │   ├── dashboard/           # Dashboard pages and widgets
│   │   ├── devices/             # Device inventory management
│   │   ├── firmware/            # Firmware lifecycle (upload, test, approve, deploy)
│   │   ├── deployment/          # Deployment pipeline and scheduling
│   │   ├── compliance/          # NIST compliance tracking, vulnerability management
│   │   ├── service-orders/      # Service order CRUD, Kanban board
│   │   ├── analytics/           # Reports, charts, export
│   │   ├── geo-location/        # Map views, device clustering
│   │   ├── user-management/     # User CRUD, role assignment
│   │   ├── account-service/     # Customer/account management
│   │   ├── settings/            # System configuration
│   │   ├── shared/              # Reusable UI components (Badge, DataTable, ErrorState)
│   │   └── layout/              # Shell, sidebar, header, navigation
│   └── App.tsx                  # Route definitions + ErrorBoundary
├── lib/
│   ├── types.ts                 # All TypeScript interfaces/types
│   ├── rbac.ts                  # Role-based access control
│   ├── security.ts              # Sanitization, CSP, security utils
│   ├── hlm-api.ts               # API client (stub → replaced by provider)
│   ├── auth-context.tsx         # Auth hook and context
│   ├── hooks/                   # Shared custom hooks
│   ├── providers/               # Cloud provider adapters
│   │   ├── types.ts             # IAuthAdapter, IApiProvider, IStorageProvider
│   │   ├── platform.config.ts   # Platform detection and config
│   │   ├── mock/                # Mock provider (dev/demo)
│   │   ├── aws-amplify/         # Amplify Gen2 adapter
│   │   ├── aws-cdk/             # CDK adapter
│   │   ├── aws-terraform/       # Terraform adapter
│   │   └── azure/               # Azure adapter (planned)
│   └── utils/                   # Pure utility functions
└── __tests__/                   # Unit tests (mirrors src/ structure)
```

### Rules

- **One feature = one folder** under `components/`. Never mix features.
- **Shared components** go in `components/shared/`. If 2+ features use it, it's shared.
- **No feature cross-imports** — `devices/` must NOT import from `firmware/`. If both need it, lift to `shared/` or `lib/`.
- **Page components** are the entry point per route — compose child components, max 200 lines.
- **No nested feature folders** — keep it flat. `devices/DeviceList.tsx`, not `devices/list/DeviceList.tsx`.

---

## Dependency Flow (Strict Layering)

```
┌─────────────────────────────┐
│     app/components/         │  UI Layer — renders, handles user interaction
│     (pages + features)      │  Can import from: lib/, shared/
├─────────────────────────────┤
│     lib/hooks/              │  Logic Layer — state, data fetching, business rules
│     (custom hooks)          │  Can import from: lib/types, lib/providers, lib/utils
├─────────────────────────────┤
│     lib/providers/          │  Adapter Layer — cloud-specific implementations
│     (platform adapters)     │  Can import from: lib/types only
├─────────────────────────────┤
│     lib/types.ts            │  Type Layer — interfaces, enums, type guards
│     lib/utils/              │  Zero imports from other src/ modules
└─────────────────────────────┘
```

### Dependency Rules

| From                        | Can Import                                | Cannot Import                        |
| --------------------------- | ----------------------------------------- | ------------------------------------ |
| `app/components/{feature}/` | `lib/*`, `shared/`, own files             | Other feature folders                |
| `app/components/shared/`    | `lib/types`, `lib/utils`                  | Feature folders, `lib/providers`     |
| `lib/hooks/`                | `lib/types`, `lib/providers`, `lib/utils` | `app/components/*`                   |
| `lib/providers/*`           | `lib/providers/types.ts`                  | `lib/types.ts`, `app/*`, `lib/hooks` |
| `lib/types.ts`              | External packages only                    | Any `src/` module                    |
| `lib/utils/`                | External packages only                    | Any `src/` module                    |

### Violation Checks

```
# No circular imports
npx madge --circular src/

# No feature cross-imports
grep -rn "from.*components/devices" src/app/components/firmware/ → must return 0
grep -rn "from.*components/firmware" src/app/components/devices/ → must return 0
```

---

## Component Architecture

### Single Responsibility

Each file does ONE thing:

| File Type           | Responsibility                       | Max Lines |
| ------------------- | ------------------------------------ | --------- |
| `{Feature}Page.tsx` | Route entry, layout, tab routing     | 200       |
| `{Feature}.tsx`     | One feature's UI + interactions      | 400       |
| `use-{feature}.ts`  | Data fetching, state, business logic | 200       |
| `{Shared}.tsx`      | Reusable UI element                  | 300       |

### Composition Over Inheritance

- Pages compose features: `<DashboardPage>` renders `<KPICards>`, `<DeviceStatusChart>`, `<RecentAlerts>`
- Features compose shared: `<DeviceList>` renders `<DataTable>`, `<StatusBadge>`, `<SearchInput>`
- Never extend component classes — use hooks for shared behavior

### God Component Prevention

A component is a "God component" if it has:

- More than 10 `useState` calls → extract to custom hook
- More than 400 lines → split into sub-components
- More than 3 responsibilities (data fetching + rendering + form handling) → separate concerns
- Direct API calls → move to custom hook

---

## Provider Abstraction (Cloud-Agnostic)

### The Three Interfaces

```typescript
// lib/providers/types.ts
interface IAuthAdapter {
  signIn(email: string, password: string): Promise<AuthSession>;
  signOut(): Promise<void>;
  refreshToken(): Promise<AuthSession>;
  getSession(): Promise<AuthSession | null>;
  // ... MFA, password reset, etc.
}

interface IApiProvider {
  listDevices(filters?): Promise<PaginatedResponse<Device>>;
  createDevice(data): Promise<Device>;
  // ... all CRUD operations
}

interface IStorageProvider {
  getItem(key: string): string | null;
  setItem(key: string, value: string): void;
  removeItem(key: string): void;
}
```

### Rules

- **Frontend code NEVER imports cloud SDKs directly** — always through an adapter
- `platform.config.ts` is the ONLY file that selects which adapter to use
- Adapters must implement the full interface — no partial implementations
- Adding a new cloud provider = adding a new folder under `providers/` + one case in `platform.config.ts`
- The `mock` provider is always the default — app must run without any cloud config

---

## State Management Architecture

### State Categories

| Category         | Tool                         | Where                                          |
| ---------------- | ---------------------------- | ---------------------------------------------- |
| **Server state** | TanStack Query (React Query) | Data from API — devices, firmware, orders      |
| **Client state** | Zustand                      | UI state — sidebar open, selected tab, filters |
| **Form state**   | react-hook-form              | Form inputs, validation, submission            |
| **URL state**    | React Router                 | Current route, query params, navigation        |
| **Auth state**   | Auth Context                 | User session, roles, permissions               |

### Rules

- Never use `useState` for server data — use TanStack Query
- Never use global state for form inputs — use `react-hook-form`
- Never store derived data — compute it with `useMemo`
- Zustand stores: max 1 per feature domain, keep flat
- No prop drilling beyond 2 levels — use context or Zustand

---

## Error Handling Architecture

### Three Layers

```
Layer 1: ErrorBoundary (App.tsx)      → catches React render crashes → shows fallback UI
Layer 2: API try/catch (hooks)         → catches API errors → toast notification
Layer 3: Form validation (Zod)         → catches input errors → inline field errors
```

### Rules

- Every async operation: `loading → success → error` states, all handled
- API errors: `toast.error()` with user-friendly message, log technical details
- Never swallow errors — `catch (e) {}` is a violation
- Error boundaries at: App root, per-page layout, around high-risk widgets
- Network failures: show retry button, not just error message

---

## Scalability Patterns

### Code Splitting

- Every route is lazy-loaded: `React.lazy(() => import('./pages/DevicesPage'))`
- Heavy libraries loaded on demand: charts, maps, PDF export
- Minimum 3 route-level code splits

### Multi-Tenant Readiness

- All data queries include `tenantId` filter (enforced at API layer)
- UI never shows data across tenants (except for Admin in platform mode)
- CustomerAdmin role is tenant-scoped — adapter enforces this

### Internationalization Readiness

- No hardcoded user-facing strings in components (prepare for i18n extraction)
- Date/time formatting through `Intl.DateTimeFormat`, not string concatenation
- Number formatting through `Intl.NumberFormat`
- RTL layout support: use logical CSS properties (`margin-inline-start`, not `margin-left`)
