package com.elster.jupiter.issue.tests;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IssueServiceImplTest extends BaseTest{

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "rule", strict = false)
    public void testIssueCreationWithDefault(){
        try (TransactionContext context = getContext()) {
            Issue issue = getDataModel().getInstance(IssueImpl.class);
            issue.save();
        }
    }

    @Test
    public void testIssueCreationMinInfo() {
        Query <OpenIssue> issueQuery = getIssueService().query(OpenIssue.class);
        List<OpenIssue> issueList = issueQuery.select(Condition.TRUE);
        assertThat(issueList).isEmpty();
        Issue issue = createIssueMinInfo();
        assertThat(issue).isNotNull();
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
            OpenIssue issue = getDataModel().getInstance(OpenIssueImpl.class);
            issue.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orNull());
            issue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).orNull());
            issue.setRule(getSimpleCreationRule());
            issue.assignTo(null);
            issue.assignTo(role);
            issue.save();
        }
        // Check that we save correct assignee for closed issues
        try (TransactionContext context = getContext()) {
            OpenIssue issue = getDataModel().getInstance(OpenIssueImpl.class);
            issue.setReason(getIssueService().findReason(ISSUE_DEFAULT_REASON).orNull());
            issue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).orNull());
            issue.setRule(getSimpleCreationRule());
            issue.assignTo(null);
            issue.assignTo(role);
            issue.save();
            issue.close(getIssueService().findStatus(IssueStatus.RESOLVED).orNull());
            Issue closedIssue = getIssueService().findHistoricalIssue(issue.getId()).orNull();
            assertThat(closedIssue).isNotNull();
            IssueAssignee assignee = closedIssue.getAssignee();
            assertThat(assignee).isNotNull();
            assertThat(getIssueService().findAssigneeTeam(closedIssue.getAssignee().getId())).isEqualTo(Optional.absent());
            assertThat(getIssueService().findAssigneeRole(closedIssue.getAssignee().getId())).isNotEqualTo(Optional.absent());
            assertThat(assignee.getId()).isEqualTo(role.getId());
            assertThat(assignee.getName()).isEqualTo(role.getName());
            assertThat(getIssueService().checkIssueAssigneeType("ALIEN")).isFalse();
            assertThat(getIssueService().checkIssueAssigneeType(assignee.getType())).isTrue();
            assertThat(getIssueService().findIssueAssignee("extraterrestre", 1)).isNull();
            assertThat(getIssueService().findIssueAssignee(assignee.getType(), assignee.getId())).isNotNull();
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

    @Test
    public void testCommentCreation() {
        Issue issue = createIssueMinInfo();
        try (TransactionContext context = getContext()) {
            Optional<User> userRef = getUserService().findUser("admin");
            assertThat(userRef).isNotEqualTo(Optional.absent());
            issue.addComment("comment", userRef.get());
            issue.save();
            context.commit();
        }
        Query <IssueComment> commentQuery = getIssueService().query(IssueComment.class);
        List<IssueComment> issueCommentList = commentQuery.select(Condition.TRUE);
        assertThat(issueCommentList).isNotEmpty();
        Optional<IssueComment> commentRef = getIssueService().findComment(issueCommentList.get(0).getId());
        assertThat(commentRef).isNotEqualTo(Optional.absent());
        assertThat(commentRef.get().getComment()).isEqualTo("comment");
    }

    @Test
    public void testAssigneeTeamCreation() {
        AssigneeTeam team = getIssueService().createAssigneeTeam();
        try (TransactionContext context = getContext()) {
            assertSaveConstraintException(team, true);
            team.setName("name");
            assertSaveConstraintException(team, false);
            context.commit();
        }
    }

    @Test
    public void testIssueApiQuery(){
        deactivateEnvironment();
        setEnvironment();
        createIssueMinInfo();
        OpenIssue issue = createIssueMinInfo();
        try (TransactionContext context = getContext()) {
            issue.close(getIssueService().findStatus(IssueStatus.RESOLVED).get());
            context.commit();
        }
        // So fo now we have one open issue and one closed
        int size = getIssueService().query(OpenIssue.class).select(Condition.TRUE).size();
        assertThat(size).isEqualTo(1);
        size = getIssueService().query(HistoricalIssue.class).select(Condition.TRUE).size();
        assertThat(size).isEqualTo(1);
        size = getIssueService().query(Issue.class).select(Condition.TRUE).size();
        assertThat(size).isEqualTo(2);
    }

    public void testIssueGroupList() {
        createIssueMinInfo();
        createIssueMinInfo();
        try (TransactionContext context = getContext()) {
            IssueGroupFilter builder = new IssueGroupFilter();
            builder.using(Issue.class)
                    .onlyGroupWithKey(ISSUE_DEFAULT_REASON)
                    .withIssueType(ISSUE_DEFAULT_TYPE_UUID)
                    .groupBy("reason")
                    .from(0).to(10);
            List<IssueGroup> resultList = getIssueService().getIssueGroupList(builder);
            assertThat(resultList).isNotEmpty();
        }
    }
}
