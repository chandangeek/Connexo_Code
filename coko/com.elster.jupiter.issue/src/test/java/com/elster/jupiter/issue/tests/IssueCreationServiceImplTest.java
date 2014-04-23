package com.elster.jupiter.issue.tests;

import com.elster.jupiter.issue.impl.records.CreationRuleActionTypeImpl;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IssueCreationServiceImplTest extends BaseTest {

    @Test
    public void testSimpleCreation(){
        long id = 0L;
        // Simple save creation rule
        try (TransactionContext context = getContext()){
            CreationRule rule = getSimpleCreationRule();
            rule.save();
            id = rule.getId();
            context.commit();
        }
        // Add creation rule parameter
        try (TransactionContext context = getContext()){
            List<CreationRule> rules = getIssueCreationService().getCreationRuleQuery().select(Condition.TRUE);
            assertThat(rules).hasSize(1);

            CreationRule rule = getIssueCreationService().findCreationRule(id).orNull();
            assertThat(rule).isNotNull();

            rule.addParameter("key", "value");
            rule.update();
            context.commit();
        }
        // Clear parameters
        try (TransactionContext context = getContext()){
            CreationRule rule = getIssueCreationService().findCreationRule(id).orNull();
            assertThat(rule.getParameters()).hasSize(1);
            rule.getParameters().clear();
            rule.update();

            rule = getIssueCreationService().findCreationRule(id).orNull();
            assertThat(rule.getParameters()).hasSize(0);
        }
    }

    @Test
    public void testCretionRuleDelete() {
        try (TransactionContext context = getContext()) {
            // Simple deletion
            CreationRule rule = getSimpleCreationRule();
            rule.save();
            rule.delete();
            rule = getIssueCreationService().findCreationRule(rule.getId()).orNull();
            assertThat(rule).isNull();

            // Delete when some issue has reference
            rule = getSimpleCreationRule();
            rule.save();
            Issue issue = getDataModel().getInstance(IssueImpl.class);
            issue.setReason(getIssueService().findReason(1L).orNull());
            issue.setStatus(getIssueService().findStatus(1L).orNull());
            issue.setRule(rule);
            issue.save();
            rule.delete();
            rule = getIssueCreationService().findCreationRule(rule.getId()).orNull();
            assertThat(rule).isNotNull();
            assertThat(rule.getObsoleteTime()).isNotNull();

            // Delete when closed issue has reference
            rule = getSimpleCreationRule();
            rule.save();
            issue.setRule(rule);
            issue.close(getIssueService().findStatus(2L).orNull());
            rule.delete();
            assertThat(rule).isNotNull();
            assertThat(rule.getObsoleteTime()).isNotNull();

            // Check all rules after deletion
            List<CreationRule> rules = getIssueCreationService().getCreationRuleQuery().select(Condition.TRUE);
            for (CreationRule creationRule : rules) {
                assertThat(creationRule.getObsoleteTime()).isNull();
            }
        }
    }

    @Test
    public void testRuleActions(){
        try (TransactionContext context = getContext()){
            CreationRule rule = getSimpleCreationRule();
            rule.save();

            // TODO use default rule action types
            CreationRuleActionType actionType = getDataModel().getInstance(CreationRuleActionTypeImpl.class);
            actionType.setName("name");
            actionType.setClassName("className");
            actionType.save();

            CreationRuleAction action = rule.addAction(actionType, CreationRuleActionPhase.CREATE);
            action.addParameter("key", "value");
            rule.update();

            rule = getIssueCreationService().findCreationRule(rule.getId()).orNull();
            assertThat(rule.getActions()).hasSize(1);
            assertThat(rule.getActions().get(0).getParameters()).hasSize(1);
        }
    }

    private CreationRule getSimpleCreationRule() {
        CreationRule rule = getIssueCreationService().createRule();
        rule.setName("Simple Rule");
        rule.setComment("Comment for rule");
        rule.setContent("Empty content");
        rule.setReason(getIssueService().findReason(1L).orNull());
        rule.setDueInValue(15L);
        rule.setDueInType(DueInType.DAY);
        rule.setTemplateUuid("Parent template uuid");
        return rule;
    }
}
