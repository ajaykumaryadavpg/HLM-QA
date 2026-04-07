# Rulebook: NIST 800-53 Security Standards

> Enforceable security controls for the IMS Gen2 Hardware Lifecycle Management platform.
> Maps NIST 800-53 Rev 5 controls to concrete code rules. Violations block PRs.

---

## Access Control

### AC-3: Access Enforcement (RBAC)

**Rule:** Every page and mutation MUST be gated by `rbac.ts`.

- All routes in `App.tsx` must use `<ProtectedRoute requiredPermission="...">` or equivalent guard
- Every API mutation must check `canPerformAction(user, action, resource)` before executing
- Never hardcode role checks (`if (user.role === "Admin")`) — always go through `rbac.ts`
- New pages/features MUST have RBAC entries added to `src/lib/rbac.ts` before the component is created

**Violation check:**

```
grep -r "user\.role ===" src/ → must return 0 results
grep -r "user\.groups\[" src/app/ → must return 0 results (use canPerformAction instead)
```

### AC-5: Separation of Duties

**Rule:** Multi-stage workflows must enforce different users at each stage.

- Firmware: uploader ≠ tester ≠ approver — enforced in resolver/mutation, not just UI
- Service orders: creator ≠ closer (for audit integrity)
- Never allow `userId === previousStageUserId` to pass a stage transition
- UI must disable action buttons when SoD would be violated (with explanation tooltip)

### AC-6: Least Privilege

**Rule:** Roles get minimum required permissions.

- Viewer: read-only across all resources
- Technician: read + update assigned service orders + update device status
- Manager: Technician + create/assign service orders + view reports
- Admin: Manager + user management + firmware approval + compliance config
- CustomerAdmin: scoped to own tenant — read devices, service orders, compliance

**Violation check:** Any PR that adds a permission to a role must justify why in the PR description.

### AC-11: Session Lock

**Rule:** Inactive sessions must lock after the configured timeout.

- Default timeout: 15 minutes of inactivity
- Session lock shows re-authentication prompt, does not destroy session
- Timeout value must be configurable (not hardcoded)
- Timer resets on: mouse move, keypress, touch, API call

### AC-12: Session Termination

**Rule:** Explicit logout must clear ALL client-side state.

- Clear all tokens (access, ID, refresh) from memory and storage
- Clear any cached user data from state management
- Redirect to sign-in page
- Invalidate server-side session/refresh token if supported by auth provider
- Never store tokens in `localStorage` without expiry — use `sessionStorage` or in-memory with secure cookie refresh

---

## Audit & Accountability

### AU-2: Auditable Events

**Rule:** These events MUST generate audit records:

| Event Category     | Examples                                                       |
| ------------------ | -------------------------------------------------------------- |
| Authentication     | Login, logout, failed login, MFA challenge, session timeout    |
| Authorization      | Access denied, role change, permission escalation              |
| Data changes       | Create, update, delete of any managed entity                   |
| Firmware lifecycle | Upload, test start/complete, approval, rejection, deployment   |
| Compliance         | Review submission, certification status change                 |
| User management    | User create, disable, role assignment, group membership change |
| Configuration      | System setting change, threshold update                        |

### AU-3: Content of Audit Records

**Rule:** Every audit record MUST include these fields:

```typescript
interface AuditRecord {
  timestamp: string; // ISO 8601 UTC — e.g., "2026-04-04T10:30:00.000Z"
  action: string; // Verb — "CREATE" | "UPDATE" | "DELETE" | "LOGIN" | "APPROVE"
  resourceType: string; // "Device" | "Firmware" | "ServiceOrder" | "User" | ...
  resourceId: string; // ID of the affected resource
  userId: string; // ID of the acting user
  userEmail: string; // Email for human-readable logs
  clientIP: string; // Source IP (from request headers)
  userAgent: string; // Browser/client identifier
  before?: unknown; // Previous state (for updates/deletes)
  after?: unknown; // New state (for creates/updates)
  result: string; // "SUCCESS" | "FAILURE" | "DENIED"
}
```

- Never omit `before`/`after` for update operations — diffs are required for compliance
- Timestamps must be UTC, never local time

### AU-6: Audit Review

**Rule:** Audit logs must be accessible and searchable in the UI.

- Audit log page with filters: date range, user, action, resource type
- Export capability: CSV and JSON
- Retention: minimum 90 days online, 1 year in archive
- Audit logs are immutable — no delete or update operations exposed

### AU-12: Audit Generation

**Rule:** Audit logging must be automatic, not opt-in.

- DynamoDB Streams / Cosmos DB Change Feed triggers audit record creation
- Frontend must pass `X-Client-IP` and `X-User-Agent` headers on mutations
- API layer must capture and forward these to the audit function
- Never bypass audit logging — no "silent" mutations

---

## Identification & Authentication

### IA-2: User Identification

