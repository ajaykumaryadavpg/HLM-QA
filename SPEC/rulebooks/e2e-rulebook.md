# Rulebook: E2E Testing Standards

> Quality gates, code patterns, and naming conventions for the HLM E2E framework.
> This rulebook is the single source of truth for generating test plans and test code.

---

## Test Plan Rules

1. Every AC in the story MUST have at least one P1 test case
2. Every test case MUST include: Pre-conditions, Steps, Expected Result, Priority
3. Selectors in test plans MUST be discovered from the live app via Playwright MCP — never guessed
4. Edge cases must cover: empty state, error state, boundary values, unauthorized access
5. Test plan Jira ticket summary format: `[QA Plan] Story N.M — {title}`
6. Test plan ticket labels: `qa-plan`
7. Test plan must be linked as sub-task of parent story in Jira
8. Priority levels: P1 = core AC validation, P2 = UI/UX behavior, P3 = edge/performance

---

## Framework Architecture

### Layer Stack (bottom → top)

```
novus-core          → Actor, Performable, Waiter, Annotations, Assertions, Reporting, Utilities
novus-core-ui       → NovusGuiTestBase, UI Actions (Click/Type/Enter/etc.), LocateBy, Verify, Retrieve
novus-core-api      → NovusApiTestBase, ApiCore<T>, Get/Post/Put/Delete, JsonUtil
novus-example-tests → Pages, PageImpls, Macros, Listeners, Test Classes, Suites
```

### Key Interfaces

```java
// Actor executes Performable actions
actor.attemptsTo(Performable... tasks);  // sequential execution
actor.is(Waiter waiter);                  // returns boolean
actor.usesBrowser();                      // returns Playwright Page

// Performable — any executable action
@FunctionalInterface
public interface Performable {
    void performAs(Actor actor);
}

// Waiter — any wait condition
public interface Waiter {
    boolean waitAs(Actor actor);
}
```

### Annotations (com.tpg.annotations)

```java
@MetaData(
    testCaseId = "TC-FEAT-001",         // required — format: TC-{PREFIX}-NNN
    author = "QA Automation",            // required
    category = "feature",                // required — lowercase module/feature name
    stories = {"HLM-42"},               // optional — Jira ticket keys
    bugs = {"HLM-99"}                   // optional — linked bug tickets
)

@Description("What the test verifies")   // on method or class
@Outcome("Expected end state")           // on method
```

### Assertions

```java
// Soft assertions (preferred — non-blocking, collects all failures)
softly.assertTrue(boolean condition, String description);
softly.assertFalse(boolean condition, String description);
softly.verify("description").actual(obj).matches(expected);
softly.verify("description").actual(obj).contains("text");
softly.verify("description").actual(obj).isEmpty();
softly.assertAll();  // MUST call at end of every test method

// Hard assertions (use sparingly — fails immediately)
actor.wantsTo(NovusHardAssert.verify(actual, matcher).describedAs("desc"));
actor.wantsTo(Verify.uiElement(locator).isVisible());
actor.wantsTo(Verify.uiElement(locator).containsText("text"));
actor.wantsTo(Verify.uiElement(locator).hasText("exact text"));
actor.wantsTo(Verify.uiElement(locator).isNotVisible());
actor.wantsTo(Verify.uiElement(locator).isDisabled());
actor.wantsTo(Verify.page().url().contains("/dashboard"));
```

---

## UI Actions Reference (novus-core-ui)

### Click

```java
Click.on(locator)                           // basic click
Click.on(locator).nth(2)                    // click 3rd match
Click.on(locator).last()                    // click last match
Click.on(locator).ifDisplayed(locator)      // conditional click
Click.on(locator).ifNotDisplayed(locator)   // inverse conditional
Click.on(locator).afterWaiting(Waiting.on(loc))   // pre-wait
Click.on(locator).laterWaiting(Waiting.on(loc))   // post-wait
Click.on(locator).retryTimes(3)             // retry with 0.5s gaps
Click.on(locator).until(targetLocator)      // retry until element appears
Click.on(locator).multipleTimes()           // click all matches
Click.on(locator).bySwitchingToFrame("name") // within iframe
```

### Type (pressSequentially — simulates keystrokes)

```java
Type.text("query").on(locator)              // type character by character
Type.text("query").on(locator).withDelay()  // slow typing (20ms per char)
```

### Enter (fill — instant value injection)

```java
Enter.text("value").on(locator)             // fill input instantly
Enter.text(42).on(locator)                  // numeric fill
Enter.text("v").on(locator).nth(2)          // fill 3rd match
Enter.text("v").on(locator).multi()         // fill all matches
Enter.text("v").on(locator).ifDisplayed(loc) // conditional fill
```

