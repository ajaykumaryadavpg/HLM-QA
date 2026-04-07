# PS-20: Replace Static Map in Inventory Module - Geo Location Tab Test Plan

## Application Overview

This test plan covers JIRA Story PS-20: "Replace Static Map in Inventory Module". The Inventory module's Geo Location tab has been updated to replace a static SVG map with an interactive MapLibre-based Amazon Location map. The plan verifies the new map renders correctly, all 12 device pins display with correct status colors (green=Online, red=Offline, orange=Maintenance), all four filter buttons (All, Online, Offline, Maintenance) work correctly, stat pills show correct device counts, device detail cards open on pin click and close via the X button, the map supports zoom and pan interactions, and that the Hardware Inventory and Firmware Status sibling tabs remain unaffected. Console errors are also checked.

Application URL: https://main.dddsig2mih3hw.amplifyapp.com
Login: ajaykumar.yadav@3pillarglobal.com / Secure@12345
Total devices in test data: 12 (6 Online, 3 Offline, 3 Maintenance)

## Test Scenarios

### 1. Authentication

**Seed:** `seed.spec.ts`

#### 1.1. User can log in with valid credentials and reach the dashboard

**File:** `tests/ps-20/authentication.spec.ts`

**Steps:**
  1. Navigate to https://main.dddsig2mih3hw.amplifyapp.com
    - expect: Page redirects to /login
    - expect: Login page is displayed with heading 'Sign in to your account'
    - expect: Email and Password fields are visible
  2. Enter 'ajaykumar.yadav@3pillarglobal.com' in the Email address field
    - expect: Email field accepts the value
  3. Enter 'Secure@12345' in the Password field
    - expect: Password field accepts the value (masked)
  4. Click the 'Sign in' button
    - expect: Button shows 'Signing in…' loading state while authenticating
    - expect: Page navigates to / (Dashboard)
    - expect: Dashboard heading is visible
    - expect: Navigation sidebar shows: Dashboard, Inventory & Assets, Account & Service, Deployment, Firmware Compliance, Reporting & Analytics

### 2. Geo Location Tab - Map Rendering

**Seed:** `seed.spec.ts`

#### 2.1. Geo Location tab renders the interactive MapLibre map and not a static SVG map

**File:** `tests/ps-20/geo-location-map-rendering.spec.ts`

**Steps:**
  1. Log in and navigate to /inventory
    - expect: Inventory & Assets page is displayed with the Hardware Inventory tab active by default
  2. Click the 'Geo Location' tab button
    - expect: Geo Location tab becomes active (aria-pressed or active class applied)
    - expect: The Hardware Inventory table is no longer visible
    - expect: The Geo Location view is rendered
  3. Inspect the page for map elements: check for presence of a canvas element (MapLibre renders to canvas), maplibregl-map container class, and MapLibre attribution link
    - expect: At least 1 canvas element is present in the DOM
    - expect: A container with class 'maplibregl-map' or 'maplibregl-canvas' is present
    - expect: A region with aria-label='Map' is rendered
    - expect: MapLibre attribution link is visible in the bottom-right of the map
    - expect: OpenStreetMap attribution link is visible
  4. Inspect the page for the absence of a static SVG map element (svg[class*='map'], svg[id*='map'], .static-map)
    - expect: No static SVG map element is found in the DOM
    - expect: The old SVG map has been replaced
  5. Verify all 12 device pins (Map marker buttons) are rendered on the map
    - expect: Exactly 12 elements with aria-label='Map marker' are present in the map container
    - expect: All pins are circular dot-shaped with a white border

#### 2.2. Map legend is displayed with correct status color indicators

