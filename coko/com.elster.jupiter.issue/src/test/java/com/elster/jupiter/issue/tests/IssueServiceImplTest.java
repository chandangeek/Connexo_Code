package com.elster.jupiter.issue.tests;

import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.transaction.TransactionContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IssueServiceImplTest extends BaseTest{

    @Test (expected = UnderlyingSQLFailedException.class)
    public void testIssueCreationWithDefault(){
        try (TransactionContext context = getContext()) {
            Issue issue = getIssueService().createIssue();
            issue.save();
        }
    }

    @Test
    public void testIssueCreationMinInfo(){
        try (TransactionContext context = getContext()) {
            Issue issue = getIssueService().createIssue();
            issue.setReason(getIssueService().findReason(1).orNull());
            issue.setStatus(getIssueService().findStatus(1).orNull());
            issue.save();
        }
    }

    @Test
    public void testIssueAssignee(){
        AssigneeRole role = getIssueService().createAssigneeRole();
        try (TransactionContext context = getContext()) {
            assertSaveSqlException(role, true);
            role.setName("name");
            assertSaveSqlException(role, true);
            role.setDescription("description");
            assertSaveSqlException(role, false);
            context.commit();
        }
        try (TransactionContext context = getContext()) {
            Issue issue = getIssueService().createIssue();
            issue.setReason(getIssueService().findReason(1).orNull());
            issue.setStatus(getIssueService().findStatus(1).orNull());
            issue.setAssignee(null);
            issue.setAssignee(new IssueAssignee());
            issue.assignTo(role);
            issue.save();
        }
    }

    private void assertSaveSqlException(Entity entity, boolean expeceted){
        try {
            entity.save();
        }catch (UnderlyingSQLFailedException ex){
            assertThat(expeceted).isTrue();
            return;
        }
        assertThat(expeceted).isFalse();
    }
}
