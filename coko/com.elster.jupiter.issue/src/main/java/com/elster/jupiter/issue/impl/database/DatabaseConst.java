package com.elster.jupiter.issue.impl.database;

public final class DatabaseConst {

    private DatabaseConst(){} //Hide Utility Class Constructor

// Bundle tables in database
    public static final String OPEN_ISSUE_JOURNAL_TABLE_NAME = "ISU_ISSUEJRNL";
    public static final String ISSUE_REASON_JOURNAL_TABLE_NAME = "ISU_REASONJRNL";
    public static final String ISSUE_COMMENT_JOURNAL_TABLE_NAME = "ISU_COMMENTJRNL";
    public static final String METERING_DEVICE_TABLE = "MTR_ENDDEVICE";
    public static final String USER_TABLE = "USR_USER";
    public static final String ASSIGNEE_RULE_JOURNAL_TABLE_NAME = "ISU_ASSIGMENTRULEJRNL";
    public static final String CREATION_RULE_JOURNAL_TABLE_NAME = "ISU_CREATIONRULEJRNL";

// Issue table
    public static final String ISSUE_COLUMN_DUE_DATE = "DUE_DATE";
    public static final String ISSUE_COLUMN_REASON_ID = "REASON_ID";
    public static final String ISSUE_COLUMN_STATUS_ID = "STATUS";
    public static final String ISSUE_COLUMN_DEVICE_ID = "DEVICE_ID";
    public static final String ISSUE_COLUMN_ASSIGNEE_TYPE = "ASSIGNEE_TYPE";
    public static final String ISSUE_COLUMN_USER_ID = "ASSIGNEE_USER_ID";
    public static final String ISSUE_COLUMN_RULE_ID = "RULE_ID";
    public static final String ISSUE_COLUMN_OVERDUE = "OVERDUE";

    public static final String OPEN_ISSUE_PK_NAME = "ISU_OPEN_PK_ISSUE";
    public static final String OPEN_ISSUE_FK_TO_DEVICE = "ISU_OPEN_FK_TO_DEVICE";
    public static final String OPEN_ISSUE_FK_TO_REASON = "ISU_OPEN_FK_TO_REASON";
    public static final String OPEN_ISSUE_FK_TO_STATUS = "ISU_OPEN_FK_TO_STATUS";
    public static final String OPEN_ISSUE_FK_TO_USER = "ISU_OPEN_FK_TO_USER";
    public static final String OPEN_ISSUE_FK_TO_RULE = "ISU_OPEN_FK_TO_RULE";

    public static final String ISSUE_PK_NAME = "ISU_PK_ISSUE";
    public static final String ISSUE_FK_TO_DEVICE = "ISU_FK_TO_DEVICE";
    public static final String ISSUE_FK_TO_REASON = "ISU_FK_TO_REASON";
    public static final String ISSUE_FK_TO_STATUS = "ISU_FK_TO_STATUS";
    public static final String ISSUE_FK_TO_USER = "ISU_FK_TO_USER";
    public static final String ISSUE_FK_TO_RULE = "ISU_FK_TO_RULE";

// Issue Reason table
    public static final String ISSUE_REASON_COLUMN_KEY = "KEY";
    public static final String ISSUE_REASON_COLUMN_TRANSLATION = "TRANSLATION";
    public static final String ISSUE_REASON_COLUMN_DESCRIPTION = "DESCRIPTION_KEY";
    public static final String ISSUE_REASON_COLUMN_TYPE = "ISSUE_TYPE";

    public static final String ISSUE_REASON_PK_NAME = "ISU_PK_ISSUE_REASON";
    public static final String ISSUE_REASON_FK_TO_ISSUE_TYPE = "ISU_REASON_FK_TO_ISSUE_TYPE";

// Issue Status table
    public static final String ISSUE_STATUS_COLUMN_KEY = "KEY";
    public static final String ISSUE_STATUS_COLUMN_TRANSLATION = "TRANSLATION";
    public static final String ISSUE_STATUS_COLUMN_DEFAULT_NAME = "DEFAULT_NAME";
    public static final String ISSUE_STATUS_COLUMN_IS_HISTORICAL = "STATUS_IS_HISTORICAL";

    public static final String ISSUE_STATUS_PK_NAME = "ISU_PK_ISSUE_STATUS";

// Issue Type
    public static final String ISSUE_TYPE_COLUMN_KEY = "KEY";
    public static final String ISSUE_TYPE_COLUMN_DEFAULT_NAME = "DEFAULT_NAME";
    public static final String ISSUE_TYPE_COLUMN_TRANSLATION = "TRANSLATION";

