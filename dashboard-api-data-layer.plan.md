# Dashboard API Data Layer - Comprehensive Test Plan (PS-3)

## Application Overview

The Inventory Management Dashboard (PS-3) is the home screen of the application. It fetches and displays live data from backend APIs in parallel for four data domains: devices, service orders, firmware, and compliance records. The dashboard presents KPI summary cards, Quick Action cards with badge counts, a Recent Alerts panel (audit logs from the last 24 hours), and a System Status panel with four service health percentages. A Refresh Dashboard button (circular arrow icon, top-right) allows the user to re-fetch all data on demand. The dashboard must handle loading states (placeholder "—" indicators), API error states (error banners), zero-data edge cases, and the 100-device cap. All API calls are made in parallel on initial load and on every refresh.

## Test Scenarios

### 1. Happy Path – Dashboard Loads Successfully

**Seed:** `/Users/ajaykumar.yadav/tools/playwright-setup/tests/seed.spec.ts`

#### 1.1. Dashboard renders all four KPI cards after successful login

**File:** `tests/dashboard/happy-path/kpi-cards-render.spec.ts`

**Steps:**
  1. Navigate to https://main.dddsig2mih3hw.amplifyapp.com and log in with valid credentials (ajaykumar.yadav@3pillarglobal.com / Secure@12345).
    - expect: The application redirects to the Dashboard (home screen) after successful authentication.
  2. Wait for the Dashboard to fully load (all API calls complete).
    - expect: All four KPI cards are visible: 'Total Devices', 'Active Deployments', 'Pending Approvals', 'Compliance Score'.
  3. Inspect each KPI card for its displayed numeric value.
    - expect: Each KPI card displays a numeric value (not the placeholder '—').
    - expect: The 'Total Devices' card shows a non-negative integer.
    - expect: The 'Active Deployments' card shows a non-negative integer.
    - expect: The 'Pending Approvals' card shows a non-negative integer.
    - expect: The 'Compliance Score' card shows a percentage value between 0 and 100.
  4. Verify that no error banners or alert messages are displayed on the page.
    - expect: No error messages, warning banners, or failure indicators are visible on the Dashboard.

#### 1.2. Dashboard renders all four Quick Action cards with badge counts

**File:** `tests/dashboard/happy-path/quick-action-cards-render.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for all data to load.
    - expect: Dashboard is fully loaded with all panels visible.
  2. Verify that four Quick Action cards are present: 'View Inventory', 'Schedule Service', 'Deploy Firmware', 'Check Compliance'.
    - expect: All four Quick Action cards are visible on the Dashboard.
  3. Inspect the badge count on each Quick Action card.
    - expect: Each Quick Action card displays a badge count that is a non-negative integer.
    - expect: Badge counts reflect the actual number of items from the respective API responses.
  4. Verify that each Quick Action card is clickable (interactive).
    - expect: Each Quick Action card responds to hover with a visual state change.

#### 1.3. Recent Alerts panel displays audit logs from the last 24 hours

**File:** `tests/dashboard/happy-path/recent-alerts-panel.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for all data to load.
    - expect: Dashboard is fully loaded.
  2. Locate the 'Recent Alerts' panel on the Dashboard.
    - expect: The 'Recent Alerts' panel is visible.
  3. Inspect the alerts/log entries displayed in the panel.
    - expect: Each displayed alert entry has a timestamp and a description.
    - expect: All displayed alert timestamps fall within the last 24 hours.
    - expect: Alerts older than 24 hours are not shown.

#### 1.4. System Status panel shows four services with health percentages

