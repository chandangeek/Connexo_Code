package com.elster.jupiter.issue.impl.service;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.share.IssueGroupFilter;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueServiceImplTest extends BaseTest {

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "rule", strict = false)
    public void testIssueCreationWithDefault() {
        Issue issue = getDataModel().getInstance(IssueImpl.class);
        issue.update();
    }

    @Test
    @Transactional
    public void testIssueCreationMinInfo() {
        Query <OpenIssue> issueQuery = getIssueService().query(OpenIssue.class);
        List<OpenIssue> issueList = issueQuery.select(Condition.TRUE);
        assertThat(issueList).isEmpty();
        Issue issue = createIssueMinInfo();
        assertThat(issue).isNotNull();
    }

    @Test
    @Transactional
    public void testIssueAssigneeInfo() {
        Query <OpenIssue> issueQuery = getIssueService().query(OpenIssue.class);
        List<OpenIssue> issueList = issueQuery.select(Condition.TRUE);
        assertThat(issueList).isEmpty();
        Issue issue = createAssigneeInfo();
        assertThat(issue).isNotNull();
        assertThat(issue.getAssignee()).isNotNull();
        assertThat(issue.getAssignee().getWorkGroup().getName()).isEqualTo("WorkGroupName");
        assertThat(issue.getAssignee().getUser().getName()).isEqualTo("UserName");
    }

    @Test
    @Transactional
    public void testCommentCreation() {
        Issue issue = createIssueMinInfo();
        Optional<User> userRef = getUserService().findUser("admin");
        assertThat(userRef).isNotEqualTo(Optional.empty());
        issue.addComment("comment", userRef.get());
        issue.update();
        Query<IssueComment> commentQuery = getIssueService().query(IssueComment.class);
        List<IssueComment> issueCommentList = commentQuery.select(Condition.TRUE);
        assertThat(issueCommentList).isNotEmpty();
        Optional<IssueComment> commentRef = getIssueService().findComment(issueCommentList.get(0).getId());
        assertThat(commentRef).isNotEqualTo(Optional.empty());
        assertThat(commentRef.get().getComment()).isEqualTo("comment");
    }

    @Test
    @Transactional
    public void testIssueApiQuery(){
        createIssueMinInfo();
        OpenIssue closedIssue = createIssueMinInfo();
        closedIssue.close(getIssueService().findStatus(IssueStatus.RESOLVED).get());
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
        IssueGroupFilter builder = getIssueService().newIssueGroupFilter();
        builder.using(Issue.class)
                .onlyGroupWithKey(ISSUE_DEFAULT_REASON)
                .withIssueTypes(Arrays.asList(ISSUE_DEFAULT_TYPE_UUID))
                .groupBy("reason")
                .from(0).to(10);
        List<IssueGroup> resultList = getIssueService().getIssueGroupList(builder);
        assertThat(resultList).isNotEmpty();
    }
}
