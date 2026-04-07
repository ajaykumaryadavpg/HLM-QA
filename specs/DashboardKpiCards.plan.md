# PS-4: Dashboard KPI Cards — Live Computed Metrics

## Application Overview

This test plan covers Story PS-4 (8.2 KPI Cards — Live Computed Metrics) for the HLM Platform (Hardware Lifecycle Management) Inventory Management System hosted at https://main.dddsig2mih3hw.amplifyapp.com.

The Dashboard presents four KPI summary cards immediately below the "Welcome back" header in the main content area. Each card is a div.bg-card container holding a large numeric value (div.text-3xl) and a label (div.text-sm):

1. Total Devices — blue Package icon, shows total device count fetched from the devices API (capped at 100).
2. Active Deployments — green Download icon, count of service orders with status "In Progress".
3. Pending Approvals — orange Shield icon, sum of pending firmware records + pending compliance records.
4. Health Score — green Check-circle icon, average health score across all devices rounded to the nearest whole number, displayed with a "%" suffix (e.g., "77%").

Before API responses arrive each value element shows an em-dash placeholder "—" (U+2014). After successful resolution the placeholder is replaced by a numeric value. When no data exists the value resolves to "0" (or "0%") rather than remaining blank or returning to "—". A red error banner (div.bg-red-50) appears inside the main content wrapper on API failure, and cards fall back to "0". A "Refresh Dashboard" button (button[aria-label='Refresh dashboard']) in the welcome row re-triggers all API calls, briefly cycling through the loading placeholder before populating fresh values. Both light and dark themes must render all four cards correctly.

The test class DashboardKpiTests in package com.tpg.automation.inventory extends InventoryTestBase, which authenticates once per class in @BeforeClass via AuthenticateAs macro, then lands on the Dashboard before any test method runs. Test ID range: TC-8.2.01 through TC-8.2.15+. New TestGroups constant DASHBOARD_KPI = "dashboard-kpi" must be added to TestGroups alongside the existing SMOKE_TESTS, REGRESSION, and DASHBOARD_API constants.

## Test Scenarios

### 1. PS-4: Dashboard KPI Cards — Live Computed Metrics

**Seed:** `seed.spec.ts`

#### 1.1. TC-8.2.01: Four KPI cards are visible on Dashboard after login

**File:** `tests/dashboard-kpi/TC-8.2.01-kpi-cards-visible-after-login.spec.ts`

**Steps:**
  1. Start from a fresh unauthenticated browser session. Navigate to https://main.dddsig2mih3hw.amplifyapp.com — the app should redirect to the login page.
    - expect: Login page is displayed with the 'Sign in to your account' heading (LoginPage.PAGE_TITLE is visible).
  2. Enter username 'ajaykumar.yadav@3pillarglobal.com' in the Email field and 'Secure@12345' in the Password field, then click the Sign in button.
    - expect: Login succeeds and the browser redirects to the Dashboard. The Dashboard h1 heading (DashboardPage.DASHBOARD_HEADER) is visible within 30 seconds.
  3. Wait for all four KPI card value elements to no longer show the loading placeholder '—'. Locate: Total Devices card via KpiCard.TOTAL_DEVICES_VALUE, Active Deployments via KpiCard.ACTIVE_DEPLOYMENTS_VALUE, Pending Approvals via KpiCard.PENDING_APPROVALS_VALUE, Health Score via KpiCard.HEALTH_SCORE_VALUE.
    - expect: All four KPI card value elements (div.text-3xl inside each div.bg-card) are visible in the DOM.
    - expect: The loading placeholder '—' (KpiCard.LOADING_PLACEHOLDER) is no longer visible — all four data values have resolved.
  4. Verify the structural position of the four KPI cards relative to the welcome header. Confirm the cards appear as a horizontal group below the h2 'Welcome back' heading (DashboardPage.WELCOME_MESSAGE) and above the Quick Actions section.
    - expect: The four KPI cards are rendered in the main content area directly below the 'Welcome back, ...' h2 heading (AC-1).
    - expect: No error banner (ErrorBanner.CONTAINER / div.bg-red-50) is visible in the KPI section.

#### 1.2. TC-8.2.02: Total Devices card — blue Package icon and numeric count

**File:** `tests/dashboard-kpi/TC-8.2.02-total-devices-card.spec.ts`

