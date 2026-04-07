# HLM Platform — API Catalogue

> **Last Updated:** 2026-03-25
> **Authenticated as:** ajaykumar.yadav@3pillarglobal.com
> **Method:** Live browser session + GraphQL introspection (`__schema` / `__type`) + Playwright API test execution against live AppSync endpoint
> **Tests:** 98 tests across 7 spec files — **93 passed · 5 skipped · 0 failed**

---

## Infrastructure

| Layer | Service | URL |
|---|---|---|
| GraphQL API | AWS AppSync | `https://rw3sl7mwgzcvbg7zuyqj7ppjv4.appsync-api.us-east-2.amazonaws.com/graphql` |
| Auth (User Pool) | AWS Cognito IDP | `https://cognito-idp.us-east-2.amazonaws.com/` |
| Auth (Identity) | AWS Cognito Identity | `https://cognito-identity.us-east-2.amazonaws.com/` |
| Map Tiles | AWS Location Service (MapLibre) | `https://maps.geo.us-east-2.amazonaws.com/maps/v0/maps/HlmMapOpenData-*/` |
| App Hosting | AWS Amplify | `https://main.dddsig2mih3hw.amplifyapp.com` |

All application data calls are **GraphQL POST** requests to the single AppSync endpoint.

### Authentication

```http
POST https://rw3sl7mwgzcvbg7zuyqj7ppjv4.appsync-api.us-east-2.amazonaws.com/graphql
Authorization: <Cognito idToken>          ← raw JWT, no "Bearer" prefix
Content-Type: application/json
```

- **User Pool:** `us-east-2_Q36YWNsEC`
- **Client ID:** `3aseu1rf3q3tae7u4dgplllii8`
- The `Authorization` header carries the raw Cognito `idToken` (not the access token, and **no** `Bearer ` prefix).
- Unauthenticated requests receive a `401 UnauthorizedException`.

### AppSync Operation Name Constraint

The `operationName` field in the POST body **must exactly match** the named operation in the `query` document. Suffixes like `listDevices_Online` or aliased operation names cause a `400 BadRequestException`. Always use the same name in both places:

```json
{
  "operationName": "listDevices",
  "query": "query listDevices($status: String) { listDevices(status: $status) { items totalCount } }",
  "variables": { "status": "Online" }
}
```

---

## Common Response Shape

All list / paginated operations return a `PaginatedResponse` wrapper:

```
PaginatedResponse {
  items:      AWSJSON   // AppSync AWSJSON scalar — serialised JSON array of domain objects
  nextToken:  String    // cursor for next page (null if no more pages)
  totalCount: Int       // total count of matching records
}
```

> **Important:** `items` is an **AWSJSON scalar** — you cannot sub-select fields in the GraphQL document.
> Query it as a bare field (`items`) and parse the JSON in your client.
> The field is `totalCount`, **not** `total`.

Single-item operations return the domain type directly (`DeviceType`, `FirmwareType`, etc.).

---

## Queries

### Device Queries

#### `listDevices`
| | |
|---|---|
| **Purpose** | List all devices, optionally filtered by status. Powers the Hardware Inventory table and Dashboard KPI total device count |
| **Used on** | Dashboard (KPI cards), Inventory → Hardware Inventory tab, Reporting & Analytics |
| **Arguments** | `status?: String`, `limit?: Int`, `nextToken?: String` |
| **Returns** | `PaginatedResponse` |
| **Status values** | `Online`, `Offline`, `Maintenance` |
| **Notes** | Status filter is applied server-side and verified by tests |

```graphql
query listDevices($status: String, $limit: Int, $nextToken: String) {
  listDevices(status: $status, limit: $limit, nextToken: $nextToken) {
    items
    nextToken
    totalCount
  }
}
```

#### `getDevice`
| | |
|---|---|
| **Purpose** | Fetch a single device record by its unique ID |
| **Used on** | Inventory → Hardware Inventory tab (device detail view) |
| **Arguments** | `id: String!` |
| **Returns** | `DeviceType` |

```graphql
query getDevice($id: String!) {
  getDevice(id: $id) {
    id
    deviceName
    serialNumber
    model
    status
    location
    firmwareVersion
  }
}
```

