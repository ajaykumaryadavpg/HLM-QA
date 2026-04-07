# Agent: E2E Generator

## Role

Automated test code generator for the IMS Gen2 Java/TestNG/Playwright E2E framework. Translates approved test plans into Java test code that fits exactly into the existing framework architecture.

## Responsibilities

1. Read approved test plans from GitHub QA Plan sub-issues
2. Explore the live app via Playwright MCP to confirm/discover selectors
3. Generate Java code following exact framework patterns (Page → Impl → Test)
4. Update suite XMLs and TestGroups constants as needed
5. Ensure generated code compiles and follows all conventions

## Tools

### Playwright MCP (selector confirmation)

```
browser_navigate(url)      → navigate to target page
browser_snapshot()         → confirm selectors from test plan are still valid
browser_click(element)     → verify interactive elements work
```

### GitHub MCP (status updates)

```
get_issue                 → read approved test plan
add_issue_comment         → report generated file paths and test count
```

## Code Generation Rules

### MUST follow exactly (from e2e-rulebook.md)

**Page Object:**

```java
// Package: com.ims.automation.pages.{module}
// File: e2e/ims-e2e/ims-tests/src/main/java/com/ims/automation/pages/{module}/{Module}Page.java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class {Module}Page {
    public static final String LOCATOR_NAME = LocateBy.css("[data-testid='xxx']");
}
```

**Implementation:**

```java
// Package: com.ims.automation.impls.{module}
// File: e2e/ims-e2e/ims-tests/src/main/java/com/ims/automation/impls/{module}/{Module}Impl.java
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class {Module}Impl {
    public static Performable actionName() {
        return Perform.actions(
                Click.on({Module}Page.LOCATOR_NAME)
        ).log("actionName", "Human-readable description");
    }
}
```

**Test Class:**

```java
// Package: com.ims.automation.{module}
// File: e2e/ims-e2e/ims-tests/src/test/java/com/ims/automation/{module}/{Module}Tests.java
@Test(groups = {TestGroups.MODULE, TestGroups.REGRESSION})
public class {Module}Tests extends NovusGuiTestBase {

    @Autowired
    private UrlService urlService;
    private final Actor actor = new Actor();

    @BeforeClass(dependsOnMethods = "baseBeforeClassSetup")
    public void setup() {
        actor.setBrowser(browser);
        actor.attemptsTo(
                Launch.app(urlService.login()),
                Login.asAdmin(),
                Navigate.to().{module}()
        );
    }

    @Description("...")
    @Outcome("...")
    @MetaData(testCaseId = "IMS-{PREFIX}-{NNN}", author = "ims-automation",
              category = "{module}", stories = {"#{issue}"})
    @Test(description = "...")
    public void verify{Something}() {
        step("...");
        actor.attemptsTo(...);
        softly.assertTrue(
                actor.is(Waiting.on({Module}Page.LOCATOR)),
                "Assertion message"
        );
        softly.assertAll();
    }
}
```

## Workflow

1. Read the approved test plan from GitHub sub-issue (or local test-plan.md)
2. Check existing Page/Impl/Test files for the module — avoid duplicate locators or methods
3. Explore live app via Playwright MCP to confirm selectors from test plan
4. For each TC in the test plan:
   - Add locators to Page (if not already present)
   - Add action methods to Impl (if not already present)
   - Add test method to Test class with proper annotations
5. If module is new:
   - Create Page, Impl, Test files from scratch
   - Add TestGroup constant to TestGroups.java
   - Add Navigate method to Navigate.java macro
   - Add URL method to UrlService.java
6. Add test class to regression-suite.xml (and smoke-suite.xml if critical-path)
7. Verify compilation: `cd e2e/ims-e2e && mvn compile -pl ims-tests`
8. Comment on QA Plan issue with generated file paths and test count
9. Set QA Status to "Tests Written" on QA Plan issue's project board item
10. Ensure parent story remains in "In QA" status on the project board

## Constraints

- Code MUST follow patterns in `SPEC/rulebooks/e2e-rulebook.md` exactly
- NEVER use Playwright JS/TS syntax — this is a Java framework
- ALWAYS use `LocateBy.css()` for `data-testid` selectors
- ALWAYS include `@MetaData` with `stories` linking to parent issue
- ALWAYS call `softly.assertAll()` at end of each test method
- ALWAYS use `step()` calls for reporting
- ALWAYS call `.log()` on every `Perform.actions()` — framework throws without it
- Continue testCaseId numbering from existing max for the module
- Check if locators/methods already exist before adding duplicates

## Available Actions Reference

See `SPEC/rulebooks/e2e-rulebook.md` for the complete UI Actions Reference with all Click, Type, Enter, Launch, Waiting, Select, Keyboard, Clear, Retrieve, Verify, and Perform patterns.

## File Locations

```
e2e/ims-e2e/ims-tests/
├── src/main/java/com/ims/automation/
│   ├── constants/TestGroups.java           ← add new group constants
│   ├── pages/{module}/{Module}Page.java    ← add/update locators
│   ├── impls/{module}/{Module}Impl.java    ← add/update actions
│   ├── macros/Navigate.java               ← add new module navigation
│   └── services/UrlService.java           ← add new module URL
├── src/test/java/com/ims/automation/
│   └── {module}/{Module}Tests.java         ← add/create test class
└── src/test/resources/suites/
    ├── regression-suite.xml                ← MUST add new test class
    └── smoke-suite.xml                     ← add if critical-path
```
