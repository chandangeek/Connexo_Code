package com.elster.jupiter.issue.rest.response.issue;

import com.elster.jupiter.issue.rest.response.IssueAssigneeInfo;
import com.elster.jupiter.issue.rest.response.IssueReasonInfo;
import com.elster.jupiter.issue.rest.response.IssueStatusInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.device.DeviceShortInfo;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.EndDevice;

public class IssueInfo<T extends DeviceInfo, I extends Issue> {
    public long id;
    public IssueReasonInfo reason;
    public IssueStatusInfo status;
    public long dueDate;
    public IssueAssigneeInfo assignee;
    public DeviceInfo device;
    public String title;
    public long creationDate;
    public long modTime;
    public long version;

    public IssueInfo(I issue){
        init(issue, DeviceShortInfo.class);
    }

    public IssueInfo(I issue, Class<T> deviceType){
        init(issue, deviceType);
    }

    private final void init(Issue issue, Class<? extends DeviceInfo> deviceType){
        if (issue != null) {
            this.id = issue.getId();
            this.reason = new IssueReasonInfo(issue.getReason());
            this.status = new IssueStatusInfo(issue.getStatus());
            this.dueDate = issue.getDueDate() != null ? issue.getDueDate().toEpochMilli() : 0;
            this.assignee = (issue.getAssignee() != null ? new IssueAssigneeInfo(issue.getAssignee()) : null);
            try {
                this.device = issue.getDevice() != null ? deviceType.getConstructor(EndDevice.class).newInstance(issue.getDevice()) : null;
            } catch (ReflectiveOperationException e) {
            }
            this.title = issue.getTitle();
            this.creationDate = issue.getCreateTime().toEpochMilli();
            this.modTime = issue.getModTime().toEpochMilli();
            this.version = issue.getVersion();
        }
    }
}
