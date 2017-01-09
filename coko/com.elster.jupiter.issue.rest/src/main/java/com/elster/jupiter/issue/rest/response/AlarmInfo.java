package com.elster.jupiter.issue.rest.response;


import com.elster.jupiter.issue.share.entity.Issue;

public class AlarmInfo {

    public long id;
    public String alarmId;
    public IssueReasonInfo reason;
    public IssueStatusInfo status;
    public long dueDate;
    public IssueAssigneeInfo assignee;
    public IssueAssigneeInfo workGroupAssignee;
    public IssueAssigneeInfo userAssignee;
    public String title;
    public long creationDate;
    public long modTime;
    public long version;

    public AlarmInfo(){

    }

    public AlarmInfo(Issue alarm){
        this.id = alarm.getId();
        this.alarmId = alarm.getIssueId();
        this.reason = new IssueReasonInfo(alarm.getReason());
        this.status = new IssueStatusInfo(alarm.getStatus());
        this.dueDate = alarm.getDueDate() != null ? alarm.getDueDate().toEpochMilli() : 0;
        this.assignee = (alarm.getAssignee() != null ? new IssueAssigneeInfo(alarm.getAssignee()) : null);
        this.workGroupAssignee = (alarm.getAssignee() != null ? new IssueAssigneeInfo("WORKGROUP", alarm.getAssignee()) : null);
        this.userAssignee = (alarm.getAssignee() != null ? new IssueAssigneeInfo("USER", alarm.getAssignee()) : null);
        this.title = alarm.getTitle();
        this.creationDate = alarm.getCreateTime().toEpochMilli();
        this.modTime = alarm.getModTime().toEpochMilli();
        this.version = alarm.getVersion();
    }
}
