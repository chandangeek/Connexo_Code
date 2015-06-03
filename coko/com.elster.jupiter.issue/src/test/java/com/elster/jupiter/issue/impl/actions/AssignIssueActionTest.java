package com.elster.jupiter.issue.impl.actions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.actions.AssignIssueAction.Assignee;
import com.elster.jupiter.issue.impl.service.BaseTest;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.properties.IdWithNameValue;
import com.elster.jupiter.users.User;


public class AssignIssueActionTest extends BaseTest {
    
    IssueAction action;
    
    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(AssignIssueAction.class.getName());
    }
    
    @Test
    @Transactional
    public void testCreateAndExecuteAction() {
        User user = getUserService().findOrCreateUser("user", "local", "directoryType");
        getThreadPrincipalService().set(user);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put(AssignIssueAction.ASSIGNEE, new Assignee(user));
        properties.put(AssignIssueAction.COMMENT, "Comment");
        Issue issue = createIssueMinInfo();
        
        assertThat(issue.getAssignee()).isNull();
        
        IssueActionResult actionResult = action.initAndValidate(properties).execute(issue);
        
        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(issue.getAssignee().getId()).isEqualTo(user.getId());
        assertThat(getIssueComments(issue)).hasSize(1);
        assertThat(getIssueComments(issue).get(0).getComment()).isEqualTo("Comment");
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "Property invalid value", property = "properties.AssignIssueAction.assignee", strict = true)
    public void testExecuteActionWrongAssignee() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AssignIssueAction.ASSIGNEE, IdWithNameValue.EMPTY);
        
        action.initAndValidate(properties);
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "Field cant be empty", property = "properties.AssignIssueAction.assignee", strict = true)
    public void testValidateMandatoryParameters() {
        action.initAndValidate(new HashMap<>());
    }
}