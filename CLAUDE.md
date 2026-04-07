# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# SPEC Method - Claude Code Instructions
> This project uses **SPEC Method** for structured AI-native software development.

---

## SPEC Method Workflows

### Available Workflows

When user says any of these commands, read the corresponding workflow file:

| Command        | Workflow File                    |
| -------------- | -------------------------------- |
| test-plan    | SPEC/workflows/test-plan.md    |
| generate-e2e | SPEC/workflows/generate-e2e.md |
| run-e2e      | SPEC/workflows/run-e2e.md      |

### Skills Available

| Skill        | Command                                  | Description                                    |
| ------------ | ---------------------------------------- | ---------------------------------------------- |
| test-plan    | test-plan story-1.1 or test-plan HLM-42 | Generate QA test plan for a story              |
| generate-e2e | generate-e2e story-1.1                   | Generate E2E test code from approved plan      |
| run-e2e      | run-e2e dashboard-kpi or run-e2e regression | Run E2E locally, triage failures, file bugs |

### Workflow Execution Protocol

1. **Read** the workflow file from SPEC/workflows/
2. **Read** the agent definition from SPEC/agents/
3. **Read** the rulebook from SPEC/rulebooks/e2e-rulebook.md
4. **Ask** "Type 'proceed' to start" and wait for confirmation
5. **Execute** following the checklist exactly

### Rulebooks (Enforced Standards)

| Rulebook    | File                       | Scope                                              |
| ----------- | -------------------------- | -------------------------------------------------- |
| E2E Testing | e2e-rulebook.md          | Test plan rules, framework patterns, naming, selectors |

### Configuration Files

| Type              | Location                |
| ----------------- | ----------------------- |
| Agent Definitions | SPEC/agents/          |
| Rulebooks         | SPEC/rulebooks/       |
| Workflows         | SPEC/workflows/       |
| MCP Servers       | .mcp.json             |

---

## Core Rules

1. **Zero Assumptions** - Ask clarifying questions when uncertain
2. **Evidence-Based** - Paste actual test output as proof
3. **User Approval** - Ask "proceed?" before major actions

---

Built with SPEC Method

## Build & Test Commands

Requires Java 17. Maven wrapper is not present — use the system Maven installation.

```bash
# Install all modules (skip tests)
mvn clean install -DskipTests

# Install a single module
mvn install -pl novus-core-ui -am -DskipTests

# Run a specific TestNG suite (from project root)
mvn test -pl novus-example-tests -DsuiteXmlFile=geo-location-suite.xml
mvn test -pl novus-example-tests -DsuiteXmlFile=dashboard-kpi-suite.xml
mvn test -pl novus-example-tests -DsuiteXmlFile=dashboard-api-suite.xml
mvn test -pl novus-example-tests -DsuiteXmlFile=inventory-login-suite.xml

# Build without downloading Playwright browsers (uses pre-installed Chromium)
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn test -pl novus-example-tests -DsuiteXmlFile=<suite>.xml
```

Suite XML files live in `novus-example-tests/src/test/resources/`. The `pom.xml` surefire config automatically prepends `src/test/resources/` — pass just the filename.

The Chromium binary path is set in `novus-example-tests/src/test/resources/application-local.properties` via `browser.executable.path`.

## Architecture

### Module Structure

```
novus-core          — Shared abstractions: Actor, Performable, Waiter, annotations, exceptions, utils
novus-core-ui       — UI layer: BrowserConfig (Playwright Page DI), Waiting, Verify, all UI actions (Click, Enter, etc.), LocateBy
novus-core-api      — API layer: ApiDriver, Get/Post/Put/Delete, JsonUtil
novus-example-tests — Actual test classes + page objects for the HLM Inventory app
```

### Actor/Performable Pattern (core design)

Every interaction goes through an `Actor`. Tests express intent in near-English:

