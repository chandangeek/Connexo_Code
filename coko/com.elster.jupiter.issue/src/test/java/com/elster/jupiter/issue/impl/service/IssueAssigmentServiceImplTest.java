/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.records.IssueForAssignImpl;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueForAssign;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueAssigmentServiceImplTest extends BaseTest {

    @Test
    @Transactional
    public void issueAutoAssigmentTest() {
        Issue issue = createIssueMinInfo();
        assertThat(issue).isNotNull();

        AssignmentRule rule = getIssueAssignmentService().createAssignmentRule("assigmentRule", "some data");
        rule.setDescription("Some description");
        rule.setEnabled(true);
        rule.update();
        getThreadPrincipalService().set(() -> "console");

        List<AssignmentRule> assignmentRules = getIssueAssignmentService().getAssignmentRuleQuery().select(Condition.TRUE);
        assertThat(assignmentRules).isNotEmpty();

        Optional<AssignmentRule> ruleRef = getIssueAssignmentService().findAssignmentRule(rule.getId());
        assertThat(ruleRef).isNotEqualTo(Optional.empty());

        List<IssueForAssign> issueList = new ArrayList<>();
        issueList.add(new IssueForAssignImpl(issue, Instant.now(Clock.systemDefaultZone())));
        getIssueAssignmentService().assignIssue(issueList);
    }

}