**File:** `tests/dashboard/happy-path/system-status-panel.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for all data to load.
    - expect: Dashboard is fully loaded.
  2. Locate the 'System Status' panel on the Dashboard.
    - expect: The 'System Status' panel is visible.
  3. Count the number of service entries shown in the System Status panel.
    - expect: Exactly 4 service entries are displayed.
  4. Inspect the health percentage value for each service.
    - expect: Each service entry displays a health percentage value between 0% and 100%.
    - expect: Health percentages are numeric values, not placeholder '—' characters.

### 2. Loading States – Placeholder Indicators During Data Fetch

**Seed:** `/Users/ajaykumar.yadav/tools/playwright-setup/tests/seed.spec.ts`

#### 2.1. KPI cards show '—' placeholder while API data is loading

**File:** `tests/dashboard/loading-states/kpi-placeholder.spec.ts`

**Steps:**
  1. Navigate to the Dashboard while throttling all API network requests to simulate slow responses (delay API responses by at least 3 seconds).
    - expect: The Dashboard page starts loading but API data has not yet arrived.
  2. Immediately observe the KPI cards before the API responses arrive.
    - expect: Each KPI card displays the placeholder indicator '—' instead of a numeric value.
    - expect: The KPI card layout and labels are visible during loading.
    - expect: No blank white spaces, broken layouts, or JavaScript errors are present.
  3. Allow the API responses to complete and observe the KPI cards again.
    - expect: The '—' placeholder is replaced with actual numeric values once data loads.

#### 2.2. Quick Action badge counts show '—' placeholder while loading

**File:** `tests/dashboard/loading-states/quick-action-badge-placeholder.spec.ts`

**Steps:**
  1. Navigate to the Dashboard while throttling all API network requests.
    - expect: Dashboard is in a loading state.
  2. Observe the Quick Action cards before API responses arrive.
    - expect: Each Quick Action card badge displays '—' as a placeholder.
    - expect: The Quick Action card titles and icons are still visible during loading.
  3. Allow the API responses to complete.
    - expect: Each badge count transitions from '—' to the actual numeric value.

#### 2.3. All API calls are initiated in parallel on page load

**File:** `tests/dashboard/loading-states/parallel-api-calls.spec.ts`

**Steps:**
  1. Open browser developer tools Network tab and navigate to the Dashboard.
    - expect: Network tab is ready to capture requests.
  2. Monitor the timing of all dashboard API requests for devices, service orders, firmware, and compliance.
    - expect: All four API requests are initiated within a very short window of each other (within 100ms).
    - expect: No API request waits for another to complete before starting.
    - expect: The network waterfall shows overlapping request timelines for all four API calls.

### 3. Error Handling – API Failure Scenarios

**Seed:** `/Users/ajaykumar.yadav/tools/playwright-setup/tests/seed.spec.ts`

#### 3.1. Error banner appears when all dashboard APIs fail

**File:** `tests/dashboard/error-handling/all-apis-fail.spec.ts`

**Steps:**
  1. Navigate to the Dashboard while intercepting all four API requests to return HTTP 500 errors.
    - expect: Dashboard page loads but all API calls receive error responses.
  2. Observe the Dashboard after the failed API responses.
    - expect: A clear, visible error message or banner is displayed on the Dashboard.
    - expect: The error message does not expose raw HTTP error codes or stack traces.
    - expect: KPI cards show '—' or an error state rather than numeric values.
  3. Verify the Dashboard layout remains intact.
    - expect: The Dashboard structure remains visible and undamaged despite API failures.

#### 3.2. Error handling when only devices API fails

**File:** `tests/dashboard/error-handling/devices-api-fail.spec.ts`

**Steps:**
  1. Intercept only the devices API to return HTTP 500. Allow all other APIs to succeed.
    - expect: Dashboard loads with partial failure for device data.
  2. Observe the 'Total Devices' KPI card and 'View Inventory' Quick Action badge.
    - expect: The 'Total Devices' KPI card shows '—' or an error indicator.
    - expect: The 'View Inventory' Quick Action badge shows '—' or an error indicator.
    - expect: Other KPI cards ('Active Deployments', 'Pending Approvals', 'Compliance Score') display data normally.
    - expect: Other Quick Action badges display counts normally.

#### 3.3. Error handling when service orders API fails

**File:** `tests/dashboard/error-handling/service-orders-api-fail.spec.ts`

**Steps:**
  1. Intercept only the service orders API to return HTTP 500.
    - expect: Dashboard loads with a partial failure for service orders data.
  2. Observe the 'Active Deployments' KPI card and 'Schedule Service' Quick Action badge.
    - expect: The 'Active Deployments' KPI card shows '—' or an error state.
    - expect: The 'Schedule Service' badge shows '—' or an error state.
    - expect: Other dashboard panels display their data normally.

#### 3.4. Error handling when firmware API fails

**File:** `tests/dashboard/error-handling/firmware-api-fail.spec.ts`

**Steps:**
  1. Intercept only the firmware API to return HTTP 500.
    - expect: Dashboard loads with partial failure for firmware data.
  2. Observe the 'Deploy Firmware' Quick Action card badge.
    - expect: The 'Deploy Firmware' badge shows '—' or an error state.
    - expect: Other dashboard panels display their data normally.

#### 3.5. Error handling when compliance API fails

**File:** `tests/dashboard/error-handling/compliance-api-fail.spec.ts`

**Steps:**
  1. Intercept only the compliance API to return HTTP 500.
    - expect: Dashboard loads with partial failure for compliance data.
  2. Observe the 'Compliance Score' KPI card and 'Check Compliance' badge.
    - expect: The 'Compliance Score' KPI card shows '—' or an error state.
    - expect: The 'Check Compliance' badge shows '—' or an error state.
    - expect: Other dashboard panels display data normally.

#### 3.6. Error handling when audit logs API fails

**File:** `tests/dashboard/error-handling/audit-logs-api-fail.spec.ts`

**Steps:**
  1. Intercept the audit log API to return HTTP 500. Navigate to the Dashboard.
    - expect: Dashboard loads with failure for the Recent Alerts panel.
  2. Observe the 'Recent Alerts' panel.
    - expect: The panel displays an error message or empty state indicator.
    - expect: The error does not propagate to break other panels on the Dashboard.

#### 3.7. Error handling when API returns HTTP 401 Unauthorized

**File:** `tests/dashboard/error-handling/unauthorized-api-response.spec.ts`

**Steps:**
  1. Simulate an expired authentication session by clearing auth token, then navigate to the Dashboard.
    - expect: The app encounters authentication failures on dashboard API calls.
  2. Observe the application response.
    - expect: The application redirects to the login page OR displays an authentication error message.
    - expect: No raw API error responses or sensitive data is shown to the user.

### 4. Refresh Dashboard Button

**Seed:** `/Users/ajaykumar.yadav/tools/playwright-setup/tests/seed.spec.ts`

#### 4.1. Refresh button is visible in the top-right area with circular arrow icon

**File:** `tests/dashboard/refresh/refresh-button-visibility.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for it to fully load.
    - expect: Dashboard is loaded with all data displayed.
  2. Locate the Refresh Dashboard button (circular arrow icon) in the top-right area.
    - expect: The Refresh button is visible and accessible.
    - expect: The button displays a circular arrow icon.
    - expect: The button is positioned in the top-right corner of the Dashboard.

