# HLM Platform Dashboard — PS-7 E2E Integration Test Plan

## Application Overview

The HLM (Sungrow/HLM) Inventory Management Platform is a React + AWS AppSync application hosted at https://main.dddsig2mih3hw.amplifyapp.com. After login, users land on a Dashboard that aggregates live data from six parallel GraphQL API calls into four KPI cards (Total Devices, Active Deployments, Pending Approvals, Health Score), a Recent Alerts panel populated from a 24-hour audit-log window, four Quick Action cards with live badge counts, a System Status panel tracking four services, and a Manual Refresh button. The UI is built with Tailwind CSS and uses no custom data-* attributes; locators rely on structural CSS class selectors (e.g. div.bg-card, span.bg-orange-500). This test plan covers all seven E2E Integration Scenarios defined in JIRA Story PS-7, mapping directly to acceptance criteria documented across sub-tasks PS-14, PS-30, and PS-31.

## Test Scenarios

### 1. PS-7 Scenario 1 — KPI Data Loading (Parallel API Calls on Page Load)

**Seed:** `specs/seed.spec.ts`

#### 1.1. TC-PS7-KPI-01: All four KPI cards populate from parallel API calls on dashboard page load

**File:** `tests/ps7/kpi-data-loading.spec.ts`

**Steps:**
  1. Open a fresh browser session and navigate to https://main.dddsig2mih3hw.amplifyapp.com
    - expect: The login page is displayed with email and password fields
  2. Enter username 'ajaykumar.yadav@3pillarglobal.com' and password 'Secure@12345', then click the Sign In button
    - expect: Authentication succeeds and the browser navigates to the Dashboard at the root URL path '/'
  3. Attach a network request listener before navigating to the dashboard to capture all outgoing GraphQL calls
    - expect: Listener is registered; no requests observed yet
  4. Reload the Dashboard page and observe network traffic during page initialisation
    - expect: Six distinct GraphQL queries are dispatched in parallel within the same event loop tick: listDevices (all), listDevices (status=Offline), listServiceOrdersByStatus (In Progress), listServiceOrdersByStatus (Scheduled), listFirmware (status=Pending), listFirmwareCompliance (status=Pending)
  5. Wait for the em-dash loading placeholder '—' (U+2014) to disappear from all KPI card value elements (div[class*='text-3xl'])
    - expect: The '—' placeholder is no longer visible in any KPI card - all six API calls have resolved
  6. Assert that the 'Total Devices' KPI card (div.bg-card containing label 'Total Devices') displays a non-negative integer value
    - expect: Total Devices card shows a numeric value >= 0 (derived from total device count returned by listDevices)
  7. Assert that the 'Active Deployments' KPI card displays a non-negative integer value
    - expect: Active Deployments card shows a numeric value >= 0 (count of In Progress service orders)
  8. Assert that the 'Pending Approvals' KPI card displays a non-negative integer value
    - expect: Pending Approvals card shows a numeric value >= 0 (sum of pending firmware + pending compliance records)
  9. Assert that the 'Health Score' KPI card displays a non-negative integer or percentage value
    - expect: Health Score card shows a numeric value >= 0 (average health score computed from device records, rounded to integer)
  10. Verify no red error banner (div.p-6.space-y-6 > div.bg-red-50) is present in the main content area
    - expect: Error banner is absent - all six API calls completed without error

#### 1.2. TC-PS7-KPI-02: KPI values match independent API query results (data accuracy)