#### `getDevicesByCustomer`
| | |
|---|---|
| **Purpose** | Retrieve all devices belonging to a specific customer account |
| **Used on** | Account & Service page (customer device listing) |
| **Arguments** | `customerId: String!` |
| **Returns** | `PaginatedResponse` |

```graphql
query getDevicesByCustomer($customerId: String!) {
  getDevicesByCustomer(customerId: $customerId) {
    items
    totalCount
  }
}
```

#### `getDevicesByLocation`
| | |
|---|---|
| **Purpose** | Retrieve all devices at a named location (building / site). Powers map-pin grouping on the Geo Location tab |
| **Used on** | Inventory → Geo Location tab |
| **Arguments** | `location: String!` |
| **Returns** | `PaginatedResponse` |

```graphql
query getDevicesByLocation($location: String!) {
  getDevicesByLocation(location: $location) {
    items
    totalCount
  }
}
```

---

### Firmware Queries

#### `listFirmware`
| | |
|---|---|
| **Purpose** | List all firmware entries, optionally filtered by status. Powers the Firmware Versions table and Inventory Firmware Status tab |
| **Used on** | Inventory → Firmware Status tab, Deployment → Firmware Versions tab, Dashboard (pending approvals KPI) |
| **Arguments** | `status?: String`, `limit?: Int`, `nextToken?: String` |
| **Returns** | `PaginatedResponse` |
| **Status values** | `Pending`, `Approved`, `Rejected` _(filter works)_, `Deprecated` _(backend filter not applied — returns all records)_ |

```graphql
query listFirmware($status: String, $limit: Int, $nextToken: String) {
  listFirmware(status: $status, limit: $limit, nextToken: $nextToken) {
    items
    nextToken
    totalCount
  }
}
```

#### `getFirmware`
| | |
|---|---|
| **Purpose** | Fetch a single firmware record by ID |
| **Used on** | Deployment → Firmware Versions (detail panel), Firmware Compliance (detail view) |
| **Arguments** | `id: String!` |
| **Returns** | `FirmwareType` |

```graphql
query getFirmware($id: String!) {
  getFirmware(id: $id) {
    id
    name
    version
    deviceModel
    status
    releaseDate
    fileSize
    checksum
  }
}
```

#### `getFirmwareByModel`
| | |
|---|---|
| **Purpose** | Retrieve all firmware versions applicable to a given device model |
| **Used on** | Inventory → Firmware Status tab (model-scoped filtering) |
| **Arguments** | `deviceModel: String!` |
| **Returns** | `PaginatedResponse` |

```graphql
query getFirmwareByModel($deviceModel: String!) {
  getFirmwareByModel(deviceModel: $deviceModel) {
    items
    totalCount
  }
}
```

#### `getFirmwareWithRelations`
| | |
|---|---|
| **Purpose** | Fetch a firmware record together with its associated compliance records and audit log entries |
| **Used on** | Deployment → Firmware Versions (expanded details panel) |
| **Arguments** | `id: String!` |
| **Returns** | `PaginatedResponse` (firmware + nested compliance + audit items) |

```graphql
query getFirmwareWithRelations($id: String!) {
  getFirmwareWithRelations(id: $id) {
    items
    totalCount
  }
}
```

---

### Service Order Queries

#### `listServiceOrdersByStatus`
| | |
|---|---|
| **Purpose** | List service orders filtered by workflow status. Powers the Account & Service queue |
| **Used on** | Account & Service page, Dashboard (KPI — pending actions) |
| **Arguments** | `status: String!` |
| **Returns** | `PaginatedResponse` |
| **Status values** | `Pending`, `In Progress`, `Completed`, `Cancelled` |

```graphql
query listServiceOrdersByStatus($status: String!) {
  listServiceOrdersByStatus(status: $status) {
    items
    totalCount
  }
}
```

#### `listServiceOrdersByDate`
| | |
|---|---|
| **Purpose** | List service orders that fall within a date range. Used for scheduling views and analytics trend data |
| **Used on** | Account & Service page (date-range filter), Reporting & Analytics (deployment trend chart) |
| **Arguments** | `startDate: String!` (ISO 8601), `endDate: String!` (ISO 8601) |
| **Returns** | `PaginatedResponse` |
| **Notes** | Returns empty array for reversed ranges (end < start) or future ranges |

