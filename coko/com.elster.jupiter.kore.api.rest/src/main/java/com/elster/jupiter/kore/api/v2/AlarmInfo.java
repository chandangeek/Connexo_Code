package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

public class AlarmInfo extends LinkInfo<Long> {
    public long id;
    public String alarmId;
    public IssueReasonInfo reason;
    public PriorityInfo priority;
    public int priorityValue;
    public AlarmStatusInfo status;
    public long dueDate;
    public IssueAssigneeInfo assignee;
    public IssueAssigneeInfo workGroupAssignee;
    public IssueAssigneeInfo userAssignee;
    public DeviceInfo device;
    public String title;
    public IssueTypeInfo alarmType;
    public long creationDate;
    public long modTime;
    public long version;
}
