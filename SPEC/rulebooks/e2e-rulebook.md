# Rulebook: E2E Testing Standards

> Quality gates, code patterns, and naming conventions for the IMS Gen2 E2E framework.
> This rulebook is the single source of truth for generating test plans and test code.

---

## Test Plan Rules

1. Every AC in the story MUST have at least one P1 test case
2. Every test case MUST include: Pre-conditions, Steps, Expected Result, Priority
3. Selectors in test plans MUST be discovered from the live app via Playwright MCP — never guessed
4. Edge cases must cover: empty state, error state, boundary values, unauthorized access
5. Test plan issue title format: `[QA Plan] Story N.M — {title}`
6. Test plan issue labels: `task`, `qa-plan`
7. Test plan must link to parent story: `Parent: #NNN`
8. Priority levels: P1 = core AC validation, P2 = UI/UX behavior, P3 = edge/performance

---

## Framework Architecture

### Layer Stack (bottom → top)

```
ims-core          → Actor, Performable, Waiter, Annotations, Assertions, Reporting, Utilities
ims-core-ui       → NovusGuiTestBase, UI Actions (Click/Type/Enter/etc.), LocateBy, Verify, Retrieve
ims-core-api      → NovusApiTestBase, ApiCore<T>, Get/Post/Put/Delete, JsonUtil
ims-tests         → Pages, Impls, Macros, Listeners, Test Classes, Suites
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

### Annotations (com.ims.annotations)

```java
@MetaData(
    testCaseId = "IMS-FEAT-001",   // required — format: IMS-{PREFIX}-NNN
    author = "ims-automation",      // required
    category = "feature",           // required — lowercase module name
    stories = {"#42"},              // optional — GitHub issue numbers
    bugs = {"#99"}                  // optional — linked bug issues
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

## UI Actions Reference (ims-core-ui)

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

- **Package:** `com.ims.automation.pages.{module}`
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

### Implementation

- **Package:** `com.ims.automation.impls.{module}`
- **Annotation:** `@NoArgsConstructor(access = AccessLevel.PRIVATE)`
- **All methods:** `public static Performable methodName()`
- **MUST call `.log("methodName", "description")`** — Perform throws NovusConfigException if missing

```java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FeatureImpl {
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

- **Package:** `com.ims.automation.{module}` (under `src/test/java`)
- **Extends:** `NovusGuiTestBase`
- **Class annotation:** `@Test(groups = {TestGroups.MODULE, TestGroups.REGRESSION})`
- **Setup:** `@BeforeClass(dependsOnMethods = "baseBeforeClassSetup")`
- **Fields:** `@Autowired UrlService urlService;` and `Actor actor = new Actor();`
- **Test body:** `step("...")` → `actor.attemptsTo(...)` → `softly.assertTrue(...)` → `softly.assertAll()`

```java
@Test(groups = {TestGroups.FEATURE, TestGroups.REGRESSION})
public class FeatureTests extends NovusGuiTestBase {

    @Autowired
    private UrlService urlService;
    private final Actor actor = new Actor();

    @BeforeClass(dependsOnMethods = "baseBeforeClassSetup")
    public void setup() {
        actor.setBrowser(browser);
        actor.attemptsTo(
                Launch.app(urlService.login()),
                Login.asAdmin(),
                Navigate.to().feature()
        );
    }

    @Description("Verify feature page loads correctly")
    @Outcome("Feature page is displayed with data table")
    @MetaData(testCaseId = "IMS-FEAT-001", author = "ims-automation",
              category = "feature", stories = {"#42"})
    @Test(description = "Verify feature page loads correctly")
    public void verifyFeaturePageLoads() {
        step("Verify the data table is displayed");
        softly.assertTrue(
                actor.is(Waiting.on(FeaturePage.DATA_TABLE)),
                "Data table should be visible on the feature page"
        );
        softly.assertAll();
    }
}
```

### Macros (reusable multi-step flows)

- **Package:** `com.ims.automation.macros`
- Only create for flows used across 2+ test classes
- Follow existing: `Login.asAdmin()`, `Navigate.to().dashboard()`

### Existing Macros

```java
// Login
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

| Module          | Prefix | TestGroup Constant           | Existing Tests      |
| --------------- | ------ | ---------------------------- | ------------------- |
| Auth            | AUTH   | `TestGroups.AUTH`            | IMS-AUTH-001 to 005 |
| Dashboard       | DASH   | `TestGroups.DASHBOARD`       | IMS-DASH-001 to 004 |
| Inventory       | INV    | `TestGroups.INVENTORY`       | IMS-INV-001 to 004  |
| Deployment      | DEP    | `TestGroups.DEPLOYMENT`      | IMS-DEP-001 to 004  |
| Compliance      | CMP    | `TestGroups.COMPLIANCE`      | IMS-CMP-001 to 004  |
| Account Service | ACC    | `TestGroups.ACCOUNT_SERVICE` | IMS-ACC-001 to 004  |
| Analytics       | ANL    | `TestGroups.ANALYTICS`       | IMS-ANL-001 to 004  |
| Smoke           | SM     | `TestGroups.SMOKE`           | IMS-SM-001 to 003   |

**When adding new tests:** Continue numbering from the last existing ID for that module.

### TestGroups Rules

- New module → add constant to `TestGroups.java`
- Format: `public static final String MODULE_NAME = "module-name";`
- Location: `e2e/ims-e2e/ims-tests/src/main/java/com/ims/automation/constants/TestGroups.java`

### Suite XML Rules

- New test classes MUST be added to `regression-suite.xml`
- Critical-path tests should also be added to `smoke-suite.xml`
- Module group name must match the `TestGroups` constant

---

## Existing Page Locators Reference

### Common

```java
// LoginPage
LoginPage.EMAIL_INPUT       = "input#signin-email"
LoginPage.PASSWORD_INPUT    = "input#signin-password"
LoginPage.SIGN_IN_BUTTON    = "button[type='submit']"
LoginPage.ERROR_MESSAGE     = "[role='alert']"

// HeaderPage
HeaderPage.SEARCH_BUTTON    = "[data-testid='header-search']"
HeaderPage.NOTIFICATION_BELL = "[data-testid='notification-bell']"
HeaderPage.THEME_TOGGLE     = "[data-testid='theme-toggle']"
HeaderPage.USER_INFO        = "[data-testid='user-info']"
HeaderPage.SIGN_OUT_BUTTON  = "[data-testid='sign-out']"

// SidebarPage
SidebarPage.HAMBURGER_BUTTON = "button[aria-label='Toggle navigation']"
SidebarPage.SIDEBAR_PANEL   = "aside[aria-label='Primary navigation']"
SidebarPage.DASHBOARD_LINK  = "[aria-label='Dashboard']"
// ... INVENTORY_LINK, DEPLOYMENT_LINK, COMPLIANCE_LINK, etc.
```

### Module Pages (all use data-testid pattern)

```java
// DashboardPage
DashboardPage.PAGE_HEADING, KPI_CARD, QUICK_ACTIONS_PANEL, RECENT_ALERTS, SYSTEM_STATUS

// InventoryPage
InventoryPage.HARDWARE_TAB, FIRMWARE_TAB, GEO_TAB, SEARCH_INPUT, DEVICE_TABLE, PAGINATION, CSV_EXPORT_BUTTON

// DeploymentPage
DeploymentPage.FIRMWARE_TAB, AUDIT_LOG_TAB, FIRMWARE_CARD, APPROVAL_STAGE_INDICATOR, UPLOAD_BUTTON

// CompliancePage
CompliancePage.STATUS_FILTER_BUTTONS, COMPLIANCE_TABLE, VULNERABILITY_PANEL, SUBMIT_FOR_REVIEW_BUTTON

// AccountServicePage
AccountServicePage.KANBAN_VIEW_TOGGLE, CALENDAR_VIEW_TOGGLE, SERVICE_ORDER_CARD, CREATE_ORDER_BUTTON

// AnalyticsPage
AnalyticsPage.KPI_CARD, TIME_RANGE_7D/30D/90D, CHART_CONTAINER, AUDIT_TABLE
```

---

## API Testing Reference (ims-core-api)

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
e2e/ims-e2e/
├── pom.xml                          # Parent POM (Java 17, Spring Boot 3.2.2, TestNG 7.10.1, Playwright 1.49.0)
├── ims-core/                        # Base: Actor, Performable, Annotations, Assertions, Reporting
├── ims-core-ui/                     # UI: NovusGuiTestBase, Actions, LocateBy, Verify, Retrieve
│   └── src/main/resources/application-web.properties  # Browser settings
├── ims-core-api/                    # API: ApiCore, Get/Post/Put/Delete, JsonUtil
└── ims-tests/                       # Test implementations
    ├── src/main/java/com/ims/automation/
    │   ├── constants/TestGroups.java
    │   ├── pages/{module}/{Module}Page.java
    │   ├── impls/{module}/{Module}Impl.java
    │   ├── macros/Login.java, Navigate.java
    │   ├── listeners/ImsTestListener.java, RetryAnalyzer.java, RetryTransformer.java
    │   └── services/UrlService.java
    ├── src/test/java/com/ims/automation/{module}/{Module}Tests.java
    └── src/test/resources/
        ├── suites/smoke-suite.xml, regression-suite.xml, module-suite.xml
        ├── application-test.properties   # aut.protocol, aut.domain, report settings
        └── application-local.properties  # browser.headless=false for headed mode
```

---

## NovusGuiTestBase Lifecycle

```
@BeforeSuite  → springTestContextPrepareTestInstance() → initReport()
@BeforeClass  → baseBeforeClassSetup() → [your setup() depends on this]
@BeforeMethod → reset stepCount, new NovusSoftAssert, add test to report
                ↓
              TEST EXECUTION
                ↓
@AfterMethod  → on FAILURE: screenshot → attach to report
              → on SUCCESS: attach result
@AfterClass   → process skipped tests → saveReport()
@AfterSuite   → close browser/context → clear LocalCache
```

**Protected fields available in test classes:**

- `Page browser` — Playwright page instance
- `NovusSoftAssert softly` — fresh per test method
- `NovusReportingService reportingService`
- `NovusLoggerService log`
- `int stepCount` — auto-incremented by step()

**Protected method:**

- `step(String step, Object... obj)` — logs numbered step to report + console