#### 4.2. Clicking Refresh re-fetches all data and shows loading state

**File:** `tests/dashboard/refresh/refresh-triggers-loading-state.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for it to fully load.
    - expect: All KPI cards, Quick Action badges, Recent Alerts, and System Status panels show data.
  2. Click the Refresh Dashboard button.
    - expect: The Dashboard enters a loading state immediately.
    - expect: KPI card values are replaced with '—' placeholders.
    - expect: Quick Action badge counts are replaced with '—' placeholders.
  3. Wait for all API calls to complete after refresh.
    - expect: All KPI cards display updated numeric values.
    - expect: All Quick Action badge counts are updated.
    - expect: The Recent Alerts panel shows updated entries.
    - expect: The System Status panel shows updated health percentages.
    - expect: No error banners are visible.

#### 4.3. Refresh button re-initiates all four API calls in parallel

**File:** `tests/dashboard/refresh/refresh-parallel-api-calls.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and fully load. Open the browser Network tab.
    - expect: Dashboard is fully loaded.
  2. Click the Refresh Dashboard button and monitor the Network tab.
    - expect: All four API calls are re-initiated in parallel.
    - expect: The network waterfall shows overlapping request timelines.

#### 4.4. Refresh button shows loading state while refresh is in progress

**File:** `tests/dashboard/refresh/refresh-button-disabled-during-load.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for it to fully load. Throttle API responses to extend loading.
    - expect: Dashboard is loaded and API throttling is active.
  2. Click the Refresh Dashboard button and immediately observe its state.
    - expect: The Refresh button shows a loading indicator (spinner animation) or is disabled while refresh is in progress.
    - expect: Clicking the Refresh button again while loading does not trigger duplicate API calls.

#### 4.5. Refresh button recovers and shows error state if APIs fail

**File:** `tests/dashboard/refresh/refresh-with-api-failure.spec.ts`

**Steps:**
  1. Navigate to the Dashboard with successful initial load. Then intercept all APIs to return HTTP 500 errors and click Refresh.
    - expect: Refresh is triggered and all API calls fail.
  2. Observe the Dashboard after the failed refresh.
    - expect: An error banner is displayed indicating refresh failed.
    - expect: The Refresh button is re-enabled and can be clicked again to retry.

### 5. Quick Action Badge Counts – Zero vs Non-Zero

**Seed:** `/Users/ajaykumar.yadav/tools/playwright-setup/tests/seed.spec.ts`

#### 5.1. View Inventory badge count matches number of offline devices

**File:** `tests/dashboard/quick-actions/view-inventory-badge.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for all data to load.
    - expect: Dashboard is fully loaded.
  2. Record the badge count on the 'View Inventory' Quick Action card.
    - expect: Badge count is visible and numeric.
  3. Navigate to the Inventory section and count offline devices.
    - expect: The offline device count in Inventory matches the 'View Inventory' badge count on the Dashboard.