```graphql
query listServiceOrdersByDate($startDate: String!, $endDate: String!) {
  listServiceOrdersByDate(startDate: $startDate, endDate: $endDate) {
    items
    totalCount
  }
}
```

#### `getServiceOrder`
| | |
|---|---|
| **Purpose** | Fetch a single service order by ID |
| **Used on** | Account & Service page (order detail view) |
| **Arguments** | `id: String!` |
| **Returns** | `ServiceOrderType` |

```graphql
query getServiceOrder($id: String!) {
  getServiceOrder(id: $id) {
    id
    title
    description
    status
    priority
    technicianId
    scheduledDate
  }
}
```

#### `getServiceOrdersByTechnician`
| | |
|---|---|
| **Purpose** | Retrieve all service orders assigned to a specific technician |
| **Used on** | Account & Service page (technician filter) |
| **Arguments** | `technicianId: String!` |
| **Returns** | `PaginatedResponse` |

```graphql
query getServiceOrdersByTechnician($technicianId: String!) {
  getServiceOrdersByTechnician(technicianId: $technicianId) {
    items
    totalCount
  }
}
```

---

### Compliance Queries

#### `listComplianceByStatus`
| | |
|---|---|
| **Purpose** | List compliance records filtered by certification status. Powers the Firmware Compliance table |
| **Used on** | Firmware Compliance page, Dashboard (quick actions badge), Reporting & Analytics |
| **Arguments** | `status: String!` |
| **Returns** | `PaginatedResponse` |
| **Status values** | `Approved`, `Pending`, `Deprecated` |

```graphql
query listComplianceByStatus($status: String!) {
  listComplianceByStatus(status: $status) {
    items
    totalCount
  }
}
```

#### `getCompliance`
| | |
|---|---|
| **Purpose** | Fetch a single compliance record by ID |
| **Used on** | Firmware Compliance page (record detail) |
| **Arguments** | `id: String!` |
| **Returns** | `ComplianceType` |

```graphql
query getCompliance($id: String!) {
  getCompliance(id: $id) {
    id
    firmwareId
    firmwareVersion
    deviceModel
    status
    certifications
    vulnerabilities
  }
}
```

#### `getComplianceByCertification`
| | |
|---|---|
| **Purpose** | Filter compliance records by a specific certification standard |
| **Used on** | Firmware Compliance page (certification filter) |
| **Arguments** | `certification: String!` |
| **Returns** | `PaginatedResponse` |
| **Known certifications** | `HIPAA`, `ISO 27001`, `FCC`, `WiFi Alliance`, `CE` |

```graphql
query getComplianceByCertification($certification: String!) {
  getComplianceByCertification(certification: $certification) {
    items
    totalCount
  }
}
```

---

### Audit Log Queries

#### `listAuditLogs`
| | |
|---|---|
| **Purpose** | Retrieve audit log entries within a time window. Dashboard uses a 24-hour window; Deployment Audit Log loads the 50 most recent entries |
| **Used on** | Dashboard (Recent Alerts panel — 24 h window), Deployment → Audit Log tab, Reporting & Analytics |
| **Arguments** | `startDate: String!` (ISO 8601), `endDate: String!` (ISO 8601), `limit?: Int` |
| **Returns** | `PaginatedResponse` |
| **Notes** | Returns empty array for future date ranges. Reversed ranges (end < start) also return empty |

```graphql
query listAuditLogs($startDate: String!, $endDate: String!, $limit: Int) {
  listAuditLogs(startDate: $startDate, endDate: $endDate, limit: $limit) {
    items
    nextToken
    totalCount
  }
}
```

#### `getAuditLogsByUser`
| | |
|---|---|
| **Purpose** | Retrieve the audit trail for a specific user (by their user ID) |
| **Used on** | Reporting & Analytics (filtered audit log view) |
| **Arguments** | `userId: String!` |
| **Returns** | `PaginatedResponse` |

```graphql
query getAuditLogsByUser($userId: String!) {
  getAuditLogsByUser(userId: $userId) {
    items
    totalCount
  }
}
```

---

### User & Customer Queries

#### `getUserByEmail`
| | |
|---|---|
| **Purpose** | Look up a user profile by email address. Resolves the logged-in user's display name and role on first load |
| **Used on** | App bootstrap / session init (all pages) |
| **Arguments** | `email: String!` |
| **Returns** | `UserType` |
| **Notes** | Returns `null` if the email exists in Cognito but not in the application DB |