**Steps:**
  1. Log in as admin and wait for the Dashboard to fully load (all KPI placeholders resolved). Locate the 'Total Devices' KPI card using: div.bg-card:has(div.text-sm:text-is('Total Devices')).
    - expect: The 'Total Devices' KPI card container (div.bg-card) is visible on the Dashboard.
  2. Verify the card label text. Use Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).describedAs('Total Devices KPI value is visible').isVisible() and also assert the label text 'Total Devices' is present in the card.
    - expect: The label 'Total Devices' (div.text-sm) is visible inside the card container (AC-2).
  3. Inspect the icon element inside the Total Devices card. Look for an SVG or icon element with CSS classes that indicate a blue Package icon (e.g., text-blue-600 or similar color utility class). Use a CSS selector such as: div.bg-card:has(div.text-sm:text-is('Total Devices')) svg or the icon wrapper element.
    - expect: A blue Package icon is present in the Total Devices card (AC-2).
    - expect: The icon has a blue color class (e.g., text-blue-500, text-blue-600, or equivalent Tailwind class).
  4. Read the numeric value displayed in the KpiCard.TOTAL_DEVICES_VALUE element (div.text-3xl inside the Total Devices card). Validate that the text content is a non-negative integer string (e.g., '0', '5', '100').
    - expect: The Total Devices value is a numeric string representing the total number of device records fetched from the API (AC-2).
    - expect: The value is not '—' (not a loading placeholder), not blank, and not a non-numeric string.

#### 1.3. TC-8.2.03: Active Deployments card — green Download icon and In-Progress order count

**File:** `tests/dashboard-kpi/TC-8.2.03-active-deployments-card.spec.ts`

**Steps:**
  1. Log in as admin and wait for the Dashboard to fully load. Locate the 'Active Deployments' KPI card using: div.bg-card:has(div.text-sm:text-is('Active Deployments')).
    - expect: The 'Active Deployments' KPI card container is visible on the Dashboard.
  2. Verify the label 'Active Deployments' (div.text-sm) is visible inside the card using Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE).isVisible().
    - expect: The 'Active Deployments' label text is visible in the card (AC-3).
  3. Inspect the icon element inside the Active Deployments card. Look for an SVG or icon element with a green color utility class (e.g., text-green-600) representing a Download icon.
    - expect: A green Download icon is present inside the Active Deployments card (AC-3).
    - expect: The icon's color class is a green Tailwind utility (text-green-500, text-green-600, or equivalent).
  4. Read the numeric value from KpiCard.ACTIVE_DEPLOYMENTS_VALUE. Confirm the value represents the count of service orders with status 'In Progress'.
    - expect: The Active Deployments value is a non-negative integer string (AC-3).
    - expect: The value is not '—', not blank, and not a non-numeric string.
    - expect: The count corresponds to the number of 'In Progress' service orders returned by the service orders API.

#### 1.4. TC-8.2.04: Pending Approvals card — orange Shield icon and combined pending count

**File:** `tests/dashboard-kpi/TC-8.2.04-pending-approvals-card.spec.ts`

**Steps:**
  1. Log in as admin and wait for the Dashboard to fully load. Locate the 'Pending Approvals' KPI card using: div.bg-card:has(div.text-sm:text-is('Pending Approvals')).
    - expect: The 'Pending Approvals' KPI card container is visible on the Dashboard.
  2. Verify the label 'Pending Approvals' (div.text-sm) is visible inside the card using Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE).isVisible().
    - expect: The 'Pending Approvals' label text is visible in the card (AC-4).
  3. Inspect the icon element inside the Pending Approvals card for an orange Shield icon (e.g., text-orange-600 or text-amber-600 Tailwind color class on the SVG/icon element).
    - expect: An orange Shield icon is present inside the Pending Approvals card (AC-4).
    - expect: The icon's color class is an orange Tailwind utility (text-orange-500, text-orange-600, or equivalent).
  4. Read the numeric value from KpiCard.PENDING_APPROVALS_VALUE. Cross-reference: the displayed value should equal the sum of pending firmware records plus pending compliance records from their respective API responses.
    - expect: The Pending Approvals value is a non-negative integer string (AC-4).
    - expect: The value is not '—', not blank, and not a non-numeric string.
    - expect: The displayed count equals pending-firmware count + pending-compliance count (summed, not either individually).

