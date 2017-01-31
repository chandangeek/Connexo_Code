/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.issue.impl.records.HistoricalIssueImpl;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.alarms.impl.records.HistoricalDeviceAlarmImpl;

import java.time.Clock;
import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HistoricalDeviceAlarmEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    DataModel dataModel;
    @Mock
    IssueService issueService;
    @Mock
    DeviceAlarmService deviceAlarmService;

    HistoricalIssueImpl baseIssue;
    HistoricalDeviceAlarmImpl historicalDeviceAlarm;

    @Override
    protected Object getInstanceA() {
        if (historicalDeviceAlarm == null) {
            baseIssue = new HistoricalIssueImpl(dataModel, issueService, Clock.systemDefaultZone());
            baseIssue.setId(ID);
            historicalDeviceAlarm = new HistoricalDeviceAlarmImpl(dataModel, deviceAlarmService);
            historicalDeviceAlarm.setIssue(baseIssue);
        }
        return historicalDeviceAlarm;
    }

    @Override
    protected Object getInstanceEqualToA() {
        HistoricalIssueImpl baseIssue = new HistoricalIssueImpl(dataModel, issueService, Clock.systemDefaultZone());
        baseIssue.setId(ID);
        HistoricalDeviceAlarmImpl historicalDeviceAlarm = new HistoricalDeviceAlarmImpl(dataModel, deviceAlarmService);
        historicalDeviceAlarm.setIssue(baseIssue);
        return historicalDeviceAlarm;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        HistoricalIssueImpl baseIssue = new HistoricalIssueImpl(dataModel, issueService, Clock.systemDefaultZone());
        baseIssue.setId(OTHER_ID);
        HistoricalDeviceAlarmImpl historicalDeviceAlarm = new HistoricalDeviceAlarmImpl(dataModel, deviceAlarmService);
        historicalDeviceAlarm.setIssue(baseIssue);
        return Collections.singletonList(historicalDeviceAlarm);
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