**File:** `tests/ps7/kpi-data-loading.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is fully loaded and all KPI cards are populated
  2. Execute an independent GraphQL query for listDevices (no filter) and record the totalCount from the API response
    - expect: API returns a totalCount integer
  3. Read the text content of the 'Total Devices' KPI card value element via browser evaluate
    - expect: The displayed Total Devices value equals the totalCount returned by the independent listDevices query
  4. Execute an independent GraphQL query for listServiceOrdersByStatus (status: 'In Progress') and record the totalCount
    - expect: API returns a totalCount integer
  5. Read the text content of the 'Active Deployments' KPI card value element
    - expect: The displayed Active Deployments value equals the totalCount returned by listServiceOrdersByStatus for In Progress orders
  6. Execute independent queries for listFirmware (status: 'Pending') and listFirmwareCompliance (status: 'Pending') and sum their totalCount values
    - expect: Combined pending count is computed from both API responses
  7. Read the text content of the 'Pending Approvals' KPI card value element
    - expect: The displayed Pending Approvals value equals the sum of pending firmware + pending compliance counts

#### 1.3. TC-PS7-KPI-03: KPI cards show em-dash loading placeholder during API fetch delay

**File:** `tests/ps7/kpi-data-loading.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard page starts loading
  2. Intercept all AppSync GraphQL requests (URL pattern: **appsync-api**) and introduce a 2-second artificial delay before passing through
    - expect: All GraphQL responses are delayed by 2 seconds
  3. Reload the Dashboard with the delay route active and immediately check for the loading placeholder
    - expect: At least one KPI card value element (div[class*='text-3xl']) shows the em-dash character '—' (U+2014) confirming the loading state is rendered before data arrives
  4. Wait for the 2-second delay to expire and the APIs to respond then check the placeholder state again
    - expect: The '—' placeholder is replaced by numeric values in all four KPI cards - the loading-to-data transition completes correctly
  5. Remove all network route intercepts via unrouteAll()
    - expect: Routes are cleared; subsequent navigations load normally

### 2. PS-7 Scenario 2 — Alerts from Audit Logs (24-Hour Window)

**Seed:** `specs/seed.spec.ts`

#### 2.1. TC-PS7-ALERTS-01: Recent Alerts panel displays entries from a 24-hour audit log window

**File:** `tests/ps7/alerts-audit-logs.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is fully loaded
  2. Intercept the listAuditLogs GraphQL request and inject a mock response containing exactly three audit log entries with timestamps within the last 24 hours: entry 1 - action='Firmware update approved', auditStatus='approved'; entry 2 - action='Deployment failed', auditStatus='failed'; entry 3 - action='Device configuration updated', auditStatus='info'
    - expect: Route is registered; the mock response will be served to all listAuditLogs queries
  3. Reload the Dashboard with the mock audit-log route active
    - expect: Dashboard loads; the Recent Alerts panel receives the injected 3-entry response
  4. Locate the Recent Alerts panel container (div.bg-card:has(h3:text-is('Recent Alerts'))) and assert it is visible
    - expect: Panel container is rendered and visible in the right column of the Dashboard
  5. Count the number of alert item rows (div.bg-card:has(h3:text-is('Recent Alerts')) div.space-y-3 > div.bg-muted) in the panel
    - expect: Exactly 3 alert item rows are rendered matching the 3-entry mock response
  6. Verify that the 'No recent activity' empty-state text is NOT visible in the panel
    - expect: Empty-state message is absent - the panel correctly shows entries rather than the empty state
  7. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 2.2. TC-PS7-ALERTS-02: Recent Alerts panel shows empty state when no audit logs exist in the last 24 hours

**File:** `tests/ps7/alerts-audit-logs.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is fully loaded
  2. Intercept the listAuditLogs GraphQL request and inject a mock response with an empty items array: {data:{listAuditLogs:{items:[],nextToken:null}}}
    - expect: Route is registered to return zero audit log entries
  3. Reload the Dashboard with the empty-log route active
    - expect: Dashboard loads; the Recent Alerts panel receives the empty response
  4. Assert that the 'No recent activity' text is visible inside the Recent Alerts panel
    - expect: 'No recent activity' empty-state message is displayed inside div.text-sm.text-muted-foreground within div.space-y-3
  5. Assert that no alert item rows (div.bg-muted) are rendered inside div.space-y-3
    - expect: Zero div.bg-muted item rows are present - the empty state is correctly shown without ghost item rows
  6. Assert that the Recent Alerts panel container itself is still visible
    - expect: Panel heading 'Recent Alerts' and the empty-state container remain rendered - the panel does not disappear when there are no logs
  7. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 2.3. TC-PS7-ALERTS-03: Recent Alerts 'View all' link navigates to /analytics