    public static final String ISSUE_TYPE_PK_NAME = "ISU_PK_ISSUE_TYPE";

// Issue Historical Table
    public static final String ISSUE_HIST_COLUMN_ID = "ISU_HIST_ISSUE_ID";
    public static final String ISSUE_HIST_PK_NAME = "ISU_PK_HIST_ISSUE";

    public static final String ISSUE_HIST_FK_TO_DEVICE = "ISU_HIST_FK_TO_DEVICE";
    public static final String ISSUE_HIST_FK_TO_REASON = "ISU_HIST_FK_TO_REASON";
    public static final String ISSUE_HIST_FK_TO_STATUS = "ISU_HIST_FK_TO_STATUS";
    public static final String ISSUE_HIST_FK_TO_USER = "ISU_HIST_FK_TO_USER";
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
    public static final String CREATION_RULE_REASON_ID = "REASON";
    public static final String CREATION_RULE_DUE_IN_VALUE = "DUE_IN_VALUE";
    public static final String CREATION_RULE_DUE_IN_TYPE = "DUE_IN_TYPE";
    public static final String CREATION_RULE_TEMPLATE_NAME = "TEMPLATE";
    public static final String CREATION_RULE_OBSOLETE_TIME = "OBSOLETE_TIME";

    public static final String CREATION_RULE_PK_NAME = "ISU_PK_CREATION_RULE";
    public static final String CREATION_RULE_UQ_NAME = "ISU_UQ_RULE_NAME";
    public static final String CREATION_RULE_FK_TO_REASON = "CREATION_RULE_FK_TO_REASON";

// Issue Creation Rules Parameters
    public static final String CREATION_RULE_PROPS_NAME = "NAME";
    public static final String CREATION_RULE_PROPS_VALUE = "VALUE";
    public static final String CREATION_RULE_PROPS_RULE = "CREATIONRULE";

    public static final String CREATION_RULE_PROPS_PK_NAME = "ISU_PK_CREATION_RULE_PROPS";
    public static final String CREATION_RULE_PROPS_FK_TO_RULE = "CREATION_RULE_PROPS_FK_TO_RULE";

// Issue Action Types
    public static final String RULE_ACTION_TYPE_CLASS_NAME = "CLASS_NAME";
    public static final String RULE_ACTION_TYPE_FACTORY_ID = "FACTORY_ID";
    public static final String RULE_ACTION_TYPE_ISSUE_TYPE = "ISSUE_TYPE";
    public static final String RULE_ACTION_TYPE_REASON = "REASON";
    public static final String RULE_ACTION_TYPE_PHASE = "PHASE";

    public static final String RULE_ACTION_TYPE_PK_NAME = "ISU_PK_ACT_TYPE";
    public static final String RULE_ACTION_TYPE_FK_TO_ISSUE_TYPE = "ISU_ACT_TYPE_FK_TO_ISSUE_TYPE";
    public static final String RULE_ACTION_TYPE_FK_TO_REASON = "ISU_ACT_TYPE_FK_TO_REASON";
    public static final String RULE_ACTION_TYPE_UNIQUE_CONSTRAINT = "ISU_ACT_TYPE_UNIQ_CONSTR";

// Issue Creation Rule Action
    public static final String RULE_ACTION_PHASE = "PHASE";
    public static final String RULE_ACTION_RULE = "RULE";
    public static final String RULE_ACTION_TYPE = "ACTIONTYPE";

    public static final String RULE_ACTION_PK_NAME = "ISU_PK_RULE_ACT";
    public static final String RULE_ACTION_FK_TO_RULE = "RULE_ACT_FK_TO_RULE";
    public static final String RULE_ACTION_FK_TO_ACTION_TYPE = "RULE_ACT_FK_TO_ACT_TYPE";


// Issue Creation Rule Action Properties
    public static final String RULE_ACTION_PROPS_NAME = "NAME";
    public static final String RULE_ACTION_PROPS_VALUE = "VALUE";
    public static final String RULE_ACTION_PROPS_RULEACTION = "CREATIONRULEACTION";

    public static final String RULE_ACTION_PROPS_PK_NAME = "ISU_PK_RULE_ACTION_PROPS";
    public static final String RULE_ACTION_PROPS_FK_TO_ACTION_RULE = "RULE_ACT_PROPS_FK_TO_ACT_RULE";
}


