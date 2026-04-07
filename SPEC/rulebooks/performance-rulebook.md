# Rulebook: Performance Standards

> Rendering, bundle size, lazy loading, and optimization rules for IMS Gen2.
> Measurable thresholds enforced by CI and Lighthouse audits.

---

## Performance Budgets

### Core Web Vitals Targets

| Metric                             | Target  | Measurement                               |
| ---------------------------------- | ------- | ----------------------------------------- |
| **LCP** (Largest Contentful Paint) | < 2.5s  | First meaningful render after navigation  |
| **FID** (First Input Delay)        | < 100ms | Time to respond to first user interaction |
| **CLS** (Cumulative Layout Shift)  | < 0.1   | Visual stability during load              |
| **TTI** (Time to Interactive)      | < 3.5s  | Full interactivity after navigation       |
| **FCP** (First Contentful Paint)   | < 1.8s  | First visible content rendered            |

### Bundle Size Budgets

| Bundle                 | Max Size (gzipped) | Contents                             |
| ---------------------- | ------------------ | ------------------------------------ |
| **Initial JS**         | 150 KB             | React, Router, core UI shell         |
| **Per-route chunk**    | 80 KB              | Page component + direct dependencies |
| **Vendor chunk**       | 200 KB             | Third-party libraries                |
| **CSS**                | 50 KB              | Tailwind (purged)                    |
| **Total initial load** | 300 KB             | JS + CSS before any route            |

### Measurement

```bash
# Build analysis
npx vite-bundle-visualizer

# Lighthouse CI (add to CI pipeline)
npx lhci autorun --preset=desktop
```

---

## Rendering Optimization

### Memoization Rules

| Pattern         | When to Use                                                            | When NOT to Use                                |
| --------------- | ---------------------------------------------------------------------- | ---------------------------------------------- |
| `React.memo()`  | Pure display components receiving stable props (Badge, Card, TableRow) | Components with frequently changing props      |
| `useMemo()`     | Filtered/sorted lists, computed aggregations, chart data               | Simple derivations (`fullName = first + last`) |
| `useCallback()` | Event handlers passed to memoized children or dependency arrays        | Handlers used only in the same component       |

### Rules

- **Measure before optimizing** — use React DevTools Profiler to identify actual bottlenecks
- Every `React.memo` must have a comment explaining why it's needed
- Never memoize components that receive new object/array literals as props — fix the props first
- `useMemo`/`useCallback` dependency arrays must be correct — wrong deps are worse than no memo

### Re-Render Prevention

```typescript
// BAD — creates new object every render, breaks React.memo
<DeviceCard style={{ marginTop: 8 }} filters={{ status: "online" }} />

// GOOD — stable references
const cardStyle = useMemo(() => ({ marginTop: 8 }), []);
const filters = useMemo(() => ({ status: "online" }), []);
<DeviceCard style={cardStyle} filters={filters} />

// BEST — if truly static, define outside component
const CARD_STYLE = { marginTop: 8 } as const;
const ONLINE_FILTER = { status: "online" } as const;
```

### Rules

- No inline object/array creation in JSX props for memoized components
- No inline function creation in JSX props for memoized components
- State updates that don't change the value should be avoided (`setState(sameValue)`)
- Use `useRef` for values that shouldn't trigger re-renders (timers, previous values)

---

## Code Splitting & Lazy Loading

### Route-Level Splitting (Required)

```typescript
// App.tsx — every route is lazy loaded
const DashboardPage = React.lazy(() => import("./components/dashboard/DashboardPage"));
const DevicesPage = React.lazy(() => import("./components/devices/DevicesPage"));
const FirmwarePage = React.lazy(() => import("./components/firmware/FirmwarePage"));
// ... all pages lazy loaded

<Suspense fallback={<PageSkeleton />}>
  <Routes>
    <Route path="/dashboard" element={<DashboardPage />} />
    <Route path="/devices" element={<DevicesPage />} />
    {/* ... */}
  </Routes>
</Suspense>
```

### Component-Level Splitting (Heavy Dependencies)

Lazy load components that pull in large libraries:

| Component         | Library           | Size     | Action                         |
| ----------------- | ----------------- | -------- | ------------------------------ |
| Charts/graphs     | Recharts          | ~150 KB  | Lazy load chart components     |
| Map views         | react-simple-maps | ~100 KB  | Lazy load map component        |
| PDF export        | jspdf / pdfmake   | ~200 KB  | Dynamic import on export click |
| Rich text editor  | (future)          | ~100 KB+ | Lazy load on focus             |
| Date range picker | (if heavy)        | varies   | Evaluate, lazy load if >30 KB  |

```typescript
// Lazy load heavy component
const GeoMap = React.lazy(() => import("./GeoMap"));

function GeoLocationPage() {
  return (
    <Suspense fallback={<MapSkeleton />}>
      <GeoMap devices={devices} />
    </Suspense>
  );
}
```

### Rules