**File:** `tests/ps7/alerts-audit-logs.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is fully loaded with the Recent Alerts panel visible
  2. Locate the 'View all' anchor element (a[href='/analytics']) inside the Recent Alerts panel and assert it is visible
    - expect: 'View all' link is rendered in the panel header row
  3. Click the 'View all' link
    - expect: Browser initiates navigation
  4. Read the current URL path via window.location.pathname
    - expect: URL path is '/analytics' - the link navigates to the Reporting and Analytics module

### 3. PS-7 Scenario 3 — Alert Severity Icons (Failed Entries Render Warning Icon)

**Seed:** `specs/seed.spec.ts`

#### 3.1. TC-PS7-ICON-01: Failed audit log entry renders an orange warning SVG icon

**File:** `tests/ps7/alert-severity-icons.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is fully loaded
  2. Intercept the listAuditLogs GraphQL request and inject a mock response with a single failed entry: action='Deployment failed', auditStatus='failed', timestamp within the last 24 hours
    - expect: Route is registered; the mock failed-status entry will be served
  3. Reload the Dashboard with the failed-entry route active
    - expect: Dashboard loads; the Recent Alerts panel receives the single failed-entry response
  4. Assert that exactly one alert item row (div.bg-muted) is visible in the panel
    - expect: One row is rendered for the injected failed entry
  5. Assert that an SVG element with a Tailwind orange color class is present inside the alert item row using selector: div.bg-card:has(h3:text-is('Recent Alerts')) div.space-y-3 > div.bg-muted svg[class*='text-orange']
    - expect: Orange warning icon is visible - confirms the app maps auditStatus='failed' to the orange icon variant
  6. Assert that NO green SVG icon (svg[class*='text-green']) and NO blue SVG icon (svg[class*='text-blue']) are present in the same item row
    - expect: Only the orange icon is rendered - icon colour is exclusive to the severity level
  7. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 3.2. TC-PS7-ICON-02: Approved audit log entry renders a green check SVG icon

**File:** `tests/ps7/alert-severity-icons.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is fully loaded
  2. Intercept the listAuditLogs GraphQL request and inject a mock response with a single approved entry: action='Firmware update approved', auditStatus='approved', timestamp within the last 24 hours
    - expect: Route is registered; the mock approved-status entry will be served
  3. Reload the Dashboard with the approved-entry route active
    - expect: Dashboard loads; the Recent Alerts panel receives the approved entry
  4. Assert that an SVG element with a Tailwind green color class (svg[class*='text-green']) is visible inside the alert item row
    - expect: Green check icon is visible - confirms the app maps auditStatus='approved' to the green icon variant
  5. Assert that no orange icon (svg[class*='text-orange']) is present in the same item row
    - expect: Orange icon is absent - approved entries must not render the warning icon
  6. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 3.3. TC-PS7-ICON-03: Generic (info) audit log entry renders a blue info SVG icon

**File:** `tests/ps7/alert-severity-icons.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is fully loaded
  2. Intercept the listAuditLogs GraphQL request and inject a mock response with a single info entry: action='Device configuration updated', auditStatus='info', timestamp within the last 24 hours
    - expect: Route is registered; the mock info-status entry will be served
  3. Reload the Dashboard with the info-entry route active
    - expect: Dashboard loads; the Recent Alerts panel receives the info entry
  4. Assert that an SVG element with a Tailwind blue color class (svg[class*='text-blue']) is visible inside the alert item row
    - expect: Blue info icon is visible - confirms the app maps auditStatus='info' to the blue icon variant
  5. Assert that no orange or green icon is present in the same item row
    - expect: Only the blue icon is rendered - icon colour is exclusive per severity level
  6. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 3.4. TC-PS7-ICON-04: Each alert item row displays action text and a timestamp

