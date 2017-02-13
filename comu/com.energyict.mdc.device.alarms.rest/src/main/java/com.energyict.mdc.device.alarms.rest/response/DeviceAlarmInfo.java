package com.energyict.mdc.device.alarms.rest.response;

import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;

import java.time.Instant;
import java.util.List;

public class DeviceAlarmInfo {

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
    public boolean clearedStatus;
    public long version;

    public DeviceInfo device;
    public IdWithNameInfo logBook;
    public List<RelatedEventsInfo> relatedEvents;

    public DeviceAlarmInfo(DeviceAlarm deviceAlarm){
        this.id = deviceAlarm.getId();
        this.alarmId = deviceAlarm.getIssueId();
        this.reason = new IdWithNameInfo(deviceAlarm.getReason().getKey(), deviceAlarm.getReason().getName());
        this.status = new IdWithNameInfo(deviceAlarm.getStatus().getKey(), deviceAlarm.getStatus().getName());
        this.dueDate = deviceAlarm.getDueDate() != null ? deviceAlarm.getDueDate().toEpochMilli() : 0;
        this.workGroupAssignee = (deviceAlarm.getAssignee() != null ? new IssueAssigneeInfo("WORKGROUP", deviceAlarm.getAssignee()) : null);
        this.userAssignee = (deviceAlarm.getAssignee() != null ? new IssueAssigneeInfo("USER", deviceAlarm.getAssignee()) : null);
        this.title = deviceAlarm.getTitle();
        this.creationDate = deviceAlarm.getCreatedDateTime().toEpochMilli();
        this.version = deviceAlarm.getVersion();
        this.clearedStatus = deviceAlarm.isStatusCleared();
        this.priority = new PriorityInfo(deviceAlarm.getPriority());
    }

}
