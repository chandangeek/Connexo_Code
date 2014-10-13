package com.elster.jupiter.issue.tests;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.records.IssueActionTypeImpl;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.impl.service.IssueCreationServiceImpl;
import com.elster.jupiter.issue.share.cep.CreationRuleTemplate;
import com.elster.jupiter.issue.share.cep.IssueEvent;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;
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
            assertThat(rules).isNotEmpty();

            CreationRule rule = getIssueCreationService().findCreationRule(id).orNull();
            assertThat(rule).isNotNull();

            rule.addParameter("key", "value");
            rule.save();
            context.commit();
        }
        // Clear parameters
        try (TransactionContext context = getContext()){
            CreationRule rule = getIssueCreationService().findCreationRule(id).orNull();
            assertThat(rule.getParameters()).hasSize(1);
            rule.getParameters().clear();
            rule.save();

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
            OpenIssue issue = getDataModel().getInstance(OpenIssueImpl.class);
            issue.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orNull());
            issue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).orNull());
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
            issue.close(getIssueService().findStatus(IssueStatus.WONT_FIX).orNull());
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

            IssueActionTypeImpl actionType = getDataModel().getInstance(IssueActionTypeImpl.class);
            actionType.init("some", "class", (IssueType) null);
            actionType.save();

            CreationRuleAction action = rule.addAction(actionType, CreationRuleActionPhase.CREATE);
            action.addParameter("key", "value");
            rule.save();

            Query<CreationRuleAction> actionQuery = getIssueCreationService().getCreationRuleActionQuery();
            List<CreationRuleAction> actionList = actionQuery.select(Condition.TRUE);
            assertThat(actionList).isNotEmpty();
            CreationRule foundRule = getIssueCreationService().findCreationRule(rule.getId()).orNull();
            assertThat(foundRule.getActions()).isNotEmpty();
            assertThat(foundRule.getActions().get(0).getParameters()).hasSize(1);

            Optional<CreationRuleAction> foundActionRef = getIssueCreationService()
                    .findCreationRuleAction(foundRule.getActions().get(0).getId());
            assertThat(foundActionRef).isNotEqualTo(Optional.absent());
        }
    }

    @Test
    public void testRuleTemplates() {
        Optional<CreationRuleTemplate> templateRef = getIssueCreationService().findCreationRuleTemplate("fakeUuid");
        assertThat(templateRef).isEqualTo(Optional.absent());
        List<CreationRuleTemplate> templates = getIssueCreationService().getCreationRuleTemplates();
        assertThat(templates).isEmpty();

        CreationRuleTemplate template = getMockCreationRuleTemplate();
        IssueCreationServiceImpl impl = IssueCreationServiceImpl.class.cast(getIssueCreationService());
        impl.addRuleTemplate(template, new HashMap<String, Object>());
        assertThat(getIssueCreationService().getCreationRuleTemplates().size()).isEqualTo(1);

        assertThat(getIssueCreationService().reReadRules()).isTrue();

        impl.removeRuleTemplate(template);
        assertThat(getIssueCreationService().getCreationRuleTemplates()).isEmpty();
    }

    @Test
    @Ignore
    public void testCreationEvents() {
        IssueCreationServiceImpl impl = IssueCreationServiceImpl.class.cast(getIssueCreationService());
        IssueEvent event = getMockIssueEvent();
        impl.dispatchCreationEvent(Collections.singletonList(event));

        try (TransactionContext context = getContext()) {
            CreationRule rule = getSimpleCreationRule();
            IssueReason reason = getIssueService().findReason(ISSUE_DEFAULT_REASON).orNull();
            rule.setReason(reason);
            rule.save();

            impl.processIssueEvent(rule.getId(), event);

            Query<? extends Issue> query = getIssueService().query(OpenIssue.class, IssueReason.class);
            Condition condition = where("reason.key").isEqualTo(reason.getKey());
            List<? extends Issue> list = query.select(condition);
            assertThat(list.size()).isEqualTo(1);
        }
    }
}
