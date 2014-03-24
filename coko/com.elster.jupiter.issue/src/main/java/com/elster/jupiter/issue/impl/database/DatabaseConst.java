package com.elster.jupiter.issue.impl.database;

public class DatabaseConst {

// Bundle tables in database
    public static final String ISSUE_JOURNAL_TABLE_NAME = "ISU_ISSUEJRNL";
    public static final String ALL_ISSUES_VIEW_NAME = "ISU_ALL_ISSUES_VIEW";
    public static final String ISSUE_REASON_JOURNAL_TABLE_NAME = "ISU_REASONJRNL";
    public static final String ISSUE_STATUS_JOURNAL_TABLE_NAME = "ISU_STATUSJRNL";
    public static final String ISSUE_COMMENT_JOURNAL_TABLE_NAME = "ISU_COMMENTJRNL";
    public static final String METERING_DEVICE_TABLE = "MTR_ENDDEVICE";
    public static final String USER_TABLE = "USR_USER";
    public static final String ASSIGNEE_RULE_JOURNAL_TABLE_NAME = "ISU_ASSIGMENTRULESJRNL";

// MDC AMR system id
    public static final Long MDC_AMR_SYSTEM_ID = 1L;

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
    public static final String ISSUE_REASON_COLUMN_NAME = "REASON_NAME";
    public static final String ISSUE_REASON_COLUMN_TOPIC = "REASON_TOPIC";

    public static final String ISSUE_REASON_PK_NAME = "ISU_PK_ISSUE_REASON";

// Issue Status table
    public static final String ISSUE_STATUS_COLUMN_ID = "STATUS_ID";
    public static final String ISSUE_STATUS_COLUMN_NAME = "STATUS_NAME";
    public static final String ISSUE_STATUS_COLUMN_IS_FINAL = "STATUS_IS_FINAL";

    public static final String ISSUE_STATUS_PK_NAME = "ISU_PK_ISSUE_STATUS";

// Issue Assignee tables
    public static final String ISSUE_ASSIGNEE_NAME = "ASSIGNEE_NAME";
    public static final String ISSUE_ASSIGNEE_DESCRIPTION = "DESCRIPTION";

    public static final String ISSUE_ASSIGNEE_TEAM_PK_NAME = "ISU_PK_ISSUE_ASSIGNEE_TEAM";
    public static final String ISSUE_ASSIGNEE_ROLE_PK_NAME = "ISU_PK_ISSUE_ASSIGNEE_ROLE";

// Issue Historical Table
    public static final String ISSUE_HIST_COLUMN_ID = "ISU_HIST_ISSUE_ID";
    public static final String ISSUE_HIST_PK_NAME = "ISU_PK_HIST_ISSUE";

    public static final String ISSUE_HIST_FK_TO_DEVICE = "ISU_HIST_FK_TO_DEVICE";
    public static final String ISSUE_HIST_FK_TO_REASON = "ISU_HIST_FK_TO_REASON";
    public static final String ISSUE_HIST_FK_TO_STATUS = "ISU_HIST_FK_TO_STATUS";
    public static final String ISSUE_HIST_FK_TO_USER = "ISU_HIST_FK_TO_USER";
    public static final String ISSUE_HIST_FK_TO_TEAM = "ISU_HIST_FK_TO_TEAM";
    public static final String ISSUE_HIST_FK_TO_ROLE = "ISU_HIST_FK_TO_ROLE";

// Assignment rules table
    public static final String ASSIGNMENT_RULES_PRIORITY = "PRIORITY";
    public static final String ASSIGNMENT_RULES_DESCRIPTION = "DESCRIPTION";
    public static final String ASSIGNMENT_RULES_TITLE = "TITLE";
    public static final String ASSIGNMENT_RULES_ENABLED = "ENABLED";
    public static final String ASSIGNMENT_RULES_RULE_DATA = "RULEDATA";

    public static final String ASSIGNMENT_RULES_PK = "ISU_ASSIGNMENT_RULE_PK";


// Issue Comment table
    public static final String ISSUE_COMMENT_COMMENT = "ISSUE_COMMENT";
    public static final String ISSUE_COMMENT_ISSUE_ID = "ISSUE_ID";
    public static final String ISSUE_COMMENT_USER_ID = "USER_ID";
    public static final String ISSUE_COMMENT_PK_NAME = "ISU_PK_COMMENT";
    public static final String ISSUE_COMMENT_FK_TO_USER = "ISU_COMMENT_FK_TO_USER";

// Issue View
    public static final String ALL_ISSUES_PK_NAME = "ISU_ALL_PK_ISSUE";
    public static final String ALL_ISSUES_FK_TO_DEVICE = "ISU_ALL_FK_TO_DEVICE";
    public static final String ALL_ISSUES_FK_TO_REASON = "ISU_ALL_FK_TO_REASON";
    public static final String ALL_ISSUES_FK_TO_STATUS = "ISU_ALL_FK_TO_STATUS";
    public static final String ALL_ISSUES_FK_TO_USER = "ISU_ALL_FK_TO_USER";
    public static final String ALL_ISSUES_FK_TO_TEAM = "ISU_ALL_FK_TO_TEAM";
    public static final String ALL_ISSUES_FK_TO_ROLE = "ISU_ALL_FK_TO_ROLE";
}


