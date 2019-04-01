/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.database;

final class DatabaseConst {
    private DatabaseConst() {}

    static final String ITK_ISSUE_HISTORY_PK = "ITK_ISSUE_HISTORY_PK";
    static final String ITK_ISSUE_HISTORY_FK_TO_ISSUE = "ITK_ISSUE_HIST_FK_TO_ISSUE";
    static final String ITK_ISSUE_HISTORY_FK_TO_TASKOCCURRENCE = "ITK_ISSUE_HIST_FK_TO_TASKOCC";


    static final String ITK_ID = "ID";
    static final String ITK_BASE_ISSUE = "ISSUE";
    static final String ITK_TASKOCCURRENCE= "TASK_OCCURRENCE";
    static final String ITK_FAILURE_TIME= "FAIL_TIME";
    static final String ITK_ERROR_MESSAGE= "ERROR_MESSAGE";


    static final String ITK_ISSUE_PK = "ITK_ISSUE_PK";
    static final String ITK_ISSUE_FK_TO_ISSUE = "ITK_ISSUE_FK_TO_ISSUE";
    static final String ITK_ISSUE_FK_TO_TASKOCCURRENCE = "ITK_ISSUE_FK_TO_TASKOCC";

    static final String ITK_ISSUE_OPEN_PK = "ITK_ISSUE_OPEN_PK";
    static final String ITK_ISSUE_OPEN_FK_TO_ISSUE = "ITK_ISSUE_OPEN_FK_TO_ISSUE";
    static final String ITK_ISSUE_OPEN_FK_TO_TASKOCCURRENCE = "ITK_ISSUE_OPEN_FK_TO_TASKOCC";

}