### Launch

```java
Launch.app(urlService.login())              // navigate to URL
Launch.app(url).withConfigs(navigateOptions) // custom Page.NavigateOptions
```

### Waiting

```java
Waiting.on(locator)                         // wait visible, 30s default
Waiting.on(locator).seconds(10)             // custom timeout
Waiting.on(locator).nth(2)                  // wait for 3rd match
Waiting.on(locator).within(5)               // shorthand for seconds
Waiting.on(locator).toBe(WaitForSelectorState.HIDDEN)  // wait hidden
Waiting.on(locator).withState(ElementState.ENABLED)     // wait enabled
```

### Select

```java
Select.option("Label").on(locator)          // select by label
Select.options("A", "B").on(locator)        // multi-select
```

### Keyboard

```java
Keyboard.press("Enter").on(locator)         // press key
Keyboard.press("Tab").on(locator).times(3)  // press 3 times
```

### Clear

```java
Clear.locator(locator)                      // force clear input
Clear.locator(locator).afterWaiting(Waiting.on(loc))  // wait then clear
```

### Other Actions

```java
DoubleClick.on(locator)                     // double click
CheckBox.check(locator)                     // check checkbox
CheckBox.uncheck(locator)                   // uncheck checkbox
Alert.accept()                              // accept dialog
Alert.dismiss()                             // dismiss dialog
BrowserRefresh.refreshBrowser()             // reload page
BrowserRefresh.refreshBrowser().times(3)    // reload N times
BrowserRefresh.refreshBrowser().times(5).checking(Waiting.on(loc), "desc") // stop when condition met
Close.browser()                             // close browser
Open.aNewBrowser()                          // open new browser tab
```

### Retrieve (get values from page)

```java
Retrieve.text().ofLocator(locator)          // get innerText
Retrieve.text().ofLocator(locator).atIndex(2) // 3rd match text
Retrieve.currentUrl().ofLocator(locator)    // window.location.href
Retrieve.attribute("href").ofLocator(locator) // get attribute
Retrieve.value().ofLocator(locator)         // get input value
Retrieve.inputValue().ofLocator(locator)    // get input value (alias)
Retrieve.href().ofLocator(locator)          // get href
Retrieve.count().ofLocator(locator)         // count matching elements
Retrieve.ifChecked().ofLocator(locator)     // checkbox state
// All return String — use .getAs(actor) to execute
```

### Perform (compose actions with logging)

```java
Perform.actions(action1, action2, ...)      // compose multiple actions
    .log("methodName", "description")       // REQUIRED — will throw if missing
    .twice()                                // repeat 2x
    .thrice()                               // repeat 3x
    .iff(locator).isPresent()               // conditional execution
    .ifExceptionOccurs(TimeoutError.class)  // catch and retry
    .then(fallbackAction)                   // fallback on exception
    .meanwhile(() -> { ... })               // side effect during retry
```

### LocateBy (locator strategy)

```java
LocateBy.css("[data-testid='name']")        // CSS selector (preferred)
LocateBy.text("Button Label")              // text content
LocateBy.id("element-id")                  // by ID
LocateBy.xpath("//div[@class='x']")        // xpath (avoid)
LocateBy.name("input-name")               // by name attribute
LocateBy.dataIdentifier("value")           // [data-identifier='value']
LocateBy.withCssText("css", "text")        // css:has-text("text")
LocateBy.withExactCssText("css", "text")   // css:text-is("text")
```

---

## Test Code Patterns

### Page Object

- **Package:** `com.tpg.automation.pages.inventory`
- **Annotation:** `@NoArgsConstructor(access = AccessLevel.PRIVATE)`
- **Only static final String locators** — no methods, no instances
- **Naming:** UPPER_SNAKE_CASE
- **Locator priority:** `data-testid` > `role/aria-label` > CSS > text > xpath

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeaturePage {
    public static final String PAGE_HEADING = LocateBy.text("Feature");
    public static final String SEARCH_INPUT = LocateBy.css("[data-testid='feature-search'] input");
    public static final String DATA_TABLE = LocateBy.css("[data-testid='feature-table']");
    public static final String TABLE_ROW = LocateBy.css("[data-testid='feature-table'] tbody tr");
}
```

### Implementation (PageImpl)

- **Package:** `com.tpg.automation.impls.inventory`
- **File naming:** `{Module}PageImpl.java` (e.g., `DashboardPageImpl.java`)
- **Annotation:** `@NoArgsConstructor(access = AccessLevel.PRIVATE)`
- **All methods:** `public static Performable methodName()`
- **MUST call `.log("methodName", "description")`** — Perform throws NovusConfigException if missing

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeaturePageImpl {
    public static Performable searchFor(String query) {
        return Perform.actions(
                Type.text(query).on(FeaturePage.SEARCH_INPUT)
        ).log("searchFor", "Searching for: " + query);
    }

    public static Performable waitForTableToLoad() {
        return Perform.actions(
                (actor) -> actor.is(Waiting.on(FeaturePage.DATA_TABLE))
        ).log("waitForTableToLoad", "Waiting for data table to load");
    }
}
```

