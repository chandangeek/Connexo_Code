package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.IssueForAssign;
import com.google.common.base.Optional;

import java.util.List;

public interface IssueAssignmentService {
    Optional<AssignmentRule> findAssignmentRule(long id);
    Query<AssignmentRule> getAssignmentRuleQuery(Class<?> ... eagers);
    void assignIssue(List<IssueForAssign> issueList);
}
