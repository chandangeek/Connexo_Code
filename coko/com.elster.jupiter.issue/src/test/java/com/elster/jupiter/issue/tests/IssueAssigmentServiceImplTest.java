package com.elster.jupiter.issue.tests;

import com.elster.jupiter.issue.impl.records.AssignmentRuleImpl;
import com.elster.jupiter.issue.impl.records.IssueForAssignImpl;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueForAssign;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IssueAssigmentServiceImplTest extends BaseTest {

    @Test
    public void issueAutoAssigmentTest() {
        Issue issue = createIssueMinInfo();
        assertThat(issue).isNotNull();

        try (TransactionContext context = getContext()) {
            AssignmentRule rule = getDataModel().getInstance(AssignmentRuleImpl.class);
            rule.setTitle("assigmentRule");
            rule.setDescription("Some description");
            rule.setRuleData("some data");
            rule.setEnabled(true);
            getThreadPrincipalService().set(new Principal() {
                @Override
                public String getName() {
                    return "console";
                }
            });
            rule.save();

            List<AssignmentRule> assignmentRules = getIssueAssignmentService().getAssignmentRuleQuery().select(Condition.TRUE);
            assertThat(assignmentRules).isNotEmpty();

            Optional<AssignmentRule> ruleRef = getIssueAssignmentService().findAssignmentRule(rule.getId());
            assertThat(ruleRef).isNotEqualTo(Optional.absent());

            List<IssueForAssign> issueList = new ArrayList<>();
            issueList.add(new IssueForAssignImpl(issue));
            getIssueAssignmentService().assignIssue(issueList);
        }
    }
}
