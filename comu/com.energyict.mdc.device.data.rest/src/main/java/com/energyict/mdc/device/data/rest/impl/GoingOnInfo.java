/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.bpm.UserTaskInfo;

import java.time.Instant;
import java.util.Comparator;

public class GoingOnInfo {
    public String type;
    public String id;
    public String reference;
    public String description;
    public Instant dueDate;
    public Severity severity;
    public String userAssignee;
    public String workGroupAssignee;
    public boolean isMyWorkGroup = false;
    public String issueType;
    public String reason;
    public Boolean userAssigneeIsCurrentUser;
    public String status;
    public UserTaskInfo userTaskInfo;

    static Comparator<GoingOnInfo> order() {

        return Comparator
                .comparing(GoingOnInfo::getSeverity)
                .thenComparing(GoingOnInfo::getDueDate, Comparator.<Instant>naturalOrder().reversed());
    }

    /**
     * for comparator use only
     */
    private Instant getDueDate() {
        return dueDate == null ? Instant.MAX : dueDate;
    }

    /**
     * for comparator use only
     */
    private Severity getSeverity() {
        return severity == null ? Severity.NONE : severity;
    }
}
