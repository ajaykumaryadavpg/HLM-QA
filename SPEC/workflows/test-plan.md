# Workflow: test-plan

> **Trigger:** User says `test-plan story-N.M` or `test-plan #issue`
> **Agent:** [qa-planner](../agents/qa-planner.md)
> **Rulebook:** [e2e-rulebook](../rulebooks/e2e-rulebook.md)
> **Output:** GitHub sub-issue + local test plan file

---

## Pre-requisites

- [ ] Dev server running (`npm run dev` on localhost:5173) — needed for Playwright MCP exploration
- [ ] GitHub MCP server configured in `.mcp.json`
- [ ] Playwright MCP server configured in `.mcp.json`

---

## Checklist

### Step 1: Parse Input

- [ ] Extract epic number (N), story number (N.M), and GitHub issue number from command argument
- [ ] Accepted formats: `story-N.M`, `#NNN`, `epic-N/story-N.M`
- [ ] If only story ID given, look up GitHub issue # from `Docs/epics/epic-{N}/story-{N.M}.md` → `GitHub Issue: #N`

### Step 2: Read the Story

- [ ] Read local file: `Docs/epics/epic-{N}/story-{N.M}.md`
- [ ] Extract: User Story, all Acceptance Criteria (AC1..ACn), UI Behavior, Implementation Notes, Out of Scope
- [ ] If local file not found, fetch from GitHub via MCP: `get_issue(#{issue_number})`
- [ ] Identify the application route this story exercises (e.g., `/inventory`, `/deployment`)

### Step 3: Explore Live App via Playwright MCP

- [ ] `browser_navigate` to the relevant route (login first if needed)
- [ ] `browser_snapshot` to get accessibility tree — extract real selectors:
  - `data-testid` attributes
  - `aria-label` values
  - Element roles and states
- [ ] `browser_screenshot` to visually confirm UI layout
- [ ] If story involves interactions (tabs, modals, forms, filters):
  - `browser_click` on interactive elements
  - `browser_snapshot` again to discover dynamic content selectors
  - `browser_screenshot` to confirm state changes
- [ ] Record all discovered selectors in a working list

### Step 4: Generate Test Plan

- [ ] Read template: `Docs/epics/test-plan-template.md`
- [ ] For each AC → create one P1 test case with:
  - Pre-conditions (logged in as which role, on which page)
  - Steps (using real selectors from Step 3)
  - Expected result (observable outcome)
- [ ] For UI behavior items → create P2 test cases
- [ ] For edge cases → create P3 test cases:
  - Empty state (no data matches filter)
  - Error state (invalid input, API failure)
  - Boundary values (max items, long strings)
  - Unauthorized access (wrong role)
- [ ] Fill the "Discovered Selectors" table
- [ ] Fill the "E2E Mapping" table with proposed testCaseId values:
  - Check existing IDs in `SPEC/rulebooks/e2e-rulebook.md` Module Prefixes table
  - Continue numbering from the last existing ID for the module
- [ ] Present test plan to user for review

### Step 5: Create GitHub Sub-Issue

- [ ] Via GitHub CLI or MCP `create_issue`:
  - **Title:** `[QA Plan] Story {N.M} — {story title}`
  - **Labels:** `task`, `qa-plan`
  - **Body:** Full test plan markdown
  - **Include:** `Parent: #{parent_story_issue_number}` in body
- [ ] Record the new issue number
- [ ] **Link as sub-issue** of the parent story via GraphQL:
  ```graphql
  mutation {
    addSubIssue(input: { issueId: "{parent_node_id}", subIssueId: "{qa_plan_node_id}" }) {
      issue {
        id
      }
      subIssue {
        id
      }
    }
  }
  ```

  - Get node IDs: `gh api repos/{owner}/{repo}/issues/{number} --jq '.node_id'`

### Step 6: Add to GitHub Projects Board & Update Statuses

- [ ] Add QA Plan issue to project board via GraphQL:
  - `addProjectV2ItemById` with Project ID `PVT_kwHOARf1_84BTD7o`
  - `updateProjectV2ItemFieldValue` → set Status field (`PVTSSF_lAHOARf1_84BTD7ozhAadYA`) to **"In Development"** (`1f601fb5`) — QA plan is in progress
- [ ] **Update parent story status to "In QA"** on the project board:
  - `addProjectV2ItemById` to ensure parent is on board (idempotent)
  - `updateProjectV2ItemFieldValue` → set Status field (`PVTSSF_lAHOARf1_84BTD7ozhAadYA`) to **"In QA"** (`69b90ffa`)
- [ ] If GraphQL fails, log a note and continue (user can manually update board)

### Step 7: Write Local Test Plan File

- [ ] Create directory if needed: `Docs/epics/epic-{N}/test-plans/`
- [ ] Write file: `Docs/epics/epic-{N}/test-plans/story-{N.M}-test-plan.md`
- [ ] Content: Same test plan as GitHub issue body + metadata header (date, issue links)

### Step 8: Report

- [ ] Print summary:
  - Number of test cases generated (P1/P2/P3 breakdown)
  - GitHub QA Plan issue URL
  - Local test plan file path
  - Parent story issue reference
  - Next step: "Review the test plan, then run `generate-e2e story-{N.M}` to create test scripts"

---

## Degraded Mode

If the dev server is not running (Playwright MCP cannot navigate):

1. Skip Step 3 (live exploration)
2. Generate test plan from ACs only — mark selectors as `TBD (discover via Playwright MCP)`
3. Add a note to the test plan: "Selectors not confirmed — run Playwright MCP exploration before generating test code"
4. Continue with Steps 5-8 as normal

---

## Example

```
User: test-plan story-1.1

Claude:
→ Reads Docs/epics/epic-1/story-1.1.md (User Login with Email and Password)
→ Navigates to http://localhost:5173/login via Playwright MCP
→ Snapshots: discovers input#signin-email, input#signin-password, button[type='submit'], [role='alert']
→ Generates 6 test cases (3 P1 from ACs, 2 P2 UI behavior, 1 P3 edge)
→ Creates GitHub issue: [QA Plan] Story 1.1 — User Login with Email and Password
→ Writes: Docs/epics/epic-1/test-plans/story-1.1-test-plan.md
→ Reports: "Created 6 test cases. QA Plan: #147. Review and run `generate-e2e story-1.1` next."
```
