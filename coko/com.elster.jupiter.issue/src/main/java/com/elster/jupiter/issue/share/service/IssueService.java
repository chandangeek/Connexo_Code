package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

import java.util.List;
import java.util.Map;

public interface IssueService {
    String COMPONENT_NAME = "ISU";

    List<GroupByReasonEntity> getIssueGroupList (String groupColumn, boolean isAsc, long from, long to, List<Long> id);
    Optional<Issue> createIssue(Map<?, ?> map);

    OperationResult<String, String[]> closeIssue(long issueId, long version, IssueStatus newStatus, String comment, User author);
    IssueAssignee getAssigneeFromRule(Rule rule);
    OperationResult<String, String[]> assignIssue(long issueId, long version, IssueAssigneeType type, long assignId, String comment, User author);
    void processAutoAssign (Issue issue);
}
