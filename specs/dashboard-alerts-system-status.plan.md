# Dashboard Recent Alerts and System Status Panels Test Plan

## Application Overview

Test plan for the HLM Inventory Management Platform Dashboard covering two new panels introduced as part of the feature story:

1. Recent Alerts Panel — displays audit log entries from the last 24 hours with severity-based icons (orange warning for failed actions, green check for approved/approve actions, blue info for all others), a "No recent activity" empty state, an error state when the audit log API fails, and a "View all" link navigating to /analytics.

2. System Status Panel — displays health for four services (Deployment Service, Compliance Engine, Asset Database, Analytics Platform) with color-coded status labels (green "Operational" for ≥90%, orange "Degraded" for <90%) and progress bars showing health percentages. Service health is derived from live KPI data: Deployment Service from in-progress orders, Compliance Engine from pending compliance items, Asset Database from average device health score, Analytics Platform always Operational.

Technology stack: Playwright/TestNG Java tests using the Actor/Performable pattern from the novus framework. Tests extend InventoryTestBase which authenticates once per class. Browser-level network interception (browser.route()) is used for deterministic error and loading-state tests.

Key DOM selectors (verified via live inspection):
- Alerts panel container: div.bg-card:has(h3:text-is('Recent Alerts'))
- Alerts items (actual rows): div.bg-card:has(h3:text-is('Recent Alerts')) div.space-y-3 > div.bg-muted
- Alerts loading text: "Loading alerts…" (Unicode ellipsis U+2026)
- Alerts error: div.bg-card:has(h3:text-is('Recent Alerts')) div.bg-red-50
- Alerts empty state: div.text-sm.text-muted-foreground inside div.space-y-3
- System Status container: div.bg-card:has(h3:text-is('System Status'))
- Service rows: div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div
- Operational label: span.text-green-600
- Degraded label: span.text-orange-600
- View all link: expected anchor navigating to /analytics within the alerts panel

## Test Scenarios

### 1. Recent Alerts Panel — Happy Path

**Seed:** `specs/seed.spec.ts`

#### 1.1. TC-ALERTS-01: Recent Alerts panel is visible on the Dashboard after login

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. Extend InventoryTestBase which authenticates once per class in @BeforeClass; the session lands on the Dashboard at https://main.dddsig2mih3hw.amplifyapp.com/
    - expect: The browser is on the Dashboard URL and the h1 Dashboard header is visible
  2. Verify element located by css='div.bg-card:has(h3:text-is("Recent Alerts"))' is visible using Verify.uiElement(AlertsPanel.CONTAINER).isVisible()
    - expect: The Recent Alerts panel container (div.bg-card holding the 'Recent Alerts' h3 heading) is rendered and visible in the right column of the dashboard
  3. Verify the h3 heading inside the panel has exact text 'Recent Alerts' using Verify.uiElement(AlertsPanel.CONTAINER).containsText("Recent Alerts")
    - expect: The heading text 'Recent Alerts' is visible inside the panel
  4. Verify no loading placeholder '—' em-dash remains in the KPI section — all APIs have resolved
    - expect: KpiCard.LOADING_PLACEHOLDER is not visible — dashboard has fully loaded

#### 1.2. TC-ALERTS-02: Recent Alerts panel displays audit log entries from the last 24 hours

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. Set up browser.route("**/graphql", ...) to intercept the listAuditLogs GraphQL query and inject a mock response containing one audit log entry with timestamp within the last 24 hours; all other queries pass through via route.fallback()
    - expect: Route is registered before navigation
  2. Re-navigate to the Dashboard URL via Launch.app() so the mock audit-log response is applied on page load
    - expect: Dashboard loads with the mock audit log data injected
  3. Verify element located by css='div.bg-card:has(h3:text-is("Recent Alerts")) div.space-y-3 > div.bg-muted' is visible using Verify.uiElement(AlertsPanel.ALERT_ITEM).isVisible()
    - expect: At least one alert item row (div.flex.items-start.gap-3.p-3.bg-muted.rounded-lg) is rendered inside the div.space-y-3 wrapper — the injected mock entry is displayed
  4. Verify the 'No recent activity' empty-state text is NOT visible using Verify.uiElement(AlertsPanel.EMPTY_STATE).isNotVisible()
    - expect: Empty-state div.text-sm.text-muted-foreground with 'No recent activity' text is absent — entries exist in the panel
  5. Clean up by calling browser.unrouteAll() in the finally block
    - expect: Routes are cleared for subsequent tests

