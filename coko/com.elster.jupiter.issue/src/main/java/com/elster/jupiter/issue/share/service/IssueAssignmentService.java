package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.drools.IssueForAssign;
import com.elster.jupiter.issue.share.entity.Rule;
import com.google.common.base.Optional;

import java.util.List;

public interface IssueAssignmentService {
    Optional<Rule> findAssignmentRule(long id);
    Query<Rule> getAssignmentRuleQuery(Class<?> ... eagers);
    void assignIssue(List<IssueForAssign> issueList);
}
