package com.energyict.mdc.device.alarms.entity;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.readings.EndDeviceEvent;

import java.time.Instant;

public interface OpenDeviceAlarm extends OpenIssue, DeviceAlarm {
    
    HistoricalDeviceAlarm close(IssueStatus status);

    void addOpenDeviceAlarm(EndDeviceEvent event, Instant timeStamp);

    void removeOpenDeviceAlarm(EndDeviceEvent event, Instant timeStamp);
    
}
