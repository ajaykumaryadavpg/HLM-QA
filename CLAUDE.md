# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build all modules (required before first test run)
mvn clean install -DskipTests

# Install Playwright browser binaries (one-time, or after upgrading Playwright)
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install" -pl novus-core-ui

# Run a specific suite
cd novus-example-tests && mvn clean test -DsuiteXmlFile=src/test/resources/dashboard-api-suite.xml

# Run the full inventory suite (login + dashboard)
cd novus-example-tests && mvn clean test -DsuiteXmlFile=src/test/resources/inventory-management-suite.xml

# Run only smoke tests
cd novus-example-tests && mvn clean test -DsuiteXmlFile=src/test/resources/dashboard-api-suite.xml -Dgroups=SMOKE_TESTS

# Override credentials at runtime
cd novus-example-tests && mvn clean test -DsuiteXmlFile=src/test/resources/dashboard-api-suite.xml \
  -Dinventory.admin.username=you@example.com -Dinventory.admin.password=YourPassword
```

## Architecture

### Module dependency chain

```text
novus-core          — shared contracts (Actor, Performable, Waiter, annotations, utils)
  ├── novus-core-ui   — Playwright browser actions, BrowserScope, LocateBy, NovusGuiTestBase
  └── novus-core-api  — Playwright APIRequestContext wrappers, ApiCore<T>, NovusApiTestBase
        └── novus-example-tests — concrete page objects, macros, test classes for HLM Platform
```

Build order matters. Always run `mvn clean install -DskipTests` from the root before running tests in `novus-example-tests`.

### Application under test

The HLM Platform (Hardware Lifecycle Management) is a React + Tailwind CSS SPA hosted on AWS Amplify. It has no `data-identifier` attributes — all locators are CSS selectors scoped by Tailwind class names and text matchers.

### Spring profile system

Tests are driven by Spring Boot profiles set via `@ActiveProfiles`. Three profiles combine:

| Profile | File | Controls |
| --- | --- | --- |
| `web` | `novus-core-ui/src/main/resources/application-web.properties` | Browser size, headless flag, slowmo |
| `inventory` | `novus-example-tests/src/test/resources/application-inventory.properties` | AUT base URL + credentials |
| `local` | `novus-example-tests/src/test/resources/application-local.properties` | Overrides `browser.headless=false` for headed runs |

`InventoryTestBase` uses `@ActiveProfiles({"web", "inventory", "local"})`. Remove `local` for a headless CI run. Default credentials are in `application-inventory.properties`; override via `-D` flags or environment-specific properties files.

### Actor / Screenplay pattern

Every UI test drives an `Actor`. The actor executes `Performable` tasks:

```java
user.attemptsTo(
    Click.on(SomePage.BUTTON),
    Enter.text("value").on(SomePage.FIELD)
);
```

- `Performable` — interface with `performAs(Actor)` and `byWaitingFor(double seconds)`
- `Perform.actions(...)` — wraps one or more `Performable` items; supports `.log()`, `.iff()`, `.twice()`, `.retryTimes()`, `.ifExceptionOccurs()`
- All action classes (`Click`, `Type`, `Enter`, `Select`, etc.) live in `novus-core-ui` and return `this` for chaining

### Page objects

Page objects are final classes with `@NoArgsConstructor(access = AccessLevel.PRIVATE)`. Locators are `public static final String` fields built with `LocateBy` helpers:

```java
LocateBy.css("div.bg-card:has(div.text-sm:text-is('Total Devices')) div.text-3xl")
LocateBy.withExactCssText("h1", "Dashboard")   // → h1:text-is("Dashboard")
LocateBy.withCssText("h2", "Welcome back")      // → h2:has-text("Welcome back")
```

Nested static inner classes group related locators (e.g. `DashboardPage.KpiCard`, `DashboardPage.ErrorBanner`, `DashboardPage.AlertsPanel`).

### Macros and impls

- **Macros** (`src/main/java/.../macros/`) — reusable multi-step `Performable` sequences (e.g. `AuthenticateAs`, `Navigate`)
- **Impls** (`src/main/java/.../impls/`) — fine-grained helper methods that return `Performable` for use inside macros or directly in tests

### API testing

API test classes extend `NovusApiTestBase`. HTTP calls are built by subclassing `ApiCore<T>` and implementing `execute()`. The fluent chain on `ApiCore` supports `.withHeader()`, `.withBody()`, `.jsonBody()`, `.withParam()`, `.isOk()`, `.statusCodeMatches()`, `.mapToObject()`, `.mapToList()`.

### Test lifecycle (UI tests)

1. `NovusGuiTestBase.baseBeforeClassSetup` — initialises logger
2. `InventoryTestBase.loginToApplication` — launches browser, clears session, authenticates **once per class**
3. `NovusGuiTestBase.beforeMethodSetup` — resets step counter, registers test with Extent report
4. On failure — full-page screenshot saved to `src/test/resources/screenshots/`
5. After suite — HTML report at `src/test/resources/reports/<yyyy-MM-dd>/`

### Disabled tests

Five tests in `DashboardApiTests` have `enabled = false`. They require `page.route()` / `page.on("request")` network interception, which is not yet exposed by the framework. They are specification-complete placeholders; enable them once `InterceptNetwork` in `novus-core-ui` exposes a network-stubbing API.

## Tech Stack

- Java 17, Maven multi-module, Spring Boot 3.2.2
- UI automation: Microsoft Playwright Java 1.44.0
- API testing: Playwright `APIRequestContext` via `ApiCore<T>`
- Test runner: TestNG 7.10.1 via `maven-surefire-plugin` (TestNG provider forced explicitly)
- Reporting: ExtentReports 5.1.1 + custom `NovusReportingService`
- Assertions: TestNG `Assert`, custom `NovusSoftAssert`

## Coding Standards

- **Fluent / builder style** throughout — method names are verb-based (`on`, `nth`, `ifDisplayed`, `bySwitchingToFrame`, `retryTimes`, `until`). Match this style exactly when adding actions.
- New action classes must implement `Performable` and override `performAs(Actor)` and `byWaitingFor(double seconds)`.
- New API methods must extend `ApiCore<T>` and implement `execute()`.
- Use `NovusLoggerService` for all logging — never `System.out.println` or raw logger frameworks.
- Config is property-driven via `@Value` — no hardcoded browser or env values.
- Wrap Playwright exceptions in `NovusActionException` for UI; use Spring `Assert` for API assertions.
- Do not mock Playwright or the Spring context — tests run against real browser/API instances.
- Commit messages follow Semantic Commit Messages: `fix:`, `feat:`, `docs:`.