**File:** `tests/ps-20/geo-location-map-rendering.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, and click the Geo Location tab
    - expect: Geo Location view is displayed
  2. Locate the map legend panel (bottom-right area of the map view)
    - expect: A legend panel is visible containing three entries: 'Online', 'Offline', 'Maintenance'
  3. Verify the color indicator for 'Online' in the legend
    - expect: Online legend indicator uses green color (rgb(34, 197, 94))
  4. Verify the color indicator for 'Offline' in the legend
    - expect: Offline legend indicator uses red color (rgb(239, 68, 68))
  5. Verify the color indicator for 'Maintenance' in the legend
    - expect: Maintenance legend indicator uses orange color (rgb(249, 115, 22))

### 3. Geo Location Tab - Device Pin Colors

**Seed:** `seed.spec.ts`

#### 3.1. Online device pins render with green color (rgb(34, 197, 94))

**File:** `tests/ps-20/geo-location-pin-colors.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the Geo Location tab, then click the 'Online(6)' filter button
    - expect: Only 6 Map marker buttons are visible on the map
    - expect: Online filter button is in active/highlighted state
  2. Inspect the background-color CSS property of each of the 6 visible Map marker div elements
    - expect: All 6 visible markers have background-color: rgb(34, 197, 94)
    - expect: No red or orange markers are present when Online filter is active

#### 3.2. Offline device pins render with red color (rgb(239, 68, 68))

**File:** `tests/ps-20/geo-location-pin-colors.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the Geo Location tab, then click the 'Offline(3)' filter button
    - expect: Only 3 Map marker buttons are visible on the map
    - expect: Offline filter button is in active/highlighted state
  2. Inspect the background-color CSS property of each of the 3 visible Map marker div elements
    - expect: All 3 visible markers have background-color: rgb(239, 68, 68)
    - expect: No green or orange markers are present when Offline filter is active

#### 3.3. Maintenance device pins render with orange color (rgb(249, 115, 22))

**File:** `tests/ps-20/geo-location-pin-colors.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the Geo Location tab, then click the 'Maintenance(3)' filter button
    - expect: Only 3 Map marker buttons are visible on the map
    - expect: Maintenance filter button is in active/highlighted state
  2. Inspect the background-color CSS property of each of the 3 visible Map marker div elements
    - expect: All 3 visible markers have background-color: rgb(249, 115, 22)
    - expect: No green or red markers are present when Maintenance filter is active

#### 3.4. All 12 pins appear when the All filter is active with correct color distribution

**File:** `tests/ps-20/geo-location-pin-colors.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the Geo Location tab, and verify the 'All' filter is selected by default
    - expect: 'All' button has active styling (blue background: bg-[#2563eb] text-white)
    - expect: All 12 Map marker buttons are visible
  2. Count map markers by each background-color among the 12 visible pins
    - expect: Exactly 6 markers have green color rgb(34, 197, 94) - Online
    - expect: Exactly 3 markers have red color rgb(239, 68, 68) - Offline
    - expect: Exactly 3 markers have orange color rgb(249, 115, 22) - Maintenance

### 4. Geo Location Tab - Stat Pills

**Seed:** `seed.spec.ts`

#### 4.1. Stat pills display correct device counts matching the global summary bar

**File:** `tests/ps-20/geo-location-stat-pills.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, and note the summary bar counts above the tabs: Total Devices, Online, In Maintenance, Offline
    - expect: Summary bar shows: Total Devices=12, Online=6, In Maintenance=3, Offline=3
  2. Click the Geo Location tab
    - expect: Geo Location stat pills section is visible above the filter buttons
  3. Read the values in all four stat pills in the Geo Location section
    - expect: 'Total' stat pill shows the value 12
    - expect: 'Online' stat pill shows the value 6
    - expect: 'Offline' stat pill shows the value 3
    - expect: 'Maintenance' stat pill shows the value 3
  4. Verify that the stat pill counts match the filter button counts
    - expect: Online stat pill (6) matches Online filter button label 'Online(6)'
    - expect: Offline stat pill (3) matches Offline filter button label 'Offline(3)'
    - expect: Maintenance stat pill (3) matches Maintenance filter button label 'Maintenance(3)'

### 5. Geo Location Tab - Filter Buttons