**File:** `tests/ps7/alert-severity-icons.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard; inject a mock listAuditLogs response with one approved entry having action='Firmware update approved' and a recent timestamp
    - expect: Dashboard loads with the single approved entry visible in the panel
  2. Assert that the text 'Firmware update approved' is present within the Recent Alerts panel container
    - expect: Action text is rendered inside the alert item row
  3. Read the full text content of the alert item row element via browser.evaluate and verify it is non-empty and contains a time reference
    - expect: Item text content is non-empty and includes a recognisable time string confirming the timestamp is rendered alongside the action text
  4. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

### 4. PS-7 Scenario 4 — Quick Action Badge Counts (Live Data)

**Seed:** `specs/seed.spec.ts`

#### 4.1. TC-PS7-BADGE-01: All four Quick Action cards are rendered with labels icons and live badge counts

**File:** `tests/ps7/quick-action-badges.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is fully loaded; Quick Actions section is visible below KPI cards
  2. Assert the 'View Inventory' quick action card (main a[href='/inventory']) is visible in the main content area, not the sidebar
    - expect: 'View Inventory' card is rendered in the Dashboard main content grid, separate from the sidebar nav link
  3. Assert the 'Schedule Service' quick action card (main a[href='/account-service']) is visible in the main content area
    - expect: 'Schedule Service' card is rendered in the Dashboard main content grid
  4. Assert the 'Deploy Firmware' quick action card (a[href='/deployment'].relative.bg-card) is visible
    - expect: 'Deploy Firmware' card is rendered - disambiguated from the sidebar nav link by the .relative.bg-card class pair
  5. Assert the 'Check Compliance' quick action card (main a[href='/compliance']) is visible in the main content area
    - expect: 'Check Compliance' card is rendered in the Dashboard main content grid
  6. Use browser.evaluate to count the total number of Quick Action card elements and assert the count is exactly 4
    - expect: Exactly 4 Quick Action cards are present in the main content area - no duplicates, no missing cards
  7. Assert that each card displays a visible SVG icon element
    - expect: All four cards have SVG icons visible

#### 4.2. TC-PS7-BADGE-02: View Inventory card displays correct offline device badge count

**File:** `tests/ps7/quick-action-badges.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is loaded
  2. Intercept the listDevices (status=Offline) GraphQL query and inject a mock response with 3 offline devices
    - expect: Route is registered; 3 offline devices will be returned
  3. Reload the Dashboard with the mock offline-devices route active
    - expect: Dashboard loads; the offline device count API returns 3
  4. Locate the orange badge span on the 'View Inventory' card using selector main a[href='/inventory'] span.bg-orange-500 and assert it is visible
    - expect: Orange badge is rendered on the 'View Inventory' card
  5. Read the text content of the badge span and assert it equals '3'
    - expect: Badge displays '3' matching the 3 offline devices returned by the mock API
  6. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 4.3. TC-PS7-BADGE-03: Schedule Service card displays correct scheduled orders badge count

**File:** `tests/ps7/quick-action-badges.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is loaded
  2. Intercept the listServiceOrdersByStatus (status=Scheduled) GraphQL query and inject a mock response with 5 scheduled orders
    - expect: Route is registered; 5 scheduled orders will be returned
  3. Reload the Dashboard with the mock scheduled-orders route active
    - expect: Dashboard loads; the scheduled orders count API returns 5
  4. Locate the orange badge span on the 'Schedule Service' card using selector main a[href='/account-service'] span.bg-orange-500 and assert it is visible
    - expect: Orange badge is rendered on the 'Schedule Service' card
  5. Read the text content of the badge span and assert it equals '5'
    - expect: Badge displays '5' matching the 5 scheduled orders returned by the mock API
  6. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 4.4. TC-PS7-BADGE-04: Deploy Firmware card displays correct pending firmware badge count