#### 5.2. Quick Action badges display '0' correctly when no items exist

**File:** `tests/dashboard/quick-actions/zero-badge-count.spec.ts`

**Steps:**
  1. Ensure a state where all badge category counts are zero. Navigate to the Dashboard.
    - expect: Dashboard is loaded with all API responses returning zero-count data.
  2. Observe the badge counts on all four Quick Action cards.
    - expect: Each badge displays '0' explicitly (not '—', blank, or hidden).
    - expect: Badges with '0' count remain visible rather than being hidden.
    - expect: The zero count is clearly distinguishable from the loading placeholder '—'.

#### 5.3. Non-zero Quick Action badges use distinct visual styling

**File:** `tests/dashboard/quick-actions/non-zero-badge-styling.spec.ts`

**Steps:**
  1. Ensure at least one Quick Action category has a count greater than zero. Navigate to the Dashboard.
    - expect: Dashboard is loaded with at least one non-zero badge count.
  2. Compare visual styling of zero badges versus non-zero badges.
    - expect: Non-zero badges use a different color or visual prominence compared to zero badges.
    - expect: Badge counts are clearly legible in all cases.

### 6. Edge Cases

**Seed:** `/Users/ajaykumar.yadav/tools/playwright-setup/tests/seed.spec.ts`

#### 6.1. Dashboard renders correctly with zero devices in inventory

**File:** `tests/dashboard/edge-cases/zero-devices.spec.ts`

**Steps:**
  1. Intercept the devices API to return an empty device list. Navigate to the Dashboard.
    - expect: Devices API returns an empty response.
  2. Observe the 'Total Devices' KPI card and 'View Inventory' badge.
    - expect: The 'Total Devices' KPI card shows '0'.
    - expect: The 'View Inventory' badge shows '0' offline devices.
    - expect: No layout breakage or JavaScript errors occur with zero device data.

#### 6.2. Dashboard handles exactly 100 devices (device cap boundary)