#### 1.3. TC-ALERTS-03: Each alert item displays action text and timestamp

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. Set up browser.route() to inject a mock listAuditLogs response with one entry: action='Firmware update approved', timestamp within the last 24 hours; all other queries pass through
    - expect: Route registered before navigation
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads with mock audit log entry injected
  3. Verify the alert item row is visible: Verify.uiElement(AlertsPanel.ALERT_ITEM).isVisible()
    - expect: div.bg-muted alert item is present inside div.space-y-3
  4. Verify the alert item contains action text by asserting the panel contains the injected action text: Verify.uiElement(AlertsPanel.CONTAINER).containsText("Firmware update approved")
    - expect: The action text 'Firmware update approved' is visible inside the Recent Alerts panel
  5. Verify a timestamp is displayed within the alert item using browser.evaluate() to check that the div.bg-muted element contains a non-empty time string
    - expect: The timestamp text is rendered alongside the action text in the alert item row
  6. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 1.4. TC-ALERTS-04: Failed actions display an orange warning icon

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. Set up browser.route() to inject a mock listAuditLogs response where the action text contains 'failed', e.g. action='Deployment failed', auditStatus='failed'
    - expect: Route registered before navigation
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads with the failed-action mock entry
  3. Verify the alert item is rendered: Verify.uiElement(AlertsPanel.ALERT_ITEM).isVisible()
    - expect: The alert row for the failed action is present
  4. Verify an orange warning icon SVG is present inside the alert item for the failed action using locator: css='div.bg-card:has(h3:text-is("Recent Alerts")) div.space-y-3 > div.bg-muted svg[class*="text-orange"]' — verify it is visible
    - expect: An SVG icon carrying an orange Tailwind color class (text-orange-*) is rendered inside the failed-action alert row, indicating warning severity
  5. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 1.5. TC-ALERTS-05: Approved/approve actions display a green check icon

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. Set up browser.route() to inject a mock listAuditLogs response with action='Compliance check approved', auditStatus='approved'
    - expect: Route registered
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads with approved-action mock entry
  3. Verify the alert item row is visible: Verify.uiElement(AlertsPanel.ALERT_ITEM).isVisible()
    - expect: Alert row is present for the approved action
  4. Verify a green check icon SVG is present inside the alert item row using locator: css='div.bg-card:has(h3:text-is("Recent Alerts")) div.space-y-3 > div.bg-muted svg[class*="text-green"]' — verify it is visible
    - expect: An SVG icon carrying a green Tailwind color class (text-green-*) is rendered inside the approved-action alert row — indicates CheckCircle icon for approved severity
  5. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 1.6. TC-ALERTS-06: Other (non-failed, non-approved) actions display a blue info icon

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. Set up browser.route() to inject a mock listAuditLogs response with action='Device configuration updated', auditStatus='info'
    - expect: Route registered
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads with info-action mock entry
  3. Verify the alert item row is visible: Verify.uiElement(AlertsPanel.ALERT_ITEM).isVisible()
    - expect: Alert row is present for the info action
  4. Verify a blue info icon SVG is present inside the alert item row using locator: css='div.bg-card:has(h3:text-is("Recent Alerts")) div.space-y-3 > div.bg-muted svg[class*="text-blue"]' — verify it is visible
    - expect: An SVG icon carrying a blue Tailwind color class (text-blue-*) is rendered inside the generic-action alert row — indicates Info icon for informational severity
  5. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 1.7. TC-ALERTS-07: 'View all' link is visible and navigates to the Analytics page (/analytics)

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. On the loaded Dashboard, verify the 'View all' link is present inside the Recent Alerts panel using locator: css='div.bg-card:has(h3:text-is("Recent Alerts")) a[href="/analytics"]' OR LocateBy.withCssText("div.bg-card:has(h3:text-is('Recent Alerts')) a", "View all") — verify it is visible
    - expect: A link with text 'View all' is visible inside the Recent Alerts panel
  2. Click the 'View all' link using Click.on(AlertsPanel.VIEW_ALL_LINK) via user.attemptsTo()
    - expect: The browser navigates away from the Dashboard after the click
  3. Verify the current URL path is /analytics using browser.evaluate('() => window.location.pathname') and assert it equals '/analytics'
    - expect: The URL path is /analytics — the 'View all' link correctly routes to the Analytics/Reporting page