**File:** `tests/ps7/quick-action-badges.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is loaded
  2. Intercept the listFirmware (status=Pending) GraphQL query and inject a mock response with 2 pending firmware records
    - expect: Route is registered; 2 pending firmware records will be returned
  3. Reload the Dashboard with the mock pending-firmware route active
    - expect: Dashboard loads; the pending firmware count API returns 2
  4. Locate the orange badge span on the 'Deploy Firmware' card using selector a[href='/deployment'].relative.bg-card span.bg-orange-500 and assert it is visible
    - expect: Orange badge is rendered on the 'Deploy Firmware' card
  5. Read the text content of the badge span and assert it equals '2'
    - expect: Badge displays '2' matching the 2 pending firmware records returned by the mock API
  6. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 4.5. TC-PS7-BADGE-05: Check Compliance card displays correct pending compliance badge count

**File:** `tests/ps7/quick-action-badges.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is loaded
  2. Intercept the listFirmwareCompliance (status=Pending) GraphQL query and inject a mock response with 4 pending compliance records
    - expect: Route is registered; 4 pending compliance records will be returned
  3. Reload the Dashboard with the mock pending-compliance route active
    - expect: Dashboard loads; the pending compliance count API returns 4
  4. Locate the orange badge span on the 'Check Compliance' card using selector main a[href='/compliance'] span.bg-orange-500 and assert it is visible
    - expect: Orange badge is rendered on the 'Check Compliance' card
  5. Read the text content of the badge span and assert it equals '4'
    - expect: Badge displays '4' matching the 4 pending compliance records returned by the mock API
  6. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 4.6. TC-PS7-BADGE-06: Quick Action cards navigate to correct module routes when clicked

**File:** `tests/ps7/quick-action-badges.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard; reload to ensure clean state
    - expect: Dashboard is fully loaded
  2. Click the 'View Inventory' quick action card (main a[href='/inventory'])
    - expect: Browser navigates to the /inventory route and the Inventory page header is visible
  3. Navigate back to the Dashboard by reloading the base URL
    - expect: Dashboard is displayed again
  4. Click the 'Schedule Service' quick action card (main a[href='/account-service'])
    - expect: Browser navigates to the /account-service route
  5. Navigate back to the Dashboard
    - expect: Dashboard is displayed again
  6. Click the 'Deploy Firmware' quick action card (a[href='/deployment'].relative.bg-card)
    - expect: Browser navigates to the /deployment route
  7. Navigate back to the Dashboard
    - expect: Dashboard is displayed again
  8. Click the 'Check Compliance' quick action card (main a[href='/compliance'])
    - expect: Browser navigates to the /compliance route

### 5. PS-7 Scenario 5 — System Status Toggle (Deployment Service Operational vs Degraded)

**Seed:** `specs/seed.spec.ts`

#### 5.1. TC-PS7-STATUS-01: Deployment Service shows Operational (green) when active In Progress orders exist

**File:** `tests/ps7/system-status-toggle.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is loaded
  2. Intercept AppSync GraphQL requests. For listServiceOrdersByStatus queries with status='In Progress', return a mock response with one In Progress order. All other queries pass through.
    - expect: Route is registered; In Progress orders mock will return 1 order
  3. Reload the Dashboard with the mock route active
    - expect: Dashboard loads; the Active Deployments KPI API returns 1 In Progress order
  4. Locate the System Status panel (div.bg-card:has(h3:text-is('System Status'))) and assert it is visible
    - expect: System Status panel is rendered and visible in the Dashboard content area
  5. Assert that the Deployment Service row inside the System Status panel contains a green 'Operational' label using selector: div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div:has-text('Deployment Service') span.text-green-600
    - expect: Deployment Service shows 'Operational' in green confirming active In Progress orders produce an Operational status
  6. Assert that no orange 'Degraded' label (span.text-orange-600) is present in the Deployment Service row
    - expect: No Degraded label is rendered for Deployment Service when active orders exist
  7. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 5.2. TC-PS7-STATUS-02: Deployment Service shows Degraded (orange) when no active orders exist

