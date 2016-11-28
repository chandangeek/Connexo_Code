package com.energyict.mdc.device.alarms.entity;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;

public interface OpenDeviceAlarm extends OpenIssue, DeviceAlarm {
    
    HistoricalDeviceAlarm close(IssueStatus status);
    
}