#### 1.5. TC-8.2.05: Health Score card — green Check-circle icon and percentage format

**File:** `tests/dashboard-kpi/TC-8.2.05-health-score-card.spec.ts`

**Steps:**
  1. Log in as admin and wait for the Dashboard to fully load. Locate the 'Health Score' KPI card using: div.bg-card:has(div.text-sm:text-is('Health Score')).
    - expect: The 'Health Score' KPI card container is visible on the Dashboard.
  2. Verify the label 'Health Score' (div.text-sm) is visible inside the card using Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE).isVisible().
    - expect: The 'Health Score' label text is visible in the card (AC-5).
  3. Inspect the icon element inside the Health Score card for a green Check-circle icon (e.g., text-green-600 Tailwind color class on the icon element).
    - expect: A green Check-circle icon is present inside the Health Score card (AC-5).
    - expect: The icon's color class is a green Tailwind utility (text-green-500, text-green-600, or equivalent).
  4. Read the text value from KpiCard.HEALTH_SCORE_VALUE (div.text-3xl in the Health Score card). Validate: (a) the value ends with a '%' suffix; (b) the numeric portion before '%' is a whole number (no decimal point); (c) the number is between 0 and 100 inclusive.
    - expect: The Health Score value ends with '%' suffix (AC-5). Example valid formats: '77%', '100%', '0%'.
    - expect: The numeric portion is a rounded whole number — no decimal places (e.g., '77.3%' is invalid, '77%' is valid).
    - expect: The value is within the valid range 0–100.
    - expect: The value is not '—', not blank, and does not contain a decimal point.

#### 1.6. TC-8.2.06: Loading state — all four KPI cards show em-dash placeholder while APIs are pending

**File:** `tests/dashboard-kpi/TC-8.2.06-kpi-loading-placeholder.spec.ts`

**Steps:**
  1. Ensure the user is already authenticated (session cookie present from InventoryTestBase.loginToApplication()). Navigate directly to the Dashboard URL (urlService.baseUrl()) to trigger a fresh page load: user.attemptsTo(Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())).
    - expect: The browser begins loading the Dashboard page.
  2. Immediately after navigation (before waiting for API responses), assert the loading placeholder '—' (U+2014 em dash) is visible using Verify.uiElement(KpiCard.LOADING_PLACEHOLDER).describedAs('Loading placeholder em-dash is visible during initial data fetch').isVisible(). Note: this assertion is timing-sensitive and may need a short explicit wait or network throttling to reliably catch the transient state.
    - expect: At least one KPI card value element (div.text-3xl) displays '—' (U+2014 em dash) while the API calls are in-flight (AC-6).
    - expect: The loading placeholder is the exact em-dash character U+2014, not a hyphen (-) or en-dash (–).
  3. Wait for the loading state to resolve (up to 15 seconds). Assert that Verify.uiElement(KpiCard.LOADING_PLACEHOLDER).isNotVisible() — i.e., all four cards have replaced the placeholder with actual values.
    - expect: After APIs respond, no KPI card retains the '—' placeholder (AC-6 post-load).
    - expect: All four KPI value elements now show either a numeric count or '0%' (for Health Score when no data).

#### 1.7. TC-8.2.07: Zero-data fallback — KPI cards show '0' (or '0%') not blank or em-dash

**File:** `tests/dashboard-kpi/TC-8.2.07-kpi-zero-data-fallback.spec.ts`

**Steps:**
  1. Configure the test environment so that all dashboard APIs return empty result sets (zero devices, zero in-progress orders, zero pending firmware, zero pending compliance). This requires either: (a) a dedicated clean-state backend environment with no data; or (b) Playwright page.route() interception to mock empty array responses for all six KPI API endpoints. Note: this test is currently a placeholder pending network-interception support in novus-core (see DashboardApiTests TC-8.1.19 for the equivalent pattern).
    - expect: The test environment is in a state where all KPI APIs return empty/zero data before proceeding.
  2. Navigate to the Dashboard URL and wait for all KPI placeholders to resolve (KpiCard.LOADING_PLACEHOLDER is not visible).
    - expect: The Dashboard page loads without crashing (DashboardPage.DASHBOARD_HEADER is visible).
    - expect: No error banner (ErrorBanner.CONTAINER) is displayed — empty results are not treated as an error (AC-7).
  3. Assert the value of each KPI card when data is empty: Total Devices should read '0'; Active Deployments should read '0'; Pending Approvals should read '0'; Health Score should read '0%'. Use Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).containsText('0'), and similar for the other three cards.
    - expect: Total Devices card shows '0', not blank and not '—' (AC-7).
    - expect: Active Deployments card shows '0', not blank and not '—' (AC-7).
    - expect: Pending Approvals card shows '0', not blank and not '—' (AC-7).
    - expect: Health Score card shows '0%', not blank and not '—' and not '0' without the '%' suffix (AC-7).