**Seed:** `seed.spec.ts`

#### 5.1. All filter button is active by default and shows all 12 map markers

**File:** `tests/ps-20/geo-location-filters.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, and click the Geo Location tab
    - expect: Geo Location view loads
  2. Observe the default selected filter button without clicking anything
    - expect: 'All' button is in active state with blue background styling (bg-[#2563eb] text-white)
    - expect: All 12 Map marker buttons are rendered on the map
    - expect: No filter button other than 'All' is highlighted

#### 5.2. Online filter button shows only 6 Online device pins

**File:** `tests/ps-20/geo-location-filters.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab, then click the 'Online(6)' filter button
    - expect: 'Online(6)' button becomes active (highlighted in blue or distinct active style)
    - expect: 'All' button is no longer active
  2. Count the Map marker buttons currently visible on the map
    - expect: Exactly 6 Map marker buttons are present in the map container
    - expect: All 6 visible markers have green background-color rgb(34, 197, 94)

#### 5.3. Offline filter button shows only 3 Offline device pins

**File:** `tests/ps-20/geo-location-filters.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab, then click the 'Offline(3)' filter button
    - expect: 'Offline(3)' button becomes active
    - expect: 'All' and 'Online' buttons are no longer active
  2. Count the Map marker buttons currently visible on the map
    - expect: Exactly 3 Map marker buttons are present in the map container
    - expect: All 3 visible markers have red background-color rgb(239, 68, 68)

#### 5.4. Maintenance filter button shows only 3 Maintenance device pins

**File:** `tests/ps-20/geo-location-filters.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab, then click the 'Maintenance(3)' filter button
    - expect: 'Maintenance(3)' button becomes active
    - expect: Other filter buttons are not active
  2. Count the Map marker buttons currently visible on the map
    - expect: Exactly 3 Map marker buttons are present in the map container
    - expect: All 3 visible markers have orange background-color rgb(249, 115, 22)

#### 5.5. Switching from a status filter back to All restores all 12 pins

**File:** `tests/ps-20/geo-location-filters.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab, click 'Online(6)', then click 'All'
    - expect: 'All' button becomes active again
  2. Count the Map marker buttons visible on the map
    - expect: All 12 Map marker buttons are restored on the map
    - expect: Markers include green, red, and orange pins

#### 5.6. Clicking each filter in sequence (All -> Online -> Offline -> Maintenance -> All) works correctly

**File:** `tests/ps-20/geo-location-filters.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the Geo Location tab
    - expect: All filter active, 12 markers visible
  2. Click 'Online(6)' filter
    - expect: 6 markers visible, all green
  3. Click 'Offline(3)' filter
    - expect: 3 markers visible, all red
  4. Click 'Maintenance(3)' filter
    - expect: 3 markers visible, all orange
  5. Click 'All' filter
    - expect: 12 markers visible with mixed green/red/orange colors
    - expect: Only one filter button is active at a time throughout the sequence

### 6. Geo Location Tab - Device Detail Card

**Seed:** `seed.spec.ts`

#### 6.1. Clicking a Maintenance device pin opens a detail card with correct data

**File:** `tests/ps-20/geo-location-detail-card.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab, click 'Maintenance(3)' filter, then force-click the first Map marker button
    - expect: A device detail card panel appears adjacent to or overlaying the map
  2. Read the content of the device detail card heading and status badge
    - expect: Card heading (h4) shows a device name (e.g., 'Firewall-DMZ-02')
    - expect: Status badge shows 'Maintenance'
  3. Verify all detail fields are present in the card
    - expect: 'Serial:' field is displayed with a value
    - expect: 'Model:' field is displayed with a value
    - expect: 'Firmware:' field is displayed with a version string
    - expect: 'Health:' field is displayed with a percentage value
    - expect: 'Customer:' field is displayed with a customer ID
    - expect: 'City:' field is displayed with a city and country
    - expect: 'Location:' field is displayed with a physical location string
    - expect: 'Last Seen:' field is displayed (may show em dash if not available)

#### 6.2. Clicking an Online device pin opens a detail card showing Online status

**File:** `tests/ps-20/geo-location-detail-card.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab, click 'Online(6)' filter, then force-click the first Map marker button
    - expect: A device detail card appears
  2. Read the status badge in the device detail card
    - expect: Status badge shows 'Online'
    - expect: Health percentage is a relatively high value (>= 85% for online devices observed in test data)

