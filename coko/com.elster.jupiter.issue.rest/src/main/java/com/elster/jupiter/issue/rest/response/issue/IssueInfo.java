/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.issue;

import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.IssueStatusInfo;
import com.elster.jupiter.issue.rest.response.IssueTypeInfo;
import com.elster.jupiter.issue.rest.response.PriorityInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.issue.rest.response.device.UsagePointInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.EndDevice;

public class IssueInfo<T extends DeviceInfo, I extends Issue> {
    public long id;
    public String issueId;
    public IssueReasonInfo reason;
    public PriorityInfo priority;
    public int priorityValue;
    public IssueStatusInfo status;
    public long dueDate;
    public IssueAssigneeInfo assignee;
    public IssueAssigneeInfo workGroupAssignee;
    public IssueAssigneeInfo userAssignee;
    public DeviceInfo device;
    //TODO - refactor to to make IssueInfo independent of DeviceInfo
    public UsagePointInfo usagePointInfo;
    public String title;
    public IssueTypeInfo issueType;
    public long creationDate;
    public long modTime;
    public long version;
    public long snoozedDateTime;

    public IssueInfo(I issue) {
        init(issue, DeviceShortInfo.class);
    }

    public IssueInfo(I issue, Class<T> deviceType) {
        init(issue, deviceType);
    }

    private void init(Issue issue, Class<? extends DeviceInfo> deviceType) {
        if (issue != null) {
            this.id = issue.getId();
            this.issueId = issue.getIssueId();
            this.reason = new IssueReasonInfo(issue.getReason());
            this.priority = new PriorityInfo(issue.getPriority());
            this.priorityValue = issue.getPriority().getImpact() + issue.getPriority().getUrgency();
            this.status = new IssueStatusInfo(issue.getStatus());
            this.snoozedDateTime = issue.getSnoozeDateTime().isPresent() ? issue.getSnoozeDateTime().get().toEpochMilli() : 0;
            this.dueDate = issue.getDueDate() != null ? issue.getDueDate().toEpochMilli() : 0;
            this.assignee = (issue.getAssignee() != null ? new IssueAssigneeInfo(issue.getAssignee()) : null);
            this.workGroupAssignee = (issue.getAssignee() != null ? new IssueAssigneeInfo("WORKGROUP", issue.getAssignee()) : null);
            this.userAssignee = (issue.getAssignee() != null ? new IssueAssigneeInfo("USER", issue.getAssignee()) : null);
            try {
                this.device = issue.getDevice() != null ? deviceType.getConstructor(EndDevice.class).newInstance(issue.getDevice()) : null;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Coding exception: ", e);
            }
            this.usagePointInfo = issue.getUsagePoint().isPresent() ? new UsagePointInfo(issue.getUsagePoint().get()) : null;
            this.title = issue.getTitle();
            this.issueType = new IssueTypeInfo(issue.getType());
            this.creationDate = issue.getCreateDateTime().toEpochMilli();
            this.modTime = issue.getModTime().toEpochMilli();
            this.version = issue.getVersion();
        }
    }

    public String getDeviceName(){
        if (this.device != null)
            return this.device.getName();
        return "";
    }

    public String getUsageName() {
        if(this.usagePointInfo != null)
            return this.usagePointInfo.getInfo();
        return "";
    }
    public int getPriorityTotal(){
        return this.priority.urgency + this.priority.impact;
    }

    public long getDueDate(){
        return this.dueDate;
    }
    public long getId(){
        return this.id;
    }

    public long getCreatedDateTime(){
        return this.creationDate;
    }


}