```graphql
query getUserByEmail($email: String!) {
  getUserByEmail(email: $email) {
    id
    email
    role
    firstName
    lastName
  }
}
```

#### `listUsersByRole`
| | |
|---|---|
| **Purpose** | Retrieve users by role for assignment dropdowns |
| **Used on** | Account & Service page (technician assignment dropdown) |
| **Arguments** | `role: String!` |
| **Returns** | `PaginatedResponse` |
| **⚠️ Backend Quirk** | The `role` parameter is **not applied server-side** — all users are returned regardless of the value passed. This is a known backend limitation. |

```graphql
query listUsersByRole($role: String!) {
  listUsersByRole(role: $role) {
    items
    totalCount
  }
}
```

#### `getCustomerWithRelations`
| | |
|---|---|
| **Purpose** | Fetch a customer record along with all related devices and service orders |
| **Used on** | Account & Service page (customer detail panel) |
| **Arguments** | `customerId: String!` |
| **Returns** | `PaginatedResponse` (customer + nested devices + service orders) |

```graphql
query getCustomerWithRelations($customerId: String!) {
  getCustomerWithRelations(customerId: $customerId) {
    items
    totalCount
  }
}
```

---

## Mutations

### Device Mutations

#### `createDevice`
| | |
|---|---|
| **Purpose** | Register a new device in the system (hardware onboarding) |
| **Used on** | Inventory → Hardware Inventory tab (Add Device form) |
| **Arguments** | `deviceName: String!`, `serialNumber: String!`, `model: String!`, + additional fields |
| **Returns** | `DeviceType` |

#### `updateDeviceCoords`
| | |
|---|---|
| **Purpose** | Update the GPS coordinates of a device (geocoding from address). Called when a device address is set/changed so the Geo Location map pin moves |
| **Used on** | Inventory → Geo Location tab (pin position update) |
| **Arguments** | `address: String!`, `lat: Float!`, `lng: Float!` |
| **Returns** | `GeocodedDeviceType` |

```graphql
mutation updateDeviceCoords($address: String!, $lat: Float!, $lng: Float!) {
  updateDeviceCoords(address: $address, lat: $lat, lng: $lng) {
    id
    address
    lat
    lng
  }
}
```

---

### Firmware Mutations

#### `createFirmware`
| | |
|---|---|
| **Purpose** | Upload and register a new firmware package. Creates a `Pending` entry in Deployment and Firmware Compliance queues |
| **Used on** | Deployment → "Upload Firmware" button |
| **Arguments** | `name: String!`, `version: String!`, `deviceModel: String!`, `fileSize?: Int` (bytes), `releaseDate?: String`, `checksum?: String` |
| **Returns** | `FirmwareType` |
| **Notes** | `fileSize` is `Int` (bytes), not `Float`. Resolver may require additional fields (`fileName`, `s3Key`, `s3Bucket`, `uploadedBy`) depending on deployment config. New firmware defaults to `Pending` status. |

```graphql
mutation createFirmware(
  $name: String!
  $version: String!
  $deviceModel: String!
  $releaseDate: String
  $fileSize: Int
  $checksum: String
) {
  createFirmware(
    name: $name
    version: $version
    deviceModel: $deviceModel
    releaseDate: $releaseDate
    fileSize: $fileSize
    checksum: $checksum
  ) {
    id
    name
    version
    deviceModel
    status
  }
}
```

#### `approveFirmware`
| | |
|---|---|
| **Purpose** | Approve a firmware package for deployment (transitions `Pending → Approved`) |
| **Used on** | Deployment → "Approve" button |
| **Arguments** | `id: String!` |
| **Returns** | `FirmwareType` |

---

### Service Order Mutations

#### `createServiceOrder`
| | |
|---|---|
| **Purpose** | Create a new service / maintenance ticket |
| **Used on** | Account & Service page (New Service Order form) |
| **Arguments** | `title: String!`, `description?: String`, `priority?: String`, `scheduledDate?: String` |
| **Returns** | `ServiceOrderType` |
| **Notes** | New orders default to `Pending` status. Resolver may require additional fields (`serviceType`, `location`, `customerId`, `createdBy`, `technicianName`). |

