# Rulebook: API & Data Layer Standards

> API client architecture, data flow patterns, caching, and validation rules for IMS Gen2.
> Ensures clean separation between UI and data, consistent error handling, and type safety.

---

## API Client Architecture

### Single Entry Point

All API calls go through the provider abstraction:

```
Component → Custom Hook → IApiProvider → Cloud Adapter → Backend
```

**Never:**

- Call `fetch()` or `axios` directly from a component
- Import a cloud SDK (`@aws-amplify/*`, `@azure/*`) from a component
- Construct API URLs in component code

### API Provider Contract

Every `IApiProvider` method must:

1. Accept typed input parameters (Zod-validated at system boundary)
2. Return typed output (`Promise<T>` where T is from `lib/types.ts`)
3. Throw typed errors that the hook layer can catch and translate
4. Include request metadata headers (`X-App-Version`, `X-Request-Id`)

### Request/Response Flow

```
┌──────────────┐    ┌──────────────┐    ┌──────────────────┐    ┌──────────────┐
│  Component   │───▶│  Custom Hook │───▶│  API Provider    │───▶│   Backend    │
│  (renders)   │    │  (TanStack Q)│    │  (adapter impl)  │    │  (AppSync/   │
│              │◀───│              │◀───│                  │◀───│   Functions) │
└──────────────┘    └──────────────┘    └──────────────────┘    └──────────────┘
     UI Model          Cache Layer         Transport Layer         Data Source
```

---

## Data Model Separation

### Three Model Layers

| Layer            | Name    | Location           | Purpose                                             |
| ---------------- | ------- | ------------------ | --------------------------------------------------- |
| **API Model**    | DTO     | `lib/providers/*/` | Wire format — matches backend schema exactly        |
| **Domain Model** | Type    | `lib/types.ts`     | App-internal — what components work with            |
| **View Model**   | Derived | Component/hook     | Display-specific — formatted dates, computed labels |

### Transformation Rules

- **API → Domain:** Transform in the provider adapter. Rename fields, parse dates, map enums.
- **Domain → View:** Transform in the custom hook or component. Format for display.
- **Domain → API:** Transform in the provider adapter before sending.

```typescript
// In provider adapter (e.g., amplify-api-provider.ts)
function toDevice(dto: DeviceDTO): Device {
  return {
    id: dto.PK.replace("DEVICE#", ""),
    name: dto.deviceName,
    status: dto.deviceStatus as DeviceStatus,
    lastSeen: new Date(dto.lastSeenTimestamp),
    // ... map all fields explicitly
  };
}

function toDeviceDTO(device: Partial<Device>): Partial<DeviceDTO> {
  return {
    deviceName: device.name,
    deviceStatus: device.status,
    // ... reverse map
  };
}
```

### Rules

- Components NEVER see DTOs — only domain types from `lib/types.ts`
- Provider adapters own the DTO ↔ Domain mapping
- Never spread DTOs into domain objects (`{...dto}`) — explicit mapping only
- Date strings from API → `Date` objects in domain model → formatted strings in view

---

## Caching Strategy (TanStack Query)

### Query Key Convention

```typescript
// Consistent key structure: [entity, scope, filters]
queryKey: ["devices"]; // all devices
queryKey: ["devices", deviceId]; // single device
queryKey: ["devices", { status: "online" }]; // filtered list
queryKey: ["firmware", firmwareId, "versions"]; // nested resource
queryKey: ["dashboard", "metrics", timeRange]; // scoped query
```

### Cache Configuration by Data Type

| Data Type                                        | staleTime        | gcTime | Refetch Strategy           |
| ------------------------------------------------ | ---------------- | ------ | -------------------------- |
| **Reference data** (roles, statuses, categories) | 30 min           | 60 min | On window focus            |
| **List data** (devices, orders, firmware)        | 30 sec           | 5 min  | On window focus + interval |
| **Detail data** (single device, single order)    | 1 min            | 10 min | On window focus            |
| **Dashboard metrics**                            | 30 sec           | 2 min  | Polling interval (30s)     |
| **Audit logs**                                   | 0 (always fresh) | 5 min  | Manual refetch             |
| **Real-time data** (device status)               | 0                | 1 min  | WebSocket/subscription     |

### Mutation Patterns

```typescript
// Optimistic update pattern
const mutation = useMutation({
  mutationFn: (data) => api.updateDevice(data),
  onMutate: async (newData) => {
    await queryClient.cancelQueries({ queryKey: ["devices", id] });
    const previous = queryClient.getQueryData(["devices", id]);
    queryClient.setQueryData(["devices", id], newData);
    return { previous };
  },
  onError: (err, newData, context) => {
    queryClient.setQueryData(["devices", id], context?.previous);
    toast.error("Failed to update device");
  },
  onSettled: () => {
    queryClient.invalidateQueries({ queryKey: ["devices"] });
  },
});
```

