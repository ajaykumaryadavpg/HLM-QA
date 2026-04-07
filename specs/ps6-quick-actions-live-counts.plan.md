# PS-6 Quick Actions with Live Counts - Test Plan

## Application Overview

Test plan for the HLM Inventory Management Platform Dashboard feature "Quick Actions with Live Counts" (JIRA Story PS-6). The Dashboard is accessible at https://main.dddsig2mih3hw.amplifyapp.com after login. The feature renders four Quick Action cards in the main content area of the Dashboard page, each linking to a module page (Inventory, Account & Service, Deployment, Compliance) and optionally displaying an orange badge with a live count of items requiring attention. The Dashboard also displays a personalized welcome message and a manual refresh button. All badge counts are fetched via GraphQL API calls. The application is built with React and Tailwind CSS; badge elements use class span.bg-orange-500 and cards are rendered as anchor elements with href attributes pointing to the respective module routes. The sidebar navigation shares the same href values as the quick-action cards but does NOT render badge spans in the same way — selectors must be scoped to the main content area to avoid false positives. Credentials for testing: username ajaykumar.yadav@3pillarglobal.com, password Secure@12345.

## Test Scenarios

### 1. TC-PS6-QA: Quick Action Cards - Rendering and Layout

**Seed:** `specs/seed.spec.ts`

#### 1.1. TC-PS6-QA-01: Four Quick Action cards are displayed on the Dashboard

**File:** `specs/dashboard/tc-ps6-qa-01-four-cards-displayed.spec.ts`

**Steps:**
  1. Navigate to https://main.dddsig2mih3hw.amplifyapp.com and log in using credentials ajaykumar.yadav@3pillarglobal.com / Secure@12345
    - expect: Login succeeds and the user is redirected to the Dashboard page
  2. Wait for the Dashboard main content area to fully render (wait for the h2 heading containing 'Welcome back' to be visible)
    - expect: The h2 heading with 'Welcome back' is visible in the main content area
  3. Locate the Quick Actions section in the main content area and verify the 'View Inventory' card is present using selector: main a[href='/inventory']
    - expect: A clickable card element linking to /inventory is visible in the Quick Actions section
  4. Verify the 'Schedule Service' card is present using selector: main a[href='/account-service']
    - expect: A clickable card element linking to /account-service is visible in the Quick Actions section
  5. Verify the 'Deploy Firmware' card is present using selector: a[href='/deployment'].relative.bg-card
    - expect: A clickable card element linking to /deployment is visible in the Quick Actions section
  6. Verify the 'Check Compliance' card is present using selector: main a[href='/compliance']
    - expect: A clickable card element linking to /compliance is visible in the Quick Actions section
  7. Verify that exactly 4 Quick Action card elements are visible in the Quick Actions grid
    - expect: Exactly 4 Quick Action cards are visible: View Inventory, Schedule Service, Deploy Firmware, and Check Compliance — no more, no fewer

#### 1.2. TC-PS6-QA-02: Each Quick Action card displays a descriptive label and icon

**File:** `specs/dashboard/tc-ps6-qa-02-card-labels-and-icons.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for the Quick Actions section to render
    - expect: The Dashboard is loaded and the Quick Actions section is visible
  2. Inspect the 'View Inventory' card (main a[href='/inventory']) and verify it contains the text label 'View Inventory' and an SVG icon element
    - expect: The card displays the label 'View Inventory' and has an associated icon
  3. Inspect the 'Schedule Service' card (main a[href='/account-service']) and verify it contains the text label 'Schedule Service' and an SVG icon element
    - expect: The card displays the label 'Schedule Service' and has an associated icon
  4. Inspect the 'Deploy Firmware' card (a[href='/deployment'].relative.bg-card) and verify it contains the text label 'Deploy Firmware' and an SVG icon element
    - expect: The card displays the label 'Deploy Firmware' and has an associated icon
  5. Inspect the 'Check Compliance' card (main a[href='/compliance']) and verify it contains the text label 'Check Compliance' and an SVG icon element
    - expect: The card displays the label 'Check Compliance' and has an associated icon

### 2. TC-PS6-NAV: Quick Action Cards - Navigation Links

**Seed:** `specs/seed.spec.ts`

#### 2.1. TC-PS6-NAV-01: 'View Inventory' card navigates to the Inventory page

**File:** `specs/dashboard/tc-ps6-nav-01-view-inventory-navigation.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for the Quick Actions section to render
    - expect: The Dashboard is loaded and the 'View Inventory' card is visible
  2. Click the 'View Inventory' quick action card (main a[href='/inventory'])
    - expect: The browser navigates away from the Dashboard
  3. Verify the current URL contains '/inventory'
    - expect: The URL is https://main.dddsig2mih3hw.amplifyapp.com/inventory
  4. Verify the Inventory & Assets page content is rendered (look for a page heading or content indicating the Inventory module)
    - expect: The Inventory & Assets page is displayed — not a 404, blank page, or error state