**File:** `tests/dashboard/edge-cases/100-device-cap.spec.ts`

**Steps:**
  1. Intercept the devices API to return exactly 100 devices. Navigate to the Dashboard.
    - expect: The devices API returns 100 devices (the cap).
  2. Observe the 'Total Devices' KPI card.
    - expect: The 'Total Devices' KPI card displays '100'.
    - expect: No overflow, truncation, or display error occurs at the cap boundary.

#### 6.3. Dashboard handles network offline condition gracefully

**File:** `tests/dashboard/edge-cases/network-offline.spec.ts`

**Steps:**
  1. Load the Dashboard successfully. Then simulate a network offline condition and click the Refresh Dashboard button.
    - expect: All API requests fail due to network unavailability.
  2. Observe the Dashboard response.
    - expect: A user-friendly error message is displayed (e.g., 'Network error' or 'Unable to connect').
    - expect: The Dashboard does not show a blank screen or uncaught errors.
    - expect: The Refresh button remains accessible to retry.
  3. Re-enable the network and click Refresh.
    - expect: The Dashboard successfully re-fetches all data.
    - expect: All KPI cards, badges, and panels display current data.
    - expect: The error message disappears once data loads successfully.

#### 6.4. Dashboard handles API timeout gracefully

**File:** `tests/dashboard/edge-cases/api-timeout.spec.ts`

**Steps:**
  1. Intercept all dashboard API calls to hang indefinitely, then navigate to the Dashboard.
    - expect: API calls are pending and do not resolve.
  2. Wait for the application timeout threshold.
    - expect: The application shows an error state after the timeout.
    - expect: An error message is shown to the user.
    - expect: The Dashboard does not remain in an infinite loading state.

#### 6.5. Compliance Score at boundary values (0% and 100%)

**File:** `tests/dashboard/edge-cases/compliance-score-boundary.spec.ts`

**Steps:**
  1. Intercept the compliance API to return a 0% compliance score. Navigate to the Dashboard.
    - expect: Compliance API returns 0%.
  2. Observe the 'Compliance Score' KPI card.
    - expect: The card displays '0%'.
    - expect: A visual indicator (e.g., red color) may highlight the critical state.
  3. Intercept the compliance API to return 100%, then refresh.
    - expect: The card displays '100%'.
    - expect: No overflow or display errors occur at the 100% boundary.

#### 6.6. Dashboard handles slow individual API while others resolve quickly (mixed response times)

**File:** `tests/dashboard/edge-cases/mixed-api-response-times.spec.ts`

**Steps:**
  1. Intercept the compliance API with a 5-second delay. Allow all other APIs to respond normally. Navigate to the Dashboard.
    - expect: Three API calls complete quickly; compliance API is delayed.
  2. Observe the Dashboard during the slow API window.
    - expect: KPI cards and panels with resolved data display values immediately.
    - expect: The 'Compliance Score' card and 'Check Compliance' badge show '—' placeholder while pending.
    - expect: The rest of the Dashboard is fully interactive while the slow API is completing.
  3. Wait for the compliance API to complete.
    - expect: The 'Compliance Score' card updates with the actual value.
    - expect: The 'Check Compliance' badge updates with the actual count.
    - expect: No disruption occurs to the already-loaded sections.

#### 6.7. Dashboard displays correctly across different viewport sizes

**File:** `tests/dashboard/edge-cases/responsive-layout.spec.ts`

**Steps:**
  1. Navigate to the Dashboard in a standard desktop viewport (1920x1080).
    - expect: All KPI cards, Quick Action cards, Recent Alerts, and System Status panels are visible and properly laid out.
  2. Resize the browser window to a smaller viewport (e.g., 1280x720).
    - expect: The Dashboard layout adjusts responsively without overlapping elements or horizontal scrollbars.
    - expect: All panels remain accessible and readable.
  3. Resize to a tablet viewport (e.g., 768x1024).
    - expect: Dashboard layout adapts appropriately for the tablet viewport size.
