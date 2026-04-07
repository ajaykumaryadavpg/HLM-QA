# PS-34: FirmwareFamily DynamoDB Entity and AppSync Schema - QA Test Plan

## Application Overview

This test plan covers JIRA story PS-34: "FirmwareFamily DynamoDB Entity and AppSync Schema". The story introduces a new FirmwareFamily entity in DynamoDB, adds an AppSync GraphQL schema type for FirmwareFamily, updates the Firmware type with a familyId field, and expands the firmware status enum from three values to five (Screening, Staged, Active, Deprecated, Recalled).

Application under test: https://main.dddsig2mih3hw.amplifyapp.com
AppSync GraphQL endpoint: https://rw3sl7mwgzcvbg7zuyqj7ppjv4.appsync-api.us-east-2.amazonaws.com/graphql

Current state observed on 2026-04-03:
- The Deployment page (/deployment) lists firmware versions with statuses currently rendered as: Pending, Approved, Deprecated, Rejected.
- The existing test data seed file (specs/api/test-data/seed.ts) defines firmwareStatuses as ['Pending', 'Approved', 'Deprecated', 'Rejected'] — this must be updated to the 5-value enum after PS-34 is implemented.
- The listFirmware GraphQL query exists and returns items with fields: id, name, version, deviceModel, status, releaseDate, fileSize, checksum. No familyId field is currently present.
- No FirmwareFamily type, createFirmwareFamily mutation, or listFirmwareFamilies query exists in the current schema.
- Audit log PK pattern observed: FW#uuid for Firmware, USER#uuid for Users. FirmwareFamily should use FF#uuid or FIRMWARE_FAMILY#uuid.
- The Upload Firmware UI form at /deployment contains required fields: Name, Version, Device Model, plus optional Manufacturer, Release Date, Firmware File, Release Notes. No familyId field is currently exposed in the UI.

All 8 acceptance criteria are covered across 9 test suites with 35 individual test cases spanning API/GraphQL validation, DynamoDB design verification, UI behavior, and unit test coverage checks.

## Test Scenarios

### 1. Suite 1: FirmwareFamily DynamoDB Entity Fields (AC-1)

**Seed:** `specs/api/global-setup.ts`

#### 1.1. TC-34.1.01: Create FirmwareFamily with all required fields and verify response

**File:** `specs/api/PS-34/firmware-family-entity.api.spec.ts`

**Steps:**
  1. Send a GraphQL createFirmwareFamily mutation to the AppSync endpoint with input: { familyName: '[AUTO-TEST] Solar Inverter Family', targetModels: ['LN-11', 'LN-12'], status: 'Active', createdBy: 'user-test-001' }
    - expect: The HTTP response status is 200
    - expect: The response body contains no 'errors' array
    - expect: The returned FirmwareFamily object contains all required fields: id, familyName, targetModels, status, createdAt, updatedAt, createdBy
  2. Inspect the id field in the response
    - expect: The id is a non-null UUID string matching the pattern xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
  3. Inspect the familyName field in the response
    - expect: The familyName equals '[AUTO-TEST] Solar Inverter Family'
  4. Inspect the targetModels field in the response
    - expect: The targetModels is a non-empty list containing 'LN-11' and 'LN-12'
  5. Inspect the createdAt and updatedAt fields in the response
    - expect: Both createdAt and updatedAt are valid ISO 8601 timestamps
    - expect: createdAt equals updatedAt on a freshly created record
  6. Inspect the createdBy field in the response
    - expect: The createdBy field equals 'user-test-001'

#### 1.2. TC-34.1.02: Retrieve FirmwareFamily by id and confirm all fields are persisted

**File:** `specs/api/PS-34/firmware-family-entity.api.spec.ts`

**Steps:**
  1. Create a FirmwareFamily via createFirmwareFamily mutation and capture the returned id
    - expect: The createFirmwareFamily mutation returns a valid id
  2. Send a getFirmwareFamily GraphQL query with the captured id
    - expect: The query returns a FirmwareFamily object whose id, familyName, targetModels, status, createdAt, updatedAt, and createdBy fields all match the values from the creation step

