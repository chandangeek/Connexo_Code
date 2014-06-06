package com.elster.jupiter.issue.tests;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
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

    @Test (expected = UnderlyingSQLFailedException.class)
    public void testIssueCreationWithDefault(){
        try (TransactionContext context = getContext()) {
            Issue issue = getDataModel().getInstance(IssueImpl.class);
            issue.save();
        }
    }

    @Test
    public void testIssueCreationMinInfo() {
        Query <Issue> issueQuery = getIssueService().query(Issue.class);
        List<Issue> issueList = issueQuery.select(Condition.TRUE);
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
    public void testStatusCreation() {
        String statusName = "not an issue";
        try (TransactionContext context = getContext()) {
            IssueStatus status = getIssueService().createStatus(statusName, true);
            Optional<IssueStatus> statusRef = getIssueService().findStatus(status.getId());
            assertThat(statusRef.isPresent()).isTrue();
            assertThat(statusRef.get().getName()).isEqualTo(statusName);
            assertThat(statusRef.get().isFinal()).isTrue();
        }
    }

    @Test
    public void testIssueTypeCreation() {
        String typeUUid = "3-14-15-92-65";
        String typeName = "depressive type";
        try (TransactionContext context = getContext()) {
            IssueType type = getIssueService().createIssueType(typeUUid, typeName);
            Optional<IssueType> typeRef = getIssueService().findIssueType(type.getUUID());
            assertThat(typeRef.isPresent()).isTrue();
            assertThat(typeRef.get().getName()).isEqualTo(typeName);
        }
    }

    @Test
    public void testReasonCreation() {
        String reasonName = "End of days reached";
        try (TransactionContext context = getContext()) {
            IssueType type = getIssueService().createIssueType("3-14-15-92-65", "depressive type");
            IssueReason reason = getIssueService().createReason(reasonName, type);
            Optional<IssueReason> reasonRef = getIssueService().findReason(reason.getId());
            assertThat(reasonRef.isPresent()).isTrue();
            assertThat(reasonRef.get().getName()).isEqualTo(reasonName);
        }
    }

    @Test
    public void testCommentCreation() {
        Issue issue = createIssueMinInfo();
        try (TransactionContext context = getContext()) {
            Optional<User> userRef = getUserService().findUser("admin");
            assertThat(userRef).isNotEqualTo(Optional.absent());
            issue.addComment("comment", userRef.get());
            issue.update();
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

/*    @Test
    public void testIssueGroupList() {
        Issue issueId = createIssueMinInfo();
        try (TransactionContext context = getContext()) {
            GroupQueryBuilder builder = new GroupQueryBuilder();
            builder.setId(1) // Reason id
                    .setFrom(0) // Pagination
                    .setTo(10)
                    .setSourceClass(Issue.class) // Issues, Historical Issues or Both
                    .setGroupColumn("reason") // Main grouping column
                    .setIssueType(getIssueService().findReason(1).get().getIssueType().getUUID()) // Reasons only with specific issue type
                    .setStatuses(null) // All selected statuses
                    .setAssigneeType(null) // User, Group ot Role type of assignee
                    .setAssigneeId(0) // Id of selected assignee
                    .setMeterId(0); // Filter by meter MRID
            List<GroupByReasonEntity> resultList = getIssueService().getIssueGroupList(builder);
            assertThat(resultList).isNotEmpty();
        }
    }*/
}