#### 2.2. TC-PS6-NAV-02: 'Schedule Service' card navigates to the Account and Service page

**File:** `specs/dashboard/tc-ps6-nav-02-schedule-service-navigation.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for the Quick Actions section to render
    - expect: The Dashboard is loaded and the 'Schedule Service' card is visible
  2. Click the 'Schedule Service' quick action card (main a[href='/account-service'])
    - expect: The browser navigates away from the Dashboard
  3. Verify the current URL contains '/account-service'
    - expect: The URL is https://main.dddsig2mih3hw.amplifyapp.com/account-service
  4. Verify the Account and Service page content is rendered
    - expect: The Account and Service page is displayed — not a 404, blank page, or error state

#### 2.3. TC-PS6-NAV-03: 'Deploy Firmware' card navigates to the Deployment page

**File:** `specs/dashboard/tc-ps6-nav-03-deploy-firmware-navigation.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for the Quick Actions section to render
    - expect: The Dashboard is loaded and the 'Deploy Firmware' card is visible
  2. Click the 'Deploy Firmware' quick action card (a[href='/deployment'].relative.bg-card)
    - expect: The browser navigates away from the Dashboard
  3. Verify the current URL contains '/deployment'
    - expect: The URL is https://main.dddsig2mih3hw.amplifyapp.com/deployment
  4. Verify the Deployment page content is rendered
    - expect: The Deployment page is displayed — not a 404, blank page, or error state

#### 2.4. TC-PS6-NAV-04: 'Check Compliance' card navigates to the Compliance page