**Rule:** All users must be uniquely identified and authenticated.

- No shared accounts or generic logins
- MFA required for Admin and Manager roles
- MFA recommended (prompted) for all other roles
- Password policy: minimum 12 characters (NIST 800-63B), no composition rules, check against breach lists

### IA-5: Authenticator Management

**Rule:** Credentials must be handled securely.

- Never store passwords in application code or config files
- Never log tokens, passwords, or credentials at any log level
- Token refresh must happen transparently (silent refresh before expiry)
- Failed login attempts: lock account after 5 consecutive failures, with configurable lockout duration

---

## System & Information Integrity

### SI-3: Malicious Code Protection

**Rule:** All user-facing output must be sanitized.

- Never use `dangerouslySetInnerHTML` — if absolutely required, sanitize with DOMPurify first
- All string interpolation in JSX is auto-escaped by React — do not bypass this
- CSP headers must be configured: `script-src 'self'`, no `unsafe-inline`, no `unsafe-eval`
- File uploads: validate MIME type, enforce size limits, scan before processing

**Violation check:**

```
grep -r "dangerouslySetInnerHTML" src/ → must return 0 results (or each instance must have DOMPurify)
```

### SI-10: Input Validation

**Rule:** Every form and API input MUST have a Zod schema.

- Form validation: `react-hook-form` + `zodResolver(schema)` — no manual validation
- API request validation: Zod schema on the API handler before processing
- URL parameters: parse and validate with Zod before use
- File uploads: validate type, size, and name format before accepting

**Violation check:**

```
# Every component with <form> must have a corresponding z.object() schema
grep -rn "<form" src/ → count forms
grep -rn "zodResolver" src/ → count must match
```

### SI-4: System Monitoring

**Rule:** The application must support observability.

- Application Insights / CloudWatch SDK integrated for error tracking
- API response times logged for performance monitoring
- Error rates tracked and alertable
- Health check endpoint available at `/api/health`

---

## System & Communications Protection

### SC-7: Boundary Protection

**Rule:** WAF or equivalent must protect all public endpoints.

- Rate limiting on authentication endpoints (max 10 attempts/minute/IP)
- Rate limiting on API endpoints (configurable per role)
- Geo-blocking capability (configurable)
- SQL injection and XSS managed rule sets enabled

### SC-8: Transmission Confidentiality

**Rule:** All data in transit must be encrypted.

- HTTPS only — no HTTP URLs anywhere in code or config
- TLS 1.2+ minimum
- HSTS header with minimum 1 year max-age
- Never disable TLS verification in any config, even for development

**Violation check:**

```
grep -rn "http://" src/ → must return 0 results (except localhost for dev server)
grep -rn "rejectUnauthorized.*false" → must return 0 results
```

### SC-12: Cryptographic Key Management

**Rule:** Encryption keys must be managed through the cloud provider's KMS.

- Firmware packages: encrypted at rest with KMS-managed keys
- Key rotation: automatic, minimum annually
- Never hardcode encryption keys or secrets in source code
- Environment variables for secrets: use cloud provider's secret manager, not `.env` files in production

### SC-28: Protection of Information at Rest

**Rule:** All stored data must be encrypted at rest.

- DynamoDB/Cosmos DB: server-side encryption enabled (default)
- S3/Blob Storage: AES-256 encryption enabled
- Search indexes: encryption at rest enabled
- Backups: encrypted with same or stronger key

---

## Environment Variable Security

### Rule: No Secrets in Source Control

- `.env` files must be in `.gitignore`
- CI/CD secrets must use the platform's secret manager (GitHub Secrets, AWS Secrets Manager, Azure Key Vault)
- Never commit: API keys, tokens, passwords, connection strings, private keys
- `VITE_*` env vars are embedded in the build — never put secrets in VITE\_ vars

**Violation check:**

```
# .env* must be in .gitignore
grep ".env" .gitignore → must exist
# No secrets in committed files
grep -rn "sk-\|AKIA\|password.*=.*['\"]" src/ → must return 0 results
```

---

## Compliance Checklist for PRs

Every PR touching security-sensitive files must include:

- [ ] NIST control reference in PR description (e.g., "Enforces AC-3")
- [ ] No new `any` types in auth/RBAC code
- [ ] No `dangerouslySetInnerHTML` added
- [ ] No `http://` URLs added (except localhost)
- [ ] Zod schema for any new form or API input
- [ ] RBAC entry for any new page/feature
- [ ] Audit trail preserved for data mutations
- [ ] No tokens/secrets in code or config

### Security-Sensitive Files (extra scrutiny required)

```
src/lib/rbac.ts
src/lib/security.ts
src/lib/auth-context.tsx
src/lib/providers/*/auth-adapter.ts
src/app/components/sign-in.tsx
src/app/components/user-management.tsx
infra/**/cognito* | **/entra* | **/auth*
```
