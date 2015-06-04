package com.elster.jupiter.issue.impl.actions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.service.BaseTest;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.users.User;

public class CommentIssueActionTest extends BaseTest {

    IssueAction action;
    
    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(CommentIssueAction.class.getName());
    }
    
    @Test
    @Transactional
    public void testCreateAndExecuteAction() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(CommentIssueAction.ISSUE_COMMENT, "Comment");
        Issue issue = createIssueMinInfo();
        
        User user = getUserService().findOrCreateUser("user", "local", "directoryType");
        getThreadPrincipalService().set(user);
        
        IssueActionResult actionResult = action.initAndValidate(properties).execute(issue);
        
        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(getIssueComments(issue)).hasSize(1);
        assertThat(getIssueComments(issue).get(0).getComment()).isEqualTo("Comment");
    }
    
    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PROPERTY_MISSING +"}", property = "properties.CommentIssueAction.comment", strict = true)
    public void testValidateMandatoryParameters() {
        action.initAndValidate(new HashMap<>());
    }
}