**File:** `specs/dashboard/tc-ps6-nav-04-check-compliance-navigation.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for the Quick Actions section to render
    - expect: The Dashboard is loaded and the 'Check Compliance' card is visible
  2. Click the 'Check Compliance' quick action card (main a[href='/compliance'])
    - expect: The browser navigates away from the Dashboard
  3. Verify the current URL contains '/compliance'
    - expect: The URL is https://main.dddsig2mih3hw.amplifyapp.com/compliance
  4. Verify the Firmware Compliance page content is rendered
    - expect: The Firmware Compliance page is displayed — not a 404, blank page, or error state

### 3. TC-PS6-BADGE: Quick Action Cards - Live Count Badges

**Seed:** `specs/seed.spec.ts`

#### 3.1. TC-PS6-BADGE-01: 'View Inventory' badge shows count of offline devices

**File:** `specs/dashboard/tc-ps6-badge-01-view-inventory-offline-count.spec.ts`

**Steps:**
  1. Query the GraphQL API using listDevices with status='Offline' to obtain the current offline device count (use the totalCount field from the PaginatedResponse)
    - expect: The API returns a numeric totalCount for offline devices
  2. Navigate to the Dashboard and wait for the Quick Actions section to fully load (wait until no loading indicators are visible)
    - expect: The Dashboard is loaded and Quick Actions badges have resolved their values
  3. Locate the badge on the 'View Inventory' card using selector: main a[href='/inventory'] span.bg-orange-500
    - expect: If the offline device count from the API is greater than zero, the orange badge span is visible on the 'View Inventory' card
  4. Read the text content of the badge element and compare it to the API-reported offline device count
    - expect: The badge text matches the offline device count returned by listDevices(status='Offline')

#### 3.2. TC-PS6-BADGE-02: 'Schedule Service' badge shows count of scheduled service orders

**File:** `specs/dashboard/tc-ps6-badge-02-schedule-service-orders-count.spec.ts`

**Steps:**
  1. Query the GraphQL API using listServiceOrdersByStatus with status='Scheduled' to obtain the count of scheduled service orders
    - expect: The API returns a numeric count (items.length or totalCount) for scheduled service orders
  2. Navigate to the Dashboard and wait for the Quick Actions section to fully load
    - expect: The Dashboard is loaded and Quick Actions badges have resolved their values
  3. Locate the badge on the 'Schedule Service' card using selector: main a[href='/account-service'] span.bg-orange-500
    - expect: If the scheduled orders count is greater than zero, the orange badge span is visible on the 'Schedule Service' card
  4. Read the badge text content and compare it to the scheduled service order count from the API
    - expect: The badge text matches the API-reported count of scheduled service orders

#### 3.3. TC-PS6-BADGE-03: 'Deploy Firmware' badge shows count of pending firmware items

**File:** `specs/dashboard/tc-ps6-badge-03-deploy-firmware-pending-count.spec.ts`

**Steps:**
  1. Query the GraphQL API using listFirmware with status='Pending' to obtain the count of pending firmware items (use totalCount from the response)
    - expect: The API returns a numeric totalCount for pending firmware records
  2. Navigate to the Dashboard and wait for the Quick Actions section to fully load
    - expect: The Dashboard is loaded and Quick Actions badges have resolved their values
  3. Locate the badge on the 'Deploy Firmware' card using selector: a[href='/deployment'].relative.bg-card span.bg-orange-500
    - expect: If the pending firmware count is greater than zero, the orange badge span is visible on the 'Deploy Firmware' card
  4. Read the badge text content and compare it to the pending firmware count from the API
    - expect: The badge text matches the API-reported count of pending firmware items (listFirmware status='Pending')

#### 3.4. TC-PS6-BADGE-04: 'Check Compliance' badge shows count of pending compliance records

**File:** `specs/dashboard/tc-ps6-badge-04-check-compliance-pending-count.spec.ts`

**Steps:**
  1. Query the GraphQL API using listComplianceByStatus with status='Pending' to obtain the count of pending compliance records (use totalCount from the response)
    - expect: The API returns a numeric totalCount for pending compliance records
  2. Navigate to the Dashboard and wait for the Quick Actions section to fully load
    - expect: The Dashboard is loaded and Quick Actions badges have resolved their values
  3. Locate the badge on the 'Check Compliance' card using selector: main a[href='/compliance'] span.bg-orange-500
    - expect: If the pending compliance count is greater than zero, the orange badge span is visible on the 'Check Compliance' card
  4. Read the badge text content and compare it to the pending compliance count from the API
    - expect: The badge text matches the API-reported count of pending compliance records (listComplianceByStatus status='Pending')

#### 3.5. TC-PS6-BADGE-05: Badges are hidden when their count is zero

**File:** `specs/dashboard/tc-ps6-badge-05-badge-hidden-when-zero.spec.ts`

**Steps:**
  1. Intercept the GraphQL API responses for all four badge data sources and mock them to return count=0 for: listDevices(status='Offline'), listServiceOrdersByStatus(status='Scheduled'), listFirmware(status='Pending'), listComplianceByStatus(status='Pending')
    - expect: Network interception is active and will return zero counts before page navigation
  2. Navigate to the Dashboard with the mocked responses active and wait for the Quick Actions section to render
    - expect: The Dashboard loads with zero counts from the mocked API responses
  3. Verify the 'View Inventory' badge is NOT present: main a[href='/inventory'] span.bg-orange-500 must not exist in the DOM or must not be visible
    - expect: No orange badge span appears on the 'View Inventory' card when offline device count is zero
  4. Verify the 'Schedule Service' badge is NOT present: main a[href='/account-service'] span.bg-orange-500 must not exist or must not be visible
    - expect: No orange badge span appears on the 'Schedule Service' card when scheduled orders count is zero
  5. Verify the 'Deploy Firmware' badge is NOT present: a[href='/deployment'].relative.bg-card span.bg-orange-500 must not exist or must not be visible
    - expect: No orange badge span appears on the 'Deploy Firmware' card when pending firmware count is zero
  6. Verify the 'Check Compliance' badge is NOT present: main a[href='/compliance'] span.bg-orange-500 must not exist or must not be visible
    - expect: No orange badge span appears on the 'Check Compliance' card when pending compliance count is zero

#### 3.6. TC-PS6-BADGE-06: Visible badge values are positive integers only

**File:** `specs/dashboard/tc-ps6-badge-06-badge-value-format.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for the Quick Actions section to fully load
    - expect: The Dashboard is loaded and any visible badges have rendered their count values
  2. Query all visible badge elements on the page using selector: main [href] span.bg-orange-500
    - expect: A list of all currently visible orange badge elements is obtained
  3. For each visible badge element, read its text content and parse it as an integer
    - expect: Each badge element contains only numeric text that is parseable as an integer
  4. Assert that every parsed badge value is a positive integer greater than zero
    - expect: All visible badge values are positive integers — no negative numbers, decimals, 'NaN', 'undefined', or non-numeric content appears in any badge element

