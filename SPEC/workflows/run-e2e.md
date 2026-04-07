# Workflow: run-e2e

> **Trigger:** User says `run-e2e smoke`, `run-e2e regression`, or `run-e2e {suite-name}`
> **Agent:** [e2e-triage](../agents/e2e-triage.md)
> **Rulebook:** [e2e-rulebook](../rulebooks/e2e-rulebook.md)
> **Output:** Test results summary + Jira tickets for genuine bugs

---

## Pre-requisites

- [ ] Dev server running (HLM Inventory app accessible)
- [ ] Java 17 installed (`java -version`)
- [ ] E2E framework compiles (`mvn compile -pl novus-example-tests -am`)

---

## Checklist

### Step 1: Parse Input

- [ ] Extract suite name from command
- [ ] Map to suite file in `novus-example-tests/src/test/resources/`:
  - `smoke` → `all-suites.xml` (or a future `smoke-suite.xml`)
  - `regression` → `all-suites.xml`
  - `{suite-name}` → `{suite-name}-suite.xml` (e.g., `dashboard-kpi` → `dashboard-kpi-suite.xml`)
- [ ] Available suites: `dashboard-kpi`, `dashboard-alerts`, `dashboard-system-status`, `dashboard-quick-actions`, `dashboard-api`, `dashboard-e2e-integration`, `dashboard-alerts-system-status-integration`, `geo-location`, `inventory-login`, `firmware-status`, `upload-firmware`, `firmware-family-api`, `firmware-family-e2e`, `inventory-management`

### Step 2: Verify Environment

- [ ] Check dev server is responding
- [ ] Check Java version: `java -version` — must be 17+

### Step 3: Run Tests

- [ ] Execute Maven from project root:
  ```bash
  mvn test -pl novus-example-tests -DsuiteXmlFile={suite-name}-suite.xml
  ```
- [ ] Capture full output for analysis
- [ ] Note the exit code (0 = all pass, non-zero = failures)

### Step 4: Locate Report

- [ ] Find the HTML report at:
      `novus-example-tests/src/test/resources/reports/{MM-DD-YY}/{report-name}/{report-name}.html`
- [ ] Tell the user the exact file path to open

### Step 5: Analyze Results (if failures)

- [ ] Parse Maven output for failed test names and error messages
- [ ] Read surefire XML reports at `novus-example-tests/target/surefire-reports/`
- [ ] For each failure, classify using the [e2e-triage agent rules](../agents/e2e-triage.md):
  - **Genuine Bug** → proceed to Step 6
  - **Selector Error** → report to user as "test maintenance needed", suggest selector fix
  - **Environment Error** → report to user, no ticket
  - **Flaky** → report to user as warning, no ticket

### Step 6: File Jira Tickets (genuine bugs only)

- [ ] For each genuine bug:
  1. Search Jira for existing open bug with matching summary
  2. If duplicate found → add comment with new run data
  3. If new → create Jira ticket using the [template from e2e-triage agent](../agents/e2e-triage.md)
  4. Apply labels: `bug`, `e2e`, and epic label if determinable
- [ ] Report created/updated ticket keys to user

### Step 7: Summary

- [ ] Print results table:
  ```
  ┌─────────────────────────────────────────────┐
  │ E2E Run Summary                             │
  ├──────────────┬──────────────────────────────┤
  │ Suite        │ {suite-name}                 │
  │ Total        │ {N} tests                    │
  │ Passed       │ {N}                          │
  │ Failed       │ {N}                          │
  │ Skipped      │ {N}                          │
  │ Bugs Filed   │ {N} (ticket keys)            │
  │ Report       │ {path}                       │
  └──────────────┴──────────────────────────────┘
  ```

---

## Example Usage

```
> run-e2e dashboard-kpi
▶ Running dashboard-kpi suite...
▶ 12 tests: 10 passed, 2 failed
▶ Analyzing failures...
  - TC-KPI-003: Genuine bug — wrong KPI value displayed
  - TC-KPI-001: Selector error — KPI card selector changed
▶ Filed Jira ticket HLM-230: [E2E] Wrong KPI value displayed on dashboard
▶ Selector fix needed: Update DashboardPage.KPI_CARD selector
▶ Report: novus-example-tests/src/test/resources/reports/04-07-26/Dashboard-KPI-Report/Dashboard-KPI-Report.html
```
