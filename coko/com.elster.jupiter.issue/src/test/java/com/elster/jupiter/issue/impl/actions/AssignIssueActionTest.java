/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.service.BaseTest;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AssignIssueActionTest extends BaseTest {

    IssueAction action;

    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(AssignIssueAction.class.getName());
    }
    
    @Test
    @Transactional
    public void testCreateAndExecuteAction() {
        LdapUserDirectory local = getUserService().createApacheDirectory("local");
        local.setSecurity("0");
        local.setUrl("url");
        local.setDirectoryUser("directoryUser");
        local.setPassword("password");
        local.update();
        User user = getUserService().findOrCreateUser("user", "local", "APD");
        getThreadPrincipalService().set(user);
        IssueAssignee issueAssignee = mock(IssueAssignee.class);
        when(issueAssignee.getUser()).thenReturn(user);
        when(issueAssignee.getWorkGroup()).thenReturn(null);


        Map<String, Object> properties = new HashMap<>();
        properties.put(AssignIssueAction.ASSIGNEE, new AssignIssueAction.Assignee(user, null, null));
        Issue issue = createIssueMinInfo();
        
        assertThat(issue.getAssignee().getUser()).isNull();
        assertThat(issue.getAssignee().getWorkGroup()).isNull();

        IssueActionResult actionResult = action.initAndValidate(properties).execute(issue);
        
        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(issue.getAssignee().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @Transactional
    public void testExecuteAssignToMeAction() {
        LdapUserDirectory local = getUserService().createApacheDirectory("local");
        local.setSecurity("0");
        local.setUrl("url");
        local.setDirectoryUser("directoryUser");
        local.setPassword("password");
        local.update();
        User user = getUserService().findOrCreateUser("user", "local", "APD");
        getThreadPrincipalService().set(user);

        Map<String, Object> properties = new HashMap<>();
        properties.put(AssignIssueAction.ASSIGNEE, new AssignIssueAction.Assignee(user, null, null));
        Issue issue = createIssueMinInfo();

        assertThat(issue.getAssignee().getUser()).isNull();
        assertThat(issue.getAssignee().getWorkGroup()).isNull();
        IssueAction actionAssignToMe = getDefaultActionsFactory().createIssueAction(AssignIssueAction.class.getName());

        IssueActionResult actionResult = actionAssignToMe.initAndValidate(properties).execute(issue);

        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(issue.getAssignee().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @Transactional
    public void testExecuteAssignToMeAndUnssignAction() {
        LdapUserDirectory local = getUserService().createApacheDirectory("local");
        local.setSecurity("0");
        local.setUrl("url");
        local.setDirectoryUser("directoryUser");
        local.setPassword("password");
        local.update();
        User user = getUserService().findOrCreateUser("user", "local", "APD");
        getThreadPrincipalService().set(user);

        Map<String, Object> properties = new HashMap<>();
        properties.put(AssignIssueAction.ASSIGNEE, new AssignIssueAction.Assignee(user, null, null));
        Issue issue = createIssueMinInfo();

        assertThat(issue.getAssignee().getUser()).isNull();
        assertThat(issue.getAssignee().getWorkGroup()).isNull();
        IssueAction actionAssignToMe = getDefaultActionsFactory().createIssueAction(AssignIssueAction.class.getName());
        IssueAction actionUnssign = getDefaultActionsFactory().createIssueAction(AssignIssueAction.class.getName());
        IssueActionResult actionResult = actionAssignToMe.initAndValidate(properties).execute(issue);

        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(issue.getAssignee().getUser().getId()).isEqualTo(user.getId());

        properties = new HashMap<>();
        actionResult = actionUnssign.initAndValidate(properties).execute(issue);
        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(issue.getAssignee().getUser()).isEqualTo(null);
    }

    @Test
    @Transactional
    public void testUserHasPrivileges() {
        User hasPrivilege = mock(User.class);
        Privilege assignPrivilege = mock(Privilege.class);
        when(assignPrivilege.getName()).thenReturn(Privileges.Constants.ASSIGN_ISSUE);
        Privilege actionPrivilege = mock(Privilege.class);
        when(actionPrivilege.getName()).thenReturn(Privileges.Constants.ACTION_ISSUE);
        Set<Privilege> privileges = new HashSet<>();
        privileges.add(assignPrivilege);
        privileges.add(actionPrivilege);
        when(hasPrivilege.getPrivileges()).thenReturn(privileges);

        User hasNoPrivileges = mock(User.class);

        assertThat(action.isApplicableForUser(hasPrivilege)).isTrue();
        assertThat(action.isApplicableForUser(hasNoPrivileges)).isFalse();
    }
}