### Test Class

- **Package:** `com.tpg.automation.inventory` (under `src/test/java`)
- **Extends:** `InventoryTestBase` (provides `user` Actor, `urlService`, login via `@BeforeClass`)
- **Class annotation:** `@Test(groups = {TestGroups.FEATURE_GROUP, TestGroups.REGRESSION})`
- **Actor field:** `user` (inherited from `InventoryTestBase`) — NOT `actor`
- **Test body:** `step("...")` → `user.attemptsTo(...)` → `softly.assertTrue(...)` → `softly.assertAll()`

```java
@Test(groups = {TestGroups.DASHBOARD_KPI, TestGroups.REGRESSION})
public class DashboardKpiTests extends InventoryTestBase {

    // InventoryTestBase provides: urlService, user (Actor), login via @BeforeClass

    @Description("Verify dashboard KPI cards load correctly")
    @Outcome("KPI cards are displayed with correct data")
    @MetaData(testCaseId = "TC-KPI-001", author = "QA Automation",
              category = "dashboard-kpi", stories = {"HLM-42"})
    @Test(description = "Verify dashboard KPI cards load correctly")
    public void verifyDashboardKpiCardsLoad() {
        step("Verify the KPI cards are displayed");
        softly.assertTrue(
                user.is(Waiting.on(DashboardPage.KPI_CARD)),
                "KPI cards should be visible on the dashboard"
        );
        softly.assertAll();
    }
}
```

### Macros (reusable multi-step flows)

- **Package:** `com.tpg.automation.macros`
- Only create for flows used across 2+ test classes
- Follow existing patterns in the codebase

### Existing Macros

```java
// Login (handled by InventoryTestBase @BeforeClass)
Login.asAdmin()                    // admin@company.com / Admin@12345678
Login.asTechnician()               // tech@company.com / Tech@123456789
Login.asViewer()                   // viewer@company.com / Viewer@12345678
Login.withCredentials(email, pwd)  // custom credentials

// Navigation
Navigate.to().dashboard()          // via sidebar + wait
Navigate.to().inventory()
Navigate.to().deployment()
Navigate.to().compliance()
Navigate.to().accountService()
Navigate.to().analytics()
Navigate.directlyTo(url)           // direct URL navigation
```

---

## Module Prefixes & TestGroups

| Module / Feature              | Prefix        | TestGroup Constant                        | testCaseId example    |
| ----------------------------- | ------------- | ----------------------------------------- | --------------------- |
| Inventory Login               | LOGIN         | `TestGroups.INVENTORY_LOGIN`              | TC-LOGIN-001          |
| Dashboard KPI                 | KPI           | `TestGroups.DASHBOARD_KPI`               | TC-KPI-001            |
| Dashboard Alerts              | ALERTS        | `TestGroups.DASHBOARD_ALERTS`            | TC-ALERTS-001         |
| Dashboard System Status       | SYS-STATUS    | `TestGroups.DASHBOARD_SYSTEM_STATUS`     | TC-SYS-STATUS-001     |
| Dashboard Quick Actions       | QA            | `TestGroups.DASHBOARD_QUICK_ACTIONS`     | TC-QA-001             |
| Dashboard API                 | API           | `TestGroups.DASHBOARD_API`               | TC-API-001            |
| Dashboard E2E Integration     | INTEGRATION   | `TestGroups.DASHBOARD_E2E_INTEGRATION`   | TC-INTEGRATION-001    |
| Geo Location                  | GEO           | `TestGroups.GEO_LOCATION`               | TC-GEO-001            |
| Firmware Status               | FW-STATUS     | `TestGroups.FIRMWARE_STATUS`             | TC-FW-STATUS-001      |
| Upload Firmware               | FW-UPLOAD     | `TestGroups.UPLOAD_FIRMWARE`             | TC-FW-UPLOAD-001      |
| Firmware Family API           | FW-FAM-API    | `TestGroups.FIRMWARE_FAMILY_API`         | TC-FW-FAM-API-001     |
| Firmware Family E2E           | FW-FAM-E2E    | `TestGroups.FIRMWARE_FAMILY_E2E`         | TC-FW-FAM-E2E-001     |

