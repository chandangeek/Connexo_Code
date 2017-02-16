/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

public class IssueInfo extends LinkInfo<Long> {
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
    public String title;
    public IssueTypeInfo issueType;
    public long creationDate;
    public long modTime;
    public long version;

}