# PS-35: FirmwareFamily CRUD Resolvers and API — QA Test Plan

## Application Overview

**JIRA Story:** PS-35 — FirmwareFamily CRUD AppSync JavaScript Resolvers and TypeScript Wrappers

**Application URL:** https://main.dddsig2mih3hw.amplifyapp.com
**AppSync GraphQL Endpoint:** https://rw3sl7mwgzcvbg7zuyqj7ppjv4.appsync-api.us-east-2.amazonaws.com/graphql
**Auth:** AWS Cognito User Pool `us-east-2_Q36YWNsEC`, Client ID `3aseu1rf3q3tae7u4dgplllii8`. The `Authorization` header carries the raw Cognito `idToken` — no `Bearer` prefix.

**Test Plan Date:** 2026-04-06
**Author:** Prepared by Claude AI for ajaykumar.yadav@3pillarglobal.com

### Story Summary

PS-35 builds on the PS-34 DynamoDB/schema foundation and delivers the working resolver layer for FirmwareFamily CRUD. Specifically it delivers:

1. **createFirmwareFamily** AppSync JavaScript resolver with input validation
2. **listFirmwareFamilies** resolver with pagination (`limit` / `nextToken`)
3. **getFirmwareFamily** resolver fetching a single record by ID
4. **updateFirmwareFamily** resolver
5. Typed **TypeScript wrappers** in `hlm-api.ts` for each operation
6. **Admin-only authorization** on `createFirmwareFamily` and `updateFirmwareFamily` — non-Admin callers must receive an authorization error

### Current State Observed (2026-04-06)

- The schema type `FirmwareFamily` and mutations `createFirmwareFamily` / `updateFirmwareFamily` were introduced in PS-34 but resolvers were stubs or unimplemented.
- `listFirmwareFamilies` and `getFirmwareFamily` resolvers are being added in PS-35.
- The `hlm-api.ts` TypeScript wrapper file does not yet contain FirmwareFamily-specific typed helpers.
- The authenticated test user (`ajaykumar.yadav@3pillarglobal.com`) has **Admin** role in the system (confirmed via `getUserByEmail` API returning `role: "Admin"`). This user must be used for Admin-only positive tests.
- A non-Admin test scenario must be simulated by using a Cognito token for a user with `Manager` or `Technician` role (e.g., `bob@acmecorp.com` / `carol@acmecorp.com` from seeded data), or by crafting a request with a deliberately degraded or absent token.
- All list/paginated operations return `PaginatedResponse { items (AWSJSON scalar), nextToken (String), totalCount (Int) }`. The `items` field is an AWSJSON scalar and must be parsed client-side — no sub-field selection in the GraphQL document.
- The `operationName` in the POST body must exactly match the named operation in the query document.

### Conventions Used in This Plan

- **[AUTO-TEST]** prefix on all created test records makes them identifiable and safe to clean up.
- All GraphQL requests use `POST https://rw3sl7mwgzcvbg7zuyqj7ppjv4.appsync-api.us-east-2.amazonaws.com/graphql` with `Authorization: <idToken>` and `Content-Type: application/json`.
- Test file paths follow the project convention: `specs/api/PS-35/<name>.api.spec.ts` for API specs and `specs/PS-35/<name>.spec.ts` for UI-level specs.
- Starting state for every scenario is a clean, authenticated session unless stated otherwise.

---

## Acceptance Criteria Coverage Matrix

| AC | Description | Suites |
|---|---|---|
| AC-1 | createFirmwareFamily resolver with input validation | Suite 1, Suite 7 |
| AC-2 | listFirmwareFamilies resolver with pagination | Suite 2 |
| AC-3 | getFirmwareFamily by ID resolver | Suite 3 |
| AC-4 | updateFirmwareFamily resolver | Suite 4 |
| AC-5 | Typed TypeScript wrappers in hlm-api.ts | Suite 5 |
| AC-6 | Admin-only authorization on create/update | Suite 6 |
| Cross-cutting | Edge cases, error handling, negative tests | Suite 7, Suite 8 |
| End-to-end | Full CRUD flow + UI verification | Suite 9 |

**Total test cases: 47** across 9 suites.

---

## Test Scenarios

### Suite 1: createFirmwareFamily Resolver — Happy Path (AC-1)

**File:** `specs/api/PS-35/create-firmware-family.api.spec.ts`
**Seed:** `specs/api/global-setup.ts` (authenticates and persists `idToken` to `.auth/storageState.json`)
**Starting state:** Authenticated as Admin user. No pre-existing FirmwareFamily records with `[AUTO-TEST]` prefix.

---

#### TC-35.1.01: Create FirmwareFamily with all required and optional fields

**Steps:**

1. Send a `createFirmwareFamily` GraphQL mutation to the AppSync endpoint with the following variables:
   ```json
   {
     "familyName": "[AUTO-TEST] Solar Inverter Family",
     "targetModels": ["LN-11", "LN-12"],
     "status": "Active",
     "createdBy": "auto-test-user-001",
     "description": "Automated test family — safe to delete"
   }
   ```
   Use `Authorization: <Admin idToken>`.
   - Expected: HTTP response status is 200.
   - Expected: The response body contains no `errors` array (or `errors` is `undefined`).
   - Expected: The `data.createFirmwareFamily` object is non-null.

2. Inspect the `id` field of the returned `createFirmwareFamily` object.
   - Expected: `id` is a non-null, non-empty string in UUID format (`xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`).

3. Inspect the `familyName` field of the returned object.
   - Expected: `familyName` equals `"[AUTO-TEST] Solar Inverter Family"` exactly.

4. Inspect the `targetModels` field.
   - Expected: `targetModels` is a non-empty list containing `"LN-11"` and `"LN-12"`.

5. Inspect the `status` field.
   - Expected: `status` equals `"Active"`.

6. Inspect the `createdAt` and `updatedAt` fields.
   - Expected: Both fields are present and contain valid ISO 8601 timestamp strings.
   - Expected: On a freshly created record, `createdAt` and `updatedAt` have equal or near-equal values.

7. Inspect the `createdBy` field.
   - Expected: `createdBy` equals `"auto-test-user-001"`.

8. Inspect the `description` field.
   - Expected: `description` equals `"Automated test family — safe to delete"`.

**Success criteria:** All eight field assertions pass. The resolver correctly persists and returns all fields including optional ones.

**Failure criteria:** Any `errors` in the response, any field missing from the response, or `id` not in UUID format.

---

#### TC-35.1.02: Create FirmwareFamily with only required fields (minimal payload)

**Steps:**

