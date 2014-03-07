package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.issue.share.entity.Issue;
import com.google.common.base.Optional;

public interface IssueHelpService {
    void setEventTopics();
    void getEvent();
    Optional<Issue> createTestIssue(String statusStr, String reasonStr, String deviceStr, long dueDate);
}
