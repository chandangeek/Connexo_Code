/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.service;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.IssueForAssign;

import java.util.Optional;
import java.util.List;

@ProviderType
public interface IssueAssignmentService {

    AssignmentRule createAssignmentRule(String title, String ruleData);
    
    Optional<AssignmentRule> findAssignmentRule(long id);

    Query<AssignmentRule> getAssignmentRuleQuery(Class<?>... eagers);

    void assignIssue(List<IssueForAssign> issueList);
    
    void loadAssignmentRuleFromFile(String absolutePath);

    void rebuildAssignmentRules();
}
