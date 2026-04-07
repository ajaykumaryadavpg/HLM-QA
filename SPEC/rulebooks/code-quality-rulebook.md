# Rulebook: Code Quality Standards

> Component architecture, file size limits, and patterns for the IMS Gen2 frontend.
> This rulebook is enforced by ESLint, pre-commit hooks, and CI.

---

## File Size Limits

| Threshold     | Action                             |
| ------------- | ---------------------------------- |
| **400 lines** | Warning — consider splitting       |
| **600 lines** | Block — must refactor before merge |

Applies to all `.ts` and `.tsx` files in `src/`.

---

## Component Architecture

### Single Responsibility

Each component file should do ONE thing:

- **Page component**: Layout + composition of child components
- **Feature component**: One feature (e.g., FirmwareUpload, not FirmwareUpload + VulnerabilityTracker + AuditLog)
- **Shared component**: Reusable UI element (Badge, StatusChip, DataTable)

### File Organization

```
src/app/components/{module}/
  {Module}Page.tsx          — Page layout, tab routing (max 200 lines)
  {Feature}.tsx             — Individual feature (max 400 lines)
  use-{feature}.ts          — Custom hook for data/state (max 200 lines)

src/app/components/shared/
  StatusBadge.tsx           — Reusable badge component
  DataTable.tsx             — Shared table with sorting/pagination
  ErrorState.tsx            — Error fallback UI
  LoadingState.tsx          — Loading skeleton UI
```

### Anti-Patterns (Do Not)

- Do NOT embed mock data in components — use `src/lib/mock-data/`
- Do NOT have 10+ useState in one component — extract to custom hook
- Do NOT duplicate badge/status rendering — use shared components
- Do NOT mix data fetching with rendering — use custom hooks
- Do NOT skip ErrorBoundary — wrap at layout level minimum

---

## DRY — Don't Repeat Yourself

### Rule: Extract When Duplicated 3+ Times

If the same logic, UI pattern, or data transformation appears in **3 or more places**, extract it. Two occurrences are acceptable — three is a pattern that needs a shared abstraction.

### What to Extract and Where

| Duplication Type                                               | Extract To                                        | Example                                          |
| -------------------------------------------------------------- | ------------------------------------------------- | ------------------------------------------------ |
| **Same UI pattern** (badge, status chip, card layout)          | `src/app/components/shared/{Component}.tsx`       | `StatusBadge`, `EmptyState`, `DataTable`         |
| **Same data fetching** (list devices, get firmware)            | `src/lib/hooks/use-{entity}.ts`                   | `useDevices()`, `useFirmware()`                  |
| **Same business logic** (calculate health score, format dates) | `src/lib/utils/{domain}.ts`                       | `calculateHealthScore()`, `formatRelativeDate()` |
| **Same form validation**                                       | `src/lib/schemas/{entity}.schema.ts`              | `deviceSchema`, `firmwareSchema`                 |
| **Same API transform**                                         | Provider adapter (DTO ↔ Domain mapping)           | `toDevice(dto)`, `toFirmwareDTO(firmware)`       |
| **Same type definition**                                       | `src/lib/types.ts` or `src/lib/types/{domain}.ts` | Never define the same interface in two files     |

### What NOT to Extract (Acceptable Duplication)

- **2 occurrences** — wait for the third before extracting. Premature abstraction is worse than duplication.
- **Similar but not identical** logic — if two functions share 60% of their code but differ in business rules, keep them separate. Forced abstraction creates fragile shared code.
- **Test setup** — duplicate test setup is fine. Tests should be readable in isolation.
- **Constants used in one file** — don't create a shared constants file for values used in a single module.

### Detection (How to Find DRY Violations)

```bash
# Find duplicate component patterns (same className combinations)
grep -rn "className=\".*flex.*items-center.*gap-2.*rounded" src/app/components/ | sort

# Find duplicate fetch patterns
grep -rn "useState.*loading.*setLoading" src/app/components/ --include="*.tsx"

# Find duplicate Zod schemas
grep -rn "z\.object" src/ --include="*.ts" --include="*.tsx"

# Find duplicate status/badge rendering
grep -rn "badge\|Badge\|status.*===.*\"online\"\|status.*===.*\"active\"" src/app/components/
```

### Enforcement

- Code review: reviewer checks for 3+ occurrences of same pattern
- `/review-architecture` skill flags duplicate UI patterns
- `/review-full` audit area #9 (Code Quality) checks for duplication

---

## SRP — Single Responsibility Principle

### Rule: One File = One Job. One Function = One Task.

Every file, component, hook, and function should have **exactly one reason to change**. If you can describe what it does with "and" — it does too much.

### File-Level SRP

| File Type                                       | Single Responsibility                | Max Lines | Violation Signal                                              |
| ----------------------------------------------- | ------------------------------------ | --------- | ------------------------------------------------------------- |
| **Page component** (`{Module}Page.tsx`)         | Route entry + layout composition     | 200       | Contains business logic, API calls, or complex state          |
| **Feature component** (`{Feature}.tsx`)         | One feature's UI + interactions      | 400       | Has >10 useState, mixes data fetching with rendering          |
| **Custom hook** (`use-{feature}.ts`)            | Data fetching + state for one entity | 200       | Manages state for multiple unrelated entities                 |
| **Shared component** (`shared/{Component}.tsx`) | One reusable UI element              | 300       | Has domain-specific logic (device status, firmware version)   |
| **Utility function** (`utils/{name}.ts`)        | Pure transformation/computation      | 100       | Has side effects (API calls, state updates, DOM manipulation) |
| **Schema** (`schemas/{entity}.schema.ts`)       | Validation for one entity            | 100       | Validates multiple unrelated entities                         |

