# Workflow: generate-e2e

> **Trigger:** User says `generate-e2e story-N.M` or `generate-e2e JIRA-KEY`
> **Agent:** [e2e-generator](../agents/e2e-generator.md)
> **Rulebook:** [e2e-rulebook](../rulebooks/e2e-rulebook.md)
> **Output:** Java Page/PageImpl/Test files in `novus-example-tests/`

---

## Pre-requisites

- [ ] Test plan approved (QA Plan sub-task exists in Jira and has been reviewed)
- [ ] Dev server running — needed for selector confirmation
- [ ] E2E framework compiles (`mvn compile -pl novus-example-tests -am`)

---

## Checklist

### Step 1: Read Approved Test Plan

- [ ] Parse input to find the QA Plan ticket (from story ID or Jira key)
- [ ] Read test plan from Jira sub-task description
- [ ] If Jira is unavailable, fall back to local `Docs/epics/epic-{N}/test-plans/story-{N.M}-test-plan.md`
- [ ] Extract: all TCs with steps, expected results, selectors, and E2E mapping table
- [ ] Identify the module/feature name and testCaseId prefix

### Step 2: Check Existing Code

- [ ] Read existing Page class for this module (if it exists):
      `novus-example-tests/src/main/java/com/tpg/automation/pages/inventory/{Module}Page.java`
- [ ] Read existing PageImpl class:
      `novus-example-tests/src/main/java/com/tpg/automation/impls/inventory/{Module}PageImpl.java`
- [ ] Read existing Test class(es):
      `novus-example-tests/src/test/java/com/tpg/automation/inventory/{Feature}Tests.java`
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
- [ ] If module is new, create the full Page class with `@NoArgsConstructor(access = AccessLevel.PRIVATE)`

### Step 5: Generate/Update Implementation

- [ ] For each new action needed by test cases, add a `public static Performable` method
- [ ] MUST call `.log("methodName", "description")` on every `Perform.actions()`
- [ ] Reuse existing actions (Click, Enter, Type, Waiting, etc.) from novus-core-ui
- [ ] If module is new, create the full PageImpl class with `@NoArgsConstructor(access = AccessLevel.PRIVATE)`

### Step 6: Generate/Update Test Class

- [ ] For each TC, create a test method:
  - `@Description("...")` — what the test verifies
  - `@Outcome("...")` — expected end state
  - `@MetaData(testCaseId, author, category, stories)` — with parent story Jira key
  - `@Test(description = "...")` — TestNG annotation
  - Body: `step()` → `user.attemptsTo()` → `softly.assertTrue()` → `softly.assertAll()`
- [ ] If module is new:
  - Create class extending `InventoryTestBase`
  - Add `@Test(groups = {TestGroups.FEATURE_GROUP, TestGroups.REGRESSION})` at class level
  - `@BeforeClass` login/navigation is handled by `InventoryTestBase` — add page-specific navigation if needed
- [ ] Continue testCaseId numbering from existing max

### Step 7: Update Supporting Files (if new module)

- [ ] `TestGroups.java` — add new constant (`novus-example-tests/src/main/java/com/tpg/automation/constants/TestGroups.java`)
- [ ] Navigation macros if needed

### Step 8: Create Suite XML

- [ ] Create a new suite XML in `novus-example-tests/src/test/resources/{feature}-suite.xml`:
  ```xml
  <test name="{Feature} Tests">
      <classes>
          <class name="com.tpg.automation.inventory.{Feature}Tests"/>
      </classes>
  </test>
  ```
- [ ] Or add to an existing suite XML if the test class belongs to an existing feature

### Step 9: Verify Compilation

- [ ] Run: `mvn compile -pl novus-example-tests -am`
- [ ] Fix any compilation errors
- [ ] Report results

### Step 10: Update Jira

- [ ] Add comment on QA Plan ticket: "Test scripts generated: {file paths} ({N} tests)"
- [ ] Set QA Plan ticket status to **"Tests Written"**
- [ ] Update local test plan file with E2E Mapping table (actual testCaseIds and method names)
- [ ] Ensure parent story remains in "In QA" status

---

## Example

```
User: generate-e2e story-1.1

Claude:
→ Reads QA Plan ticket for Story 1.1
→ Checks existing LoginPage.java, LoginPageImpl.java, InventoryLoginTests.java
→ Confirms selectors via Playwright MCP on /login
→ Adds 3 new locators to LoginPage (if needed)
→ Adds 2 new action methods to LoginPageImpl (if needed)
→ Adds 4 new test methods to InventoryLoginTests with @MetaData(stories = {"HLM-2"})
→ Verifies: mvn compile -pl novus-example-tests -am → BUILD SUCCESS
→ Comments on QA Plan ticket: "Generated 4 tests in InventoryLoginTests.java"
→ Reports: "4 tests generated. Run `run-e2e inventory-login` to execute."
```