### Rules

- Every `useQuery` must have `staleTime` set — never rely on default (0)
- Mutations must invalidate related queries on success
- Optimistic updates for user-initiated actions (toggle, status change)
- Pessimistic updates for critical operations (firmware approval, compliance submission)
- Never manually set query data without `cancelQueries` first

---

## Error Handling

### Error Classification

| Error Type       | HTTP Status | User Message                                | Action                    |
| ---------------- | ----------- | ------------------------------------------- | ------------------------- |
| **Validation**   | 400         | "Please check your input" + field errors    | Show inline errors        |
| **Unauthorized** | 401         | — (silent redirect)                         | Redirect to sign-in       |
| **Forbidden**    | 403         | "You don't have permission for this action" | Show toast                |
| **Not Found**    | 404         | "Resource not found"                        | Show empty state          |
| **Conflict**     | 409         | "This was modified by someone else"         | Show conflict resolution  |
| **Rate Limited** | 429         | "Too many requests, please wait"            | Auto-retry with backoff   |
| **Server Error** | 500+        | "Something went wrong. Please try again."   | Show toast + retry button |
| **Network**      | 0 / timeout | "Unable to connect. Check your connection." | Show retry button         |

### Retry Strategy

```typescript
// TanStack Query retry config
{
  retry: (failureCount, error) => {
    if (error.status === 401 || error.status === 403 || error.status === 404) return false;
    if (error.status === 429) return failureCount < 5;
    return failureCount < 3;
  },
  retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
}
```

### Rules

- Never show raw error messages to users — map to user-friendly text
- Never swallow errors silently — always log and/or notify
- 401 errors: redirect to sign-in, preserve intended route for post-login redirect
- Network errors: show inline retry, not just a toast that disappears
- Log all API errors with: endpoint, status, request ID, timestamp

---

## Input Validation

### Zod Schema Pattern

```typescript
// lib/schemas/{entity}.schema.ts
import { z } from "zod";

export const createDeviceSchema = z.object({
  name: z.string().min(1, "Name is required").max(100),
  serialNumber: z.string().regex(/^[A-Z0-9-]+$/, "Invalid serial number format"),
  modelId: z.string().uuid("Invalid model"),
  locationId: z.string().uuid("Invalid location"),
  customerId: z.string().uuid("Invalid customer"),
});

export type CreateDeviceInput = z.infer<typeof createDeviceSchema>;
```

### Rules

- Every form: `zodResolver(schema)` with `react-hook-form`
- Every API input: validated before sending (in the hook or provider)
- URL params: parsed with `z.string().uuid()` or equivalent before use
- Search inputs: sanitized (trim, limit length, escape special chars)
- File inputs: validate MIME type + size before upload

### Validation Locations

| Boundary    | What to Validate            | Tool                   |
| ----------- | --------------------------- | ---------------------- |
| Form submit | All user input              | react-hook-form + Zod  |
| API request | Request body before sending | Zod `.parse()` in hook |
| URL params  | Route params, query strings | Zod in route loader    |
| File upload | Type, size, name format     | Custom validator + Zod |

---

## Pagination

### Cursor-Based (Preferred)

```typescript
interface PaginatedResponse<T> {
  items: T[];
  nextToken?: string; // Opaque cursor for next page
  totalCount?: number; // Optional — expensive for DynamoDB
}
```

- Use cursor-based for DynamoDB/Cosmos DB (offset-based is anti-pattern)
- `useInfiniteQuery` for infinite scroll lists
- `useQuery` with page token for paginated tables
- Default page size: 25 items (configurable per table)

### Rules

- Never load all records — always paginate
- Show total count only if backend provides it efficiently
- Preserve pagination state in URL params for shareable links
- Loading state: show skeleton rows, not a spinner replacing the table

---

## Real-Time Data

### Subscription Pattern

```
Backend event → AppSync subscription / SignalR → Custom hook → Component re-render
```

### Rules

- Subscriptions managed in custom hooks, not components
- Unsubscribe on hook cleanup (return function in `useEffect`)
- Fallback to polling (30s) when subscriptions unavailable
- Reconnection: automatic with exponential backoff
- Stale subscription data: merge with cached query data, don't replace

---

## API Security

### Request Headers (Required)

```typescript
{
  "Authorization": "Bearer <token>",
  "Content-Type": "application/json",
  "X-App-Version": "<build-version>",
  "X-Request-Id": "<uuid>",        // For request tracing
  "X-Client-IP": "<ip>",           // For audit logging
}
```

### Rules

- Auth token attached automatically by provider adapter — never manually
- Never log request/response bodies containing PII or credentials
- GraphQL: no introspection in production
- REST: no CORS `*` — whitelist specific origins
- Rate limit awareness: read `Retry-After` header, respect it