### Function-Level SRP

```typescript
// BAD — does 3 things: fetches, transforms, AND updates state
function loadDevicesAndUpdateDashboard() {
  const raw = await api.listDevices();
  const devices = raw.map(toDevice); // transform
  setDevices(devices); // update state
  const metrics = computeMetrics(devices); // compute
  setDashboardMetrics(metrics); // update different state
}

// GOOD — each function does one thing
function useDevices() {
  return useQuery({ queryKey: ["devices"], queryFn: () => api.listDevices() });
}
function useDashboardMetrics(devices: Device[]) {
  return useMemo(() => computeMetrics(devices), [devices]);
}
```

### How to Split a God Component

When a component violates SRP (>400 lines, >10 useState, multiple concerns):

```
BEFORE: MonolithPage.tsx (800 lines)
  - Route layout
  - Tab navigation
  - Device table with filters
  - Device form with validation
  - API calls for devices
  - Dashboard metrics computation

AFTER:
  MonolithPage.tsx (150 lines)        — Route layout + tab composition
  DeviceTable.tsx (250 lines)         — Table + filters UI
  DeviceForm.tsx (200 lines)          — Form + validation UI
  use-devices.ts (120 lines)          — Device data fetching + state
  use-dashboard-metrics.ts (50 lines) — Metrics computation hook
```

### Detection (How to Find SRP Violations)

```bash
# Files over 400 lines (warning threshold)
find src/ -name "*.tsx" -exec wc -l {} + | awk '$1 > 400' | sort -rn

# Components with excessive useState (>10 = God component)
for f in src/app/components/**/*.tsx; do
  count=$(grep -c "useState" "$f" 2>/dev/null)
  if [ "$count" -gt 10 ]; then echo "$f: $count useState calls"; fi
done

# Functions with multiple responsibilities (AND in comments)
grep -rn "// .*and\|// .*then\|// .*also" src/ --include="*.tsx" --include="*.ts"
```

### Enforcement

- **400-line warning** / **600-line block** (from code-quality-rulebook)
- **>10 useState** in one component = must extract to custom hook
- Architecture hook warns on files created outside standard structure
- `/review-architecture` skill checks component responsibility

---

## Custom Hooks Pattern

```typescript
// src/lib/hooks/use-devices.ts
export function useDevices(filters?: DeviceFilters) {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch, filter, sort logic here
  // Return { devices, loading, error, refetch }
}
```

Every async data source should have a hook returning: `{ data, loading, error, refetch }`.

---

## Accessibility (WCAG 2.1 AA)

### Required on Every Component

- Images: `alt` attribute (descriptive or `alt=""` for decorative + `aria-hidden="true"`)
- Form inputs: `<label htmlFor={id}>` or `aria-label`
- Form errors: `role="alert"` + `aria-describedby` linked to input
- Interactive elements: keyboard accessible (Enter/Space triggers action)
- Color: 4.5:1 contrast ratio for normal text, 3:1 for large text

### Required on Modals/Dialogs

- Focus trap (Radix Dialog provides this — verify it's not bypassed)
- `aria-modal="true"` + `role="dialog"`
- Escape key closes
- Focus returns to trigger element on close

### Required on Loading States

- `aria-busy="true"` on container
- Screen reader announcement via `aria-live="polite"` region

### Enforced by

- `eslint-plugin-jsx-a11y` in ESLint config (errors block CI)
- Pre-commit hook runs ESLint on staged files

---

## Error Handling Pattern

```typescript
// Root level
<ErrorBoundary fallback={<ErrorState />}>
  <App />
</ErrorBoundary>

// Component level
try {
  await api.createDevice(data);
  toast.success("Device created");
} catch (err) {
  toast.error("Failed to create device");
}
```

Every async operation must handle: loading → success → error.

---

## Performance

- `React.memo` on pure display components (badges, cards, table rows)
- `useMemo` for filtered/sorted lists, chart data computations
- `useCallback` for event handlers passed to child components
- Virtualize tables with 50+ rows (`@tanstack/react-virtual`)
- No inline object/array creation in JSX props

---

## Testing

- 85% coverage on new/modified code
- Co-locate tests: `src/__tests__/{module}/{Component}.test.tsx`
- Use React Testing Library — test behavior, not implementation
- Every user-facing feature needs at least one integration test

---

## Type Safety

- **No `any` type** — use `unknown` and narrow with type guards
- Enforced by `@typescript-eslint/no-explicit-any: "error"` in ESLint
- Pre-commit hook blocks commits containing `any`

---

## Console Logging

- **No `console.log`** in production code — use structured logging
- `console.warn` and `console.error` are permitted
- Enforced by `no-console: "error"` in ESLint (allows `warn`, `error`)

---

## Import Ordering

Imports should follow this order, separated by blank lines:

1. **External packages** — `react`, `react-router`, third-party libs
2. **Internal aliases** — `@/lib/`, `@/app/components/`
3. **Relative imports** — `./`, `../` (only within the same module)

### No Barrel Exports

- Do NOT create `index.ts` files that re-export from other files
- Import directly from the source file: `import { Device } from '@/lib/types'`
- Barrel exports create circular dependency risks and slow builds

---

## Naming

- Files: `kebab-case.tsx`
- Components: `PascalCase`
- Hooks: `use-{name}.ts` → `useName()`
- Types: `PascalCase` in `src/lib/types.ts`
- Shared components: `src/app/components/shared/`
- Constants: `UPPER_SNAKE_CASE`
