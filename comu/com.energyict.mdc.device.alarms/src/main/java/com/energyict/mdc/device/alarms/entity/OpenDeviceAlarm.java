/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.entity;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.time.Instant;

public interface OpenDeviceAlarm extends OpenIssue, DeviceAlarm {
    
    HistoricalDeviceAlarm close(IssueStatus status);

    void addRelatedAlarmEvent(long endDeviceId, String endDeviceEventType, Instant eventTimestamp);

    void removeRelatedAlarmEvent(EndDeviceEvent event, Instant timeStamp);
    
}
