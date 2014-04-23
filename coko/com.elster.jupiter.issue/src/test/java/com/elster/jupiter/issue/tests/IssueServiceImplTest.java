package com.elster.jupiter.issue.tests;

import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.transaction.TransactionContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IssueServiceImplTest extends BaseTest{

    @Test (expected = UnderlyingSQLFailedException.class)
    public void testIssueCreationWithDefault(){
        try (TransactionContext context = getContext()) {
            Issue issue = getDataModel().getInstance(IssueImpl.class);
            issue.save();
        }
    }

    @Test
    public void testIssueCreationMinInfo(){
        try (TransactionContext context = getContext()) {
            Issue issue = getDataModel().getInstance(IssueImpl.class);
            issue.setReason(getIssueService().findReason(1).orNull());
            issue.setStatus(getIssueService().findStatus(1).orNull());
            issue.setRule(getSimpleCreationRule());
            issue.save();
        }
    }

    @Test
    public void testIssueAssignee(){
        AssigneeRole role = getIssueService().createAssigneeRole();
        try (TransactionContext context = getContext()) {
            assertSaveConstraintException(role, true);
            role.setName("name");
            assertSaveConstraintException(role, true);
            role.setDescription("description");
            assertSaveConstraintException(role, false);
            context.commit();
        }
        try (TransactionContext context = getContext()) {
            Issue issue = getDataModel().getInstance(IssueImpl.class);
            issue.setReason(getIssueService().findReason(1).orNull());
            issue.setStatus(getIssueService().findStatus(1).orNull());
            issue.setRule(getSimpleCreationRule());
            issue.assignTo(null);
            issue.assignTo(role);
            issue.save();
        }
        // Check that we save correct assignee for closed issues
        try (TransactionContext context = getContext()) {
            Issue issue = getDataModel().getInstance(IssueImpl.class);
            issue.setReason(getIssueService().findReason(1).orNull());
            issue.setStatus(getIssueService().findStatus(1).orNull());
            issue.setRule(getSimpleCreationRule());
            issue.assignTo(null);
            issue.assignTo(role);
            issue.save();
            issue.close(getIssueService().findStatus(2).orNull());
            Issue closedIssue = getIssueService().findIssue(issue.getId(), true).orNull();
            assertThat(closedIssue).isNotNull();
            IssueAssignee assignee = closedIssue.getAssignee();
            assertThat(assignee).isNotNull();
            assertThat(assignee.getId()).isEqualTo(role.getId());
            assertThat(assignee.getName()).isEqualTo(role.getName());
        }
    }

    private void assertSaveConstraintException(Entity entity, boolean expeceted){
        try {
            entity.save();
        }catch (ConstraintViolationException ex){
            assertThat(expeceted).isTrue();
            return;
        }
        assertThat(expeceted).isFalse();
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
        rule.save();
        return rule;
    }
}