#### 1.3. TC-34.1.03: Optional FirmwareFamily fields are persisted when provided

**File:** `specs/api/PS-34/firmware-family-entity.api.spec.ts`

**Steps:**
  1. Send a createFirmwareFamily mutation including optional fields such as description and notes
    - expect: The mutation succeeds and returns the FirmwareFamily with all optional fields populated
  2. Send a getFirmwareFamily query using the returned id and request the optional fields in the selection set
    - expect: The retrieved record includes the optional fields with the correct stored values

### 2. Suite 2: AppSync Schema Type Definition for FirmwareFamily (AC-2)

**Seed:** `specs/api/global-setup.ts`

#### 2.1. TC-34.2.01: FirmwareFamily type exists in AppSync schema via introspection

**File:** `specs/api/PS-34/appsync-schema.api.spec.ts`

**Steps:**
  1. Send the introspection query { __type(name: "FirmwareFamily") { name kind fields { name type { name kind ofType { name kind } } } } } to the AppSync endpoint
    - expect: The response contains a non-null __type with name 'FirmwareFamily' and kind 'OBJECT', confirming the type is defined in the schema
  2. Inspect the fields array of the FirmwareFamily type from the introspection response
    - expect: The fields array includes at minimum: id, familyName, targetModels, status, createdAt, updatedAt, createdBy
  3. Check the type definition of the id field
    - expect: id is typed as ID! or String! (non-nullable scalar)
  4. Check the type definition of the familyName field
    - expect: familyName is typed as String! (non-nullable scalar)
  5. Check the type definition of the targetModels field
    - expect: targetModels is typed as [String] or [String!] (a list type)
  6. Check the type definition of the status field
    - expect: status references a named ENUM type (e.g., FirmwareFamilyStatus or FirmwareStatus) rather than a plain String scalar

#### 2.2. TC-34.2.02: createFirmwareFamily mutation is defined in the schema

**File:** `specs/api/PS-34/appsync-schema.api.spec.ts`

**Steps:**
  1. Send the introspection query { __type(name: "Mutation") { fields { name args { name type { name kind ofType { name kind } } } } } }
    - expect: The Mutation type includes a field named createFirmwareFamily
  2. Inspect the argument list for the createFirmwareFamily mutation
    - expect: The mutation accepts an input argument (e.g., input: CreateFirmwareFamilyInput!) containing at minimum familyName (required String), targetModels (required list), and status (required enum)

#### 2.3. TC-34.2.03: getFirmwareFamily and listFirmwareFamilies queries are defined in the schema

**File:** `specs/api/PS-34/appsync-schema.api.spec.ts`

**Steps:**
  1. Send the introspection query { __type(name: "Query") { fields { name } } } and check for getFirmwareFamily
    - expect: The Query type includes a field named getFirmwareFamily accepting an id argument of type ID! or String!
  2. Check for listFirmwareFamilies in the Query type from the same introspection response
    - expect: The Query type includes a field named listFirmwareFamilies
  3. Execute a listFirmwareFamilies query with no filters and inspect the response shape
    - expect: The response contains an items array of FirmwareFamily objects and a nextToken field for pagination support. A totalCount field is also expected consistent with other list operations in the schema.

#### 2.4. TC-34.2.04: updateFirmwareFamily mutation is defined and functional

**File:** `specs/api/PS-34/appsync-schema.api.spec.ts`

**Steps:**
  1. Send the introspection query for the Mutation type and check for updateFirmwareFamily
    - expect: The Mutation type includes a field named updateFirmwareFamily
  2. Create a FirmwareFamily then execute an updateFirmwareFamily mutation changing the familyName to '[AUTO-TEST] Updated Family Name'
    - expect: The mutation succeeds and returns the updated FirmwareFamily with familyName equal to '[AUTO-TEST] Updated Family Name'. The updatedAt timestamp is later than the original createdAt timestamp.

### 3. Suite 3: Firmware Type Updated with familyId Field (AC-3)

**Seed:** `specs/api/global-setup.ts`

#### 3.1. TC-34.3.01: familyId field exists on the Firmware type in AppSync schema