1. Send a `createFirmwareFamily` mutation with only the required fields:
   ```json
   {
     "familyName": "[AUTO-TEST] Minimal Family",
     "targetModels": ["XR-5000"],
     "status": "Active"
   }
   ```
   - Expected: HTTP 200, no GraphQL errors.
   - Expected: `data.createFirmwareFamily.id` is a valid UUID.
   - Expected: `data.createFirmwareFamily.familyName` equals `"[AUTO-TEST] Minimal Family"`.

2. Verify that optional fields not supplied (`description`, `notes`, etc.) are returned as `null` rather than causing an error.
   - Expected: Optional fields are `null` or absent in the response — not causing the call to fail.

**Success criteria:** The resolver accepts a minimal payload and returns a complete, valid record.

---

#### TC-35.1.03: Create FirmwareFamily with a single targetModel entry

**Steps:**

1. Send a `createFirmwareFamily` mutation with `targetModels: ["LN-11"]` (single item list).
   - Expected: HTTP 200, no errors.
   - Expected: `targetModels` in the returned record contains exactly one entry: `"LN-11"`.

**Success criteria:** The resolver handles a single-element list without coercing it to a scalar or erroring.

---

#### TC-35.1.04: Create FirmwareFamily with each valid status value

**Steps:**

1. Send `createFirmwareFamily` with `status: "Active"`. Assert the returned `status` equals `"Active"`.
2. Send `createFirmwareFamily` with `status: "Deprecated"`. Assert the returned `status` equals `"Deprecated"`.
3. Send `createFirmwareFamily` with `status: "Recalled"` (if applicable per PS-34 enum). Assert the returned `status` equals `"Recalled"`.

   For each sub-step:
   - Expected: HTTP 200, no errors, and `status` in the response matches the submitted value exactly.

**Success criteria:** The resolver accepts all valid enum values for status.

---

### Suite 2: listFirmwareFamilies Resolver — Pagination and Filtering (AC-2)

**File:** `specs/api/PS-35/list-firmware-families.api.spec.ts`
**Starting state:** At least 3 FirmwareFamily records must exist in the database. The `beforeAll` hook creates them if fewer than 3 exist using `createFirmwareFamily`.

---

#### TC-35.2.01: listFirmwareFamilies returns all families with expected response shape

**Steps:**

1. Send a `listFirmwareFamilies` query with no arguments:
   ```graphql
   query listFirmwareFamilies {
     listFirmwareFamilies {
       items
       nextToken
       totalCount
     }
   }
   ```
   - Expected: HTTP 200, no GraphQL errors.
   - Expected: `data.listFirmwareFamilies` is non-null.
   - Expected: `data.listFirmwareFamilies.totalCount` is a non-negative integer.
   - Expected: `data.listFirmwareFamilies.items` is parseable as a JSON array.

2. Parse the `items` AWSJSON scalar and inspect the first item.
   - Expected: Each item contains at minimum: `id`, `familyName`, `targetModels`, `status`, `createdAt`, `updatedAt`.
   - Expected: The `id` field of each item is a non-empty UUID string.

**Success criteria:** The resolver returns a valid `PaginatedResponse` with correctly structured items.

---

#### TC-35.2.02: listFirmwareFamilies respects the limit argument

**Steps:**

1. Execute `listFirmwareFamilies(limit: 2)`.
   - Expected: HTTP 200, no errors.
   - Expected: The parsed `items` array contains at most 2 items.
   - Expected: `totalCount` reflects the total number of FirmwareFamily records in the database, not just the page count.

2. Execute `listFirmwareFamilies(limit: 1)`.
   - Expected: Parsed `items` array contains exactly 1 item (assuming at least 1 record exists).
   - Expected: `nextToken` is non-null (assuming more than 1 record exists), indicating a next page is available.

**Success criteria:** The `limit` argument correctly controls the page size returned by the resolver.

---

#### TC-35.2.03: listFirmwareFamilies pagination — nextToken cursor advances correctly

**Steps:**

1. Execute `listFirmwareFamilies(limit: 1)` and capture the `nextToken` value from the response. Call this `token1`.
   - Expected: HTTP 200, 1 item returned, `nextToken` is non-null.
   - Record the `id` of the returned item as `item1Id`.

2. Execute `listFirmwareFamilies(limit: 1, nextToken: token1)` using the captured `token1`.
   - Expected: HTTP 200, 1 item returned.
   - Expected: The `id` of the returned item is different from `item1Id` — the cursor has advanced.
   - Expected: The same record does not appear on consecutive pages.

3. Continue paginating until `nextToken` is null.
   - Expected: All records are eventually returned with no duplicates across pages.
   - Expected: The total number of records across all pages equals the `totalCount` from the first call.

**Success criteria:** Pagination works correctly without duplicating or skipping records.

---

#### TC-35.2.04: listFirmwareFamilies filters by status when status argument is provided

**Steps:**

1. Ensure at least one FirmwareFamily with `status: "Active"` and one with `status: "Deprecated"` exist (create them in `beforeAll` if not present).

2. Execute `listFirmwareFamilies(status: "Active")`.
   - Expected: HTTP 200, no errors.
   - Expected: All items in the parsed result have `status` equal to `"Active"`.
   - Expected: No item with `status` `"Deprecated"` appears in the result.

3. Execute `listFirmwareFamilies(status: "Deprecated")`.
   - Expected: All returned items have `status` equal to `"Deprecated"`.
   - Expected: No item with `status` `"Active"` appears in the result.

4. Execute `listFirmwareFamilies(status: "Active", limit: 1)` and paginate using `nextToken` to verify the filter persists across pages.
   - Expected: Every item on every page has `status` `"Active"`.

**Success criteria:** The status filter is applied server-side and persists across paginated calls.

---

#### TC-35.2.05: listFirmwareFamilies with status filter that matches zero records returns empty result

**Steps:**

1. Execute `listFirmwareFamilies(status: "Recalled")` where no FirmwareFamily with `status: "Recalled"` exists.
   - Expected: HTTP 200, no errors.
   - Expected: The parsed `items` array is empty.
   - Expected: `totalCount` is 0.
   - Expected: `nextToken` is null.

**Success criteria:** The resolver returns a valid empty response rather than erroring when no records match the filter.

---

#### TC-35.2.06: listFirmwareFamilies with limit=0 or very large limit behaves predictably

**Steps:**

1. Execute `listFirmwareFamilies(limit: 0)`.
   - Expected: Either HTTP 200 with 0 items and a valid `nextToken` / `totalCount`, OR a GraphQL validation error indicating `limit` must be at least 1. In either case no HTTP 500 response.

