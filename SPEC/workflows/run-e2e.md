# Workflow: run-e2e

> **Trigger:** User says `run-e2e smoke`, `run-e2e regression`, or `run-e2e module dashboard`
> **Agent:** [e2e-triage](../agents/e2e-triage.md)
> **Rulebook:** [e2e-rulebook](../rulebooks/e2e-rulebook.md)
> **Output:** Test results summary + GitHub issues for genuine bugs

---

## Pre-requisites

- [ ] Dev server running (`npm run dev` on localhost:5173)
- [ ] Java 17 installed (`java -version`)
- [ ] gh CLI authenticated (`gh auth status`)
- [ ] E2E framework compiles (`cd e2e/ims-e2e && ./mvnw compile -pl ims-tests`)

---

## Checklist

### Step 1: Parse Input

- [ ] Extract suite type from command: `smoke` (default), `regression`, or `module {name}`
- [ ] Map to suite file:
  - `smoke` → `src/test/resources/suites/smoke-suite.xml`
  - `regression` → `src/test/resources/suites/regression-suite.xml`
  - `module {name}` → `src/test/resources/suites/module-suite.xml -DtestGroup={name}`

### Step 2: Verify Environment

- [ ] Check dev server is responding: `curl -s http://localhost:5173`
  - If not running, tell user: "Start the dev server first: `npm run dev`"
- [ ] Check Java version: `java -version` — must be 17+
- [ ] Check gh auth: `gh auth status`

### Step 3: Run Tests

- [ ] Execute Maven from `e2e/ims-e2e/ims-tests`:
  ```bash
  cd e2e/ims-e2e/ims-tests
  mvn test -DsuiteXmlFile={suite_file} -Dspring.profiles.active=test,web
  ```
- [ ] Capture full output for analysis
- [ ] Note the exit code (0 = all pass, non-zero = failures)

### Step 4: Locate Report

- [ ] Find the HTML report at:
      `e2e/ims-e2e/ims-tests/src/test/resources/reports/{MM-DD-YY}/{report-name}/{report-name}.html`
- [ ] Tell the user the exact file path to open

### Step 5: Analyze Results (if failures)

- [ ] Parse Maven output for failed test names and error messages
- [ ] Read surefire XML reports at `e2e/ims-e2e/ims-tests/target/surefire-reports/`
- [ ] For each failure, classify using the [e2e-triage agent rules](../agents/e2e-triage.md):
  - **Genuine Bug** → proceed to Step 6
  - **Selector Error** → report to user as "test maintenance needed", suggest selector fix
  - **Environment Error** → report to user, no issue
  - **Flaky** → report to user as warning, no issue

### Step 6: File GitHub Issues (genuine bugs only)

- [ ] For each genuine bug:
  1. Search for existing open issue: `gh issue list --repo {repo} --label "bug,e2e" --state open`
  2. If duplicate found → add comment with new run data
  3. If new → create issue using the [template from e2e-triage agent](../agents/e2e-triage.md)
  4. Apply labels: `bug`, `e2e`, and epic label if determinable
- [ ] Report created/updated issue URLs to user

### Step 7: Summary

- [ ] Print results table:
  ```
  ┌─────────────────────────────────────────────┐
  │ E2E Run Summary                             │
  ├──────────────┬──────────────────────────────┤
  │ Suite        │ {smoke/regression}           │
  │ Total        │ {N} tests                    │
  │ Passed       │ {N} ✓                        │
  │ Failed       │ {N} ✗                        │
  │ Skipped      │ {N} ⊘                        │
  │ Bugs Filed   │ {N} (links)                  │
  │ Report       │ {path}                       │
  └──────────────┴──────────────────────────────┘
  ```

---

## Example Usage

```
> run-e2e smoke
▶ Running smoke suite...
▶ 12 tests: 10 passed, 2 failed
▶ Analyzing failures...
  - TC_LOGIN_003: Genuine bug — wrong error message for locked account
  - TC_DASH_001: Selector error — KPI card selector changed
▶ Filed issue #230: [E2E] Wrong error message for locked account login
▶ Selector fix needed: Update DashboardPage.KPI_CARD selector
▶ Report: e2e/ims-e2e/ims-tests/src/test/resources/reports/04-01-26/IMS-Smoke-Report/IMS-Smoke-Report.html
```
