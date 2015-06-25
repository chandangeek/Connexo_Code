package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.actions.CloseIssueAction.Status;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.service.BaseTest;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.users.User;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CloseIssueActionTest extends BaseTest {

    IssueAction action;
    
    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(CloseIssueAction.class.getName());
    }
    
    @Test
    @Transactional
    public void testCreateAndExecuteAction() {
        Map<String, Object> properties = new HashMap<>();
        IssueStatus resolvedStatus = getIssueService().findStatus(IssueStatus.RESOLVED).get();
        properties.put(CloseIssueAction.CLOSE_STATUS, new Status(resolvedStatus));
        properties.put(CloseIssueAction.COMMENT, "Comment");
        OpenIssue issue = createIssueMinInfo();
//        doAnswer(invocationOnMock -> {
//                IssueStatus status = (IssueStatus)invocationOnMock.getArguments()[0];
//                return issue.closeInternal(status);
//            }).when(issue).close(any());
        
        User user = getUserService().findOrCreateUser("user", "local", "directoryType");
        getThreadPrincipalService().set(user);
        
        assertThat(issue.getStatus().getKey()).isNotEqualTo(resolvedStatus.getKey());
        
        IssueActionResult actionResult = action.initAndValidate(properties).execute(issue);
        
        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(issue.getStatus().getKey()).isEqualTo(resolvedStatus.getKey());
        assertThat(getIssueComments(issue)).hasSize(1);
        assertThat(getIssueComments(issue).get(0).getComment()).isEqualTo("Comment");
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PROPERTY_NOT_POSSIBLE_VALUE + "}", property = "properties.CloseIssueAction.status", strict = true)
    public void testExecuteActionWrongClosingStatus() {
        Map<String, Object> properties = new HashMap<>();
        IssueStatus inProgressStatus = getIssueService().findStatus(IssueStatus.IN_PROGRESS).get();
        properties.put(CloseIssueAction.CLOSE_STATUS, new Status(inProgressStatus));
        
        action.initAndValidate(properties);
    }
    
    @Test
    @Transactional
    public void testExecuteActionIssueIsNotApplicable() {
        Map<String, Object> properties = new HashMap<>();
        IssueStatus resolvedStatus = getIssueService().findStatus(IssueStatus.RESOLVED).get();
        properties.put(CloseIssueAction.CLOSE_STATUS, new Status(resolvedStatus));
        Issue issue = mock(Issue.class);
        when(issue.getStatus()).thenReturn(resolvedStatus);
        
        IssueActionResult actionResult = action.initAndValidate(properties).execute(issue);
        
        assertThat(actionResult.isSuccess()).isFalse();
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PROPERTY_MISSING +"}", property = "properties.CloseIssueAction.status", strict = true)
    public void testValidateMandatoryParameters() {
        action.initAndValidate(new HashMap<>());
    }
    
    @Test
    public void testActionAvailabilityDependingOnStatsus() {
        Issue issue = mock(Issue.class);
        IssueStatus status = mock(IssueStatus.class);
        when(issue.getStatus()).thenReturn(status);
        when(status.getKey()).thenReturn(IssueStatus.OPEN);
        
        assertThat(action.isApplicable(issue)).isTrue();
        when(status.getKey()).thenReturn(IssueStatus.IN_PROGRESS);
        assertThat(action.isApplicable(issue)).isFalse();
        when(status.getKey()).thenReturn(IssueStatus.RESOLVED);
        assertThat(action.isApplicable(issue)).isFalse();
        when(status.getKey()).thenReturn(IssueStatus.WONT_FIX);
        assertThat(action.isApplicable(issue)).isFalse();
    }
}
