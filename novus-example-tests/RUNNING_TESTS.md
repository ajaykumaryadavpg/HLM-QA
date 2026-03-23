# Running DashboardApiTests — Step-by-Step Guide

This guide covers everything needed to set up the environment and execute the
`DashboardApiTests` suite against the HLM Platform (Hardware Lifecycle Management).

---

## Prerequisites

| Requirement | Version | Check |
|---|---|---|
| Java | **17** (exact) | `java -version` |
| Maven | 3.8+ | `mvn --version` |
| Internet | — | Must reach `https://main.dddsig2mih3hw.amplifyapp.com` |

---

## Step 1 — Clone and verify the repo

```bash
git clone <repo-url>
cd HLM-QA
java -version    # must show Java 17
mvn --version    # must show Maven 3.8+
```

---

## Step 2 — Build all modules

Run from the **repo root** to compile all four modules in dependency order
(`novus-core` → `novus-core-ui` → `novus-core-api` → `novus-example-tests`):

```bash
mvn clean install -DskipTests
```

A successful build ends with:
```
[INFO] BUILD SUCCESS
```

---

## Step 3 — Install Playwright browser binaries (one-time)

Playwright bundles its own browsers but must download them before first use:

```bash
mvn exec:java -e \
  -D exec.mainClass=com.microsoft.playwright.CLI \
  -D exec.args="install" \
  -pl novus-core-ui
```

This downloads Chromium, Firefox, and WebKit into `~/.cache/ms-playwright/`.
Only needs to be run once (or after upgrading the Playwright dependency).

---

## Step 4 — Review credentials

Default credentials are stored in:

```
novus-example-tests/src/test/resources/application-inventory.properties
```

```properties
inventory.admin.username=ajaykumar.yadav@3pillarglobal.com
inventory.admin.password=Secure@12345
```

To override without editing the file, append these flags to any `mvn` command:

```bash
-Dinventory.admin.username=your@email.com \
-Dinventory.admin.password=YourPassword
```

---

## Step 5 — Understand the active Spring profiles

`DashboardApiTests` (via `InventoryTestBase`) activates three profiles automatically
via `@ActiveProfiles({"web", "inventory", "local"})`:

| Profile | Config file | What it controls |
|---|---|---|
| `web` | `application-web.properties` | Browser size, slowmo, headless flag |
| `inventory` | `application-inventory.properties` | AUT base URL + credentials |
| `local` | `application-local.properties` | Overrides `browser.headless=false` — browser opens visibly |

When running locally, the browser window will be **visible** because of the `local` profile.
Remove `local` from `@ActiveProfiles` (or override the property) for a headless run.

---

## Step 6 — Run DashboardApiTests only

From the repo root:

```bash
cd novus-example-tests
mvn clean test -DsuiteXmlFile=src/test/resources/dashboard-api-suite.xml
```

Suite: `Inventory Management – Dashboard API Data Layer Tests`
Test class: `DashboardApiTests`

What happens:
1. Spring Boot context starts with profiles `web`, `inventory`, `local`.
2. `InventoryTestBase.loginToApplication()` launches the browser, navigates to the app,
   and authenticates as admin — **once per class**.
3. Each enabled test method runs, asserting KPI card visibility and data loading.
4. A full-page screenshot is captured automatically on any failure.
5. An HTML report is generated at the end.

---

## Step 7 — Run with login tests first (recommended)

The `inventory-management-suite.xml` suite runs `InventoryLoginTests` before
`DashboardApiTests`, which is useful for catching auth regressions at the same time:

```bash
mvn clean test -DsuiteXmlFile=src/test/resources/inventory-management-suite.xml
```

---

## Step 8 — Run only the SMOKE_TESTS group (fastest subset)

Four tests in `DashboardApiTests` are tagged `SMOKE_TESTS`:

| Test ID | Method |
|---|---|
| TC-8.1.02 | `testAllDeviceRecordsAreFetched` |
| TC-8.1.04 | `testInProgressServiceOrdersAreFetched` |
| TC-8.1.06 | `testPendingFirmwareRecordsAreFetched` |
| TC-8.1.09 | `testKpiCardsTransitionFromPlaceholderToLiveValues` |

```bash
mvn clean test \
  -DsuiteXmlFile=src/test/resources/dashboard-api-suite.xml \
  -Dgroups=SMOKE_TESTS
```

---

## Step 9 — View the report and screenshots

After a run, open the HTML report:

```
novus-example-tests/src/test/resources/reports/<yyyy-MM-dd>/Dashboard-API-Report.html
```

Failure screenshots (full-page) are saved to:

```
novus-example-tests/src/test/resources/screenshots/
```

---

## Disabled tests — what they need

The following tests have `enabled = false` and will be skipped automatically:

| Test ID | Reason disabled |
|---|---|
| TC-8.1.01 | Requires `page.on("request")` network monitoring — not yet in the Novus framework |
| TC-8.1.08 | Timing-sensitive loading-state capture — same network interception requirement |
| TC-8.1.10 | Requires `page.route()` to stub HTTP 500 on one API |
| TC-8.1.11 | Requires `page.route()` to block all six APIs |
| TC-8.1.12 | Requires `page.route()` with a known error-message payload |

These tests are specification-complete placeholders. Enable them once
`InterceptNetwork` (already scaffolded in `novus-core-ui`) exposes a network-stubbing API.

---

## Data preconditions for badge-visibility tests

Two enabled tests have backend data requirements:

| Test | Precondition |
|---|---|
| TC-8.1.03 `testOfflineDevicesAreFetchedSeparately` | At least one device with `Offline` status must exist in the backend |
| TC-8.1.05 `testScheduledServiceOrdersAreFetched` | At least one service order with `Scheduled` status must exist |

If the environment has no matching data, these tests will fail by design
(the badge element will not render).

---

## Quick-reference command summary

```bash
# 1. Build
mvn clean install -DskipTests

# 2. Install browsers (one-time)
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install" -pl novus-core-ui

# 3a. Run DashboardApiTests suite
cd novus-example-tests && mvn clean test -DsuiteXmlFile=src/test/resources/dashboard-api-suite.xml

# 3b. Run full inventory suite (login + dashboard)
cd novus-example-tests && mvn clean test -DsuiteXmlFile=src/test/resources/inventory-management-suite.xml

# 3c. Run smoke tests only
cd novus-example-tests && mvn clean test -DsuiteXmlFile=src/test/resources/dashboard-api-suite.xml -Dgroups=SMOKE_TESTS
```