2. Execute `listFirmwareFamilies(limit: 10000)` (beyond any realistic record count).
   - Expected: HTTP 200. All existing records are returned (up to whatever the backend maximum page size is). No server error.

**Success criteria:** Boundary values for `limit` do not crash the resolver.

---

### Suite 3: getFirmwareFamily Resolver — Single Record Fetch (AC-3)

**File:** `specs/api/PS-35/get-firmware-family.api.spec.ts`
**Starting state:** At least one FirmwareFamily record exists. `beforeAll` creates one via `createFirmwareFamily` and captures the returned `id`.

---

#### TC-35.3.01: getFirmwareFamily returns the correct record for a valid ID

**Steps:**

1. In `beforeAll`, create a FirmwareFamily with known field values and capture the returned `id` as `knownFamilyId`.

2. Send `getFirmwareFamily(id: knownFamilyId)`:
   ```graphql
   query getFirmwareFamily($id: String!) {
     getFirmwareFamily(id: $id) {
       id
       familyName
       targetModels
       status
       createdAt
       updatedAt
       createdBy
     }
   }
   ```
   - Expected: HTTP 200, no errors.
   - Expected: `data.getFirmwareFamily.id` equals `knownFamilyId`.
   - Expected: `familyName`, `targetModels`, `status` match the values submitted at creation.
   - Expected: `createdAt` and `updatedAt` are valid ISO 8601 timestamps.

**Success criteria:** The resolver correctly fetches and returns a single FirmwareFamily record by ID with all fields populated.

---

#### TC-35.3.02: getFirmwareFamily returns null or error for a non-existent ID

**Steps:**

1. Send `getFirmwareFamily(id: "non-existent-id-00000000-0000-0000-0000-000000000000")`.
   - Expected: HTTP 200.
   - Expected: Either `data.getFirmwareFamily` is `null`, OR a GraphQL `errors` array is present with a not-found message.
   - Expected: No HTTP 500 or unhandled exception response.

**Success criteria:** The resolver handles a missing record gracefully without a server error.

---

#### TC-35.3.03: getFirmwareFamily returns null or error for an empty string ID

**Steps:**

1. Send `getFirmwareFamily(id: "")`.
   - Expected: Either a GraphQL validation error (empty string is not a valid ID), or `data.getFirmwareFamily` is `null`.
   - Expected: No HTTP 500.

**Success criteria:** The resolver or schema validation rejects an empty string ID without crashing.

---

#### TC-35.3.04: getFirmwareFamily returns correct data after the record has been updated

**Steps:**

1. Create a FirmwareFamily with `familyName: "[AUTO-TEST] Pre-Update Family"`. Capture `id`.

2. Send an `updateFirmwareFamily` mutation to change `familyName` to `"[AUTO-TEST] Post-Update Family"`.
   - Expected: The update mutation succeeds.

3. Send `getFirmwareFamily(id: <captured id>)`.
   - Expected: `familyName` in the returned record equals `"[AUTO-TEST] Post-Update Family"`.
   - Expected: `updatedAt` is later than `createdAt`.

**Success criteria:** `getFirmwareFamily` reflects the latest state of a record after an update, confirming DynamoDB write-through consistency.

---

### Suite 4: updateFirmwareFamily Resolver (AC-4)

**File:** `specs/api/PS-35/update-firmware-family.api.spec.ts`
**Starting state:** At least one FirmwareFamily record exists to update. `beforeAll` creates a dedicated record for this suite.

---

#### TC-35.4.01: updateFirmwareFamily updates familyName successfully

**Steps:**

1. Create a FirmwareFamily with `familyName: "[AUTO-TEST] Original Name"`. Capture `id` and `createdAt`.

2. Send `updateFirmwareFamily(id: <id>, familyName: "[AUTO-TEST] Updated Name")`:
   - Expected: HTTP 200, no errors.
   - Expected: `data.updateFirmwareFamily.familyName` equals `"[AUTO-TEST] Updated Name"`.
   - Expected: `data.updateFirmwareFamily.id` equals the original `id`.
   - Expected: `data.updateFirmwareFamily.updatedAt` is a valid timestamp and is equal to or later than `createdAt`.

3. Confirm via `getFirmwareFamily(id: <id>)` that the name change is persisted.
   - Expected: `familyName` is `"[AUTO-TEST] Updated Name"`.

**Success criteria:** The `familyName` field is correctly updated and persisted in DynamoDB.

---

#### TC-35.4.02: updateFirmwareFamily updates targetModels successfully

**Steps:**

1. Create a FirmwareFamily with `targetModels: ["LN-11"]`.

2. Send `updateFirmwareFamily(id: <id>, targetModels: ["LN-11", "LN-12", "LN-13"])`.
   - Expected: HTTP 200, no errors.
   - Expected: `data.updateFirmwareFamily.targetModels` contains exactly `["LN-11", "LN-12", "LN-13"]`.

3. Confirm via `getFirmwareFamily`.
   - Expected: `targetModels` contains three entries.

**Success criteria:** The resolver correctly replaces the `targetModels` list with the new value.

---

#### TC-35.4.03: updateFirmwareFamily updates status successfully

**Steps:**

1. Create a FirmwareFamily with `status: "Active"`.

2. Send `updateFirmwareFamily(id: <id>, status: "Deprecated")`.
   - Expected: HTTP 200, no errors.
   - Expected: `data.updateFirmwareFamily.status` equals `"Deprecated"`.

3. Confirm via `getFirmwareFamily`.
   - Expected: `status` is `"Deprecated"`.

**Success criteria:** The resolver correctly transitions the status field.

---

#### TC-35.4.04: updateFirmwareFamily updates multiple fields in a single call

**Steps:**

1. Create a FirmwareFamily with `familyName: "[AUTO-TEST] Multi-Field Before"`, `status: "Active"`, `targetModels: ["LN-11"]`.

2. Send `updateFirmwareFamily(id: <id>, familyName: "[AUTO-TEST] Multi-Field After", status: "Deprecated", targetModels: ["LN-11", "XR-5000"])`.
   - Expected: HTTP 200, no errors.
   - Expected: All three fields are updated in the returned record.

3. Confirm via `getFirmwareFamily`.
   - Expected: All three updated values are persisted.

**Success criteria:** The resolver handles multi-field updates atomically.

---

#### TC-35.4.05: updateFirmwareFamily with non-existent ID returns an error (not an upsert)

**Steps:**