### 2. Recent Alerts Panel — Loading, Empty, and Error States

**Seed:** `specs/seed.spec.ts`

#### 2.1. TC-ALERTS-08: Loading state — 'Loading alerts…' indicator is shown while audit log API is pending

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. Set up browser.route("**/graphql", ...) to delay ALL GraphQL responses by 2000 ms using a background Thread so the loading state is observable before data arrives
    - expect: Route registered — all GraphQL responses will be delayed by 2 seconds
  2. Re-navigate to the Dashboard URL via Launch.app() so the delay route is active during page load
    - expect: Dashboard begins loading; APIs have not yet responded
  3. Immediately verify the loading indicator is visible using Verify.uiElement(AlertsPanel.LOADING_INDICATOR).isVisible() — locator: LocateBy.withCssText("div.bg-card:has(h3:text-is('Recent Alerts'))", "Loading alerts") — the app renders 'Loading alerts…' with Unicode ellipsis U+2026
    - expect: 'Loading alerts…' text (substring 'Loading alerts') is visible inside the Recent Alerts panel while the audit-log API response is delayed
  4. Clean up via browser.unrouteAll() in the finally block
    - expect: Routes cleared

#### 2.2. TC-ALERTS-09: Empty state — 'No recent activity' message when no audit logs exist in last 24 hours

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. Set up browser.route("**/graphql", ...) to intercept listAuditLogs and return an empty items list: data.listAuditLogs.items=[], totalCount=0; all other queries pass through via route.fallback()
    - expect: Route registered — listAuditLogs will return an empty array
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads with an empty audit log response
  3. Verify the 'No recent activity' empty-state text is visible using Verify.uiElement(AlertsPanel.EMPTY_STATE).isVisible() — locator: LocateBy.withCssText("div.bg-card:has(h3:text-is('Recent Alerts'))", "No recent activity")
    - expect: The empty-state message 'No recent activity' is displayed inside the div.space-y-3 wrapper as div.text-sm.text-muted-foreground — NOT a li element
  4. Verify no alert item rows are visible: Verify.uiElement(AlertsPanel.ALERT_ITEM).isNotVisible() — locator scoped to div.bg-muted items only
    - expect: No div.bg-muted alert item rows are rendered — the panel is in empty state
  5. Verify the alerts panel container is still visible — the panel itself is present even with no entries: Verify.uiElement(AlertsPanel.CONTAINER).isVisible()
    - expect: Panel renders its heading and empty state — not a blank space
  6. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 2.3. TC-ALERTS-10: Error state — panel shows div.bg-red-50 error element when audit log API returns HTTP 500

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. Set up browser.route("**/graphql", ...) to intercept only listAuditLogs queries and return HTTP 500 with body {"error":"Audit log service unavailable"}; all other GraphQL queries pass through via route.fallback()
    - expect: Route registered — only the audit-log API will fail
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads; KPI APIs succeed but audit-log API returns 500
  3. Verify the alerts panel error element is visible: Verify.uiElement(AlertsPanel.ERROR_MESSAGE).isVisible() — locator: css='div.bg-card:has(h3:text-is("Recent Alerts")) div.bg-red-50' (a red error box rendered inside the alerts card)
    - expect: The red error box (div.bg-red-50) is rendered inside the Recent Alerts panel card — this is separate from the global KPI error banner at div.p-6.space-y-6 > div.bg-red-50
  4. Verify KPI cards are unaffected and still display live values: Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible()
    - expect: Total Devices KPI card shows a live numeric value — alerts failure does not affect the KPI data domain
  5. Verify the global KPI error banner is NOT visible: Verify.uiElement(ErrorBanner.CONTAINER).isNotVisible() — locator: css='div.p-6.space-y-6 > div.bg-red-50'
    - expect: No global error banner is present — the failure is isolated to the alerts panel only
  6. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 2.4. TC-ALERTS-11: Alerts panel is isolated from KPI API failures and remains visible with its own state

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. Navigate to the Dashboard to establish a fully loaded state: Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible()
    - expect: Dashboard is loaded with live data
  2. Set up browser.route("**/*", ...) to return HTTP 500 for all fetch/xhr resource types while passing all other resource types through via route.fallback()
    - expect: Route registered — KPI APIs will return 500 on next refresh
  3. Click the Refresh Dashboard button via user.attemptsTo(DashboardPageImpl.clickRefreshDashboard()) to re-trigger all API calls with the error route active
    - expect: Refresh button is clicked; all KPI API re-fetch calls receive HTTP 500 responses
  4. Verify the global KPI error banner appears: Verify.uiElement(ErrorBanner.CONTAINER).isVisible()
    - expect: Red error banner (div.p-6.space-y-6 > div.bg-red-50) is visible — KPI APIs failed
  5. Verify the Recent Alerts panel container is still visible: Verify.uiElement(AlertsPanel.CONTAINER).isVisible()
    - expect: The alerts panel is still rendered — it is a separate component from the KPI error banner
  6. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 2.5. TC-ALERTS-12: Alerts panel updates correctly after clicking Refresh Dashboard

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsTests.java`

**Steps:**
  1. Verify the Recent Alerts panel is visible on the loaded Dashboard: Verify.uiElement(AlertsPanel.CONTAINER).isVisible()
    - expect: Panel is present before refresh
  2. Verify the Refresh Dashboard button is visible: Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON).isVisible()
    - expect: button[aria-label='Refresh dashboard'] is rendered in the welcome header row
  3. Click the Refresh Dashboard button: user.attemptsTo(DashboardPageImpl.clickRefreshDashboard())
    - expect: Refresh is triggered; audit-log API is re-fetched along with KPI APIs
  4. Wait for KPI cards to repopulate with byWaitingFor(10): Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible() — confirms refresh cycle completed
    - expect: KPI cards show live values post-refresh
  5. Verify the Recent Alerts panel container is still visible after refresh: Verify.uiElement(AlertsPanel.CONTAINER).isVisible()
    - expect: Alerts panel remains rendered after the refresh cycle — it is not removed or broken by the refresh operation
  6. Verify no global KPI error banner is present post-refresh: Verify.uiElement(ErrorBanner.CONTAINER).isNotVisible()
    - expect: No error banner — refresh completed successfully

### 3. System Status Panel — Happy Path

**Seed:** `specs/seed.spec.ts`

#### 3.1. TC-STATUS-01: System Status panel is visible on the Dashboard after login

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. InventoryTestBase authenticates once per class; the session is on the Dashboard after @BeforeClass
    - expect: Dashboard is loaded at the base URL; h1 Dashboard header is visible
  2. Verify the System Status panel container is visible: Verify.uiElement(SystemStatus.CONTAINER).isVisible() — locator: css='div.bg-card:has(h3:text-is("System Status"))'
    - expect: The System Status panel (div.bg-card holding the 'System Status' h3) is rendered in the Dashboard content area
  3. Verify the 'System Status' heading text is present inside the panel: Verify.uiElement(SystemStatus.CONTAINER).containsText("System Status")
    - expect: The h3 heading 'System Status' is visible inside the panel container

#### 3.2. TC-STATUS-02: System Status panel displays exactly 4 service rows

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Verify the service items are visible in the System Status panel: Verify.uiElement(SystemStatus.SERVICE_ITEM).isVisible() — locator: css='div.bg-card:has(h3:text-is("System Status")) div.space-y-4 > div'
    - expect: At least one service row (direct child div of div.space-y-4 inside the System Status panel) is visible
  2. Use browser.evaluate() to count the number of service rows: document.querySelectorAll selector scoped to the System Status panel — assert the count equals 4
    - expect: Exactly 4 service rows are rendered: Deployment Service, Compliance Engine, Asset Database, Analytics Platform
  3. Verify the panel contains text 'Deployment Service': Verify.uiElement(SystemStatus.CONTAINER).containsText("Deployment Service")
    - expect: 'Deployment Service' text is present in the System Status panel
  4. Verify the panel contains text 'Compliance Engine': Verify.uiElement(SystemStatus.CONTAINER).containsText("Compliance Engine")
    - expect: 'Compliance Engine' text is present
  5. Verify the panel contains text 'Asset Database': Verify.uiElement(SystemStatus.CONTAINER).containsText("Asset Database")
    - expect: 'Asset Database' text is present
  6. Verify the panel contains text 'Analytics Platform': Verify.uiElement(SystemStatus.CONTAINER).containsText("Analytics Platform")
    - expect: 'Analytics Platform' text is present — all four expected service names are rendered

#### 3.3. TC-STATUS-03: Each service row displays a status label — either 'Operational' (green) or 'Degraded' (orange)

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Wait for KPI data to load: Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible() — ensures service-status computation has completed
    - expect: Dashboard is fully loaded with live KPI data before checking status labels
  2. Use browser.evaluate() to count all span.text-green-600 and span.text-orange-600 elements inside the System Status panel — sum should equal 4
    - expect: The total count of green (Operational) + orange (Degraded) status label spans equals 4 — every service row has exactly one status label
  3. Verify at least one status label is present: Verify.uiElement(SystemStatus.OPERATIONAL_LABEL).isVisible() OR Verify.uiElement(SystemStatus.DEGRADED_LABEL).isVisible()
    - expect: At least one span.text-green-600 (Operational) or span.text-orange-600 (Degraded) is visible in the System Status panel

#### 3.4. TC-STATUS-04: Each service row displays a progress bar showing the health percentage

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Wait for Dashboard to fully load: Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible()
    - expect: Dashboard loaded with live KPI data
  2. Use browser.evaluate() to find all progress bar elements inside the System Status panel using candidate selectors: [role='progressbar'], div[class*='progress'], or div[style*='width'] scoped to the System Status panel — assert count is 4
    - expect: 4 progress bar elements (one per service) are present inside the System Status panel
  3. Verify each progress bar has a non-empty width property: use browser.evaluate() to check at least one progress bar element has a computed width > 0
    - expect: At least one progress bar shows a non-zero width — health percentages are computed from live data and reflected as visual progress

#### 3.5. TC-STATUS-05: Analytics Platform service always shows 'Operational' status

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Wait for Dashboard to fully load: Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible()
    - expect: Dashboard is loaded with live KPI data
  2. Verify the Analytics Platform service row contains an 'Operational' green status label using locator: css='div.bg-card:has(h3:text-is("System Status")) div.space-y-4 > div:has-text("Analytics Platform") span.text-green-600' — verify it is visible
    - expect: span.text-green-600 is present inside the Analytics Platform row — always Operational per specification
  3. Verify no orange 'Degraded' label is present in the Analytics Platform row: css='div.bg-card:has(h3:text-is("System Status")) div.space-y-4 > div:has-text("Analytics Platform") span.text-orange-600' — verify it is NOT visible
    - expect: No span.text-orange-600 in the Analytics Platform row — Analytics Platform is never Degraded
  4. Verify the Analytics Platform row contains the text 'Operational'
    - expect: Text 'Operational' is present in the Analytics Platform service row

#### 3.6. TC-STATUS-06: Deployment Service shows 'Operational' when in-progress service orders exist

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Set up browser.route("**/graphql", ...) to intercept listServiceOrdersByStatus for status='In Progress' and inject a non-empty response with one order; all other queries pass through via route.fallback()
    - expect: Route registered — in-progress service orders API will return 1 order
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads with the mock in-progress service order response
  3. Wait for KPI data to settle: Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE).isVisible()
    - expect: Active Deployments KPI shows a value — in-progress orders were processed
  4. Verify the Deployment Service row shows 'Operational' (green): css='div.bg-card:has(h3:text-is("System Status")) div.space-y-4 > div:has-text("Deployment Service") span.text-green-600' — verify it is visible
    - expect: span.text-green-600 is visible in the Deployment Service row — Operational state when in-progress orders exist (health ≥ 90%)
  5. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 3.7. TC-STATUS-07: Deployment Service shows 'Degraded' when no in-progress service orders exist

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Set up browser.route("**/graphql", ...) to intercept listServiceOrdersByStatus with status='In Progress' and return empty items; all other queries pass through
    - expect: Route registered — no in-progress orders will be returned
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads with zero in-progress orders
  3. Wait for KPI data to settle: Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE).isVisible()
    - expect: Active Deployments KPI shows 0 — API responded with empty
  4. Verify the Deployment Service row shows 'Degraded' (orange): css='div.bg-card:has(h3:text-is("System Status")) div.space-y-4 > div:has-text("Deployment Service") span.text-orange-600' — verify it is visible
    - expect: span.text-orange-600 is visible in the Deployment Service row — Degraded state when no in-progress orders exist
  5. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 3.8. TC-STATUS-08: Compliance Engine shows 'Operational' when no pending compliance items exist

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Set up browser.route("**/graphql", ...) to intercept the pending compliance query and return zero pending items; all other queries pass through
    - expect: Route registered — no pending compliance items
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads with zero pending compliance items
  3. Wait for KPI data: Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE).isVisible()
    - expect: Pending Approvals KPI shows a value
  4. Verify the Compliance Engine row shows 'Operational' (green): css='div.bg-card:has(h3:text-is("System Status")) div.space-y-4 > div:has-text("Compliance Engine") span.text-green-600' — verify it is visible
    - expect: span.text-green-600 is visible in the Compliance Engine row — Operational when no pending compliance items exist
  5. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 3.9. TC-STATUS-09: Compliance Engine shows 'Degraded' when pending compliance items exist

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Set up browser.route("**/graphql", ...) to intercept the pending compliance query and return non-empty items with one pending item; all other queries pass through
    - expect: Route registered — 1 pending compliance item returned
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads with pending compliance items present
  3. Wait for KPI data: Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE).isVisible()
    - expect: Pending Approvals KPI is populated
  4. Verify the Compliance Engine row shows 'Degraded' (orange): css='div.bg-card:has(h3:text-is("System Status")) div.space-y-4 > div:has-text("Compliance Engine") span.text-orange-600' — verify it is visible
    - expect: span.text-orange-600 is visible in the Compliance Engine row — Degraded when pending compliance items exist
  5. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 3.10. TC-STATUS-10: Asset Database status reflects average device health score (Operational ≥90%, Degraded <90%)

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Set up browser.route("**/graphql", ...) to intercept listDevices and inject devices with high health scores (healthScore=95 and healthScore=98, average=96.5%); all other queries pass through
    - expect: Route registered — devices with health scores above 90 are returned
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads with high-health device data
  3. Wait for Health Score KPI to reflect the mock data: Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE).containsText("%")
    - expect: Health Score KPI shows a percentage — device health data was processed
  4. Verify the Asset Database row shows 'Operational' (green): css='div.bg-card:has(h3:text-is("System Status")) div.space-y-4 > div:has-text("Asset Database") span.text-green-600' — verify it is visible
    - expect: span.text-green-600 is visible in the Asset Database row — Operational when average health score ≥ 90%
  5. Update the route to inject devices with low health scores (healthScore=50, average=50%); click Refresh Dashboard to re-trigger via user.attemptsTo(DashboardPageImpl.clickRefreshDashboard())
    - expect: Route updated and refresh triggered with low-health device data
  6. Verify the Asset Database row shows 'Degraded' (orange): css='div.bg-card:has(h3:text-is("System Status")) div.space-y-4 > div:has-text("Asset Database") span.text-orange-600' — verify it is visible
    - expect: span.text-orange-600 is visible in the Asset Database row — Degraded when average health score < 90%
  7. Clean up via browser.unrouteAll()
    - expect: Routes cleared

### 4. System Status Panel — Error Handling and Edge Cases

**Seed:** `specs/seed.spec.ts`

#### 4.1. TC-STATUS-11: System Status panel remains visible when KPI error banner appears

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Navigate to the Dashboard and confirm it is fully loaded: Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible()
    - expect: Dashboard fully loaded
  2. Set up browser.route("**/*", ...) to return HTTP 500 for all fetch/xhr calls to simulate all KPI API failures
    - expect: Route registered
  3. Click Refresh Dashboard: user.attemptsTo(DashboardPageImpl.clickRefreshDashboard())
    - expect: Refresh re-triggers all API calls which now return 500
  4. Verify the global error banner appears: Verify.uiElement(ErrorBanner.CONTAINER).isVisible()
    - expect: Red error banner (div.p-6.space-y-6 > div.bg-red-50) is visible
  5. Verify the System Status panel container is still visible: Verify.uiElement(SystemStatus.CONTAINER).isVisible()
    - expect: System Status panel is still rendered — the panel does not disappear when KPI APIs fail
  6. Clean up via browser.unrouteAll()
    - expect: Routes cleared

#### 4.2. TC-STATUS-12: System Status panel updates correctly after clicking Refresh Dashboard

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Verify the System Status panel is visible: Verify.uiElement(SystemStatus.CONTAINER).isVisible()
    - expect: Panel is present before refresh
  2. Click the Refresh Dashboard button: user.attemptsTo(DashboardPageImpl.clickRefreshDashboard())
    - expect: Refresh is triggered — all KPI APIs are re-fetched; system status is recomputed from fresh data
  3. Wait for KPI data to repopulate with byWaitingFor(10): Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible()
    - expect: KPI cards show live values post-refresh
  4. Verify the System Status panel container is still visible after refresh: Verify.uiElement(SystemStatus.CONTAINER).isVisible()
    - expect: System Status panel is rendered after refresh — it updates from the refreshed KPI data
  5. Verify at least one status label is still visible post-refresh
    - expect: Status labels are re-rendered from the refreshed data — the panel is not stuck in a broken state

#### 4.3. TC-STATUS-13: Service rows maintain correct ordering (Deployment, Compliance, Asset Database, Analytics)

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Wait for Dashboard to fully load: Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible()
    - expect: Dashboard is loaded
  2. Use browser.evaluate() to extract the text content of all direct child divs of div.space-y-4 inside the System Status panel and assert the service names array equals Deployment Service, Compliance Engine, Asset Database, Analytics Platform in that order
    - expect: The four service rows appear in the correct order as specified
  3. Additionally verify the first row contains 'Deployment Service' text and the fourth row contains 'Analytics Platform' text using scoped CSS selectors with nth-child pseudo-selectors
    - expect: Row ordering is deterministic: Deployment Service is row 1, Analytics Platform is row 4

#### 4.4. TC-STATUS-14: Operational label uses green (text-green-600), Degraded label uses orange (text-orange-600)

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardSystemStatusTests.java`