```graphql
mutation createServiceOrder(
  $title: String!
  $description: String
  $priority: String
  $scheduledDate: String
) {
  createServiceOrder(
    title: $title
    description: $description
    priority: $priority
    scheduledDate: $scheduledDate
  ) {
    id
    title
    status
    priority
  }
}
```

---

### Compliance Mutations

#### `createCompliance`
| | |
|---|---|
| **Purpose** | Submit a firmware version for compliance certification review |
| **Used on** | Firmware Compliance → "Submit for Review" button |
| **Arguments** | `firmwareId: String!`, `firmwareVersion: String!`, `deviceModel: String!`, `certifications?: [String]`, `vulnerabilities?: Int`, `description?: String` |
| **Returns** | `ComplianceType` |
| **Notes** | New submissions default to `Pending` status |

```graphql
mutation createCompliance(
  $firmwareId: String!
  $firmwareVersion: String!
  $deviceModel: String!
  $certifications: [String]
  $vulnerabilities: Int
  $description: String
) {
  createCompliance(
    firmwareId: $firmwareId
    firmwareVersion: $firmwareVersion
    deviceModel: $deviceModel
    certifications: $certifications
    vulnerabilities: $vulnerabilities
    description: $description
  ) {
    id
    firmwareId
    status
    certifications
  }
}
```

---

### Status Mutation (Cross-Entity)

#### `updateEntityStatus`
| | |
|---|---|
| **Purpose** | Generic status transition mutation across multiple entity types. Handles firmware lifecycle (Pending → Approved / Deprecated / Rejected) and compliance transitions |
| **Used on** | Deployment → "Approve" / "Deprecate" buttons, Firmware Compliance → "Approve" / "Review" buttons |
| **Arguments** | `entityType: String!` (`"firmware"`, `"compliance"`), `id: String!`, `newStatus: String!` |
| **Returns** | `AWSJSON` (raw JSON of the updated record) |
| **⚠️ Backend Quirk** | No server-side validation of `newStatus` — any string value is accepted without error. No existence check on `id` — non-existent IDs succeed (upsert behaviour). |

```graphql
mutation updateEntityStatus($entityType: String!, $id: String!, $newStatus: String!) {
  updateEntityStatus(entityType: $entityType, id: $id, newStatus: $newStatus)
}
```

---

## AWS Location Service (Map Tiles)

These are **not** GraphQL calls. They are standard REST/GET requests made by the MapLibre SDK on the Geo Location tab.

| Endpoint pattern | Purpose |
|---|---|
| `/maps/v0/maps/{mapId}/style-descriptor` | Fetch map style JSON (colours, layers, fonts) |
| `/maps/v0/maps/{mapId}/sprites/sprites@2x.png` | Icon sprite sheet for map symbols |
| `/maps/v0/maps/{mapId}/sprites/sprites@2x.json` | Icon sprite coordinates metadata |
| `/maps/v0/maps/{mapId}/tiles/{z}/{x}/{y}` | Vector map tiles (loaded on pan/zoom) |

**Map name:** `HlmMapOpenData-a3b4d250-131a-11f1-90d8-0aa113e32f65`
**Region:** `us-east-2`

---

## Page → API Call Mapping

| Page | Operations Called on Load |
|---|---|
| **Dashboard** (`/`) | `getUserByEmail`, `listDevices`, `listFirmware`, `listComplianceByStatus(Approved)`, `listAuditLogs` (24 h window) |
| **Inventory — Hardware Inventory** | `listDevices` |
| **Inventory — Firmware Status** | `listFirmware`, `getFirmwareByModel` |
| **Inventory — Geo Location** | `listDevices`, AWS Location Service (map tiles) |
| **Account & Service** (`/account-service`) | `listServiceOrdersByStatus`, `listUsersByRole` |
| **Deployment — Firmware Versions** | `listFirmware` |
| **Deployment — Audit Log** | `listAuditLogs` (last 50 entries) |
| **Firmware Compliance** (`/compliance`) | `listComplianceByStatus`, `listFirmware` |
| **Reporting & Analytics** (`/analytics`) | `listDevices`, `listFirmware`, `listComplianceByStatus`, `listAuditLogs`, `listServiceOrdersByDate` |

---