1. Send `updateFirmwareFamily(id: "non-existent-id-00000000-0000-0000-0000-000000000000", familyName: "Ghost Family")`.
   - Expected: The response contains a GraphQL error with a message such as `"FirmwareFamily not found"` or `"Record does not exist"`.
   - Expected: HTTP 200 (standard GraphQL transport) but `errors` array is non-empty.
   - Expected: No new record is created (do not upsert). Confirm via `getFirmwareFamily` that the ID still returns null.

**Success criteria:** The resolver enforces existence-check on update, unlike the legacy `updateEntityStatus` which performed an upsert. This is the expected behavior per AC-1's "input validation" requirement.

**Note:** If the live resolver performs an upsert (matching `updateEntityStatus` behavior), this is a defect and should be documented as a known backend limitation.

---

#### TC-35.4.06: updateFirmwareFamily with invalid status value returns a validation error

**Steps:**

1. Create a FirmwareFamily with a valid `status`.

2. Send `updateFirmwareFamily(id: <id>, status: "BOGUS_STATUS")`.
   - Expected: The AppSync schema validator or the JavaScript resolver returns a GraphQL error indicating `"BOGUS_STATUS"` is not a valid status value.
   - Expected: The record's status is not changed. Confirm via `getFirmwareFamily`.

**Success criteria:** The resolver validates the status enum on update, matching AC-1's validation requirement.

---

### Suite 5: TypeScript Wrappers in hlm-api.ts (AC-5)

**File:** `specs/PS-35/hlm-api-typescript-wrappers.spec.ts`
**Note:** These are static/structural tests that verify the TypeScript wrapper module compiles cleanly and exposes the correct function signatures. They do not require a running browser — they are run via `tsc --noEmit` or by importing the module in a test that verifies the exports.

---

#### TC-35.5.01: hlm-api.ts exports a createFirmwareFamily function

**Steps:**

1. Import or inspect the `hlm-api.ts` module.
   - Expected: The module exports a function named `createFirmwareFamily`.
   - Expected: The function accepts a typed input argument containing at minimum: `familyName: string`, `targetModels: string[]`, `status: string`.
   - Expected: The function returns a `Promise` resolving to a typed `FirmwareFamilyType` object (or equivalent named type).

2. Run `tsc --noEmit` on the file.
   - Expected: Zero TypeScript compilation errors in the `createFirmwareFamily` wrapper.

**Success criteria:** The wrapper function exists, is correctly typed, and compiles cleanly.

---

#### TC-35.5.02: hlm-api.ts exports a listFirmwareFamilies function

**Steps:**

1. Inspect the `hlm-api.ts` module for a `listFirmwareFamilies` export.
   - Expected: The function accepts optional arguments: `status?: string`, `limit?: number`, `nextToken?: string`.
   - Expected: The function returns a `Promise` resolving to a typed `PaginatedResponse<FirmwareFamilyType>` (or equivalent).

2. Run `tsc --noEmit`.
   - Expected: Zero TypeScript compilation errors.

**Success criteria:** The wrapper correctly models the paginated list operation.

---

#### TC-35.5.03: hlm-api.ts exports a getFirmwareFamily function

**Steps:**

1. Inspect the `hlm-api.ts` module for a `getFirmwareFamily` export.
   - Expected: The function accepts `id: string` as a required argument.
   - Expected: The function returns `Promise<FirmwareFamilyType | null>`.

2. Run `tsc --noEmit`.
   - Expected: Zero TypeScript compilation errors.

**Success criteria:** The wrapper correctly models the single-record fetch operation.

---

#### TC-35.5.04: hlm-api.ts exports an updateFirmwareFamily function

**Steps:**

1. Inspect the `hlm-api.ts` module for an `updateFirmwareFamily` export.
   - Expected: The function accepts `id: string` plus optional update fields: `familyName?: string`, `targetModels?: string[]`, `status?: string`.
   - Expected: The function returns `Promise<FirmwareFamilyType>`.

2. Run `tsc --noEmit`.
   - Expected: Zero TypeScript compilation errors.

**Success criteria:** The wrapper correctly models the update operation with partial input.

---

#### TC-35.5.05: FirmwareFamilyType interface is correctly defined in hlm-api.ts or a shared types file

**Steps:**

1. Locate the `FirmwareFamilyType` interface or type definition.
   - Expected: The type includes fields: `id: string`, `familyName: string`, `targetModels: string[]`, `status: string`, `createdAt: string`, `updatedAt: string`.
   - Expected: Optional fields (`createdBy?: string`, `description?: string`) are marked as optional with `?`.

2. Run `tsc --noEmit` on any file that imports this type.
   - Expected: Zero compilation errors.

**Success criteria:** The type definition is complete and consistent with the GraphQL schema introduced in PS-34.

---

### Suite 6: Admin-Only Authorization (AC-6)

**File:** `specs/api/PS-35/authorization.api.spec.ts`
**Starting state:** Two sets of Cognito tokens are required:
- Admin token: obtained via `ajaykumar.yadav@3pillarglobal.com` / `Secure@12345`
- Non-Admin token: obtained via a Manager or Technician user (e.g., `bob@acmecorp.com` for Manager role, or `carol@acmecorp.com` for Technician role, per the live sample data in HLM-API-Catalogue.md)

**Note on obtaining non-Admin tokens:** The `global-setup.ts` authenticates with the Admin account. For non-Admin tests, a separate browser login flow is required in `beforeAll`, saving the non-Admin token to a separate storage state path (e.g., `.auth/storageState-manager.json`).

---

#### TC-35.6.01: Admin user can successfully call createFirmwareFamily

**Steps:**

1. Using the Admin `idToken`, send `createFirmwareFamily` with valid input.
   - Expected: HTTP 200, no errors, `data.createFirmwareFamily.id` is non-null.

**Success criteria:** The Admin role is permitted to call `createFirmwareFamily` without restriction.

---

#### TC-35.6.02: Non-Admin user receives authorization error on createFirmwareFamily

**Steps:**

1. Obtain a Cognito `idToken` for a user with `role: "Manager"` (e.g., bob@acmecorp.com).

2. Using the Manager `idToken`, send `createFirmwareFamily` with valid input:
   ```json
   { "familyName": "[AUTO-TEST] Unauthorized Create", "targetModels": ["XR-5000"], "status": "Active" }
   ```
   - Expected: The response contains a GraphQL `errors` array.
   - Expected: The error message indicates an authorization failure (e.g., `"Unauthorized"`, `"Access denied"`, `"Not authorized to run this mutation"`).
   - Expected: `data.createFirmwareFamily` is `null`.
   - Expected: No FirmwareFamily record is created in DynamoDB.

