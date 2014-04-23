package com.elster.jupiter.issue.impl.database;

public final class DatabaseConst {

    private DatabaseConst(){} //Hide Utility Class Constructor

// Bundle tables in database
    public static final String ISSUE_JOURNAL_TABLE_NAME = "ISU_ISSUEJRNL";
    public static final String ALL_ISSUES_VIEW_NAME = "ISU_ALL_ISSUES_VIEW";
    public static final String ISSUE_REASON_JOURNAL_TABLE_NAME = "ISU_REASONJRNL";
    public static final String ISSUE_STATUS_JOURNAL_TABLE_NAME = "ISU_STATUSJRNL";
    public static final String ISSUE_COMMENT_JOURNAL_TABLE_NAME = "ISU_COMMENTJRNL";
    public static final String METERING_DEVICE_TABLE = "MTR_ENDDEVICE";
    public static final String USER_TABLE = "USR_USER";
    public static final String ASSIGNEE_RULE_JOURNAL_TABLE_NAME = "ISU_ASSIGMENTRULESJRNL";
    public static final String CREATION_RULE_JOURNAL_TABLE_NAME = "ISU_CREATIONRULESJRNL";

// Issue table
    public static final String ISSUE_COLUMN_DUE_DATE = "DUE_DATE";
    public static final String ISSUE_COLUMN_REASON_ID = "REASON_ID";
    public static final String ISSUE_COLUMN_STATUS_ID = "STATUS";
    public static final String ISSUE_COLUMN_DEVICE_ID = "DEVICE_ID";
    public static final String ISSUE_COLUMN_ASSIGNEE_TYPE = "ASSIGNEE_TYPE";
    public static final String ISSUE_COLUMN_USER_ID = "ASSIGNEE_USER_ID";
    public static final String ISSUE_COLUMN_TEAM_ID = "ASSIGNEE_TEAM_ID";
    public static final String ISSUE_COLUMN_ROLE_ID = "ASSIGNEE_ROLE_ID";
    public static final String ISSUE_COLUMN_RULE_ID = "RULE_ID";
    public static final String ISSUE_COLUMN_OVERDUE = "OVERDUE";

    public static final String ISSUE_PK_NAME = "ISU_PK_ISSUE";
    public static final String ISSUE_FK_TO_DEVICE = "ISU_FK_TO_DEVICE";
    public static final String ISSUE_FK_TO_REASON = "ISU_FK_TO_REASON";
    public static final String ISSUE_FK_TO_STATUS = "ISU_FK_TO_STATUS";
    public static final String ISSUE_FK_TO_USER = "ISU_FK_TO_USER";
    public static final String ISSUE_FK_TO_TEAM = "ISU_FK_TO_TEAM";
    public static final String ISSUE_FK_TO_ROLE = "ISU_FK_TO_ROLE";
    public static final String ISSUE_FK_TO_RULE = "ISU_FK_TO_RULE";

// Issue Reason table
    public static final String ISSUE_REASON_COLUMN_NAME = "REASON_NAME";
    public static final String ISSUE_REASON_COLUMN_TYPE = "ISSUE_TYPE";

    public static final String ISSUE_REASON_PK_NAME = "ISU_PK_ISSUE_REASON";
    public static final String ISSUE_REASON_FK_TO_ISSUE_TYPE = "ISU_REASON_FK_TO_ISSUE_TYPE";

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

// Issue Type
    public static final String ISSUE_TYPE_COLUMN_NAME = "NAME";
    public static final String ISSUE_TYPE_COLUMN_UUID = "UUID";

    public static final String ISSUE_TYPE_PK_NAME = "ISU_PK_ISSUE_TYPE";

// Issue Historical Table
    public static final String ISSUE_HIST_COLUMN_ID = "ISU_HIST_ISSUE_ID";
    public static final String ISSUE_HIST_PK_NAME = "ISU_PK_HIST_ISSUE";