#### 1.8. TC-8.2.08: Responsive layout — Desktop 4-column single row

**File:** `tests/dashboard-kpi/TC-8.2.08-responsive-desktop-4col.spec.ts`

**Steps:**
  1. Launch the browser at a desktop viewport width of 1280×800 pixels (or the default Desktop Chrome viewport). Log in and navigate to the Dashboard. Wait for all four KPI cards to load (KpiCard.LOADING_PLACEHOLDER not visible).
    - expect: The Dashboard loads successfully at desktop viewport dimensions.
  2. Inspect the KPI card grid container's CSS layout. Verify the four cards are rendered in a single horizontal row (grid-cols-4 or equivalent Tailwind class is applied at this breakpoint). Use JavaScript evaluation: document.querySelector('[class*="grid-cols-4"]') should be non-null, or verify that all four card bounding boxes have the same top offset (same row) and are distributed horizontally.
    - expect: All four KPI cards are in a single row at desktop width (AC-8).
    - expect: Each card occupies one column of a 4-column grid — no card wraps to a second row.
    - expect: The KPI grid container has a Tailwind 4-column layout class active (e.g., lg:grid-cols-4, xl:grid-cols-4, or grid-cols-4).
  3. Verify that all four card labels ('Total Devices', 'Active Deployments', 'Pending Approvals', 'Health Score') are visible simultaneously without horizontal scrolling required.
    - expect: All four card labels are visible within the viewport without scrolling (AC-8).

#### 1.9. TC-8.2.09: Responsive layout — Tablet 2×2 grid

**File:** `tests/dashboard-kpi/TC-8.2.09-responsive-tablet-2x2.spec.ts`

**Steps:**
  1. Resize the browser viewport to a tablet width (e.g., 768×1024 pixels — iPad portrait). Log in and navigate to the Dashboard. Wait for all four KPI cards to load.
    - expect: The Dashboard loads successfully at tablet viewport dimensions.
  2. Inspect the KPI card grid layout at this viewport. Verify the four cards are rendered in a 2×2 grid (two cards per row, two rows total). Check for Tailwind class sm:grid-cols-2 or md:grid-cols-2 being active. Alternatively, verify using bounding box positions: card 1 and card 2 share a top offset (row 1), card 3 and card 4 share a different top offset (row 2).
    - expect: The four KPI cards form a 2-column × 2-row layout at tablet width (AC-8).
    - expect: Cards 1–2 (Total Devices, Active Deployments) are on the first row.
    - expect: Cards 3–4 (Pending Approvals, Health Score) are on the second row.
    - expect: No card overflows or is cut off.

#### 1.10. TC-8.2.10: Responsive layout — Mobile single-column stack

**File:** `tests/dashboard-kpi/TC-8.2.10-responsive-mobile-1col.spec.ts`

**Steps:**
  1. Resize the browser viewport to a mobile width (e.g., 375×667 pixels — iPhone SE). Log in and navigate to the Dashboard. Wait for all four KPI cards to load.
    - expect: The Dashboard loads successfully at mobile viewport dimensions.
  2. Inspect the KPI card grid layout at this viewport. Verify the four cards are stacked in a single column (grid-cols-1 active). Verify using bounding box positions: each card has a unique top offset (all four cards stack vertically), and each card occupies the full available width.
    - expect: All four KPI cards are stacked in a single column at mobile width (AC-8).
    - expect: Each card spans the full content width — no two cards share the same row.
    - expect: Cards are visible by scrolling down; none are cut off or hidden.

#### 1.11. TC-8.2.11: Refresh Dashboard button — re-fetches all KPI data and cycles through loading placeholder

