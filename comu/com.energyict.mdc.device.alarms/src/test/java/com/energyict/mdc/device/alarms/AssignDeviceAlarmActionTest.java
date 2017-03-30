/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms;


import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.impl.actions.AssignDeviceAlarmAction;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AssignDeviceAlarmActionTest extends BaseTest {

    IssueAction action;

    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(AssignDeviceAlarmAction.class.getName());
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

        Map<String, Object> properties = new HashMap<>();
        properties.put(AssignDeviceAlarmAction.ASSIGNEE, new AssignDeviceAlarmAction.Assignee(user, null, null));
        DeviceAlarm alarm = createAlarmMinInfo();

        assertThat(alarm.getAssignee().getUser()).isNull();
        assertThat(alarm.getAssignee().getWorkGroup()).isNull();

        IssueActionResult actionResult = action.initAndValidate(properties).execute(alarm);

        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(alarm.getAssignee().getUser().getId()).isEqualTo(user.getId());
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
        properties.put(AssignDeviceAlarmAction.ASSIGNEE, new AssignDeviceAlarmAction.Assignee(user, null, null));
        DeviceAlarm alarm = createAlarmMinInfo();

        assertThat(alarm.getAssignee().getUser()).isNull();
        assertThat(alarm.getAssignee().getWorkGroup()).isNull();
        IssueAction actionAssignToMe = getDefaultActionsFactory().createIssueAction(AssignDeviceAlarmAction.class.getName());

        IssueActionResult actionResult = actionAssignToMe.initAndValidate(properties).execute(alarm);

        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(alarm.getAssignee().getUser().getId()).isEqualTo(user.getId());
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
        properties.put(AssignDeviceAlarmAction.ASSIGNEE, new AssignDeviceAlarmAction.Assignee(user, null, null));
        DeviceAlarm deviceAlarm = createAlarmMinInfo();

        assertThat(deviceAlarm.getAssignee().getUser()).isNull();
        assertThat(deviceAlarm.getAssignee().getWorkGroup()).isNull();
        IssueAction actionAssignToMe = getDefaultActionsFactory().createIssueAction(AssignDeviceAlarmAction.class.getName());
        IssueAction actionUnssign = getDefaultActionsFactory().createIssueAction(AssignDeviceAlarmAction.class.getName());
        IssueActionResult actionResult = actionAssignToMe.initAndValidate(properties).execute(deviceAlarm);

        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(deviceAlarm.getAssignee().getUser().getId()).isEqualTo(user.getId());

        properties = new HashMap<>();
        actionResult = actionUnssign.initAndValidate(properties).execute(deviceAlarm);
        assertThat(actionResult.isSuccess()).isTrue();
        assertThat(deviceAlarm.getAssignee().getUser()).isEqualTo(null);
    }

}