```java
user.attemptsTo(Click.on(SomePage.BUTTON));
user.wantsTo(Verify.uiElement(SomePage.HEADING).isVisible());
user.is(Waiting.on(SomePage.ELEMENT).within(15));
```

- `Performable` — interface for actions; implement it for custom actions
- `Perform.actions(...)` — chains multiple Performables with a log message (used in `*Impl` classes)
- `Verify` — assertion DSL wrapping Playwright assertions; `isVisible()`, `containsText()`, `hasText()`, etc. all throw `AssertionError` on failure
- `Waiting.on(locator)` — soft wait: **catches `TimeoutError` and returns false** instead of throwing; `.within(N)` sets seconds on `waitForSelectorOptions` but is **ignored** by the default code path (default 30s is always used). Use `browser.waitForFunction(...)` when a hard gate is needed.

### Spring Boot DI

Each test class is a Spring Boot test (`@SpringBootTest`). The `Page` browser instance, `PageOptions`, and services are all `@Autowired`. Browser scope is thread-scoped (`@ParallelThreadScope`) for parallel execution.

Active profiles control which `application-<profile>.properties` is loaded. Test classes declare:
```java
@ActiveProfiles({"web", "inventory", "local"})
```
- `web` — browser settings from `application-web.properties`
- `inventory` — AUT URL + credentials from `application-inventory.properties`
- `local` — local overrides from `application-local.properties`

### Page Object Convention (novus-example-tests)

Three-layer structure per feature:

| Layer | Location | Purpose |
|---|---|---|
| `*Page.java` | `src/main/java/.../pages/` | Locator constants only (`LocateBy.css(...)`, etc.). `@NoArgsConstructor(access = PRIVATE)`. Inner static classes for sections. |
| `*PageImpl.java` | `src/main/java/.../impls/` | Static factory methods returning `Performable` — named actions like `clickFilterOnline()`. |
| `*Tests.java` | `src/test/java/.../inventory/` | Test class. Extends `InventoryTestBase`. `@BeforeMethod` handles navigation to the feature under test. |

### LocateBy helpers (novus-core-ui)

```java
LocateBy.css("div.foo")                          // css=div.foo
LocateBy.withCssText("button", "Online(")        // button:has-text("Online(")
LocateBy.withExactCssText("button", "All")       // button:text-is("All")
LocateBy.id("myId")                              // id=myId
LocateBy.xpath("//div")                          // xpath passthrough
```

### Test Base Hierarchy

```
AbstractTestNGSpringContextTests (TestNG + Spring)
  └── NovusGuiTestBase       (browser Page, reporting, screenshot on failure, step() logging)
        └── InventoryTestBase  (login once per class in @BeforeClass, retry on error banner)
              └── GeoLocationTests / DashboardKpiTests / etc.
```

`NovusApiTestBase` is the equivalent base for API tests (no browser).

### Reporting

Extent Reports (Spark) are written to `src/test/resources/reports/<date>/`. The report name is configured per suite via the `<parameter name="report-name" value="..."/>` element in the TestNG XML. Screenshots for failed tests are saved to `src/test/resources/screenshots/`.

### Key Quirks

- **`Waiting.on().within(N)` does not affect timeout** — the `within()` value goes into `waitForSelectorOptions.setTimeout()` but the code path taken when no `.toBe()` state is set ignores `waitForSelectorOptions` and uses the hardcoded `timeOut` field (default 30s). Use `.seconds(N)` to change the actual timeout, or use `browser.waitForFunction()` for a hard assertion that won't swallow errors.
- **Map markers (MapLibre)**: Device pins render as `div[role="button"]` with class `.maplibregl-marker.maplibregl-marker-anchor-center`, not `<button>` elements. Playwright's `.click()` requires `force: true` due to the inner dot div intercepting pointer events. Use `browser.evaluate("() => document.querySelector('...').click()")` instead.
- **Filter button text** includes a count in a child `<span>`: `Online<span>(6)</span>` — use `button:has-text("Online(")` not exact-text matching.