**Steps:**
  1. Wait for Dashboard to fully load: Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible()
    - expect: Dashboard loaded
  2. If at least one service is Operational: Verify.uiElement(SystemStatus.OPERATIONAL_LABEL).isVisible() — locator: css='div.bg-card:has(h3:text-is("System Status")) span.text-green-600'
    - expect: An element with CSS class text-green-600 is present in the System Status panel — this is the Operational label color
  3. Use browser.evaluate() to get the className of the first Operational label span and assert it includes 'text-green-600'
    - expect: The className of the Operational status span includes 'text-green-600' — correct green color is applied
  4. Set up a route returning zero in-progress orders to force Deployment Service Degraded state; re-navigate; then verify css='div.bg-card:has(h3:text-is("System Status")) div.space-y-4 > div:has-text("Deployment Service") span.text-orange-600' is visible
    - expect: The Degraded status span inside the Deployment Service row has class text-orange-600 — orange color correctly applied for Degraded state
  5. Clean up via browser.unrouteAll()
    - expect: Routes cleared

### 5. Integration — Both Panels Together and Navigation

**Seed:** `specs/seed.spec.ts`

#### 5.1. TC-INTEGRATION-01: Both Recent Alerts and System Status panels are visible simultaneously on the Dashboard

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsSystemStatusIntegrationTests.java`

**Steps:**
  1. InventoryTestBase authenticates; session is on the Dashboard after @BeforeClass
    - expect: Authenticated on Dashboard
  2. Verify the Recent Alerts panel container is visible: Verify.uiElement(AlertsPanel.CONTAINER).isVisible()
    - expect: Recent Alerts panel (div.bg-card:has(h3:text-is('Recent Alerts'))) is rendered
  3. Verify the System Status panel container is visible: Verify.uiElement(SystemStatus.CONTAINER).isVisible()
    - expect: System Status panel (div.bg-card:has(h3:text-is('System Status'))) is rendered
  4. Verify the KPI cards grid is also visible to confirm both panels coexist with existing content: Verify.uiElement(KpiCard.KPI_GRID_CONTAINER).isVisible()
    - expect: The KPI card grid, Recent Alerts panel, and System Status panel all coexist on the same Dashboard page without layout breakage

#### 5.2. TC-INTEGRATION-02: 'View all' link navigates to /analytics and back-navigation to Dashboard works

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsSystemStatusIntegrationTests.java`

