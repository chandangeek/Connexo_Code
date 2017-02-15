/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.resource;

public class IssueRestModuleConst {

    private IssueRestModuleConst() {
    }

    public static final long ISSUE_UNASSIGNED_ID = -1L;
    public static final String ISSUE_UNEXISTING_TYPE = "UnexistingType";

    // Filter fields
    public static final String ID = "id";
    public static final String REASON = "reason";
    public static final String STATUS = "status";
    public static final String ISSUE_TYPE = "issueType";
    public static final String ASSIGNEE = "userAssignee";
    public static final String WORKGROUP = "workGroupAssignee";
    public static final String METER = "meter";
    public static final String DUE_DATE = "dueDate";
    public static final String DEVICE_GROUP = "deviceGroup";
    public static final String FIELD = "field";
}
