/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.response;

import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;

import java.util.List;

public class DeviceAlarmInfo<T extends DeviceInfo> extends IssueInfo<T, DeviceAlarm> {

    public long id;
    public String alarmId;
    public IdWithNameInfo reason;
    public IdWithNameInfo status;
    public PriorityInfo priority;
    public long dueDate;
    public IssueAssigneeInfo workGroupAssignee;
    public IssueAssigneeInfo userAssignee;
    public String title;
    public long creationDate;
    public DeviceAlarmClearStatusInfo clearedStatus;
    public long version;
    public long snoozedDateTime;

    public DeviceInfo device;
    public IdWithNameInfo logBook;
    public List<RelatedEventsInfo> relatedEvents;

    public DeviceAlarmInfo(DeviceAlarm deviceAlarm, Class<T> deviceInfoClass){
        super(deviceAlarm,deviceInfoClass);
        this.id = deviceAlarm.getId();
        this.alarmId = deviceAlarm.getIssueId();
        this.reason = new IdWithNameInfo(deviceAlarm.getReason().getKey(), deviceAlarm.getReason().getName());
        this.status = new IdWithNameInfo(deviceAlarm.getStatus().getKey(), deviceAlarm.getStatus().getName());
        this.snoozedDateTime = deviceAlarm.getSnoozeDateTime().isPresent() ? deviceAlarm.getSnoozeDateTime()
                .get()
                .toEpochMilli() : 0;
        this.dueDate = deviceAlarm.getDueDate() != null ? deviceAlarm.getDueDate().toEpochMilli() : 0;
        this.workGroupAssignee = (deviceAlarm.getAssignee() != null ? new IssueAssigneeInfo("WORKGROUP", deviceAlarm.getAssignee()) : null);
        this.userAssignee = (deviceAlarm.getAssignee() != null ? new IssueAssigneeInfo("USER", deviceAlarm.getAssignee()) : null);
        this.title = deviceAlarm.getTitle();
        this.creationDate = deviceAlarm.getCreateDateTime().toEpochMilli();
        this.version = deviceAlarm.getVersion();
        this.clearedStatus = new DeviceAlarmClearStatusInfo(deviceAlarm.getClearStatus());
        this.priority = new PriorityInfo(deviceAlarm.getPriority());
    }

}