#### 6.3. Clicking an Offline device pin opens a detail card showing Offline status

**File:** `tests/ps-20/geo-location-detail-card.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab, click 'Offline(3)' filter, then force-click the first Map marker button
    - expect: A device detail card appears
  2. Read the status badge in the device detail card
    - expect: Status badge shows 'Offline'
    - expect: Health percentage is a low value (e.g., 0–10% for offline devices observed in test data)

#### 6.4. Device detail card closes when the X button is clicked

**File:** `tests/ps-20/geo-location-detail-card.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab, force-click any Map marker button to open the detail card
    - expect: Device detail card is visible with device information
  2. Click the X (close) button located in the top-right corner of the detail card
    - expect: The device detail card is removed from the DOM or hidden
    - expect: The map view is restored without the detail card overlay
    - expect: Map markers remain visible on the map

#### 6.5. Only one device detail card is visible at a time

**File:** `tests/ps-20/geo-location-detail-card.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab with All filter active, force-click the first Map marker
    - expect: First device detail card opens showing device 1 information
  2. Without closing the first card, force-click a second Map marker
    - expect: The detail card updates to show the second device's information OR the first card closes and a new card opens
    - expect: At no point are two device detail cards visible simultaneously

### 7. Geo Location Tab - Map Interactivity

**Seed:** `seed.spec.ts`

#### 7.1. Zoom in button increases the map zoom level

**File:** `tests/ps-20/geo-location-map-interactivity.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the Geo Location tab
    - expect: Map is rendered with default zoom level
  2. Click the 'Zoom in' button (+ icon) in the map controls panel
    - expect: The 'Zoom in' button shows an active/pressed state briefly
    - expect: The map visually zooms in (tile resolution increases and map re-renders)
    - expect: No JavaScript errors are thrown in the console from this action
  3. Click the 'Zoom in' button two more times
    - expect: The map continues to zoom in with each click
    - expect: Pins remain visible on the map and are still clickable

#### 7.2. Zoom out button decreases the map zoom level

**File:** `tests/ps-20/geo-location-map-interactivity.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the Geo Location tab, then click 'Zoom in' twice to establish a zoomed state
    - expect: Map is at an increased zoom level
  2. Click the 'Zoom out' button (- icon) in the map controls panel
    - expect: The 'Zoom out' button shows an active/pressed state briefly
    - expect: The map zooms out (wider geographic area becomes visible)
    - expect: No JavaScript errors in the console

#### 7.3. Map can be panned by click-dragging

**File:** `tests/ps-20/geo-location-map-interactivity.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the Geo Location tab
    - expect: Map canvas (region with aria-label='Map') is rendered
  2. Perform a drag action on the map canvas: mouse down at center of map, move 100px right and 50px down, mouse up
    - expect: The map region gets an active state during drag
    - expect: The map pans (geographic content shifts in the drag direction)
    - expect: Pins reposition relative to the new map center
    - expect: No JavaScript errors in the console

#### 7.4. Compass/North reset button is present and interactive

**File:** `tests/ps-20/geo-location-map-interactivity.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the Geo Location tab
    - expect: Map controls panel is visible
  2. Verify that the compass button with label 'Drag to rotate map, click to reset north' is present in the controls panel
    - expect: The compass/north-reset button is rendered within the zoom controls group
  3. Click the compass button
    - expect: The map rotation is reset to north (no error thrown, button is responsive)

#### 7.5. Map attribution panel can be toggled

**File:** `tests/ps-20/geo-location-map-interactivity.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the Geo Location tab
    - expect: Map attribution with 'MapLibre' and 'OpenStreetMap' links is visible at bottom-right
  2. Click the 'Toggle attribution' element in the map controls
    - expect: Attribution panel visibility toggles (expands or collapses)
    - expect: No errors are thrown