**File:** `tests/ps7/system-status-toggle.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is loaded
  2. Intercept AppSync GraphQL requests. For listServiceOrdersByStatus queries with status='In Progress', return an empty response with items:[]. All other queries pass through.
    - expect: Route is registered; In Progress orders mock will return 0 orders
  3. Reload the Dashboard with the zero-orders route active
    - expect: Dashboard loads; the Active Deployments KPI API returns 0 In Progress orders
  4. Locate the Deployment Service row inside the System Status panel
    - expect: Row is visible within div.bg-card:has(h3:text-is('System Status')) div.space-y-4
  5. Assert that an orange 'Degraded' label (span.text-orange-600) is present in the Deployment Service row
    - expect: Deployment Service shows 'Degraded' in orange confirming zero active orders trigger the Degraded status
  6. Assert that no green 'Operational' label (span.text-green-600) is present in the Deployment Service row
    - expect: No Operational label is rendered - status is exclusively Degraded when no active orders exist
  7. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 5.3. TC-PS7-STATUS-03: System Status panel renders exactly 4 service rows in correct fixed order

**File:** `tests/ps7/system-status-toggle.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard; wait for the System Status panel to be fully populated
    - expect: Dashboard and System Status panel are loaded
  2. Use browser.evaluate to count all direct child div elements of div.space-y-4 inside the System Status panel
    - expect: Exactly 4 service row divs are present
  3. Assert the panel contains the text 'Deployment Service'
    - expect: 'Deployment Service' row is present
  4. Assert the panel contains the text 'Compliance Engine'
    - expect: 'Compliance Engine' row is present
  5. Assert the panel contains the text 'Asset Database'
    - expect: 'Asset Database' row is present
  6. Assert the panel contains the text 'Analytics Platform'
    - expect: 'Analytics Platform' row is present
  7. Assert the Analytics Platform row always shows a green 'Operational' label per the product specification
    - expect: Analytics Platform is always Operational - it does not have a Degraded state per the product spec

#### 5.4. TC-PS7-STATUS-04: Compliance Engine shows Degraded when pending compliance items exist

**File:** `tests/ps7/system-status-toggle.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is loaded
  2. Intercept AppSync GraphQL requests. For listFirmwareCompliance queries, return a mock response with at least one item with status='Pending'. All other queries pass through.
    - expect: Route is registered; pending compliance mock will return at least 1 pending item
  3. Reload the Dashboard with the mock route active
    - expect: Dashboard loads; the Pending Approvals KPI API receives the mock pending compliance data
  4. Assert that the Compliance Engine row inside the System Status panel contains an orange 'Degraded' label (span.text-orange-600)
    - expect: Compliance Engine shows 'Degraded' in orange when pending compliance items are present
  5. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

### 6. PS-7 Scenario 6 — Manual Refresh (Re-triggers All API Data Fetches)

**Seed:** `specs/seed.spec.ts`

#### 6.1. TC-PS7-REFRESH-01: Refresh Dashboard button is visible and accessible on the Dashboard page

**File:** `tests/ps7/manual-refresh.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is fully loaded with all KPI cards populated
  2. Locate the Refresh Dashboard button using selector button[aria-label='Refresh dashboard'] and assert it is visible in the welcome header row
    - expect: Refresh button is rendered in the top-right area of the welcome header row, identified by aria-label='Refresh dashboard'
  3. Assert that the Refresh Dashboard button is enabled and not disabled
    - expect: Button is in an enabled state and ready to be clicked

#### 6.2. TC-PS7-REFRESH-02: Clicking the Refresh button re-triggers all six parallel API calls and KPI cards repopulate

