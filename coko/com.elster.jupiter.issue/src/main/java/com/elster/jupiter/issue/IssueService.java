package com.elster.jupiter.issue;

import com.elster.jupiter.domain.util.Query;
import com.google.common.base.Optional;

import java.util.Map;

/**
 This interface provides operations for manipulation with issues in database
 */
public interface IssueService {
    String COMPONENT_NAME = "ISU";

    Optional<Issue> getIssueById(long issueId);

    Optional<IssueReason> getIssueReasonById(long reasonId);
    void createIssueReason(String reasonName);

    Optional<IssueStatus> getIssueStatusById(long statusId);
    IssueStatus getIssueStatusFromString(String status);
    void createIssueStatus(String statusName);

    Query<Issue> getIssueListQuery();
    Query<AssigneeRole> getAssigneeRoleListQuery();
    Query<AssigneeTeam> getAssigneeTeamListQuery();
    Map<String, Long> getIssueGroupList (String groupColumn, boolean isAsc, long start, long limit);

    OperationResult<String, String[]> closeIssue(long issueId, long version, IssueStatus newStatus, String comment);
    OperationResult<String, String[]> assignIssue(long issueId, long version, IssueAssigneeType type, long assignId, String comment);
}
