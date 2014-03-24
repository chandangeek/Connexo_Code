package com.elster.jupiter.issue.share.service;

import com.elster.jupiter.issue.impl.drools.IssueForAssign;

import java.util.List;

public interface IssueAssignmentService {
    void assignIssue(List<IssueForAssign> issueList);
}