    public static final String ISSUE_HIST_FK_TO_DEVICE = "ISU_HIST_FK_TO_DEVICE";
    public static final String ISSUE_HIST_FK_TO_REASON = "ISU_HIST_FK_TO_REASON";
    public static final String ISSUE_HIST_FK_TO_STATUS = "ISU_HIST_FK_TO_STATUS";
    public static final String ISSUE_HIST_FK_TO_USER = "ISU_HIST_FK_TO_USER";
    public static final String ISSUE_HIST_FK_TO_TEAM = "ISU_HIST_FK_TO_TEAM";
    public static final String ISSUE_HIST_FK_TO_ROLE = "ISU_HIST_FK_TO_ROLE";
    public static final String ISSUE_HIST_FK_TO_RULE = "ISU_HIST_FK_TO_RULE";

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
    public static final String ALL_ISSUES_FK_TO_RULE = "ISU_ALL_FK_TO_RULE";

// Issue Creation Rules
    public static final String CREATION_RULE_NAME = "NAME";
    public static final String CREATION_RULE_COMMENT = "RULE_COMMENT";
    public static final String CREATION_RULE_CONTENT = "CONTENT";
    public static final String CREATION_RULE_REASON_ID = "REASON_ID";
    public static final String CREATION_RULE_DUE_IN_VALUE = "DUE_IN_VALUE";
    public static final String CREATION_RULE_DUE_IN_TYPE = "DUE_IN_TYPE";
    public static final String CREATION_RULE_TEMPLATE_NAME = "TEMPLATE_UUID";
    public static final String CREATION_RULE_OBSOLETE_TIME = "OBSOLETE_TIME";

    public static final String CREATION_RULE_PK_NAME = "ISU_PK_CREATION_RULE";
    public static final String CREATION_RULE_FK_TO_REASON = "CREATION_RULE_FK_TO_REASON";

// Issue Creation Rules Parameters
    public static final String CREATION_PARAMETER_KEY = "KEY";
    public static final String CREATION_PARAMETER_VALUE = "VALUE";
    public static final String CREATION_PARAMETER_RULE_ID = "RULE_ID";

    public static final String CREATION_PARAMETER_PK_NAME = "ISU_PK_CREATION_PARAMETER";
    public static final String CREATION_PARAMETER_FK_TO_RULE = "CREATION_PARAMETER_FK_TO_RULE";

// Issue Action Types
    public static final String RULE_ACTION_TYPE_NAME = "NAME";
    public static final String RULE_ACTION_TYPE_CLASS_NAME = "CLASS_NAME";
    public static final String RULE_ACTION_TYPE_DESCRIPTION = "DESCRIPTION";
    public static final String RULE_ACTION_TYPE_ISSUE_TYPE = "ISSUE_TYPE";

    public static final String RULE_ACTION_TYPE_PK_NAME = "ISU_PK_ACT_TYPE";
    public static final String RULE_ACTION_TYPE_FK_TO_ISSUE_TYPE = "ISU_ACT_TYPE_FK_TO_ISSUE_TYPE";

// Issue Creation Rule Action
    public static final String RULE_ACTION_PHASE = "PHASE";
    public static final String RULE_ACTION_RULE_ID = "RULE_ID";
    public static final String RULE_ACTION_TYPE_ID = "ACT_TYPE_ID";

    public static final String RULE_ACTION_PK_NAME = "ISU_PK_RULE_ACT";
    public static final String RULE_ACTION_FK_TO_RULE = "RULE_ACT_FK_TO_RULE";
    public static final String RULE_ACTION_FK_TO_ACTION_TYPE = "RULE_ACT_FK_TO_ACT_TYPE";


// Issue Creation Rule Action Parameters
    public static final String RULE_ACTION_PARAM_KEY = "KEY";
    public static final String RULE_ACTION_PARAM_VALUE = "VALUE";
    public static final String RULE_ACTION_PARAM_RULE_ACTION_ID = "RULE_ACT_ID";

    public static final String RULE_ACTION_PARAM_PK_NAME = "ISU_PK_RULE_ACT_PARAM";
    public static final String RULE_ACTION_PARAM_FK_TO_ACTION_RULE = "RULE_ACT_PARAM_FK_TO_ACT_RULE";
}


