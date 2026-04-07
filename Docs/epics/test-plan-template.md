# Test Plan: Story {N.M} — {Story Title}

| Field             | Value                   |
| ----------------- | ----------------------- |
| **Story**         | Story {N.M}             |
| **Jira Ticket**   | {JIRA_KEY}-{NNN}        |
| **Epic**          | Epic {N} — {Epic Title} |
| **Generated**     | {YYYY-MM-DD}            |
| **QA Plan Ticket**| {JIRA_KEY}-{NNN}        |

## Story Summary

{One-line user story: As a [role], I want [capability] so that [benefit]}

## Acceptance Criteria Covered

- AC1: {description from story}
- AC2: {description from story}
- ...

## Test Cases

### TC-1: {Title matching AC1}

- **Priority:** P1
- **Pre-conditions:** {e.g., User logged in as Admin, on /inventory page}
- **Steps:**
  1. {Action using real selector discovered via Playwright MCP}
  2. {Next action}
- **Expected Result:** {Observable outcome}
- **Selector(s):** `{data-testid or CSS selector discovered}`

### TC-2: {Title matching AC2}

- **Priority:** P1
- **Pre-conditions:** {pre-conditions}
- **Steps:**
  1. {action}
- **Expected Result:** {outcome}
- **Selector(s):** `{selector}`

### TC-N: {Edge case title}

- **Priority:** P3
- **Pre-conditions:** {pre-conditions}
- **Steps:**
  1. {action}
- **Expected Result:** {outcome}

## Edge Cases

| TC     | Description                       | Priority |
| ------ | --------------------------------- | -------- |
| TC-{X} | {Empty state scenario}            | P2       |
| TC-{Y} | {Error handling scenario}         | P2       |
| TC-{Z} | {Performance/responsive scenario} | P3       |

## Discovered Selectors

| Element        | Selector           | Strategy                        |
| -------------- | ------------------ | ------------------------------- |
| {element name} | `{selector value}` | data-testid / css / role / text |
| ...            | ...                | ...                             |

## E2E Mapping

| TC   | Test Class            | Test Method         | testCaseId          |
| ---- | --------------------- | ------------------- | ------------------- |
| TC-1 | {Module}{Feature}Tests | verify{Description} | TC-{PREFIX}-{NNN}   |
| TC-2 | {Module}{Feature}Tests | verify{Description} | TC-{PREFIX}-{NNN}   |
| ...  | ...                   | ...                 | ...                 |

## Notes

- {Observations from live app exploration}
- {Any known limitations or mock data dependencies}
