/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.rest.util.IdWithNameInfo;

public class ManualIssueInfo<T extends DeviceInfo> extends IssueInfo<T, Issue> {

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
    public long version;
    public long snoozedDateTime;

    public DeviceInfo device;

    public ManualIssueInfo(Issue manualIssue, Class<T> deviceInfoClass) {
        super(manualIssue, deviceInfoClass);
        this.id = manualIssue.getId();
        this.alarmId = manualIssue.getIssueId();
        this.reason = new IdWithNameInfo(manualIssue.getReason().getKey(), manualIssue.getReason().getName());
        this.status = new IdWithNameInfo(manualIssue.getStatus().getKey(), manualIssue.getStatus().getName());
        this.snoozedDateTime = manualIssue.getSnoozeDateTime().isPresent() ? manualIssue.getSnoozeDateTime()
                .get()
                .toEpochMilli() : 0;
        this.dueDate = manualIssue.getDueDate() != null ? manualIssue.getDueDate().toEpochMilli() : 0;
        this.workGroupAssignee = (manualIssue.getAssignee() != null ? new IssueAssigneeInfo("WORKGROUP", manualIssue.getAssignee()) : null);
        this.userAssignee = (manualIssue.getAssignee() != null ? new IssueAssigneeInfo("USER", manualIssue.getAssignee()) : null);
        this.title = manualIssue.getTitle();
        this.creationDate = manualIssue.getCreateDateTime().toEpochMilli();
        this.version = manualIssue.getVersion();
        this.priority = new PriorityInfo(manualIssue.getPriority());
    }

}
