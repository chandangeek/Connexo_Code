package com.elster.jupiter.issue.share.entity;

public interface OpenIssue extends Issue {
    HistoricalIssue close(IssueStatus status);
}
