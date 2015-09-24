package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.records.AssignmentRuleImpl;
import com.elster.jupiter.issue.impl.records.IssueForAssignImpl;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueForAssign;
import com.elster.jupiter.util.conditions.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueAssigmentServiceImplTest extends BaseTest {

    @Test
    @Transactional
    public void issueAutoAssigmentTest() {
        Issue issue = createIssueMinInfo();
        assertThat(issue).isNotNull();

        AssignmentRule rule = getDataModel().getInstance(AssignmentRuleImpl.class);
        rule.setTitle("assigmentRule");
        rule.setDescription("Some description");
        rule.setRuleData("some data");
        rule.setEnabled(true);
        getThreadPrincipalService().set(() -> "console");
        rule.save();

        List<AssignmentRule> assignmentRules = getIssueAssignmentService().getAssignmentRuleQuery().select(Condition.TRUE);
        assertThat(assignmentRules).isNotEmpty();

        Optional<AssignmentRule> ruleRef = getIssueAssignmentService().findAssignmentRule(rule.getId());
        assertThat(ruleRef).isNotEqualTo(Optional.empty());

        List<IssueForAssign> issueList = new ArrayList<>();
        issueList.add(new IssueForAssignImpl(issue));
        getIssueAssignmentService().assignIssue(issueList);
    }

}