**Success criteria:** The resolver's Admin-only authorization guard correctly rejects non-Admin callers.

---

#### TC-35.6.03: Technician user receives authorization error on createFirmwareFamily

**Steps:**

1. Obtain a Cognito `idToken` for a user with `role: "Technician"` (e.g., carol@acmecorp.com).

2. Using the Technician `idToken`, send `createFirmwareFamily` with valid input.
   - Expected: GraphQL errors array with authorization error. No record created.

**Success criteria:** The Technician role is also blocked from creating FirmwareFamily records.

---

#### TC-35.6.04: Admin user can successfully call updateFirmwareFamily

**Steps:**

1. Create a FirmwareFamily using the Admin token. Capture `id`.

2. Using the Admin `idToken`, send `updateFirmwareFamily(id: <id>, familyName: "[AUTO-TEST] Admin Updated")`.
   - Expected: HTTP 200, no errors, returned `familyName` equals the updated value.

**Success criteria:** The Admin role is permitted to call `updateFirmwareFamily`.

---

#### TC-35.6.05: Non-Admin user receives authorization error on updateFirmwareFamily

**Steps:**

1. Create a FirmwareFamily using the Admin token. Capture `id`.

2. Using the Manager `idToken`, send `updateFirmwareFamily(id: <id>, familyName: "[AUTO-TEST] Manager Hack")`.
   - Expected: GraphQL errors array with authorization error.
   - Expected: `data.updateFirmwareFamily` is `null`.
   - Expected: The record is NOT updated. Confirm via `getFirmwareFamily` with Admin token that the name is unchanged.

**Success criteria:** Non-Admin users cannot update FirmwareFamily records.

---

#### TC-35.6.06: Non-Admin user CAN call listFirmwareFamilies (read is not restricted)

**Steps:**

1. Using the Manager `idToken`, send `listFirmwareFamilies { items nextToken totalCount }`.
   - Expected: HTTP 200, no authorization errors.
   - Expected: Items are returned (reads are public to all authenticated users).

**Success criteria:** The authorization guard only blocks write operations (`create`, `update`), not read operations (`list`, `get`).

---

#### TC-35.6.07: Non-Admin user CAN call getFirmwareFamily (read is not restricted)

**Steps:**

1. Create a FirmwareFamily with the Admin token. Capture `id`.

2. Using the Manager `idToken`, send `getFirmwareFamily(id: <id>)`.
   - Expected: HTTP 200, the record is returned without authorization error.

**Success criteria:** Read operations remain accessible to all authenticated roles.

---

#### TC-35.6.08: Unauthenticated request to createFirmwareFamily is rejected with 401

**Steps:**

1. Send `createFirmwareFamily` to the AppSync endpoint with no `Authorization` header.
   - Expected: HTTP status is 401, OR the response body contains `errors` with an `"UnauthorizedException"` or `"Unauthorized"` message.

**Success criteria:** Unauthenticated callers are categorically rejected at the AppSync authentication layer before the resolver executes.

---

#### TC-35.6.09: Unauthenticated request to listFirmwareFamilies is rejected with 401

**Steps:**

1. Send `listFirmwareFamilies` with no `Authorization` header.
   - Expected: HTTP 401 or GraphQL `"UnauthorizedException"`.

**Success criteria:** Even read-only operations require authentication.

---

### Suite 7: Input Validation — createFirmwareFamily and updateFirmwareFamily (AC-1 extended)

**File:** `specs/api/PS-35/input-validation.api.spec.ts`
**Starting state:** Authenticated as Admin. No precondition on existing records.

---

#### TC-35.7.01: createFirmwareFamily rejected when familyName is omitted

**Steps:**

1. Send `createFirmwareFamily` omitting `familyName` but supplying `targetModels` and `status`.
   - Expected: The AppSync schema validator returns a GraphQL error before the resolver executes.
   - Expected: The error message references `familyName` as a required field.
   - Expected: No record is created.

**Success criteria:** Schema-level type validation enforces `familyName` as a required (non-null) argument.

---

#### TC-35.7.02: createFirmwareFamily rejected when familyName is an empty string

**Steps:**

1. Send `createFirmwareFamily` with `familyName: ""`.
   - Expected: The resolver returns a GraphQL error indicating `familyName` cannot be blank or empty.
   - Expected: No record is created.

**Note:** If AppSync schema type is `String!`, an empty string passes schema validation (it is not null). The resolver's JavaScript validation layer must catch this. If the resolver does not validate empty strings, this is a defect to document.

---

#### TC-35.7.03: createFirmwareFamily rejected when familyName exceeds maximum length

**Steps:**

1. Send `createFirmwareFamily` with `familyName` set to a string of 300 characters (a string of 300 `"A"` characters).
   - Expected: A GraphQL error is returned indicating the maximum allowed length is exceeded.
   - Expected: No record is created.

**Success criteria:** The resolver validates string length and rejects oversized input.

---

#### TC-35.7.04: createFirmwareFamily rejected when targetModels is omitted

**Steps:**

1. Send `createFirmwareFamily` omitting `targetModels` (supply `familyName` and `status` only).
   - Expected: A GraphQL schema validation error indicating `targetModels` is required.
   - Expected: No record is created.

---

#### TC-35.7.05: createFirmwareFamily rejected when targetModels is an empty array

**Steps:**

1. Send `createFirmwareFamily` with `targetModels: []` (empty list).
   - Expected: A GraphQL error indicating `targetModels` must contain at least one model identifier.
   - Expected: No record is created.

**Note:** The AppSync schema may accept `[String]` with empty arrays at the type level. The resolver's JavaScript validation must catch this case. If empty arrays are accepted, document as a known limitation.

---

#### TC-35.7.06: createFirmwareFamily rejected when status is omitted

**Steps:**

1. Send `createFirmwareFamily` with `familyName` and `targetModels` but without `status`.
   - Expected: GraphQL schema validation error indicating `status` is required.
   - Expected: No record is created.

---

#### TC-35.7.07: createFirmwareFamily rejected when status is an invalid enum value

**Steps:**

1. Send `createFirmwareFamily` with `status: "INVALID_STATUS_XYZ"`.
   - Expected: A GraphQL error indicating `"INVALID_STATUS_XYZ"` is not a valid enum member.
   - Expected: No record is created.

2. Send `createFirmwareFamily` with a legacy status value `status: "Pending"` (which was removed by PS-34).
   - Expected: A GraphQL error indicating `"Pending"` is not valid.

---

#### TC-35.7.08: updateFirmwareFamily rejected when id is missing

**Steps:**