**File:** `tests/dashboard-kpi/TC-8.2.11-refresh-dashboard-button.spec.ts`

**Steps:**
  1. Log in and wait for the Dashboard to fully load (all four KPI values populated, KpiCard.LOADING_PLACEHOLDER not visible). Verify the Refresh Dashboard button is visible: Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON).describedAs('Refresh button is visible').isVisible().
    - expect: The 'Refresh Dashboard' button (button[aria-label='Refresh dashboard']) is visible in the welcome header row (AC-9).
  2. Click the Refresh Dashboard button: user.attemptsTo(DashboardPageImpl.clickRefreshDashboard()). Immediately after clicking, assert that the loading placeholder '—' is visible in at least one KPI card (KpiCard.LOADING_PLACEHOLDER is visible). Note: this step is timing-sensitive — the placeholder state is transient.
    - expect: Immediately after clicking Refresh, the KPI cards briefly display '—' (em-dash) placeholder, indicating a re-fetch is in-flight (AC-9).
  3. Wait for the refresh cycle to complete. Assert: (a) KpiCard.LOADING_PLACEHOLDER is not visible — all placeholders have been replaced; (b) all four KPI value elements are visible with numeric values; (c) ErrorBanner.CONTAINER is not visible — refresh succeeded without errors.
    - expect: After refresh completes, all four KPI cards show updated live numeric values (AC-9).
    - expect: No loading placeholder '—' remains in any card.
    - expect: No error banner is displayed after a successful refresh.
  4. Verify the Refresh Dashboard button is still interactive after the refresh cycle completes (it is not disabled or hidden).
    - expect: The Refresh button remains visible and enabled after the refresh cycle — it can be clicked again.

#### 1.12. TC-8.2.12: API failure — red error banner and KPI cards fall back to '0'

**File:** `tests/dashboard-kpi/TC-8.2.12-api-failure-error-banner.spec.ts`

**Steps:**
  1. Configure the test environment so that at least one dashboard KPI API endpoint returns an error (HTTP 500 or network failure). This requires either: (a) a dedicated stub/error environment; or (b) Playwright page.route() interception to abort or return 500 for the devices API call. Note: this test is currently a placeholder pending network-interception support in novus-core (see DashboardApiTests TC-8.1.10 for the equivalent pattern).
    - expect: The test environment is primed to return an API error for at least one KPI endpoint before navigation.
  2. Navigate to the Dashboard URL (or reload the page) to trigger API calls. Wait for the page to finish its loading cycle.
    - expect: The Dashboard page loads without a JavaScript crash — the page structure is still rendered.
  3. Verify a red error banner is displayed at the top of the KPI section: Verify.uiElement(ErrorBanner.CONTAINER).describedAs('Red error banner is visible on API failure').isVisible(). The CSS selector is: div.p-6.space-y-6 > div.bg-red-50.
    - expect: A red error banner (div.bg-red-50) is visible inside the main content area as a direct child of div.p-6.space-y-6 (AC-10).
    - expect: The banner contains a meaningful error message text — it is not empty.
  4. Verify the KPI cards that failed to load display '0' as their fallback value rather than '—' or a blank: use Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).containsText('0') for the card(s) whose API failed.
    - expect: Affected KPI cards show '0' as a fallback value after API failure (AC-10).
    - expect: Cards do not show '—' (loading placeholder) indefinitely after a definitive API error.
    - expect: Cards do not display blank/empty content.
  5. Verify the Dashboard header (h1) and welcome message are still rendered — the page did not crash: Verify.uiElement(DashboardPage.DASHBOARD_HEADER).isVisible().
    - expect: The Dashboard page structure remains intact despite the API error (AC-10).
    - expect: Navigation menu, header, and other non-KPI sections are still visible.

#### 1.13. TC-8.2.13: Dark mode theme — all four KPI cards render correctly

**File:** `tests/dashboard-kpi/TC-8.2.13-dark-mode-kpi-cards.spec.ts`

