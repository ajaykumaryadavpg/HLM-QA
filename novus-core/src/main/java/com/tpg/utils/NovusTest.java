package com.tpg.utils;

public class NovusTest {
    private String author;
    private String scenario;
    private String outcome;
    private String status;
    private String testCaseId;
    private String category;
    private String failureMessage;
    private boolean potentialBug;
    private String bugs;

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTestCaseId() { return testCaseId; }
    public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getFailureMessage() { return failureMessage; }
    public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }

    public boolean isPotentialBug() { return potentialBug; }
    public void setPotentialBug(boolean potentialBug) { this.potentialBug = potentialBug; }

    public String getBugs() { return bugs; }
    public void setBugs(String bugs) { this.bugs = bugs; }
}