**File:** `specs/api/PS-34/firmware-familyid.api.spec.ts`

**Steps:**
  1. Send the introspection query { __type(name: "Firmware") { name fields { name type { name kind ofType { name kind } } } } }
    - expect: The Firmware type's fields array includes a field named familyId
  2. Inspect the type definition of the familyId field
    - expect: The familyId field is typed as String or ID (nullable, since a firmware item may not yet be assigned to a family)

#### 3.2. TC-34.3.02: familyId is returned in listFirmware query response

**File:** `specs/api/PS-34/firmware-familyid.api.spec.ts`

**Steps:**
  1. Send a listFirmware GraphQL query explicitly requesting the familyId field: query { listFirmware { items { id name version status familyId } totalCount nextToken } }
    - expect: The AppSync endpoint accepts the query without a schema validation error. The items array contains Firmware objects that each include a familyId field.
  2. Check the familyId values of existing Firmware records (created before PS-34 was implemented)
    - expect: Existing Firmware records that predate PS-34 return familyId as null rather than causing a missing-field error or GraphQL error

#### 3.3. TC-34.3.03: familyId can be set when creating firmware and is retrievable

**File:** `specs/api/PS-34/firmware-familyid.api.spec.ts`

**Steps:**
  1. Create a FirmwareFamily via createFirmwareFamily mutation and capture its id
    - expect: A FirmwareFamily is created and its id is available
  2. Create a Firmware record via createFirmware (or uploadFirmware) mutation with the familyId argument set to the FirmwareFamily id captured above
    - expect: The mutation succeeds and the returned Firmware object has familyId populated with the correct FirmwareFamily id
  3. Send a getFirmware query for the newly created Firmware and include familyId in the selection set
    - expect: The retrieved Firmware record has familyId equal to the FirmwareFamily id set during creation

#### 3.4. TC-34.3.04: familyId can be updated on an existing Firmware record

**File:** `specs/api/PS-34/firmware-familyid.api.spec.ts`

**Steps:**
  1. Create a Firmware record with familyId: null (or without familyId). Then create a FirmwareFamily and send an updateFirmware mutation to set familyId to the new family's id
    - expect: The updateFirmware mutation succeeds and the response shows the updated familyId value
  2. Retrieve the Firmware record via getFirmware and request the familyId field
    - expect: The familyId field reflects the updated FirmwareFamily id, confirming the change was persisted in DynamoDB

### 4. Suite 4: Firmware Status Enum Expanded to 5 Values (AC-4)

**Seed:** `specs/api/global-setup.ts`

#### 4.1. TC-34.4.01: FirmwareStatus enum contains exactly the five expected values via introspection

**File:** `specs/api/PS-34/firmware-status-enum.api.spec.ts`

**Steps:**
  1. Send the introspection query { __type(name: "FirmwareStatus") { kind name enumValues { name } } } to the AppSync endpoint
    - expect: The response returns an ENUM type named FirmwareStatus with exactly five enumValues: Screening, Staged, Active, Deprecated, Recalled
  2. Verify the old three-value set (Pending, Approved, Rejected) is not present as standalone values unless mapped to the new enum
    - expect: The enum values list does not contain 'Pending', 'Approved', or 'Rejected' as standalone entries. The five new values replace the previous values.
  3. Update the test data seed file (specs/api/test-data/seed.ts) reference for firmwareStatuses to reflect the new enum: ['Screening', 'Staged', 'Active', 'Deprecated', 'Recalled']
    - expect: The updated seed constants align with the schema definition so existing firmware API tests continue to pass with the new enum values

#### 4.2. TC-34.4.02: Firmware can be created with each of the five valid status values

**File:** `specs/api/PS-34/firmware-status-enum.api.spec.ts`

**Steps:**
  1. Send a createFirmware mutation with status: 'Screening'
    - expect: The mutation succeeds and returns a Firmware object with status equal to 'Screening'
  2. Send a createFirmware mutation with status: 'Staged'
    - expect: The mutation succeeds and returns a Firmware object with status equal to 'Staged'
  3. Send a createFirmware mutation with status: 'Active'
    - expect: The mutation succeeds and returns a Firmware object with status equal to 'Active'
  4. Send a createFirmware mutation with status: 'Deprecated'
    - expect: The mutation succeeds and returns a Firmware object with status equal to 'Deprecated'
  5. Send a createFirmware mutation with status: 'Recalled'
    - expect: The mutation succeeds and returns a Firmware object with status equal to 'Recalled'