#### 3.7. TC-PS6-BADGE-07: Sidebar navigation badges do not interfere with Quick Action card badge selectors

**File:** `specs/dashboard/tc-ps6-badge-07-sidebar-badge-isolation.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for the full page to load including the sidebar navigation
    - expect: The Dashboard is fully loaded with both the sidebar nav and the main content Quick Actions area visible
  2. Query badge spans in the sidebar only: nav a[href='/inventory'] span.bg-orange-500 and nav a[href='/account-service'] span.bg-orange-500
    - expect: The count and values of sidebar nav badge spans are recorded for comparison
  3. Query badge spans in the main content area only: main a[href='/inventory'] span.bg-orange-500 and main a[href='/account-service'] span.bg-orange-500
    - expect: The count and values of main content badge spans are recorded separately
  4. Verify that the two sets of badge selectors do not overlap (a 'nav'-scoped selector does not match elements inside 'main' and vice versa)
    - expect: The 'main' scoped selectors return only Quick Action card badges; the 'nav' scoped selectors return only sidebar badges — the selector isolation works correctly
  5. Verify that the badge values in the sidebar and Quick Action cards for the same route (Inventory = /inventory, Schedule Service = /account-service) show the same numeric counts
    - expect: The badge count on the 'View Inventory' Quick Action card matches the badge count on the Inventory sidebar nav link; similarly the 'Schedule Service' badge matches the Account-Service sidebar nav badge

### 4. TC-PS6-WELCOME: Welcome Message

**Seed:** `specs/seed.spec.ts`

#### 4.1. TC-PS6-WELCOME-01: Welcome message displays the logged-in user email address

**File:** `specs/dashboard/tc-ps6-welcome-01-personalized-message.spec.ts`

**Steps:**
  1. Navigate to the application login page at https://main.dddsig2mih3hw.amplifyapp.com
    - expect: The login page is displayed with email and password input fields
  2. Enter credentials: email = ajaykumar.yadav@3pillarglobal.com, password = Secure@12345, then click the submit button
    - expect: Login succeeds and the user is redirected to the Dashboard
  3. Wait for the Dashboard to render and locate the welcome message h2 element using selector: h2:has-text('Welcome back')
    - expect: An h2 element containing 'Welcome back' is visible in the Dashboard main content area
  4. Read the full text content of the welcome message h2 element
    - expect: The text reads 'Welcome back, ajaykumar.yadav@3pillarglobal.com' — the exact email address of the logged-in user appears after 'Welcome back,'

#### 4.2. TC-PS6-WELCOME-02: Welcome message falls back to Admin when no email is available

**File:** `specs/dashboard/tc-ps6-welcome-02-fallback-to-admin.spec.ts`

**Steps:**
  1. Set up a mechanism to simulate an authenticated session where the user email is absent — either intercept the auth/session API response and strip the email field, or manipulate the auth state in localStorage/sessionStorage after login and before the welcome message renders
    - expect: The email field in the user session is absent or empty when the Dashboard loads
  2. Navigate to the Dashboard with the email-less session active and wait for the welcome message to render
    - expect: The Dashboard loads and the welcome message h2 element is visible
  3. Read the full text content of the welcome message h2 element
    - expect: The text reads 'Welcome back, Admin' — the fallback value 'Admin' is displayed when no user email is available in the session

#### 4.3. TC-PS6-WELCOME-03: Welcome message is visible immediately after login with no loading state

**File:** `specs/dashboard/tc-ps6-welcome-03-welcome-visibility.spec.ts`

**Steps:**
  1. Navigate to the Dashboard after successful login with credentials ajaykumar.yadav@3pillarglobal.com / Secure@12345
    - expect: The Dashboard page is fully loaded
  2. Verify the welcome message h2 element is visible (not hidden, not display:none, not zero opacity) using selector: h2:has-text('Welcome back')
    - expect: The h2 element with 'Welcome back' text is present and visible — it is rendered immediately from session data without requiring an additional API call
  3. Verify the welcome message text follows the exact pattern 'Welcome back, {user identifier}' with a comma and space separating the greeting from the identifier
    - expect: The welcome message text contains 'Welcome back,' followed by a space and the user email — the comma and space are present and correctly formatted

### 5. TC-PS6-REFRESH: Manual Refresh Button

**Seed:** `specs/seed.spec.ts`

#### 5.1. TC-PS6-REFRESH-01: Refresh button is visible on the Dashboard with correct aria-label

**File:** `specs/dashboard/tc-ps6-refresh-01-button-visible.spec.ts`

**Steps:**
  1. Navigate to the Dashboard after successful login and wait for the page to fully render
    - expect: The Dashboard page is fully loaded and the welcome row is visible
  2. Locate the Refresh Dashboard button using selector: button[aria-label='Refresh dashboard']
    - expect: A button element with aria-label='Refresh dashboard' is found in the DOM
  3. Verify the button is visible (not hidden, not zero-opacity, not display:none) and is positioned in the top area of the welcome message row (top-right area of the main content)
    - expect: The Refresh Dashboard button is visible in the correct position on the Dashboard
  4. Verify the button contains a circular arrow (refresh/reload) icon — inspect for an SVG element inside the button
    - expect: The button contains an SVG icon element representing a refresh or circular arrow action

#### 5.2. TC-PS6-REFRESH-02: Clicking Refresh button triggers re-fetch of all dashboard data

**File:** `specs/dashboard/tc-ps6-refresh-02-triggers-data-refetch.spec.ts`

**Steps:**
  1. Navigate to the Dashboard after successful login and wait for initial data load to complete (all KPI values show as numeric values — not the em-dash loading placeholder)
    - expect: The Dashboard is in a settled, data-loaded state with all numeric values rendered
  2. Set up network request monitoring to capture all outgoing GraphQL API requests
    - expect: Network monitoring is active before the refresh is triggered
  3. Click the Refresh Dashboard button (button[aria-label='Refresh dashboard'])
    - expect: The button click is registered and a refresh cycle begins
  4. Monitor the captured network requests and verify that GraphQL queries are re-issued after the click — specifically check for queries: listDevices, listServiceOrdersByStatus or related service order query, listFirmware, listComplianceByStatus
    - expect: At least the KPI-related and badge-count GraphQL queries are re-sent to the server after the refresh button click, confirming that a full data re-fetch is triggered
  5. Wait for the dashboard data to settle after refresh and verify all KPI values and Quick Action badge counts are still rendering valid numeric data
    - expect: After refresh completes, all Dashboard widgets including Quick Action badges display refreshed, valid numeric data — no error states or loading placeholders remain

#### 5.3. TC-PS6-REFRESH-03: Refresh button shows spinning animation during active refresh

**File:** `specs/dashboard/tc-ps6-refresh-03-spinning-animation.spec.ts`

**Steps:**
  1. Navigate to the Dashboard after successful login and wait for the page to fully load in settled state
    - expect: The Dashboard is loaded and the Refresh button is visible and not animating
  2. Click the Refresh Dashboard button (button[aria-label='Refresh dashboard'])
    - expect: The click is registered and the refresh cycle begins
  3. Immediately after clicking (within 100-500ms), inspect the Refresh button's icon SVG element for a spinning animation CSS class — check for 'animate-spin' (Tailwind animation utility) or equivalent rotation animation class on the icon inside button[aria-label='Refresh dashboard']
    - expect: The icon inside the Refresh button has an animation class applied (e.g., 'animate-spin') — the button icon visually rotates to indicate active data loading
  4. Wait for the refresh cycle to complete (wait until all network requests initiated by the refresh have received responses)
    - expect: The spinning animation stops after the refresh completes — the 'animate-spin' class or equivalent is removed from the icon, returning the button to its normal static appearance

#### 5.4. TC-PS6-REFRESH-04: Refresh button is disabled during active refresh to prevent duplicate requests

**File:** `specs/dashboard/tc-ps6-refresh-04-disabled-during-refresh.spec.ts`

**Steps:**
  1. Navigate to the Dashboard after successful login and wait for the page to fully load
    - expect: The Dashboard is in a settled state with the Refresh button visible and enabled
  2. Verify the Refresh button is NOT disabled before clicking: check that button[aria-label='Refresh dashboard'] does not have the 'disabled' HTML attribute and is not aria-disabled='true'
    - expect: The Refresh button is in an enabled/interactive state before any refresh is initiated
  3. Click the Refresh Dashboard button to initiate a refresh cycle
    - expect: The refresh cycle begins
  4. Immediately after clicking (before the refresh completes), verify the Refresh button now has the 'disabled' attribute or is aria-disabled='true', making it non-interactive
    - expect: The Refresh button is disabled during the active refresh cycle — the disabled attribute is present on the button element
  5. Attempt to click the disabled Refresh button again and monitor network requests
    - expect: Clicking the disabled button does not trigger additional API requests — no duplicate GraphQL queries are dispatched during the active refresh
  6. Wait for the refresh to complete and verify the button transitions back to an enabled state
    - expect: After the refresh completes, the Refresh button's disabled attribute is removed and the button is interactive again — it can be clicked for a new refresh cycle

#### 5.5. TC-PS6-REFRESH-05: Refresh button updates Quick Action badge counts with fresh API data

**File:** `specs/dashboard/tc-ps6-refresh-05-badge-counts-updated-after-refresh.spec.ts`

**Steps:**
  1. Navigate to the Dashboard after successful login and record the current badge values from all visible Quick Action cards (note which badges are visible and their text content)
    - expect: Initial badge count values are captured for all visible badges before refresh
  2. Click the Refresh Dashboard button and wait for the refresh cycle to complete
    - expect: The refresh cycle runs fully — spinning animation appears and then stops, indicating refresh completion
  3. After refresh completes, re-read the badge values on all Quick Action cards
    - expect: Badge values are re-rendered after the refresh — they reflect the most current data from the backend APIs. If backend data has not changed, the values remain numerically consistent with pre-refresh values; the key assertion is that the badge rendering is intact after refresh
  4. Verify no stale-data or error indicators are present: check that no em-dash loading placeholder (U+2014) appears in any KPI or badge element after refresh completes
    - expect: All Quick Action badge values are resolved numeric counts after refresh — no loading placeholders remain and no badge shows an invalid or malformed value

### 6. TC-PS6-ERR: Error and Edge Case Handling

**Seed:** `specs/seed.spec.ts`

#### 6.1. TC-PS6-ERR-01: Badge values are never negative numbers

**File:** `specs/dashboard/tc-ps6-err-01-no-negative-badge-values.spec.ts`

**Steps:**
  1. Navigate to the Dashboard and wait for all Quick Action badges to resolve their values
    - expect: The Dashboard is loaded with all available badge counts rendered
  2. Query all visible orange badge elements using selector: main span.bg-orange-500 and read their text content
    - expect: All visible badge spans have text content that can be read
  3. Parse each badge text value as an integer and assert it is strictly greater than zero
    - expect: No badge displays a value of zero or any negative number — per AC-6, badges are only shown when count > 0, so any visible badge must have a value of at least 1

#### 6.2. TC-PS6-ERR-02: Quick Action cards remain accessible when badge API returns an error

**File:** `specs/dashboard/tc-ps6-err-02-cards-accessible-on-api-error.spec.ts`

**Steps:**
  1. Intercept the GraphQL API calls that supply badge count data (specifically: listDevices, listServiceOrdersByStatus, listFirmware, listComplianceByStatus) and configure them to return HTTP 500 error responses
    - expect: Network interception is active and will return errors for badge-count queries on the next navigation
  2. Navigate to the Dashboard with the mocked error responses active and wait for the page to settle
    - expect: The Dashboard loads despite the API errors affecting badge count data
  3. Verify all four Quick Action cards are still visible and rendered: main a[href='/inventory'], main a[href='/account-service'], a[href='/deployment'].relative.bg-card, main a[href='/compliance']
    - expect: All four cards remain visible — the cards do not disappear or collapse due to badge API errors
  4. Verify that no raw error text, 'NaN', 'undefined', 'null', or malformed content appears inside badge positions on any of the four cards
    - expect: Badge positions show either nothing (badge hidden gracefully) or a valid number — no error strings are exposed to the user in badge elements
  5. Click each Quick Action card in turn and verify each one navigates to its expected route (/inventory, /account-service, /deployment, /compliance)
    - expect: All four Quick Action card navigation links remain fully functional even when badge API calls have failed — the core navigation functionality is not broken by badge API errors

#### 6.3. TC-PS6-ERR-03: Dashboard survives a page reload and re-renders all Quick Action features correctly

**File:** `specs/dashboard/tc-ps6-err-03-page-reload-resilience.spec.ts`

**Steps:**
  1. Log in to the application and navigate to the Dashboard, confirming all Quick Action cards, welcome message, and Refresh button are visible
    - expect: The initial Dashboard load is successful with all PS-6 features visible
  2. Perform a hard browser page reload (Ctrl+R or equivalent) while on the Dashboard
    - expect: The page reloads and the login session is preserved (user is not logged out)
  3. After reload, verify the welcome message h2 is visible with the correct user email
    - expect: The welcome message re-renders correctly after reload with the logged-in user's email
  4. Verify all four Quick Action cards are visible after reload
    - expect: All four cards are present and visible after the hard reload
  5. Verify the Refresh button is visible and in an enabled state after reload
    - expect: The Refresh Dashboard button is visible and interactive after the page reload

#### 6.4. TC-PS6-ERR-04: Direct URL navigation to Dashboard renders all PS-6 features

**File:** `specs/dashboard/tc-ps6-err-04-direct-url-access.spec.ts`

**Steps:**
  1. Log in to the application to establish an authenticated session, then note the session cookies or storage
    - expect: Authenticated session is established
  2. Navigate directly to the root URL https://main.dddsig2mih3hw.amplifyapp.com/ (bypassing any in-app navigation)
    - expect: The page loads without redirecting to the login page — the existing authenticated session is honored
  3. Verify the welcome message h2 (h2:has-text('Welcome back')) is visible with the logged-in user's email
    - expect: The personalized welcome message is rendered on direct URL navigation
  4. Verify all four Quick Action cards are visible in the main content area
    - expect: All four cards — View Inventory, Schedule Service, Deploy Firmware, Check Compliance — are visible
  5. Verify the Refresh Dashboard button (button[aria-label='Refresh dashboard']) is visible and enabled
    - expect: The Refresh button is present and interactive on the Dashboard after direct URL navigation
