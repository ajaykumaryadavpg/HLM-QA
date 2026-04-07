# Workflow: generate-e2e

> **Trigger:** User says `generate-e2e story-N.M` or `generate-e2e #qa-plan-issue`
> **Agent:** [e2e-generator](../agents/e2e-generator.md)
> **Rulebook:** [e2e-rulebook](../rulebooks/e2e-rulebook.md)
> **Output:** Java Page/Impl/Test files in `e2e/ims-e2e/ims-tests/`

---

## Pre-requisites

- [ ] Test plan approved (QA Plan sub-issue exists and has been reviewed)
- [ ] Dev server running (`npm run dev`) — needed for selector confirmation
- [ ] E2E framework compiles (`cd e2e/ims-e2e && mvn compile -pl ims-tests`)

---

## Checklist

### Step 1: Read Approved Test Plan

- [ ] Parse input to find the QA Plan issue (from story ID or issue number)
- [ ] Read test plan from GitHub sub-issue body (or local `Docs/epics/epic-{N}/test-plans/story-{N.M}-test-plan.md`)
- [ ] Extract: all TCs with steps, expected results, selectors, and E2E mapping table
- [ ] Identify the module name and testCaseId prefix

### Step 2: Check Existing Code

- [ ] Read existing Page class for this module (if it exists):
      `e2e/ims-e2e/ims-tests/src/main/java/com/ims/automation/pages/{module}/{Module}Page.java`
- [ ] Read existing Impl class:
      `e2e/ims-e2e/ims-tests/src/main/java/com/ims/automation/impls/{module}/{Module}Impl.java`
- [ ] Read existing Test class:
      `e2e/ims-e2e/ims-tests/src/test/java/com/ims/automation/{module}/{Module}Tests.java`
- [ ] Note which locators, methods, and test methods already exist — DO NOT duplicate

### Step 3: Confirm Selectors via Playwright MCP

- [ ] `browser_navigate` to the relevant route
- [ ] `browser_snapshot` to verify selectors from test plan are still valid
- [ ] If selectors changed, update the working list and note in test plan
- [ ] For interactive elements, `browser_click` and snapshot to confirm dynamic content

### Step 4: Generate/Update Page Object

- [ ] For each new selector not already in the Page class, add:
  ```java
  public static final String NAME = LocateBy.css("[data-testid='xxx']");
  ```
- [ ] Follow naming: UPPER_SNAKE_CASE
- [ ] Follow locator priority: data-testid > role/aria-label > CSS > text > xpath
- [ ] If module is new, create the full Page class with `@NoArgsConstructor` annotation

### Step 5: Generate/Update Implementation

- [ ] For each new action needed by test cases, add a `public static Performable` method
- [ ] MUST call `.log("methodName", "description")` on every `Perform.actions()`
- [ ] Reuse existing actions (Click, Type, Enter, Waiting, etc.) from ims-core-ui
- [ ] If module is new, create the full Impl class with `@NoArgsConstructor` annotation

### Step 6: Generate/Update Test Class

- [ ] For each TC, create a test method:
  - `@Description("...")` — what the test verifies
  - `@Outcome("...")` — expected end state
  - `@MetaData(testCaseId, author, category, stories)` — with parent story issue #
  - `@Test(description = "...")` — TestNG annotation
  - Body: `step()` → `actor.attemptsTo()` → `softly.assertTrue()` → `softly.assertAll()`
- [ ] If module is new:
  - Create class extending `NovusGuiTestBase`
  - Add `@Test(groups = {TestGroups.MODULE, TestGroups.REGRESSION})` at class level
  - Add `@BeforeClass` setup with login and navigation
- [ ] Continue testCaseId numbering from existing max

### Step 7: Update Supporting Files (if new module)

- [ ] `TestGroups.java` — add new constant
- [ ] `Navigate.java` — add `public Performable {module}()` method
- [ ] `UrlService.java` — add `public String {module}()` method

### Step 8: Update Suite XMLs

- [ ] Add new test class to `regression-suite.xml`:
  ```xml
  <test name="{Module} Tests">
      <classes>
          <class name="com.ims.automation.{module}.{Module}Tests"/>
      </classes>
  </test>
  ```
- [ ] If critical-path, also add to `smoke-suite.xml`

### Step 9: Verify Compilation

- [ ] Run: `cd e2e/ims-e2e && mvn compile -pl ims-tests`
- [ ] Fix any compilation errors
- [ ] Report results

### Step 10: Update GitHub

- [ ] Comment on QA Plan issue: "Test scripts generated: {file paths} ({N} tests)"
- [ ] Set QA Status to "Tests Written" on QA Plan issue's project board item:
  - `updateProjectV2ItemFieldValue` → QA Status field (`PVTSSF_lAHOARf1_84BTD7ozhAadh8`) to "Tests Written" (`4f85f5e4`)
- [ ] Update local test plan file with E2E Mapping table (actual testCaseIds and method names)
- [ ] Ensure parent story remains in "In QA" status on the project board

---

## Example

```
User: generate-e2e story-1.1

Claude:
→ Reads QA Plan issue for Story 1.1
→ Checks existing LoginPage.java, LoginImpl.java, AuthTests.java
→ Confirms selectors via Playwright MCP on /login
→ Adds 3 new locators to LoginPage (if needed)
→ Adds 2 new action methods to LoginImpl (if needed)
→ Adds 4 new test methods to AuthTests with @MetaData(stories = {"#2"})
→ Verifies: mvn compile -pl ims-tests → BUILD SUCCESS
→ Comments on QA Plan issue: "Generated 4 tests in AuthTests.java"
→ Reports: "4 tests generated. Run `npm run test:e2e:module -- --module=auth` to execute."
```