**Steps:**
  1. Log in and navigate to the Dashboard in the default light theme. Wait for all four KPI cards to load (KpiCard.LOADING_PLACEHOLDER not visible).
    - expect: The Dashboard loads successfully in light mode. All four KPI cards are visible with correct values.
  2. Locate the dark/light theme toggle control in the Dashboard UI (look for a toggle button, sun/moon icon, or settings control in the header or sidebar). Click the dark mode toggle to switch from light to dark theme.
    - expect: The theme toggle is visible and interactive.
    - expect: After clicking, the UI switches to dark mode — the page background changes from light to dark (e.g., bg-background class changes, html element gets 'dark' class).
  3. After switching to dark mode, verify all four KPI card containers are still visible: Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).isVisible(), Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE).isVisible(), Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE).isVisible(), Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE).isVisible().
    - expect: All four KPI cards remain visible in dark mode (AC-11).
    - expect: No KPI card is hidden, collapsed, or clipped by the theme switch.
  4. Verify that the KPI values are readable in dark mode — assert the text content of each KpiCard value element is still a non-empty, non-placeholder string. Also visually confirm (screenshot if needed) that text has adequate contrast against the dark background.
    - expect: All four KPI value texts are still readable and non-empty in dark mode (AC-11).
    - expect: Card labels ('Total Devices', 'Active Deployments', 'Pending Approvals', 'Health Score') remain visible.
    - expect: Icons remain visible within their cards.
  5. Switch back to light mode using the theme toggle. Confirm the KPI cards remain correct after returning to light mode.
    - expect: After toggling back to light mode, all four KPI cards display correctly with no visual regression.

#### 1.14. TC-8.2.14: Health Score format validation — rounding and percentage suffix

**File:** `tests/dashboard-kpi/TC-8.2.14-health-score-format-validation.spec.ts`

**Steps:**
  1. Log in and wait for the Dashboard to fully load. Read the text content of KpiCard.HEALTH_SCORE_VALUE (div.text-3xl in the Health Score card).
    - expect: The Health Score card value element is visible and contains text.
  2. Parse the Health Score value string. Validate: (a) the string ends with exactly one '%' character; (b) after removing the '%' suffix, the remaining substring is a whole-number integer (no decimal point, no extra characters); (c) the integer value is in range 0–100 inclusive.
    - expect: The Health Score value matches the regex pattern ^\d{1,3}%$ (1–3 digits followed immediately by '%').
    - expect: The numeric portion has no decimal places — the value is rounded to the nearest whole number (AC-5).
    - expect: Example valid values: '0%', '77%', '100%'. Example invalid values: '77.3%', '77', '%77', '—'.
  3. If the live backend returns a fractional average health (e.g., 77.3), confirm the UI displays it rounded to '77%' (not '77.3%' and not truncated to '77%' via floor). Verify rounding behavior by cross-referencing the API raw value against the displayed integer (note: manual verification required unless API value is directly accessible via network inspection).
    - expect: Rounding uses standard mathematical rounding (0.5 rounds up) — consistent with 'rounded to nearest whole number' requirement (AC-5).
    - expect: The '%' suffix is always present regardless of the numeric value.
  4. Verify that the Health Score card does NOT display: a plain integer without '%' (e.g., '77'), a decimal number (e.g., '77.3%'), an em-dash '—', or empty string.
    - expect: The Health Score value strictly conforms to the integer-plus-percent format defined in AC-5.

#### 1.15. TC-8.2.15: Icon color verification — each card's icon uses the correct Tailwind color class

**File:** `tests/dashboard-kpi/TC-8.2.15-kpi-icon-color-verification.spec.ts`

**Steps:**
  1. Log in and wait for the Dashboard to fully load. For the 'Total Devices' KPI card, locate the icon element inside div.bg-card:has(div.text-sm:text-is('Total Devices')). Retrieve its CSS class list or computed color.
    - expect: An icon element (SVG or icon component) is present inside the Total Devices card.
  2. Verify the Total Devices card icon has a blue color. Check for a Tailwind class such as text-blue-500, text-blue-600, or equivalent blue color utility. Alternatively, evaluate the computed CSS color value and confirm it falls in the blue spectrum.
    - expect: The Total Devices card icon is blue in color (AC-2). The element carries a blue Tailwind color utility class.
  3. For the 'Active Deployments' KPI card, locate the icon inside div.bg-card:has(div.text-sm:text-is('Active Deployments')). Verify the icon has a green color class (e.g., text-green-500, text-green-600).
    - expect: The Active Deployments card icon is green in color (AC-3). The element carries a green Tailwind color utility class.
  4. For the 'Pending Approvals' KPI card, locate the icon inside div.bg-card:has(div.text-sm:text-is('Pending Approvals')). Verify the icon has an orange color class (e.g., text-orange-500, text-orange-600).
    - expect: The Pending Approvals card icon is orange in color (AC-4). The element carries an orange Tailwind color utility class.
  5. For the 'Health Score' KPI card, locate the icon inside div.bg-card:has(div.text-sm:text-is('Health Score')). Verify the icon has a green color class (e.g., text-green-500, text-green-600).
    - expect: The Health Score card icon is green in color (AC-5). The element carries a green Tailwind color utility class.