1. Send `updateFirmwareFamily` without providing the `id` argument.
   - Expected: GraphQL schema validation error indicating `id` is required.

---

#### TC-35.7.09: updateFirmwareFamily rejected when familyName is an empty string

**Steps:**

1. Create a FirmwareFamily. Capture `id`.

2. Send `updateFirmwareFamily(id: <id>, familyName: "")`.
   - Expected: GraphQL error indicating `familyName` cannot be empty.
   - Expected: The record is not updated. Confirm the original name is unchanged via `getFirmwareFamily`.

---

#### TC-35.7.10: updateFirmwareFamily rejected when targetModels is set to an empty array

**Steps:**

1. Create a FirmwareFamily with `targetModels: ["LN-11"]`.

2. Send `updateFirmwareFamily(id: <id>, targetModels: [])`.
   - Expected: GraphQL error indicating `targetModels` cannot be empty.
   - Expected: The record is not updated.

---

### Suite 8: Edge Cases and Negative Scenarios

**File:** `specs/api/PS-35/edge-cases.api.spec.ts`
**Starting state:** Authenticated as Admin.

---

#### TC-35.8.01: listFirmwareFamilies with an expired nextToken returns an error

**Steps:**

1. Call `listFirmwareFamilies(limit: 1)` and capture a valid `nextToken`.

2. Wait at least 5 minutes (or use a synthetically expired token value such as an old base64 cursor string). Then call `listFirmwareFamilies(limit: 1, nextToken: <expired_or_invalid_token>)`.
   - Expected: Either the API returns an error (GraphQL errors array with a message about an invalid token), OR it returns the first page again gracefully (DynamoDB cursor reset). In either case, no HTTP 500.

**Success criteria:** The resolver handles invalid/expired pagination tokens without crashing.

---

#### TC-35.8.02: createFirmwareFamily is idempotent per request — duplicate names are not rejected

**Steps:**

1. Send `createFirmwareFamily` with `familyName: "[AUTO-TEST] Duplicate Family"`.
   - Expected: HTTP 200, record created, `id1` returned.

2. Send the exact same mutation again with `familyName: "[AUTO-TEST] Duplicate Family"`.
   - Expected: HTTP 200. A second record is created with a new UUID `id2` (families with duplicate names are allowed). OR an error is returned if the resolver enforces name uniqueness.

3. Document the actual behavior: if duplicates are allowed, this is the expected behavior. If uniqueness is enforced, the error message should clearly state the constraint.

**Success criteria:** The behavior is deterministic and documented. The resolver does not crash.

---

#### TC-35.8.03: getFirmwareFamily for a deleted or non-existent record returns null without 500

**Steps:**

1. Send `getFirmwareFamily(id: "00000000-0000-0000-0000-000000000000")`.
   - Expected: HTTP 200, `data.getFirmwareFamily` is `null` or a not-found GraphQL error. No HTTP 500.

---

#### TC-35.8.04: listFirmwareFamilies with a malformed nextToken returns an error gracefully

**Steps:**

1. Send `listFirmwareFamilies(nextToken: "not-a-valid-cursor-$$$$")`.
   - Expected: HTTP 200 with a GraphQL error about invalid pagination cursor, OR the resolver treats it as no-cursor and returns the first page. No HTTP 500.

---

#### TC-35.8.05: createFirmwareFamily with targetModels containing duplicate model strings

**Steps:**

1. Send `createFirmwareFamily` with `targetModels: ["LN-11", "LN-11", "LN-11"]` (all three entries identical).
   - Expected: HTTP 200. The record is created. The returned `targetModels` either deduplicates to `["LN-11"]` or preserves all three duplicates, depending on resolver behavior.
   - Expected: Document the behavior. No crash or server error.

---

#### TC-35.8.06: Concurrent createFirmwareFamily calls do not produce corrupted records

**Steps:**

1. Send 5 concurrent `createFirmwareFamily` mutations simultaneously (using `Promise.all` or parallel API requests).
   - Expected: All 5 calls return HTTP 200.
   - Expected: Each returned record has a unique UUID `id`.
   - Expected: No records are overwritten or merged — 5 distinct records exist in the database after the test.

**Success criteria:** The DynamoDB PK generation and resolver are safe under concurrent write conditions.

---

#### TC-35.8.07: listFirmwareFamilies totalCount is consistent across sequential calls without data changes

**Steps:**

1. Call `listFirmwareFamilies` (no filters) and record `totalCount1`.

2. Immediately call `listFirmwareFamilies` again and record `totalCount2`.
   - Expected: `totalCount1` equals `totalCount2` (no data was changed between calls).

**Success criteria:** `totalCount` is deterministic and does not fluctuate without data changes.

---

### Suite 9: End-to-End CRUD Flow and UI Verification

**File:** `specs/PS-35/e2e-firmware-family-crud.spec.ts`
**Starting state:** Authenticated as Admin. Browser session at the application landing page.

---

#### TC-35.9.01: Full CRUD lifecycle — create, read, update, read-again via API

**Steps:**

1. Send `createFirmwareFamily` with `familyName: "[AUTO-TEST] E2E CRUD Family"`, `targetModels: ["LN-11"]`, `status: "Active"`. Capture returned `id`.
   - Expected: HTTP 200, valid `id` returned.

2. Send `getFirmwareFamily(id: <id>)`.
   - Expected: Returns the record with all fields matching creation values.

3. Send `updateFirmwareFamily(id: <id>, familyName: "[AUTO-TEST] E2E CRUD Updated", targetModels: ["LN-11", "XR-5000"], status: "Deprecated")`.
   - Expected: HTTP 200, all three fields updated in the response.

4. Send `getFirmwareFamily(id: <id>)` again.
   - Expected: All three updated values are persisted. `updatedAt` is later than `createdAt`.

5. Send `listFirmwareFamilies(status: "Deprecated")`.
   - Expected: The updated record appears in the result.
   - Expected: The record is not returned when calling `listFirmwareFamilies(status: "Active")`.

**Success criteria:** The full CRUD lifecycle works end-to-end without any data loss, inconsistency, or error.

---

#### TC-35.9.02: FirmwareFamily listing appears in UI after creation (if UI surfaces it)

**Steps:**

1. Send `createFirmwareFamily` with `familyName: "[AUTO-TEST] UI Visible Family"`, `targetModels: ["LN-11"]`, `status: "Active"` via API. Capture `id`.
   - Expected: Record created successfully.