#### 4.3. TC-34.4.03: Invalid enum values are rejected by the AppSync schema

**File:** `specs/api/PS-34/firmware-status-enum.api.spec.ts`

**Steps:**
  1. Send a createFirmware mutation with the legacy value status: 'Pending' (which is no longer a valid enum member)
    - expect: The AppSync endpoint returns a GraphQL validation error. The errors array contains a message indicating 'Pending' is not a valid FirmwareStatus value. No Firmware record is created.
  2. Send a createFirmware mutation with status: 'Approved' (legacy value)
    - expect: The endpoint returns a GraphQL validation error for 'Approved'. No record is created.
  3. Send a createFirmware mutation with status: 'UNKNOWN_STATUS'
    - expect: The endpoint returns a GraphQL validation error. The HTTP status is 400 or the response body contains an errors array.

#### 4.4. TC-34.4.04: Firmware status can be transitioned through the lifecycle via API

**File:** `specs/api/PS-34/firmware-status-enum.api.spec.ts`

**Steps:**
  1. Create a Firmware with status: 'Screening'. Then send an updateFirmware mutation to change the status to 'Staged'
    - expect: The update succeeds and the Firmware now has status 'Staged'
  2. Send an updateFirmware mutation to advance the status from 'Staged' to 'Active'
    - expect: The update succeeds and the Firmware now has status 'Active'
  3. Send an updateFirmware mutation to set status to 'Deprecated'
    - expect: The update succeeds and the Firmware now has status 'Deprecated'
  4. Send an updateFirmware mutation to set status to 'Recalled'
    - expect: The update succeeds and the Firmware now has status 'Recalled'

#### 4.5. TC-34.4.05: UI Deployment page renders the correct status badges for new enum values

**File:** `specs/PS-34/firmware-status-enum.spec.ts`

**Steps:**
  1. Navigate to https://main.dddsig2mih3hw.amplifyapp.com/deployment and wait for the firmware list to fully load
    - expect: The Deployment page loads successfully and the firmware list is visible with multiple firmware entries
  2. Inspect all firmware card status badges visible in the Firmware Versions tab
    - expect: Each firmware card status badge displays one of the five valid enum labels: Screening, Staged, Active, Deprecated, or Recalled. No card shows a legacy status like 'Pending' or 'Approved' unless those values are valid under the old schema that remains in use before PS-34 deployment.
  3. Verify that firmware cards with status 'Deprecated' or 'Recalled' do not display the Approve, Download, or Deprecate action buttons
    - expect: Terminal-state firmware items (Deprecated, Recalled) show only a Details button. Action buttons that would cause a state transition are hidden.
  4. Verify that firmware cards with status 'Active' show Download and Deprecate action buttons
    - expect: Firmware items with status 'Active' have at minimum Download and Deprecate buttons visible

### 5. Suite 5: DynamoDB PK/SK Single-Table Pattern Design (AC-5)

**Seed:** `specs/api/global-setup.ts`

#### 5.1. TC-34.5.01: FirmwareFamily PK follows the single-table design prefix convention

**File:** `specs/api/PS-34/dynamo-pk-sk-design.api.spec.ts`

**Steps:**
  1. Create a FirmwareFamily via createFirmwareFamily mutation. Then query the audit log via listAuditLogs with resourceType: 'FirmwareFamily'
    - expect: At least one audit log entry is returned for the created FirmwareFamily
  2. Inspect the PK field of the FirmwareFamily audit log entry
    - expect: The PK is formatted as 'FF#<uuid>' or 'FIRMWARE_FAMILY#<uuid>', following the same single-table prefix pattern observed for other resource types: FW# for Firmware and USER# for Users

#### 5.2. TC-34.5.02: Firmware PK pattern is unchanged after familyId field addition