### 8. Geo Location Tab - Tab Switching

**Seed:** `seed.spec.ts`

#### 8.1. Hardware Inventory tab is unaffected and renders the device table correctly

**File:** `tests/ps-20/tab-switching.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory
    - expect: Hardware Inventory tab is active by default
  2. Verify the Hardware Inventory table is displayed with correct columns
    - expect: Table headers: Device Name, Serial Number, Model, Location, Status, Firmware, Customer, Actions
    - expect: Table shows 6 rows on page 1 of 2 (12 total results)
    - expect: Pagination controls show 'Showing 1 to 6 of 12 results' and 'Page 1 of 2'
  3. Click the Geo Location tab, then click back to Hardware Inventory tab
    - expect: Hardware Inventory tab becomes active again
    - expect: Device table is re-rendered with identical data
    - expect: Search box and Filter button are visible
    - expect: Pagination is at page 1 of 2
  4. Verify the map elements (canvas, Map markers) are no longer visible after switching back
    - expect: No map canvas is rendered in the Hardware Inventory view
    - expect: No Map marker buttons are present in the DOM within the tab content area

#### 8.2. Firmware Status tab is unaffected and renders firmware cards correctly

**File:** `tests/ps-20/tab-switching.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the 'Firmware Status' tab
    - expect: Firmware Status tab becomes active
    - expect: Hardware Inventory table is no longer visible
    - expect: Geo Location map is not rendered
  2. Verify the Firmware Status view shows device cards with the expected fields
    - expect: 12 device firmware cards are displayed (all devices)
    - expect: Each card shows: Device name, Model, Status badge (Online/Offline/Maintenance), Firmware version, Last Update date, Health Score percentage
    - expect: Search box is visible for filtering firmware cards
  3. Verify a sample of device data matches known test data: UPS-POWER-05 (Online, v1.4.2, Health 85%), Server-APP-03 (Online, v5.1.3, Health 90%), Sensor-ENV-09 (Offline, v0.9.1, Health 10%), Firewall-DMZ-02 (Maintenance, v2.3.8, Health 60%)
    - expect: UPS-POWER-05 shows Online status, firmware v1.4.2, health score 85%
    - expect: Server-APP-03 shows Online status, firmware v5.1.3, health score 90%
    - expect: Sensor-ENV-09 shows Offline status, firmware v0.9.1, health score 10%
    - expect: Firewall-DMZ-02 shows Maintenance status, firmware v2.3.8, health score 60%
  4. Click the Geo Location tab, then click back to Firmware Status tab
    - expect: Firmware Status tab becomes active again
    - expect: All 12 device firmware cards are displayed
    - expect: Data is consistent with what was shown before

#### 8.3. Tab navigation does not cause the page to reload or lose the top-level summary bar