2. Navigate to the Deployment page (`/deployment`) in the browser. Look for a "Firmware Families" tab, section, or filter.
   - Expected: If a FirmwareFamily UI section exists, the created family name `"[AUTO-TEST] UI Visible Family"` appears in the listing.
   - If no FirmwareFamily UI section exists at this time, document as a known gap: PS-35 is a backend story; UI surface may follow in a subsequent story.

3. If the FirmwareFamily is visible in the UI, click on it to open any detail view.
   - Expected: The detail view shows correct `familyName`, `targetModels`, and `status` values.

**Note:** If no FirmwareFamily UI section exists, this test is marked as `N/A (Backend story — UI not yet surfaced)` and the API-level verification in TC-35.9.01 is sufficient.

---

#### TC-35.9.03: Upload Firmware form exposes Firmware Family association field (if UI is updated)

**Steps:**

1. Navigate to `/deployment` and click the `Upload Firmware` button.
   - Expected: The Upload Firmware dialog opens.

2. Inspect all form fields for a `Firmware Family` dropdown or `familyId` field.
   - Expected (post-PS-35): A "Firmware Family" dropdown is present, populated by calling `listFirmwareFamilies`.
   - If the field is absent, record as `N/A (UI not yet updated for PS-35)`.

3. If the Firmware Family field exists, select `"[AUTO-TEST] E2E CRUD Family"` from the dropdown. Fill all other required fields and submit.
   - Expected: The firmware is created successfully. A subsequent `getFirmware(id: <new id>)` returns `familyId` populated with the correct FirmwareFamily id.

---

#### TC-35.9.04: Authorization enforcement is visible end-to-end — non-Admin cannot access write operations

**Steps:**

1. Log out from the Admin session. Log in as a Manager-role user (if test credentials are available for this role in the live environment).

2. Attempt to call `createFirmwareFamily` from the logged-in Manager session using the network request or any UI form that exposes this operation.
   - Expected: The API call returns an authorization error. No record is created.

3. Attempt to call `updateFirmwareFamily` from the Manager session.
   - Expected: Authorization error. No update persisted.

4. Attempt to call `listFirmwareFamilies` and `getFirmwareFamily` from the Manager session.
   - Expected: Both calls succeed and return data — reads are permitted for all authenticated roles.

**Success criteria:** The authorization boundary is enforced end-to-end from the UI/API consumer perspective.

---

## Test Data Requirements

| Item | Value | Purpose |
|---|---|---|
| Admin credentials | `ajaykumar.yadav@3pillarglobal.com` / `Secure@12345` | All Admin-positive and Admin-only tests |
| Manager credentials | `bob@acmecorp.com` (role: Manager) | Authorization negative tests (non-Admin) |
| Technician credentials | `carol@acmecorp.com` (role: Technician) | Authorization negative tests (non-Admin) |
| Valid targetModel values | `"LN-11"`, `"LN-12"`, `"XR-5000"` | These are confirmed models in the seeded data |
| Invalid ID | `"non-existent-id-00000000-0000-0000-0000-000000000000"` | Negative ID tests |
| Over-length familyName | 300-character string of `"A"` | Length validation test |
| Invalid status | `"INVALID_STATUS_XYZ"`, `"Pending"`, `"Approved"` | Enum validation tests |
| Test prefix | `"[AUTO-TEST]"` | All created records use this prefix for easy identification and cleanup |

---

## GraphQL Operation Reference

### createFirmwareFamily Mutation

```graphql
mutation createFirmwareFamily(
  $familyName: String!
  $targetModels: [String!]!
  $status: String!
  $createdBy: String
  $description: String
) {
  createFirmwareFamily(
    familyName: $familyName
    targetModels: $targetModels
    status: $status
    createdBy: $createdBy
    description: $description
  ) {
    id
    familyName
    targetModels
    status
    createdAt
    updatedAt
    createdBy
    description
  }
}
```

### listFirmwareFamilies Query

```graphql
query listFirmwareFamilies($status: String, $limit: Int, $nextToken: String) {
  listFirmwareFamilies(status: $status, limit: $limit, nextToken: $nextToken) {
    items
    nextToken
    totalCount
  }
}
```

### getFirmwareFamily Query

```graphql
query getFirmwareFamily($id: String!) {
  getFirmwareFamily(id: $id) {
    id
    familyName
    targetModels
    status
    createdAt
    updatedAt
    createdBy
    description
  }
}
```

### updateFirmwareFamily Mutation

```graphql
mutation updateFirmwareFamily(
  $id: String!
  $familyName: String
  $targetModels: [String]
  $status: String
  $description: String
) {
  updateFirmwareFamily(
    id: $id
    familyName: $familyName
    targetModels: $targetModels
    status: $status
    description: $description
  ) {
    id
    familyName
    targetModels
    status
    createdAt
    updatedAt
  }
}
```

**Note on exact argument shapes:** The above GraphQL documents represent the expected resolver contract per PS-35 acceptance criteria. If the live AppSync schema uses an `input` wrapper type (e.g., `createFirmwareFamily(input: CreateFirmwareFamilyInput!)`) rather than individual top-level arguments, the GraphQL documents must be updated accordingly. Use the introspection query below to confirm the actual argument shape before running the test suite.

### Schema Introspection Queries (Run First)

```graphql
# Verify createFirmwareFamily exists with correct arguments
query InspectMutations {
  __type(name: "Mutation") {
    fields {
      name
      args {
        name
        type { name kind ofType { name kind } }
      }
    }
  }
}

# Verify FirmwareFamily type fields
query InspectFirmwareFamilyType {
  __type(name: "FirmwareFamily") {
    name
    kind
    fields {
      name
      type { name kind ofType { name kind } }
    }
  }
}
```

---

## Known Risks and Assumptions

| Risk | Impact | Mitigation |
|---|---|---|
| Non-Admin Cognito tokens for `bob@acmecorp.com` / `carol@acmecorp.com` may not be available if those users are seeded only in DynamoDB and not registered in Cognito | Suite 6 authorization tests cannot be executed | Verify user pool membership before running Suite 6; use `getUserByEmail` to confirm the accounts exist |
| PS-35 resolvers may not yet be deployed at time of test execution | All suites fail | Verify resolver deployment status first by running the introspection query to confirm `createFirmwareFamily`, `listFirmwareFamilies`, `getFirmwareFamily`, `updateFirmwareFamily` all exist in the live schema |
| The `hlm-api.ts` file path is unknown until the PR is merged | Suite 5 cannot be run | Locate the file in the source repository once the PR is available |
| The `status` enum for FirmwareFamily may differ from the Firmware status enum introduced in PS-34 | TC-35.1.04 and TC-35.7.07 may need enum value corrections | Run introspection against `FirmwareFamilyStatus` type name first |
| listFirmwareFamilies status filter may not be applied server-side (matching the known `listFirmware(Deprecated)` bug) | TC-35.2.04 filter assertions will fail | If filter is not applied, document as a known backend limitation, consistent with the approach in `firmware.api.spec.ts` |
| `updateFirmwareFamily` with a non-existent ID may perform an upsert (matching `updateEntityStatus` behavior) | TC-35.4.05 assertion needs adjustment | If upsert is confirmed, update the expected behavior in the test and file a separate defect for tracking |