**File:** `specs/api/PS-34/dynamo-pk-sk-design.api.spec.ts`

**Steps:**
  1. Create a new Firmware record with a familyId set, then retrieve its audit log entry
    - expect: The audit log entry for the new Firmware record has a PK formatted as 'FW#<uuid>', unchanged by the addition of the familyId field

#### 5.3. TC-34.5.03: FirmwareFamily and its associated Firmware items can be queried together

**File:** `specs/api/PS-34/dynamo-pk-sk-design.api.spec.ts`

**Steps:**
  1. Create a FirmwareFamily record and then create two Firmware records with familyId set to the FirmwareFamily's id
    - expect: All three records are created successfully
  2. Execute a listFirmwareByFamily GraphQL query (or equivalent) using the FirmwareFamily id as the filter argument
    - expect: The query returns exactly the two Firmware records associated with the given family id. No unrelated Firmware items appear in the result.

#### 5.4. TC-34.5.04: Range query on FirmwareFamily by status returns correct items

**File:** `specs/api/PS-34/dynamo-pk-sk-design.api.spec.ts`

**Steps:**
  1. Create three FirmwareFamily records with statuses 'Active', 'Deprecated', and 'Recalled' respectively. Then execute listFirmwareFamilies with status filter: 'Active'
    - expect: The response contains the 'Active' FirmwareFamily. The 'Deprecated' and 'Recalled' records are absent from this response.

### 6. Suite 6: GSI Mappings for Query Patterns (AC-6)

**Seed:** `specs/api/global-setup.ts`

#### 6.1. TC-34.6.01: GSI query by status filters FirmwareFamily items correctly

**File:** `specs/api/PS-34/gsi-query-patterns.api.spec.ts`

**Steps:**
  1. Create two FirmwareFamily records: one with status 'Active' and one with status 'Deprecated'. Execute listFirmwareFamiliesByStatus (or listFirmwareFamilies with status argument) for status: 'Active'
    - expect: The response contains the 'Active' record. The 'Deprecated' record does not appear in this result.
  2. Execute the same status-filtered query for status: 'Deprecated'
    - expect: Only the 'Deprecated' record appears in the result. Response time is within acceptable bounds (under 3 seconds), consistent with GSI usage rather than a full DynamoDB table scan.

#### 6.2. TC-34.6.02: GSI query by targetModel returns correct FirmwareFamily items

**File:** `specs/api/PS-34/gsi-query-patterns.api.spec.ts`

**Steps:**
  1. Create two FirmwareFamily records: one with targetModels: ['LN-11'] only, and another with targetModels: ['XR-5000'] only. Execute listFirmwareFamiliesByModel (or equivalent) for targetModel: 'LN-11'
    - expect: The response returns only the FirmwareFamily record that includes 'LN-11' in its targetModels list. The 'XR-5000'-only record does not appear.

#### 6.3. TC-34.6.03: GSI query returns Firmware items by familyId

**File:** `specs/api/PS-34/gsi-query-patterns.api.spec.ts`

**Steps:**
  1. Create a FirmwareFamily and associate two Firmware records with it by setting familyId on each Firmware. Then execute listFirmwareByFamily with the FirmwareFamily id
    - expect: The response returns exactly the two Firmware items whose familyId matches the given FirmwareFamily id. No unrelated Firmware items appear.

#### 6.4. TC-34.6.04: GSI-based list queries support pagination via nextToken

**File:** `specs/api/PS-34/gsi-query-patterns.api.spec.ts`

**Steps:**
  1. Execute listFirmwareFamilies with limit: 1. Capture the nextToken from the response
    - expect: The response contains exactly one item and a non-null nextToken (assuming more than one FirmwareFamily exists in the database)
  2. Execute a second listFirmwareFamilies query with the captured nextToken and limit: 1
    - expect: The response returns a different FirmwareFamily item from the first page. The same item does not repeat. The cursor advanced correctly.

### 7. Suite 7: Input Validation on Required Fields (AC-7)

**Seed:** `specs/api/global-setup.ts`

#### 7.1. TC-34.7.01: createFirmwareFamily mutation rejected when familyName is missing