**File:** `tests/ps7/manual-refresh.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard; wait for full load
    - expect: Dashboard is fully loaded with all KPI cards showing numeric values
  2. Click the Refresh Dashboard button (button[aria-label='Refresh dashboard'])
    - expect: Button is clicked; the refresh cycle begins
  3. Wait for the KPI loading placeholder to appear in at least one KPI card then wait for it to disappear again
    - expect: The loading-to-data transition completes: KPI cards show refreshed numeric values
  4. Assert that the Total Devices, Active Deployments, Pending Approvals, and Health Score KPI card values are all visible with non-placeholder content
    - expect: All four KPI cards show populated values after the refresh cycle completes
  5. Assert that the Recent Alerts panel container is still visible and not in an error state
    - expect: Alerts panel is present and stable after refresh
  6. Assert that no red error banner (div.p-6.space-y-6 > div.bg-red-50) is present after the refresh
    - expect: No error banner - all API calls triggered by the refresh responded successfully

#### 6.3. TC-PS7-REFRESH-03: Refresh button SVG icon applies animate-spin class during an active refresh cycle

**File:** `tests/ps7/manual-refresh.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is fully loaded
  2. Intercept all AppSync GraphQL requests and introduce a 2-second artificial delay to make the refresh loading state observable
    - expect: Route is registered; all GraphQL responses will be delayed by 2 seconds
  3. Click the Refresh Dashboard button
    - expect: Button is clicked; refresh cycle starts; API calls are delayed
  4. Immediately after clicking, read the class attribute of the SVG element inside the Refresh button using selector button[aria-label='Refresh dashboard'] > svg via browser.evaluate
    - expect: The SVG element's class string contains 'animate-spin' confirming the rotation animation is applied during the loading state
  5. Wait for the 2-second delay to expire and the refresh cycle to complete
    - expect: APIs respond; KPI cards repopulate
  6. Read the class attribute of the Refresh button SVG element again
    - expect: 'animate-spin' is no longer present in the SVG class attribute - the spinning animation stops once the refresh cycle completes
  7. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 6.4. TC-PS7-REFRESH-04: Dashboard data updates after manual refresh when backend data has changed

**File:** `tests/ps7/manual-refresh.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard; wait for full load and record the current Total Devices KPI value
    - expect: Dashboard is loaded; Total Devices value is recorded (e.g. N)
  2. Intercept the listDevices GraphQL query and inject a mock response returning a different device count (e.g. N+5)
    - expect: Route is registered; next fetch will return an incremented count
  3. Click the Refresh Dashboard button
    - expect: Refresh cycle is triggered; the mock response is served for the next listDevices call
  4. Wait for KPI cards to repopulate (loading placeholder disappears) and read the new Total Devices KPI value
    - expect: Total Devices KPI card now displays the new value (N+5) from the mock response confirming the refresh re-fetches and re-renders with updated data
  5. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

### 7. PS-7 Scenario 7 — Error Handling (Error Banner on KPI Fetch Failure)

**Seed:** `specs/seed.spec.ts`

#### 7.1. TC-PS7-ERR-01: Red error banner is displayed when KPI API calls fail with HTTP 500

**File:** `tests/ps7/error-handling.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is loaded
  2. Intercept all AppSync GraphQL requests and return HTTP 500 for all of them with body {"error":"Internal Server Error"}
    - expect: Route is registered; all API calls will fail with 500
  3. Reload the Dashboard with the 500-error route active
    - expect: Dashboard page loads but all six KPI API calls fail with HTTP 500
  4. Assert that the red error banner container (div.p-6.space-y-6 > div.bg-red-50) is visible in the main content area
    - expect: Error banner is rendered as a direct child of the main content wrapper div.p-6.space-y-6, appearing above the KPI grid
  5. Assert that the error banner contains visible text such as 'A network error has occurred.'
    - expect: Error message text is readable within the red banner
  6. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 7.2. TC-PS7-ERR-02: KPI cards fall back to '0' value after all APIs fail (no crash or blank screen)