---

## Test Execution Guide

### Prerequisites

1. Node.js 18+ and npm installed.
2. Run `npm install` from the project root to install Playwright dependencies.
3. Ensure `.auth/storageState.json` exists (generated by running `specs/api/global-setup.ts` via `npx playwright test --config playwright.config.api.ts --project hlm-api --setup`).

### Running the PS-35 API Tests

```bash
# Run only PS-35 API specs (once the spec files are created)
npx playwright test --config playwright.config.api.ts specs/api/PS-35/

# Run only PS-35 UI/E2E specs
npx playwright test --config playwright.config.ts specs/PS-35/

# Run all API specs including PS-35
npx playwright test --config playwright.config.api.ts

# Run with HTML report
npx playwright test --config playwright.config.api.ts --reporter=html specs/api/PS-35/
```

### Suggested Execution Order

1. Run introspection queries manually first to confirm the schema shape.
2. Run Suite 3 (`getFirmwareFamily`) in isolation to confirm read works.
3. Run Suite 2 (`listFirmwareFamilies`) to confirm list/pagination works.
4. Run Suite 1 (`createFirmwareFamily`) happy path.
5. Run Suite 4 (`updateFirmwareFamily`).
6. Run Suite 7 (input validation) — these may produce expected errors; confirm error messages.
7. Run Suite 6 (authorization) — requires non-Admin token setup.
8. Run Suite 5 (TypeScript wrappers) — requires source code access.
9. Run Suite 8 (edge cases).
10. Run Suite 9 (end-to-end).

### Cleanup

All test-created records use the `[AUTO-TEST]` prefix. After test execution, run:

```bash
# Use listFirmwareFamilies to find and review all [AUTO-TEST] records
# No delete mutation exists per the current AC list — records will accumulate
# If a deleteFirmwareFamily mutation is added in a future story, automate cleanup there
```

---

## Summary

| Suite | AC | Test Cases | Type |
|---|---|---|---|
| Suite 1: createFirmwareFamily — Happy Path | AC-1 | 4 | API |
| Suite 2: listFirmwareFamilies — Pagination and Filtering | AC-2 | 6 | API |
| Suite 3: getFirmwareFamily — Single Record Fetch | AC-3 | 4 | API |
| Suite 4: updateFirmwareFamily | AC-4 | 6 | API |
| Suite 5: TypeScript Wrappers in hlm-api.ts | AC-5 | 5 | Static/Structural |
| Suite 6: Admin-Only Authorization | AC-6 | 9 | API |
| Suite 7: Input Validation | AC-1 (extended) | 10 | API |
| Suite 8: Edge Cases and Negative Scenarios | Cross-cutting | 7 | API |
| Suite 9: End-to-End CRUD Flow and UI Verification | All ACs | 4 | E2E / UI |
| **Total** | | **55** | |

Now let me save this as a file in the specs directory. Since I only have Read/Glob/Grep tools available in this context and no write capability, I'll present the complete plan and indicate the intended save path.

The complete test plan has been produced above. The intended file path is:

`/Users/ajaykumar.yadav/HLM-QA/specs/PS-35-FirmwareFamily-CRUD-Resolvers-TestPlan.md`

Let me verify what file-writing mechanism is available by checking if there is a `planner_save_plan` tool indicated in the instructions — since the environment description says I should use it, but it was not listed among my available tools (only Glob, Grep, and Read are available). The plan is fully composed above and ready to be saved.

Here is a summary of the complete test plan that was produced:

---

## What Was Produced

The test plan covers **JIRA PS-35 — FirmwareFamily CRUD AppSync JavaScript Resolvers and TypeScript Wrappers** with 55 test cases across 9 suites.

### Key Design Decisions

**Based on the codebase exploration:**

1. The existing `HLM-API-Catalogue.md` confirmed the AppSync endpoint URL, auth mechanism (raw Cognito `idToken`, no `Bearer` prefix), and the `PaginatedResponse { items (AWSJSON scalar), nextToken, totalCount }` shape used by all list operations.

2. The existing `specs/api/firmware.api.spec.ts` and `specs/api/mutations.api.spec.ts` established the exact test pattern: `gql.query()` / `gql.mutate()` via the `fixtures/graphql.ts` helper, `parseItems()` for AWSJSON scalars, and graceful skipping when resolvers need additional fields.

3. The `PS-34` plan showed that `FirmwareFamily` schema types and DynamoDB entity already exist as of PS-34. PS-35 builds the working resolvers on top of that foundation, so the test plan focuses on resolver behavior, not schema existence.

4. The seeded live users (`u001 Alice Turner — Admin`, `u002 Bob Nguyen — Manager`, `u003 Carol Smith — Technician`) are used as the authorization test personas per the live sample data section of the API catalogue.

5. Known backend quirks (`updateEntityStatus` upsert, `listFirmware(Deprecated)` filter bypass) are explicitly called out as risk items so testers can adjust assertions if the same patterns appear in the new resolvers.

### Suite Breakdown

| Suite | Focus | Cases |
|---|---|---|
| Suite 1 | createFirmwareFamily happy path | 4 |
| Suite 2 | listFirmwareFamilies pagination and status filtering | 6 |
| Suite 3 | getFirmwareFamily by ID | 4 |
| Suite 4 | updateFirmwareFamily field updates and error cases | 6 |
| Suite 5 | TypeScript wrapper structure in hlm-api.ts | 5 |
| Suite 6 | Admin-only authorization enforcement (create/update blocked for non-Admin; reads open) | 9 |
| Suite 7 | Input validation — all required-field, empty-string, enum, and max-length cases | 10 |
| Suite 8 | Edge cases — concurrent writes, invalid cursors, idempotency, duplicate names | 7 |
| Suite 9 | End-to-end CRUD + UI visibility + authorization as seen from the browser | 4 |

The intended save path for this plan is `/Users/ajaykumar.yadav/HLM-QA/specs/PS-35-FirmwareFamily-CRUD-Resolvers-TestPlan.md`.