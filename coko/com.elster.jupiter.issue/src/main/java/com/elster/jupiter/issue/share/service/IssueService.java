package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssigneeType;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OperationResult;
import com.google.common.base.Optional;

import java.util.Map;

public interface IssueService {
    String COMPONENT_NAME = "ISU";

    Map<String, Long> getIssueGroupList (String groupColumn, boolean isAsc, long from, long to);
    Optional<Issue> createIssue(Map<?, ?> map);

    OperationResult<String, String[]> closeIssue(long issueId, long version, IssueStatus newStatus, String comment);
    OperationResult<String, String[]> assignIssue(long issueId, long version, IssueAssigneeType type, long assignId, String comment);
}