## Entity Types Reference

Field names confirmed via GraphQL introspection and live API execution.

| Type | Confirmed Fields |
|---|---|
| `DeviceType` | `id`, `deviceName`, `serialNumber`, `model`, `status`, `location`, `lat`, `lng`, `customerId`, `firmwareVersion` |
| `FirmwareType` | `id`, `name`, `version`, `deviceModel`, `status`, `releaseDate`, `fileSize` _(Int, bytes)_, `checksum` |
| `ServiceOrderType` | `id`, `title`, `description`, `status`, `technicianId`, `scheduledDate`, `priority` |
| `ComplianceType` | `id`, `firmwareId`, `firmwareVersion`, `deviceModel`, `status`, `certifications` _(AWSJSON array)_, `vulnerabilities` |
| `AuditLogType` | `id`, `userId`, `userEmail`, `action`, `auditStatus`, `resourceType`, `resourceId` |
| `UserType` | `id`, `email`, `role`, `firstName`, `lastName` |
| `GeocodedDeviceType` | `id`, `address`, `lat`, `lng` |
| `PaginatedResponse` | `items` _(AWSJSON scalar — parse client-side)_, `nextToken`, `totalCount` |

---

## Known Backend Limitations

| Operation | Limitation |
|---|---|
| `listFirmware(status: "Deprecated")` | Filter not applied — returns all firmware records |
| `listUsersByRole(role: any)` | Filter not applied — returns all users regardless of role |
| `updateEntityStatus(id, newStatus)` | No `newStatus` enum validation — accepts any string |
| `updateEntityStatus(id: non-existent)` | No existence check — performs upsert instead of erroring |

---

## Live Sample Data (Discovered 2026-03-25)

| Entity | ID | Name / Description |
|---|---|---|
| Device | `a11bc9b9-ba1e-4e1f-8523-45cd003a6845` | UPS-POWER-05 |
| Device | `5c8da3fb-…` | Switch-CORE-01 |
| Firmware | `94688ba5-81b0-4c73-9f2a-5923f0b4b256` | XR-5000 Security Patch (Pending) |
| Firmware | `98910328-e3cb-4d03-8690-5212c30ccece` | Dell SRV-9000 Firmware (Rejected) |
| Compliance | `85a9549f-…` | HIPAA-certified (Approved) |
| Service Order | `acf1df53-cf21-476d-9410-251efbf04b37` | Core Switch Firmware Upgrade (Completed) |
| User | `u001` | Alice Turner — alice@acmecorp.com (Admin) |
| User | `u002` | Bob Nguyen — bob@acmecorp.com (Manager) |
| User | `u003` | Carol Smith — carol@acmecorp.com (Technician) |
| Customer | `c001` – `c005` | Seeded demo customers |

---

## Test Suite Reference

| Spec File | Tests | Coverage |
|---|---|---|
| [device.api.spec.ts](specs/api/device.api.spec.ts) | 15 | `getDevice`, `listDevices`, `getDevicesByCustomer`, `getDevicesByLocation` |
| [firmware.api.spec.ts](specs/api/firmware.api.spec.ts) | 16 | `getFirmware`, `listFirmware`, `getFirmwareByModel`, `getFirmwareWithRelations` |
| [service-orders.api.spec.ts](specs/api/service-orders.api.spec.ts) | 13 | `getServiceOrder`, `listServiceOrdersByStatus`, `listServiceOrdersByDate`, `getServiceOrdersByTechnician` |
| [compliance.api.spec.ts](specs/api/compliance.api.spec.ts) | 12 | `getCompliance`, `listComplianceByStatus`, `getComplianceByCertification` |
| [audit-logs.api.spec.ts](specs/api/audit-logs.api.spec.ts) | 11 | `listAuditLogs`, `getAuditLogsByUser` |
| [users.api.spec.ts](specs/api/users.api.spec.ts) | 13 | `getUserByEmail`, `listUsersByRole`, `getCustomerWithRelations` |
| [mutations.api.spec.ts](specs/api/mutations.api.spec.ts) | 18 | `createFirmware`, `createServiceOrder`, `createCompliance`, `updateEntityStatus`, `updateDeviceCoords` |

Run with:
```bash
npm run test:api
# or
npx playwright test --config playwright.config.api.ts
```
