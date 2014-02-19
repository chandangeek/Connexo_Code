package com.elster.jupiter.issue;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.exception.IssueClosingException;
import com.google.common.base.Optional;

import java.util.Map;

/**
 This interface provides operations for manipulation with issues in database
 */
public interface IssueService {
    String COMPONENT_NAME = "ISU";

    Optional<Issue> getIssueById(long issueId);
    Query<Issue> getIssueListQuery();
    Map<String, Long> getIssueGroupList (String groupColumn, boolean isAsc, long start, long limit);

    void closeIssue(long issueId, long version, IssueStatus newStatus, String comment) throws IssueClosingException;
}
