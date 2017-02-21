/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.kore.api.v2.issue.DeviceShortInfo;
import com.elster.jupiter.kore.api.v2.issue.IssueAssigneeInfo;
import com.elster.jupiter.kore.api.v2.issue.IssueReasonInfo;
import com.elster.jupiter.kore.api.v2.issue.IssueTypeInfo;
import com.elster.jupiter.kore.api.v2.issue.PriorityInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

public class AlarmInfo extends LinkInfo<Long> {
    public long id;
    public String alarmId;
    public String title;
    public IssueReasonInfo reason;
    public AlarmStatusInfo status;
    public PriorityInfo priority;
    public int priorityValue;
    public IssueAssigneeInfo workGroupAssignee;
    public IssueAssigneeInfo userAssignee;
    public DeviceShortInfo device;
    public long dueDate;
    public long creationDate;
    public long version;
}