- Every route must be a separate chunk — no eager loading of pages
- `Suspense` fallback must be a skeleton, not a spinner
- Preload adjacent routes on hover/focus for perceived performance
- Dynamic imports for user-triggered heavy operations (export, print, bulk edit)

---

## List & Table Virtualization

### When to Virtualize

| Row Count   | Action                                         |
| ----------- | ---------------------------------------------- |
| < 50 rows   | No virtualization needed                       |
| 50–200 rows | Consider virtualization if rendering is slow   |
| 200+ rows   | MUST virtualize with `@tanstack/react-virtual` |

### Implementation

```typescript
import { useVirtualizer } from "@tanstack/react-virtual";

function DeviceTable({ devices }: { devices: Device[] }) {
  const parentRef = useRef<HTMLDivElement>(null);
  const virtualizer = useVirtualizer({
    count: devices.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 48, // row height in px
    overscan: 10,
  });

  return (
    <div ref={parentRef} style={{ height: "600px", overflow: "auto" }}>
      <div style={{ height: `${virtualizer.getTotalSize()}px`, position: "relative" }}>
        {virtualizer.getVirtualItems().map((virtualRow) => (
          <DeviceRow key={devices[virtualRow.index].id} device={devices[virtualRow.index]} />
        ))}
      </div>
    </div>
  );
}
```

### Rules

- Virtualized lists must maintain scroll position on data updates
- Search/filter should not reset scroll position unless result set changes
- Row heights: fixed preferred, variable supported with `measureElement`
- Keyboard navigation must work in virtualized lists (focus management)

---

## Image & Asset Optimization

### Rules

- All images served via CDN (CloudFront/Front Door/Static Web Apps CDN)
- Use WebP format with JPEG/PNG fallback via `<picture>` element
- Lazy load images below the fold: `loading="lazy"`
- Set explicit `width` and `height` on all `<img>` to prevent CLS
- SVG icons via Lucide React (tree-shaken, not full icon sprite)
- No images > 200 KB — compress before committing

---

## API Performance

### Debouncing & Throttling

| Interaction   | Strategy            | Delay          |
| ------------- | ------------------- | -------------- |
| Search input  | Debounce            | 300ms          |
| Filter change | Debounce            | 150ms          |
| Scroll event  | Throttle            | 100ms          |
| Resize event  | Throttle            | 200ms          |
| Button click  | Disable after click | Until response |

### Rules

- Search: debounce API calls, not the input update (keep UI responsive)
- Never fire API calls on every keystroke — always debounce
- Bulk operations: batch API calls where the backend supports it
- Parallel queries: use `Promise.all` for independent data fetches on page load
- Cancel inflight requests when user navigates away (`AbortController`)

### Prefetching

```typescript
// Prefetch on hover for instant navigation
const queryClient = useQueryClient();

function DeviceListItem({ device }: { device: Device }) {
  const prefetch = () => {
    queryClient.prefetchQuery({
      queryKey: ["devices", device.id],
      queryFn: () => api.getDevice(device.id),
      staleTime: 30_000,
    });
  };

  return (
    <Link to={`/devices/${device.id}`} onMouseEnter={prefetch}>
      {device.name}
    </Link>
  );
}
```

---

## Animation Performance

### Rules

- Only animate `transform` and `opacity` — these are GPU-composited
- Never animate `width`, `height`, `top`, `left`, `margin`, `padding` — causes layout thrash
- Transitions: 150–200ms duration, `ease-out` timing (per design principles)
- Use `will-change` sparingly — only on elements about to animate
- Framer Motion: use `layout` prop for layout animations, not manual positioning
- Disable animations for users who prefer reduced motion:

```css
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
```

---

## Memory Management

### Rules

- Clean up subscriptions, timers, and event listeners in `useEffect` return
- Abort pending API calls on unmount (`AbortController`)
- Release object URLs created with `URL.createObjectURL` via `URL.revokeObjectURL`
- Large data sets: paginate, don't load all into memory
- Charts: dispose chart instances on unmount (if using direct lib APIs)

### Common Leaks to Watch

```typescript
// LEAK — timer not cleaned up
useEffect(() => {
  setInterval(fetchStatus, 30000);
}, []);

// FIXED
useEffect(() => {
  const timer = setInterval(fetchStatus, 30000);
  return () => clearInterval(timer);
}, []);
```

---

## Build Optimization (Vite)

### Required Vite Config

```typescript
// vite.config.ts
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ["react", "react-dom", "react-router"],
          ui: ["@radix-ui/react-dialog", "@radix-ui/react-dropdown-menu" /* ... */],
          charts: ["recharts"],
        },
      },
    },
    chunkSizeWarningLimit: 500, // KB — warn on large chunks
    sourcemap: true, // Always generate sourcemaps
  },
});
```

### Rules

- Tree shaking: import specific functions, not entire libraries (`import { format } from "date-fns"`, not `import * as dateFns`)
- No unused dependencies in `package.json` — audit quarterly
- Dev dependencies never in production bundle — check with bundle analyzer
- CSS: Tailwind purges unused classes automatically — verify purge config is correct