**File:** `specs/api/PS-34/input-validation.api.spec.ts`

**Steps:**
  1. Send a createFirmwareFamily mutation omitting the familyName field from the input (include targetModels and status but not familyName)
    - expect: The AppSync endpoint returns an HTTP 200 with a GraphQL errors array
    - expect: The error message indicates that familyName is a required field
    - expect: No FirmwareFamily record is created in DynamoDB

#### 7.2. TC-34.7.02: createFirmwareFamily mutation rejected when familyName is empty string

**File:** `specs/api/PS-34/input-validation.api.spec.ts`

**Steps:**
  1. Send a createFirmwareFamily mutation with familyName: '' (empty string)
    - expect: The endpoint returns an error (via the GraphQL errors array or HTTP 400)
    - expect: The error message indicates that familyName cannot be blank
    - expect: No record is created

#### 7.3. TC-34.7.03: createFirmwareFamily mutation rejected when status is missing

**File:** `specs/api/PS-34/input-validation.api.spec.ts`

**Steps:**
  1. Send a createFirmwareFamily mutation omitting the status field from the input
    - expect: The AppSync endpoint returns a GraphQL schema validation error indicating that status is required
    - expect: No record is created

#### 7.4. TC-34.7.04: createFirmwareFamily mutation rejected when targetModels is missing or empty

**File:** `specs/api/PS-34/input-validation.api.spec.ts`

**Steps:**
  1. Send a createFirmwareFamily mutation omitting the targetModels field entirely
    - expect: The endpoint returns a validation error indicating that targetModels is required
    - expect: No record is created
  2. Send a createFirmwareFamily mutation with targetModels: [] (empty array)
    - expect: The endpoint returns a validation error indicating that targetModels must contain at least one model identifier
    - expect: No record is created

#### 7.5. TC-34.7.05: updateFirmwareFamily mutation returns error when id does not exist

**File:** `specs/api/PS-34/input-validation.api.spec.ts`

**Steps:**
  1. Send an updateFirmwareFamily mutation with a non-existent id: 'non-existent-id-00000000-0000-0000-0000-000000000000'
    - expect: The resolver returns an error such as 'FirmwareFamily not found' or equivalent 404-type GraphQL error
    - expect: The operation does not create a new record or silently succeed

#### 7.6. TC-34.7.06: createFirmwareFamily mutation rejected when familyName exceeds maximum length

**File:** `specs/api/PS-34/input-validation.api.spec.ts`

**Steps:**
  1. Send a createFirmwareFamily mutation with familyName set to a string of 300 characters (exceeding any reasonable maximum)
    - expect: The endpoint returns a validation error indicating that familyName exceeds the maximum allowed length
    - expect: The error message specifies the constraint or maximum character count
    - expect: No record is created

#### 7.7. TC-34.7.07: UI Upload Firmware form validates required fields before enabling submission

**File:** `specs/PS-34/upload-firmware-validation.spec.ts`

**Steps:**
  1. Navigate to https://main.dddsig2mih3hw.amplifyapp.com/deployment and click the 'Upload Firmware' button
    - expect: The Upload Firmware dialog opens with fields: Name, Version, Device Model, Manufacturer (optional), Release Date, Firmware File, Release Notes (optional). The Submit button is disabled.
  2. Leave all fields empty and observe the Submit button state
    - expect: The Submit button remains disabled when all required fields are empty
  3. Fill in only the Name field with 'Test Firmware' and observe the Submit button
    - expect: The Submit button remains disabled because Version, Device Model, and Firmware File are still empty
  4. Fill in Name, Version ('v1.0.0'), and Device Model ('XR-5000'), but do not attach a firmware file. Observe the Submit button.
    - expect: The Submit button remains disabled until a firmware file is selected
  5. Attach a valid firmware binary file using the Firmware File picker button. Observe the Submit button.
    - expect: The Submit button becomes enabled after all required fields (Name, Version, Device Model, and Firmware File) are provided

#### 7.8. TC-34.7.08: UI Upload Firmware form exposes familyId field after PS-34 implementation

**File:** `specs/PS-34/upload-firmware-validation.spec.ts`

