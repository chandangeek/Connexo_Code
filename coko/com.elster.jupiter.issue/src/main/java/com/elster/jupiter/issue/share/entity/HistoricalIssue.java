package com.elster.jupiter.issue.share.entity;

public class HistoricalIssue extends Issue {

    public HistoricalIssue copy(Issue issue) {
        super.copy(issue);
        return this;
    }
}
