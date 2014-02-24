package com.elster.jupiter.issue.database;

public class DatabaseConst {

// Bundle tables in database
    public static final String ISSUE_JOURNAL_TABLE_NAME = "ISU_ISSUEJRNL";
    public static final String ISSUE_REASON_JOURNAL_TABLE_NAME = "ISU_REASONJRNL";
    public static final String ISSUE_STATUS_JOURNAL_TABLE_NAME = "ISU_STATUSJRNL";
    public static final String METERING_DEVICE_TABLE = "MTR_ENDDEVICE";
    public static final String USER_DEVICE_TABLE = "USR_USER";

// Issue table
    public static final String ISSUE_COLUMN_DISCRIMINATOR = "ISSUE_TYPE";
    public static final String ISSUE_COLUMN_DUE_DATE = "DUE_DATE";
    public static final String ISSUE_COLUMN_REASON_ID = "REASON_ID";
    public static final String ISSUE_COLUMN_STATUS_ID = "STATUS";
    public static final String ISSUE_COLUMN_DEVICE_ID = "DEVICE_ID";
    public static final String ISSUE_COLUMN_ASSIGNEE_TYPE = "ASSIGNEE_TYPE";
    public static final String ISSUE_COLUMN_USER_ID = "ASSIGNEE_USER_ID";
    public static final String ISSUE_COLUMN_TEAM_ID = "ASSIGNEE_TEAM_ID";
    public static final String ISSUE_COLUMN_ROLE_ID = "ASSIGNEE_ROLE_ID";

    public static final String ISSUE_PK_NAME = "ISU_PK_ISSUE";
    public static final String ISSUE_FK_TO_DEVICE = "ISU_FK_TO_DEVICE";
    public static final String ISSUE_FK_TO_REASON = "ISU_FK_TO_REASON";
    public static final String ISSUE_FK_TO_STATUS = "ISU_FK_TO_STATUS";
    public static final String ISSUE_FK_TO_USER = "ISU_FK_TO_USER";
    public static final String ISSUE_FK_TO_TEAM = "ISU_FK_TO_TEAM";
    public static final String ISSUE_FK_TO_ROLE = "ISU_FK_TO_ROLE";

// Issue Reason table
    public static final String ISSUE_REASON_COLUMN_NAME = "NAME";
    public static final String ISSUE_REASON_COLUMN_TOPIC = "REASON_TOPIC";

    public static final String ISSUE_REASON_PK_NAME = "ISU_PK_ISSUE_REASON";

// Issue Status table
    public static final String ISSUE_STATUS_COLUMN_NAME = "STATUS_NAME";

    public static final String ISSUE_STATUS_PK_NAME = "ISU_PK_ISSUE_STATUS";

// Issue Assignee tables
    public static final String ISSUE_ASSIGNEE_NAME = "NAME";
    public static final String ISSUE_ASSIGNEE_DESCRIPTION = "DESCRIPTION";

    public static final String ISSUE_ASSIGNEE_TEAM_PK_NAME = "ISU_PK_ISSUE_ASSIGNEE_TEAM";
    public static final String ISSUE_ASSIGNEE_ROLE_PK_NAME = "ISU_PK_ISSUE_ASSIGNEE_ROLE";

// Issue Historical Table
    public static final String ISSUE_HIST_COLUMN_DEVICE_ID = "DEVICE_ID";
    public static final String ISSUE_HIST_COLUMN_ASSIGNEE_TYPE = "ASSIGNEE_TYPE";
    public static final String ISSUE_HIST_COLUMN_ASSIGNEE_ID = "ASSIGNEE_ID";
    public static final String ISSUE_HIST_COLUMN_REASON_ID = "REASON_ID";
    public static final String ISSUE_HIST_COLUMN_CREATE_TIME = "CREATE_TIME";

    public static final String ISSUE_HIST_PK_NAME = "ISU_PK_HIST_ISSUE";
}