**Steps:**
  1. Navigate to the Upload Firmware dialog at /deployment and inspect all form fields
    - expect: A 'Firmware Family' dropdown or familyId field is present in the form. If this field is absent, the test is marked as FAIL against Acceptance Criterion 3 (familyId field exposure in UI).
  2. If the Firmware Family field is present: select an existing FirmwareFamily from the dropdown and submit the form with all other required fields filled
    - expect: The firmware is uploaded successfully and the created Firmware record has familyId populated. A subsequent listFirmware query requesting familyId returns the correct FirmwareFamily id for the newly uploaded firmware.

### 8. Suite 8: Unit Test Coverage Verification (>=85%) (AC-8)

**Seed:** ``

#### 8.1. TC-34.8.01: Unit test suite for FirmwareFamily entity includes happy-path coverage

**File:** `specs/PS-34/unit-test-coverage-review.spec.ts`

**Steps:**
  1. Locate and review the unit test source files for the FirmwareFamily entity and service/resolver layer in the backend Lambda or resolver codebase
    - expect: Unit tests exist that create a valid FirmwareFamily object with all required fields and assert that the object is persisted correctly to DynamoDB
  2. Verify unit tests cover the updateFirmwareFamily operation
    - expect: Unit tests exist for updating familyName, targetModels, and status fields on an existing FirmwareFamily record

#### 8.2. TC-34.8.02: Unit tests cover negative/validation cases for FirmwareFamily

**File:** `specs/PS-34/unit-test-coverage-review.spec.ts`

**Steps:**
  1. Review unit test files for the FirmwareFamily service or resolver for negative test cases
    - expect: Unit tests exist for: (1) missing required field familyName, (2) missing required field status, (3) empty targetModels array, (4) invalid status enum value, (5) updateFirmwareFamily with non-existent id. Each test asserts the correct error type and message.

#### 8.3. TC-34.8.03: Unit tests for Firmware entity cover the new familyId field

**File:** `specs/PS-34/unit-test-coverage-review.spec.ts`

**Steps:**
  1. Review unit test files for the Firmware entity/service layer to confirm familyId-specific test coverage
    - expect: Unit tests exist for: (1) creating a Firmware with a valid familyId, (2) creating a Firmware with familyId: null (field is optional), (3) updating familyId on an existing Firmware record, (4) verifying that a non-existent familyId is handled with an appropriate error if referential integrity is enforced

#### 8.4. TC-34.8.04: Unit tests for FirmwareStatus enum cover all five values

**File:** `specs/PS-34/unit-test-coverage-review.spec.ts`

**Steps:**
  1. Review unit test files for the FirmwareStatus enum serialization and deserialization logic
    - expect: Unit tests exercise each of the five status values: Screening, Staged, Active, Deprecated, Recalled. Tests verify that each enum value serializes correctly to its DynamoDB string representation and deserializes correctly to the GraphQL enum type.

#### 8.5. TC-34.8.05: Unit test coverage report meets the 85% threshold

**File:** `specs/PS-34/unit-test-coverage-review.spec.ts`

**Steps:**
  1. Execute the unit test suite for the FirmwareFamily and updated Firmware modules with a coverage reporter (Jest --coverage, Maven Jacoco, or equivalent) and capture the coverage report
    - expect: The coverage report shows line coverage of at least 85% for: FirmwareFamily entity class, FirmwareFamily service/resolver, Firmware entity changes (familyId field), and FirmwareStatus enum definition files
  2. Inspect branch coverage specifically for validation logic in createFirmwareFamily and updateFirmwareFamily
    - expect: Branch coverage for validation logic is at least 85%. All required-field checks, enum validation paths, and not-found error branches have corresponding test cases.
  3. Inspect coverage for GSI query resolver functions (listFirmwareFamiliesByStatus, listFirmwareByFamily, or equivalent)
    - expect: GSI resolver functions have at least 85% line coverage, with tests covering both populated and empty result sets, and the nextToken pagination code path
  4. Inspect coverage for DynamoDB PK/SK construction logic in the repository layer
    - expect: The PK construction code (formatting 'FF#<id>') and SK construction logic have unit test coverage ensuring correct key generation

