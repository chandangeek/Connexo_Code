/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms;


import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.impl.actions.CloseDeviceAlarmAction;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CloseDeviceAlarmActionTest extends BaseTest {

    IssueAction action;

    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(CloseDeviceAlarmAction.class.getName());
    }

    @Test
    @Transactional
    public void testCloseDeviceAlarm(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(CloseDeviceAlarmAction.CLOSE_STATUS, new CloseDeviceAlarmAction.Status(getIssueService().findStatus(IssueStatus.RESOLVED).get()));
        DeviceAlarm alarm = createAlarmMinInfo();

        assertThat(alarm.getAssignee().getUser()).isNull();
        assertThat(alarm.getAssignee().getWorkGroup()).isNull();

        IssueActionResult actionResult = action.initAndValidate(properties).execute(alarm);

        assertThat(actionResult.isSuccess()).isTrue();
    }

}