**File:** `tests/ps-20/tab-switching.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory
    - expect: Summary bar shows: Total Devices=12, Online=6, In Maintenance=3, Offline=3
  2. Click Geo Location tab, then Firmware Status tab, then Hardware Inventory tab, then Geo Location tab
    - expect: Summary bar always shows: Total Devices=12, Online=6, In Maintenance=3, Offline=3 throughout all tab switches
    - expect: Page URL remains /inventory throughout (no page-level navigation)

### 9. Console Error Verification

**Seed:** `seed.spec.ts`

#### 9.1. No critical console errors appear during Geo Location tab usage

**File:** `tests/ps-20/console-errors.spec.ts`

**Steps:**
  1. Start collecting browser console messages, then log in, navigate to /inventory, and click the Geo Location tab
    - expect: Page loads without any console errors related to the map module
    - expect: No 'Cannot read properties of undefined' or 'null' pointer exceptions in the console
    - expect: No network errors for map tile loading (tiles load from the tile provider)
    - expect: vite.svg 404 error may be present (pre-existing, not related to PS-20 changes)
  2. Click each filter button (All, Online, Offline, Maintenance) in sequence
    - expect: No console errors during filter switching
    - expect: No 'Expected value to be of type number, but found null' repeated errors from filter operations (this warning is noted during zoom, confirm it does not recur on filter changes)
  3. Force-click a map marker to open the device detail card, then click the X button to close it
    - expect: No console errors during device detail card open/close operations
    - expect: No React key warnings or component unmount errors
  4. Click 'Zoom in' and 'Zoom out' buttons once each
    - expect: No critical errors in the console
    - expect: The 'Expected value to be of type number, but found null' warning from the MapLibre library during zoom is a known/pre-existing issue and should be documented but not treated as a blocker if it has no visual impact
  5. Pan the map by dragging and verify the console after the drag
    - expect: No console errors during map pan operations
  6. Switch between Hardware Inventory, Firmware Status, and Geo Location tabs multiple times
    - expect: No console errors related to component mounting/unmounting of the map
    - expect: No memory leak warnings
    - expect: The Amplify configuration warning is a pre-existing issue unrelated to PS-20 and should be documented separately

### 10. Geo Location Tab - Edge Cases and Boundary Conditions

**Seed:** `seed.spec.ts`

#### 10.1. Detail card 'Last Seen' field handles null or missing data gracefully

**File:** `tests/ps-20/geo-location-edge-cases.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab, force-click a map marker to open the detail card
    - expect: Device detail card is opened
  2. Read the 'Last Seen:' field value in the detail card
    - expect: If 'Last Seen' data is not available, the field displays an em dash (—) as a placeholder rather than showing 'null', 'undefined', or leaving the field blank

#### 10.2. Stat pill counts remain consistent when switching between filter views

**File:** `tests/ps-20/geo-location-edge-cases.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab, observe stat pill values
    - expect: Total=12, Online=6, Offline=3, Maintenance=3
  2. Click 'Online(6)' filter, then read the stat pill values again
    - expect: Stat pills continue to show Total=12, Online=6, Offline=3, Maintenance=3 (stat pills are static counts, not affected by filter selection)
  3. Click 'Offline(3)' filter, then read the stat pill values again
    - expect: Stat pills continue to show Total=12, Online=6, Offline=3, Maintenance=3
  4. Click 'Maintenance(3)' filter, then read the stat pill values again
    - expect: Stat pills continue to show Total=12, Online=6, Offline=3, Maintenance=3

#### 10.3. Filter buttons display the correct count suffix matching stat pills

**File:** `tests/ps-20/geo-location-edge-cases.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click the Geo Location tab
    - expect: Filter buttons are visible
  2. Read the text of each filter button
    - expect: 'All' button text is 'All' (no count suffix)
    - expect: 'Online' filter button text is 'Online(6)'
    - expect: 'Offline' filter button text is 'Offline(3)'
    - expect: 'Maintenance' filter button text is 'Maintenance(3)'
    - expect: Counts in filter button labels match the corresponding stat pill values

#### 10.4. Navigating away from Inventory and returning resets Geo Location tab state

**File:** `tests/ps-20/geo-location-edge-cases.spec.ts`

**Steps:**
  1. Log in, navigate to /inventory, click Geo Location tab, then click 'Offline(3)' filter
    - expect: Offline filter is active and 3 markers shown
  2. Click 'Dashboard' in the left navigation, then click 'Inventory & Assets' to return
    - expect: Inventory page loads
  3. Click the Geo Location tab
    - expect: Geo Location tab renders correctly
    - expect: Map is displayed with markers
    - expect: Either the 'All' filter is active by default (fresh state) OR the previous filter state is preserved consistently
    - expect: No broken map or blank canvas is displayed