#### 1.16. TC-8.2.16: Negative — KPI values are never negative numbers

**File:** `tests/dashboard-kpi/TC-8.2.16-negative-kpi-values.spec.ts`

**Steps:**
  1. Log in and wait for the Dashboard to fully load (KpiCard.LOADING_PLACEHOLDER not visible). Read the text content of all four KPI card value elements: TOTAL_DEVICES_VALUE, ACTIVE_DEPLOYMENTS_VALUE, PENDING_APPROVALS_VALUE, HEALTH_SCORE_VALUE.
    - expect: All four KPI value elements are visible and contain non-empty text.
  2. For Total Devices, Active Deployments, and Pending Approvals: extract the text value and confirm it does not start with a '-' (minus) character. Confirm the integer value is >= 0.
    - expect: Total Devices value is >= 0 (never negative).
    - expect: Active Deployments value is >= 0 (never negative).
    - expect: Pending Approvals value is >= 0 (never negative).
  3. For Health Score: extract the text value, strip the '%' suffix, and confirm the integer value is between 0 and 100 inclusive (i.e., it is never negative and never exceeds 100%).
    - expect: Health Score value is in the range 0–100 inclusive (never negative, never above 100).
  4. Verify that none of the four KPI values contains unexpected characters such as: 'NaN', 'undefined', 'null', 'Infinity', '-Infinity', or any non-numeric string (other than the '%' suffix on Health Score).
    - expect: No KPI card displays 'NaN', 'undefined', 'null', 'Infinity', or any invalid computation artifact.
    - expect: All four values are clean numeric strings (or 'N%' for Health Score).

#### 1.17. TC-8.2.17: Negative — Dashboard accessed without authentication redirects to login

**File:** `tests/dashboard-kpi/TC-8.2.17-unauthenticated-redirect.spec.ts`

**Steps:**
  1. Start from a completely fresh, unauthenticated browser session (clear all cookies and localStorage). Navigate directly to the Dashboard URL https://main.dddsig2mih3hw.amplifyapp.com without logging in.
    - expect: The application detects the unauthenticated state.
  2. Verify the browser is redirected to the login page. Assert that LoginPage.PAGE_TITLE ('Sign in to your account') is visible, and that no KPI cards (KpiCard.TOTAL_DEVICES_VALUE, etc.) are visible.
    - expect: The user is redirected to the login page (URL changes to /login or equivalent).
    - expect: The login page heading 'Sign in to your account' is displayed.
    - expect: No KPI card content is accessible to an unauthenticated user — all four KPI card elements are not visible.

#### 1.18. TC-8.2.18: Pending Approvals aggregation — sum of firmware + compliance is correctly computed

**File:** `tests/dashboard-kpi/TC-8.2.18-pending-approvals-aggregation.spec.ts`

**Steps:**
  1. Using API calls (or test data setup), determine the current count of: (a) pending firmware records; (b) pending compliance records. Record these values as expectedFirmwareCount and expectedComplianceCount.
    - expect: API values for pending firmware count and pending compliance count are known before the UI assertion.
  2. Log in and wait for the Dashboard to fully load. Read the displayed value from KpiCard.PENDING_APPROVALS_VALUE.
    - expect: The Pending Approvals card value is visible and non-empty.
  3. Compare the displayed Pending Approvals value against expectedFirmwareCount + expectedComplianceCount. The displayed integer must equal the arithmetic sum of the two API counts.
    - expect: The Pending Approvals KPI value equals pendingFirmwareCount + pendingComplianceCount (AC-4).
    - expect: The value is not just the firmware count alone, not just the compliance count alone, and not the maximum of the two.