**Steps:**
  1. Verify the 'View all' link is present in the Recent Alerts panel using locator: css='div.bg-card:has(h3:text-is("Recent Alerts")) a[href="/analytics"]' OR LocateBy.withCssText("div.bg-card:has(h3:text-is('Recent Alerts')) a", "View all") — verify it is visible
    - expect: A 'View all' anchor element is visible inside the Recent Alerts panel
  2. Click the 'View all' link: user.attemptsTo(Click.on(AlertsPanel.VIEW_ALL_LINK))
    - expect: Browser navigates away from the Dashboard
  3. Verify the URL path is /analytics: use browser.evaluate('() => window.location.pathname') and assert it equals '/analytics'
    - expect: URL path is /analytics — 'View all' routes correctly to the Analytics page
  4. Navigate back to the Dashboard using the Dashboard sidebar nav link or Launch.app()
    - expect: Browser returns to the Dashboard URL
  5. Verify the Dashboard h1 header is visible: Verify.uiElement(DashboardPage.DASHBOARD_HEADER).isVisible()
    - expect: Dashboard page loads correctly after returning from the Analytics page

#### 5.3. TC-INTEGRATION-03: Both panels render correctly after Refresh Dashboard button click

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsSystemStatusIntegrationTests.java`

**Steps:**
  1. Verify all three content sections are loaded: Verify.uiElement(AlertsPanel.CONTAINER).isVisible(), Verify.uiElement(SystemStatus.CONTAINER).isVisible(), Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible()
    - expect: Dashboard is fully loaded with all three content sections present
  2. Click the Refresh Dashboard button: user.attemptsTo(DashboardPageImpl.clickRefreshDashboard())
    - expect: Refresh triggered; all APIs including KPI, alerts, and service-status sources are re-fetched
  3. Wait for KPI data to repopulate with byWaitingFor(15): Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible()
    - expect: KPI cards show live values — refresh cycle completed
  4. Verify the Recent Alerts panel is still visible post-refresh: Verify.uiElement(AlertsPanel.CONTAINER).isVisible()
    - expect: Alerts panel is still rendered after refresh
  5. Verify the System Status panel is still visible post-refresh: Verify.uiElement(SystemStatus.CONTAINER).isVisible()
    - expect: System Status panel is still rendered after refresh
  6. Verify no error banner is present after a successful refresh: Verify.uiElement(ErrorBanner.CONTAINER).isNotVisible()
    - expect: No global error banner — refresh completed without failures

#### 5.4. TC-INTEGRATION-04: Zero data state — both panels render gracefully when all APIs return empty results

**File:** `novus-example-tests/src/test/java/com/tpg/automation/inventory/DashboardAlertsSystemStatusIntegrationTests.java`

**Steps:**
  1. Set up browser.route("**/graphql", ...) to return empty items for ALL GraphQL queries including listAuditLogs, listDevices, listServiceOrdersByStatus, and compliance queries; all return {items:[], totalCount:0}
    - expect: Route registered — all API endpoints return empty data sets
  2. Re-navigate to the Dashboard via Launch.app()
    - expect: Dashboard loads with zero data from all APIs
  3. Verify KPI cards show '0' values: Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).containsText("0")
    - expect: Total Devices KPI shows 0
  4. Verify the 'No recent activity' empty state is shown in the Alerts panel: Verify.uiElement(AlertsPanel.EMPTY_STATE).isVisible()
    - expect: 'No recent activity' message is displayed inside div.space-y-3 — empty audit log response handled gracefully
  5. Verify the System Status panel container is still visible: Verify.uiElement(SystemStatus.CONTAINER).isVisible()
    - expect: System Status panel is still rendered with zero-data
  6. Verify no global error banner is shown: Verify.uiElement(ErrorBanner.CONTAINER).isNotVisible()
    - expect: No red error banner — empty data is not treated as an error, app handles it gracefully
  7. Clean up via browser.unrouteAll()
    - expect: Routes cleared