**File:** `tests/ps7/error-handling.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard; register a 500-error route for all AppSync queries and reload the Dashboard
    - expect: Dashboard loads in error state
  2. Wait for the loading state to settle (the em-dash placeholder transitions out)
    - expect: Dashboard settles into the error state - the placeholder is replaced by fallback values
  3. Assert that the KPI card value elements (div[class*='text-3xl']) display '0' as the error fallback and not the em-dash loading placeholder
    - expect: KPI cards show '0' as the zero/error fallback confirming the app gracefully degrades rather than crashing
  4. Assert that all four KPI card containers are still rendered in the DOM
    - expect: KPI card layout is intact - cards are present even in error state
  5. Assert that the page does not display a blank white screen or an unhandled JavaScript error overlay
    - expect: No crash overlay is present; the Dashboard structure including header, sidebar, KPI grid, and panels is still visible
  6. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 7.3. TC-PS7-ERR-03: Alerts panel API failure does not trigger the global KPI error banner (isolated error domains)

**File:** `tests/ps7/error-handling.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard
    - expect: Dashboard is loaded
  2. Intercept AppSync GraphQL requests. Return HTTP 500 only for requests whose POST body contains 'listAuditLogs'. All other requests pass through normally.
    - expect: Route is registered; only the audit-log API will fail; KPI APIs will succeed
  3. Reload the Dashboard with the selective 500-error route active
    - expect: KPI API calls succeed; only the audit-log API call fails
  4. Assert that the global KPI error banner (div.p-6.space-y-6 > div.bg-red-50) is NOT visible
    - expect: No global error banner - KPI API failures are the exclusive trigger for the banner; audit-log failures are isolated to the alerts panel
  5. Assert that the Total Devices KPI card shows a populated numeric value
    - expect: KPI data is intact - the audit-log failure did not affect the KPI domain
  6. Assert that the Recent Alerts panel shows either an in-panel error element (div.bg-red-50 inside the alerts card) or the 'No recent activity' empty-state text but no global banner
    - expect: Alerts panel handles its own error in isolation without propagating to the global error banner
  7. Remove the route intercept via unrouteAll()
    - expect: Routes are cleared

#### 7.4. TC-PS7-ERR-04: Error banner disappears after a successful manual refresh following API failure

**File:** `tests/ps7/error-handling.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard. Register a 500-error route for all AppSync requests. Reload the Dashboard and confirm the red error banner is visible.
    - expect: Dashboard error state is confirmed - red error banner (div.p-6.space-y-6 > div.bg-red-50) is visible
  2. Remove the error route intercept via unrouteAll() to allow APIs to succeed on the next fetch
    - expect: Routes are cleared; subsequent API calls will succeed
  3. Click the Refresh Dashboard button (button[aria-label='Refresh dashboard'])
    - expect: Refresh cycle is triggered with no error routes active
  4. Wait for KPI cards to repopulate with live data (loading placeholder disappears)
    - expect: KPI loading placeholders disappear; cards show numeric values from the live APIs
  5. Assert that the red error banner (div.p-6.space-y-6 > div.bg-red-50) is no longer visible
    - expect: Error banner is dismissed after a successful refresh confirming the error state is not permanent and recovers correctly when APIs succeed

#### 7.5. TC-PS7-ERR-05: Unauthenticated access to Dashboard redirects to login page

**File:** `tests/ps7/error-handling.spec.ts`

**Steps:**
  1. Open a fresh browser session with no stored session or authentication tokens and navigate directly to https://main.dddsig2mih3hw.amplifyapp.com/
    - expect: Browser loads the application
  2. Assert that the login page is displayed with email and password input fields visible rather than the Dashboard or a blank page
    - expect: Unauthenticated users are redirected to the login page - the app does not expose the Dashboard to unauthenticated sessions and does not render a blank or broken page