**When adding new tests:** Check existing test classes for the highest testCaseId and continue from there.

### TestGroups Rules

- New feature → add constant to `TestGroups.java`
- Format: `public static final String FEATURE_NAME = "feature-name";`
- Location: `novus-example-tests/src/main/java/com/tpg/automation/constants/TestGroups.java`

### Suite XML Rules

- Each feature gets its own suite XML: `novus-example-tests/src/test/resources/{feature}-suite.xml`
- Suite XMLs are at `src/test/resources/` directly (no `suites/` subfolder)
- `all-suites.xml` runs all suites
- The `pom.xml` surefire config auto-prepends `src/test/resources/` — pass just the filename

---

## Existing Page Locators Reference

### Common

```java
// LoginPage
LoginPage.EMAIL_INPUT       = "input#signin-email"
LoginPage.PASSWORD_INPUT    = "input#signin-password"
LoginPage.SIGN_IN_BUTTON    = "button[type='submit']"
LoginPage.ERROR_MESSAGE     = "[role='alert']"

// HeaderPage (if exists)
// SidebarPage (if exists)
```

### Module Pages

```java
// DashboardPage
DashboardPage — KPI cards, quick actions, alerts, system status

// InventoryPage
InventoryPage — hardware tab, firmware tab, search, device table, pagination

// GeoLocationPage
GeoLocationPage — map markers, device pins, filters

// DeploymentPage
DeploymentPage — firmware tab, audit log tab, upload

// LoginPage
LoginPage — email input, password input, submit button, error message
```

---

## API Testing Reference (novus-core-api)

```java
// GET request
Get.atUrl("https://api.example.com/devices")
    .withParam("status", "active")
    .withBasicAuth("user", "pass")
    .isOk()
    .mapToList(Device.class);

// POST request
Post.atUrl("https://api.example.com/devices")
    .jsonBody(deviceObject)
    .isOk()
    .mapToObject(Device.class);

// Assertions
.isOk()                          // status 200-204
.isNotOk()                       // status NOT 200-204
.statusCodeMatches(201)           // exact status
.bodyContains("deviceId")         // body contains string
.getContent()                     // raw response text
.getBody()                        // raw response bytes
```

---

## File Locations Reference

```
novus-core/                              # Base: Actor, Performable, Annotations, Assertions, Reporting
novus-core-ui/                           # UI: NovusGuiTestBase, Actions, LocateBy, Verify, Retrieve
novus-core-api/                          # API: ApiCore, Get/Post/Put/Delete, JsonUtil
novus-example-tests/                     # Test implementations
├── src/main/java/com/tpg/automation/
│   ├── constants/TestGroups.java
│   ├── pages/inventory/{Module}Page.java
│   ├── impls/inventory/{Module}PageImpl.java
│   ├── macros/Login.java
│   └── services/UrlService.java
├── src/test/java/com/tpg/automation/
│   ├── base/InventoryTestBase.java       ← extends NovusGuiTestBase, handles login
│   └── inventory/{Feature}Tests.java     ← test classes extend InventoryTestBase
└── src/test/resources/
    ├── {feature}-suite.xml               ← per-feature suite XMLs (no suites/ subfolder)
    ├── all-suites.xml                    ← runs everything
    ├── application-local.properties      ← browser.executable.path, headless mode
    ├── application-inventory.properties  ← AUT URL + credentials
    ├── application-web.properties        ← browser settings
    ├── reports/{date}/                   ← ExtentReports output
    └── screenshots/                      ← failure screenshots
```

---

## NovusGuiTestBase Lifecycle

```
@BeforeSuite  → springTestContextPrepareTestInstance() → initReport()
@BeforeClass  → baseBeforeClassSetup() → [InventoryTestBase.setup() depends on this]
@BeforeMethod → reset stepCount, new NovusSoftAssert, add test to report
                ↓
              TEST EXECUTION
                ↓
@AfterMethod  → on FAILURE: screenshot → attach to report
              → on SUCCESS: attach result
@AfterClass   → process skipped tests → saveReport()
@AfterSuite   → close browser/context → clear LocalCache
```

**InventoryTestBase provides:**

- `Page browser` — Playwright page instance (inherited from NovusGuiTestBase)
- `NovusSoftAssert softly` — fresh per test method (inherited)
- `Actor user` — pre-configured Actor with browser set
- `UrlService urlService` — @Autowired
- `step(String step, Object... obj)` — logs numbered step to report + console
- Login is handled once per class in `@BeforeClass`
