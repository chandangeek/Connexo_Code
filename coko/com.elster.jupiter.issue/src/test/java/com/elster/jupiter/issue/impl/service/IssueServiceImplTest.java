package com.elster.jupiter.issue.impl.service;

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
import java.util.Optional;
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
            assertThat(userRef).isNotEqualTo(Optional.empty());
            issue.addComment("comment", userRef.get());
            issue.save();
            context.commit();
        }
        Query <IssueComment> commentQuery = getIssueService().query(IssueComment.class);
        List<IssueComment> issueCommentList = commentQuery.select(Condition.TRUE);
        assertThat(issueCommentList).isNotEmpty();
        Optional<IssueComment> commentRef = getIssueService().findComment(issueCommentList.get(0).getId());
        assertThat(commentRef).isNotEqualTo(Optional.empty());
        assertThat(commentRef.get().getComment()).isEqualTo("comment");
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
