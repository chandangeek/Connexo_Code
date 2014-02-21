package com.elster.jupiter.issue.database;

public class DatabaseConst {
    // Bundle tables in database
    public static final String ISSUE_JOURNAL_TABLE_NAME = "ISU_ISSUEJRNL";
    public static final String ISSUE_ASSIGNEE_JOURNAL_TABLE_NAME = "ISU_ASSIGNEEJRNL";
    public static final String METERING_DEVICE_TABLE = "MTR_ENDDEVICE";
    public static final String ISSUE_REASON_JOURNAL_TABLE_NAME = "ISU_REASONJRNL";

    // Issue table column's names
    public static final String ISSUE_COLUMN_ID = "ID";
    public static final String ISSUE_COLUMN_DUE_DATE = "DUE_DATE";
    public static final String ISSUE_COLUMN_STATUS = "STATUS";
    public static final String ISSUE_COLUMN_DEVICE_REF = "DEVICE_REF";
    public static final String ISSUE_COLUMN_ASSIGNEE_REF = "ASSIGNEE_REF";
    public static final String ISSUE_COLUMN_REASON_REF = "REASON_REF";

    // Issue keys
    public static final String ISSUE_PK_NAME = "ISU_PK_ISSUE";
    public static final String ISSUE_FK_TO_DEVICE = "ISU_FK_TO_DEVICE";
    public static final String ISSUE_FK_TO_ASSIGNEE = "ISU_FK_TO_ASSIGNEE";
    public static final String ISSUE_FK_TO_REASON = "ISU_FK_TO_REASON";

    // Issue Reason table column's names
    public static final String ISSUE_REASON_COLUMN_TOPIC = "TOPIC";
    public static final String ISSUE_REASON_COLUMN_NAME = "NAME";

    public static final String ISSUE_REASON_PK_NAME = "ISU_PK_ISSUE_REASON";

    // Issue Assignee table column's names
    public static final String ISSUE_ASSIGNEE_COLUMN_TYPE = "TYPE";
    public static final String ISSUE_ASSIGNEE_COLUMN_ASSIGNEE_REF = "ASSIGNEE_REF";

    public static final String ISSUE_ASSIGNEE_PK_NAME = "ISU_PK_ISSUE_ASSIGNEE";

    // Issue Historical Table
    public static final String ISSUE_HIST_PK_NAME = "ISU_PK_HIST_ISSUE";
    public static final String ISSUE_HIST_COLUMN_DEVICE_ID = "DEVICE_ID";
    public static final String ISSUE_HIST_COLUMN_ASSIGNEE_TYPE = "ASSIGNEE_TYPE";
    public static final String ISSUE_HIST_COLUMN_ASSIGNEE_ID = "ASSIGNEE_ID";
    public static final String ISSUE_HIST_COLUMN_REASON_ID = "REASON_ID";
    public static final String ISSUE_HIST_COLUMN_CREATE_TIME = "CREATE_TIME";
}