### 9. Suite 9: End-to-End Integration — FirmwareFamily UI and API Flow

**Seed:** `specs/api/global-setup.ts`

#### 9.1. TC-34.9.01: End-to-end create FirmwareFamily, associate firmware, and verify in UI

**File:** `specs/PS-34/e2e-firmware-family-flow.spec.ts`

**Steps:**
  1. Send a createFirmwareFamily mutation with: familyName='[AUTO-TEST] E2E Family', targetModels=['LN-11'], status='Active'. Capture the returned id.
    - expect: The mutation returns a valid FirmwareFamily id and all required fields are populated
  2. Send a createFirmware mutation with: name='[AUTO-TEST] E2E Firmware', version='v1.0.0', deviceModel='LN-11', status='Screening', familyId=<family-id from previous step>
    - expect: The firmware is created successfully with familyId populated to the FirmwareFamily id
  3. Navigate to https://main.dddsig2mih3hw.amplifyapp.com/deployment and search for '[AUTO-TEST] E2E Firmware' in the search box
    - expect: The E2E Test Firmware card appears in the Firmware Versions list with the correct version 'v1.0.0' and device model 'LN-11'
  4. Click the Details button on the E2E Firmware card
    - expect: The details panel expands. If the UI exposes familyId or the firmware family name, the correct association '[AUTO-TEST] E2E Family' is displayed in the details.

#### 9.2. TC-34.9.02: Firmware status progression follows the new enum lifecycle

**File:** `specs/PS-34/e2e-firmware-family-flow.spec.ts`

**Steps:**
  1. Create a Firmware record with status: 'Screening'
    - expect: Firmware is created with status 'Screening'
  2. Send an updateFirmware mutation changing status from 'Screening' to 'Staged'
    - expect: The firmware status is updated to 'Staged'
  3. Send an updateFirmware mutation changing status from 'Staged' to 'Active'
    - expect: The firmware status is updated to 'Active'
  4. Navigate to /deployment and locate the Active firmware item. Click 'Deprecate' if visible, or send an updateFirmware mutation with status: 'Deprecated'
    - expect: The firmware status transitions to 'Deprecated'. The Deprecate button is no longer available for this firmware card.

#### 9.3. TC-34.9.03: Audit log captures FirmwareFamily creation and update events

**File:** `specs/PS-34/e2e-firmware-family-flow.spec.ts`

**Steps:**
  1. Create a FirmwareFamily via GraphQL mutation and note the creation timestamp
    - expect: The FirmwareFamily is created successfully
  2. Navigate to the Deployment page's Audit Log tab at /deployment and switch to the Audit Log tab
    - expect: The Audit Log tab loads and displays recent activity entries
  3. Query the audit log via listAuditLogs GraphQL with resourceType: 'FirmwareFamily' and look for a 'Created' event for the newly created FirmwareFamily
    - expect: An audit entry with action 'Created' and resourceType 'FirmwareFamily' appears. The PK matches the FirmwareFamily id with the expected prefix (e.g., FF#<uuid>). The auditTimestamp is within an acceptable range of the creation time.
  4. Click 'Show changes' on the FirmwareFamily creation audit entry if visible in the UI
    - expect: The changes panel shows the new_state populated with all initial field values (familyName, targetModels, status, createdAt) and old_state as null or empty, reflecting a creation event

#### 9.4. TC-34.9.04: listFirmware query filters correctly by new enum status values

**File:** `specs/PS-34/e2e-firmware-family-flow.spec.ts`

**Steps:**
  1. Send a listFirmware query with status: 'Screening' and inspect the items
    - expect: All returned items have status 'Screening'. No items with other statuses appear.
  2. Send a listFirmware query with status: 'Recalled'
    - expect: The response either returns only Recalled firmware items or returns an empty array with totalCount: 0. No items with other statuses are returned.
  3. Navigate to /deployment and verify that the firmware list only displays cards with the five valid new status values
    - expect: No firmware card in the UI displays a legacy status label (Pending or Approved) after PS-34 is deployed. All status badges correspond to one of: Screening, Staged, Active, Deprecated, or Recalled.
