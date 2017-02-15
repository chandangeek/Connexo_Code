/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.alarms.impl.records.OpenDeviceAlarmImpl;

import java.time.Clock;
import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenDeviceAlarmEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    DataModel dataModel;
    @Mock
    IssueService issueService;
    @Mock
    DeviceAlarmService deviceAlarmService;

    OpenIssueImpl baseIssue;
    OpenDeviceAlarmImpl deviceAlarm;

    @Override
    protected Object getInstanceA() {
        if (deviceAlarm == null) {
            baseIssue = new OpenIssueImpl(dataModel, issueService, Clock.systemDefaultZone());
            baseIssue.setId(ID);
            deviceAlarm = new OpenDeviceAlarmImpl(dataModel, deviceAlarmService);
            deviceAlarm.setIssue(baseIssue);
        }
        return deviceAlarm;
    }

    @Override
    protected Object getInstanceEqualToA() {
        OpenIssueImpl baseIssue = new OpenIssueImpl(dataModel, issueService, Clock.systemDefaultZone());
        baseIssue.setId(ID);
        OpenDeviceAlarmImpl deviceAlarm = new OpenDeviceAlarmImpl(dataModel, deviceAlarmService);
        deviceAlarm.setIssue(baseIssue);
        return deviceAlarm;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        OpenIssueImpl baseIssue = new OpenIssueImpl(dataModel, issueService, Clock.systemDefaultZone());
        baseIssue.setId(OTHER_ID);
        OpenDeviceAlarmImpl deviceAlarm = new OpenDeviceAlarmImpl(dataModel, deviceAlarmService);
        deviceAlarm.setIssue(baseIssue);
        return Collections.singletonList(deviceAlarm);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
