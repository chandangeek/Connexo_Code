/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v2.issue;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

public class IssueInfo extends LinkInfo<Long> {
    public long id;
    public String issueId;
    public String title;
    public IssueReasonInfo reason;
    public IssueTypeInfo issueType;
    public IssueStatusInfo status;
    public PriorityInfo priority;
    public int priorityValue;
    public IssueAssigneeInfo workGroupAssignee;
    public IssueAssigneeInfo userAssignee;
    public DeviceShortInfo device;
    public long dueDate;
    public long creationDate;
    public long version;

}