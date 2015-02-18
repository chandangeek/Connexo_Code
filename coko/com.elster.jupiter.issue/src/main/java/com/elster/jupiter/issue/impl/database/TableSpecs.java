package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.issue.impl.records.ActionParameterImpl;
import com.elster.jupiter.issue.impl.records.AssignmentRuleImpl;
import com.elster.jupiter.issue.impl.records.CreationRuleActionImpl;
import com.elster.jupiter.issue.impl.records.CreationRuleImpl;
import com.elster.jupiter.issue.impl.records.CreationRuleParameterImpl;
import com.elster.jupiter.issue.impl.records.HistoricalIssueImpl;
import com.elster.jupiter.issue.impl.records.IssueActionTypeImpl;
import com.elster.jupiter.issue.impl.records.IssueCommentImpl;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.impl.records.IssueReasonImpl;
import com.elster.jupiter.issue.impl.records.IssueStatusImpl;
import com.elster.jupiter.issue.impl.records.IssueTypeImpl;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.entity.ActionParameter;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleParameter;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.UserService;

import java.util.Arrays;
import java.util.ListIterator;

import static com.elster.jupiter.issue.impl.database.DatabaseConst.ASSIGNEE_RULE_JOURNAL_TABLE_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ASSIGNMENT_RULES_DESCRIPTION;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ASSIGNMENT_RULES_ENABLED;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ASSIGNMENT_RULES_PK;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ASSIGNMENT_RULES_PRIORITY;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ASSIGNMENT_RULES_RULE_DATA;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ASSIGNMENT_RULES_TITLE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_PARAMETER_FK_TO_RULE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_PARAMETER_KEY;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_PARAMETER_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_PARAMETER_RULE_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_PARAMETER_VALUE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_COMMENT;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_CONTENT;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_DUE_IN_TYPE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_DUE_IN_VALUE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_FK_TO_REASON;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_JOURNAL_TABLE_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_OBSOLETE_TIME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_UQ_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_REASON_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.CREATION_RULE_TEMPLATE_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COLUMN_ASSIGNEE_TYPE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COLUMN_DEVICE_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COLUMN_DUE_DATE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COLUMN_OVERDUE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COLUMN_REASON_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COLUMN_RULE_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COLUMN_STATUS_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COLUMN_USER_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COMMENT_COMMENT;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COMMENT_FK_TO_USER;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COMMENT_ISSUE_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COMMENT_JOURNAL_TABLE_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COMMENT_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_COMMENT_USER_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_FK_TO_DEVICE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_FK_TO_REASON;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_FK_TO_RULE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_FK_TO_STATUS;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_FK_TO_USER;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_HIST_COLUMN_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_HIST_FK_TO_DEVICE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_HIST_FK_TO_REASON;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_HIST_FK_TO_RULE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_HIST_FK_TO_STATUS;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_HIST_FK_TO_USER;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_HIST_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_REASON_COLUMN_DEFAULT_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_REASON_COLUMN_KEY;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_REASON_COLUMN_TRANSLATION;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_REASON_COLUMN_TYPE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_REASON_FK_TO_ISSUE_TYPE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_REASON_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_STATUS_COLUMN_DEFAULT_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_STATUS_COLUMN_IS_HISTORICAL;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_STATUS_COLUMN_KEY;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_STATUS_COLUMN_TRANSLATION;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_STATUS_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_TYPE_COLUMN_DEFAULT_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_TYPE_COLUMN_KEY;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_TYPE_COLUMN_TRANSLATION;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.ISSUE_TYPE_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.METERING_DEVICE_TABLE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.OPEN_ISSUE_FK_TO_DEVICE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.OPEN_ISSUE_FK_TO_REASON;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.OPEN_ISSUE_FK_TO_RULE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.OPEN_ISSUE_FK_TO_STATUS;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.OPEN_ISSUE_FK_TO_USER;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.OPEN_ISSUE_JOURNAL_TABLE_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.OPEN_ISSUE_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_FK_TO_ACTION_TYPE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_FK_TO_RULE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_PARAM_FK_TO_ACTION_RULE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_PARAM_KEY;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_PARAM_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_PARAM_RULE_ACTION_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_PARAM_VALUE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_PHASE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_RULE_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_TYPE_CLASS_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_TYPE_FACTORY_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_TYPE_FK_TO_ISSUE_TYPE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_TYPE_FK_TO_REASON;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_TYPE_ID;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_TYPE_ISSUE_TYPE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_TYPE_PHASE;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_TYPE_PK_NAME;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.RULE_ACTION_TYPE_REASON;
import static com.elster.jupiter.issue.impl.database.DatabaseConst.USER_TABLE;
import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.CLOB2STRING;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;

public enum TableSpecs {

    ISU_TYPE{
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueType> table = dataModel.addTable(name(), IssueType.class);
            table.map(IssueTypeImpl.class);

            Column key = table.column(ISSUE_TYPE_COLUMN_KEY).map("key").varChar(NAME_LENGTH).notNull().add();
            table.column(ISSUE_TYPE_COLUMN_DEFAULT_NAME).map("defaultName").varChar(200).notNull().add();
            table.column(ISSUE_TYPE_COLUMN_TRANSLATION).map("translationKey").varChar(NAME_LENGTH).notNull().add();
            table.addAuditColumns();

            table.primaryKey(ISSUE_TYPE_PK_NAME).on(key).add();
        }
    },

    ISU_REASON {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueReason> table = dataModel.addTable(name(), IssueReason.class);
            table.map(IssueReasonImpl.class);

            Column key = table.column(ISSUE_REASON_COLUMN_KEY).map("key").varChar(NAME_LENGTH).notNull().add();
            table.column(ISSUE_REASON_COLUMN_TRANSLATION).map("translationKey").varChar(NAME_LENGTH).notNull().add();
            table.column(ISSUE_REASON_COLUMN_DEFAULT_NAME).map("defaultName").varChar(200).notNull().add();
            Column typeRefIdColumn = table.column(ISSUE_REASON_COLUMN_TYPE).varChar(NAME_LENGTH).notNull().add();
            table.addAuditColumns();

            table.primaryKey(ISSUE_REASON_PK_NAME).on(key).add();
            table.foreignKey(ISSUE_REASON_FK_TO_ISSUE_TYPE).map("issueType").on(typeRefIdColumn).references(ISU_TYPE.name()).add();
        }
    },

    ISU_STATUS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueStatus> table = dataModel.addTable(name(), IssueStatus.class);
            table.map(IssueStatusImpl.class);
            table.cache();

            Column key = table.column(ISSUE_STATUS_COLUMN_KEY).map("key").varChar(NAME_LENGTH).notNull().add();
            table.column(ISSUE_STATUS_COLUMN_TRANSLATION).map("translationKey").varChar(NAME_LENGTH).notNull().add();
            table.column(ISSUE_STATUS_COLUMN_DEFAULT_NAME).map("defaultName").varChar(200).notNull().add();
            table.column(ISSUE_STATUS_COLUMN_IS_HISTORICAL).map("isHistorical").type("char(1)").conversion(CHAR2BOOLEAN).notNull().add();
            table.addAuditColumns();

            table.primaryKey(ISSUE_STATUS_PK_NAME).on(key).add();
        }
    },

    ISU_COMMENT {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueComment> table = dataModel.addTable(name(), IssueComment.class);
            table.map(IssueCommentImpl.class);
            table.setJournalTableName(ISSUE_COMMENT_JOURNAL_TABLE_NAME);
            Column idColumn = table.addAutoIdColumn();

            table.column(ISSUE_COMMENT_COMMENT).type("CLOB").map("comment").conversion(ColumnConversion.CLOB2STRING).add();
            table.column(ISSUE_COMMENT_ISSUE_ID).map("issueId").type("number").conversion(NUMBER2LONG).notNull().add();
            Column userRefIdColumn = table.column(ISSUE_COMMENT_USER_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            table.primaryKey(ISSUE_COMMENT_PK_NAME).on(idColumn).add();
            table.addAuditColumns();

            table.foreignKey(ISSUE_COMMENT_FK_TO_USER).map("user").on(userRefIdColumn).references(UserService.COMPONENTNAME, USER_TABLE).add();
        }
    },
    ISU_CREATIONRULES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<CreationRule> table = dataModel.addTable(name(), CreationRule.class);
            table.map(CreationRuleImpl.class);
            table.setJournalTableName(CREATION_RULE_JOURNAL_TABLE_NAME);

            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column(CREATION_RULE_NAME).map("name").varChar(NAME_LENGTH).notNull().add();
            table.column(CREATION_RULE_COMMENT).map("comment").type("clob").conversion(CLOB2STRING).add();
            table.column(CREATION_RULE_CONTENT).map("content").type("clob").conversion(CLOB2STRING).notNull().add();
            Column reasonRefIdColumn = table.column(CREATION_RULE_REASON_ID).varChar(NAME_LENGTH).notNull().add();
            table.column(CREATION_RULE_DUE_IN_VALUE).map("dueInValue").type("number").conversion(NUMBER2LONG).add();
            table.column(CREATION_RULE_DUE_IN_TYPE).map("dueInType").type("number").conversion(NUMBER2ENUM).add();
            table.column(CREATION_RULE_TEMPLATE_NAME).map("templateUuid").varChar(128).notNull().add();
            table.column(CREATION_RULE_OBSOLETE_TIME).map("obsoleteTime").type("number").conversion(NUMBER2INSTANT).add();
            table.addAuditColumns();

            table.primaryKey(CREATION_RULE_PK_NAME).on(idColumn).add();
            table.unique(CREATION_RULE_UQ_NAME).on(nameColumn).add();
            table.foreignKey(CREATION_RULE_FK_TO_REASON).map("reason").on(reasonRefIdColumn).references(ISU_REASON.name()).add();
        }
    },
    ISU_RULEPARAM {
        @Override
        public void addTo(DataModel dataModel) {
            Table<CreationRuleParameter> table = dataModel.addTable(name(), CreationRuleParameter.class);
            table.map(CreationRuleParameterImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column(CREATION_PARAMETER_KEY).map("key").varChar(SHORT_DESCRIPTION_LENGTH).notNull().add();
            table.column(CREATION_PARAMETER_VALUE).map("value").varChar(1024).notNull().add();
            Column ruleRefIdColumn = table.column(CREATION_PARAMETER_RULE_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            table.addAuditColumns();

            table.primaryKey(CREATION_PARAMETER_PK_NAME).on(idColumn).add();
            table.foreignKey(CREATION_PARAMETER_FK_TO_RULE).on(ruleRefIdColumn).references(ISU_CREATIONRULES.name())
                    .map("rule").reverseMap("parameters").reverseMapOrder("key").composition().onDelete(DeleteRule.CASCADE).add();
        }
    },
    ISU_ISSUE_HISTORY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalIssue> table = dataModel.addTable(name(), HistoricalIssue.class);
            table.map(HistoricalIssueImpl.class);
            Column idColumn = table.column(ISSUE_HIST_COLUMN_ID).map("id").type("number").conversion(NUMBER2LONG).notNull().add();

            TableBuilder.buildIssueTable(table, idColumn, ISSUE_HIST_PK_NAME,
                    // Foreign keys
                    ISSUE_HIST_FK_TO_REASON,
                    ISSUE_HIST_FK_TO_STATUS,
                    ISSUE_HIST_FK_TO_DEVICE,
                    ISSUE_HIST_FK_TO_USER,
                    ISSUE_HIST_FK_TO_RULE);
            table.addAuditColumns();
        }
    },
    ISU_ISSUE_OPEN {
        @Override
        public void addTo(DataModel dataModel) {
            Table<OpenIssue> table = dataModel.addTable(name(), OpenIssue.class);
            table.map(OpenIssueImpl.class);
            table.setJournalTableName(OPEN_ISSUE_JOURNAL_TABLE_NAME);
            Column idColumn = table.addAutoIdColumn();

            TableBuilder.buildIssueTable(table, idColumn, OPEN_ISSUE_PK_NAME,
                    // Foreign keys
                    OPEN_ISSUE_FK_TO_REASON,
                    OPEN_ISSUE_FK_TO_STATUS,
                    OPEN_ISSUE_FK_TO_DEVICE,
                    OPEN_ISSUE_FK_TO_USER,
                    OPEN_ISSUE_FK_TO_RULE);
            table.addAuditColumns();
        }
    },
    ISU_ISSUE_ALL {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Issue> table = dataModel.addTable(name(), Issue.class);
            table.map(IssueImpl.class);
            table.doNotAutoInstall();
            Column idColumn = table.addAutoIdColumn();
            TableBuilder.buildIssueTable(table, idColumn, ISSUE_PK_NAME,
                    // Foreign keys
                    ISSUE_FK_TO_REASON,
                    ISSUE_FK_TO_STATUS,
                    ISSUE_FK_TO_DEVICE,
                    ISSUE_FK_TO_USER,
                    ISSUE_FK_TO_RULE);
            table.addAuditColumns();
        }
    },
    ISU_ASSIGMENTRULES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<AssignmentRule> table = dataModel.addTable(name(), AssignmentRule.class);
            table.map(AssignmentRuleImpl.class);
            table.setJournalTableName(ASSIGNEE_RULE_JOURNAL_TABLE_NAME);

            Column idColumn = table.addAutoIdColumn();
            table.column(ASSIGNMENT_RULES_PRIORITY).map("priority").type("number").conversion(NUMBER2INT).add();
            table.column(ASSIGNMENT_RULES_DESCRIPTION).map("description").varChar(400).add();
            table.column(ASSIGNMENT_RULES_TITLE).map("title").varChar(400).notNull().add();
            table.column(ASSIGNMENT_RULES_ENABLED).map("enabled").type("number").conversion(NUMBER2BOOLEAN).add();
            table.column(ASSIGNMENT_RULES_RULE_DATA).map("ruleData").type("clob").conversion(CLOB2STRING).notNull().add();
            table.addAuditColumns();

            table.primaryKey(ASSIGNMENT_RULES_PK).on(idColumn).add();
        }
    },
    ISU_ACTIONTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueActionType> table = dataModel.addTable(name(), IssueActionType.class);
            table.map(IssueActionTypeImpl.class);

            Column idColumn = table.addAutoIdColumn();
            Column typeRefIdColumn = table.column(RULE_ACTION_TYPE_ISSUE_TYPE).varChar(NAME_LENGTH).add();
            Column reasonRefIdColumn = table.column(RULE_ACTION_TYPE_REASON).varChar(NAME_LENGTH).add();
            table.column(RULE_ACTION_TYPE_PHASE).number().conversion(ColumnConversion.NUMBER2ENUM).map("phase").add();
            table.column(RULE_ACTION_TYPE_CLASS_NAME).map("className").varChar(1024).notNull().add();
            table.column(RULE_ACTION_TYPE_FACTORY_ID).map("factoryId").varChar(NAME_LENGTH).notNull().add();
            table.addAuditColumns();

            table.primaryKey(RULE_ACTION_TYPE_PK_NAME).on(idColumn).add();
            table.foreignKey(RULE_ACTION_TYPE_FK_TO_ISSUE_TYPE).map("issueType").on(typeRefIdColumn).references(ISU_TYPE.name()).add();
            table.foreignKey(RULE_ACTION_TYPE_FK_TO_REASON).map("issueReason").on(reasonRefIdColumn).references(ISU_REASON.name()).add();
        }
    },
    ISU_RULEACTION {
        @Override
        public void addTo(DataModel dataModel) {
            Table<CreationRuleAction> table = dataModel.addTable(name(), CreationRuleAction.class);
            table.map(CreationRuleActionImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column(RULE_ACTION_PHASE).map("phase").type("number").conversion(NUMBER2ENUM).notNull().add();
            Column ruleRefIdColumn = table.column(RULE_ACTION_RULE_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            Column typeRefIdColumn = table.column(RULE_ACTION_TYPE_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            table.addAuditColumns();

            table.primaryKey(RULE_ACTION_PK_NAME).on(idColumn).add();
            table.foreignKey(RULE_ACTION_FK_TO_ACTION_TYPE).map("type").on(typeRefIdColumn).references(ISU_ACTIONTYPE.name()).add();
            table.foreignKey(RULE_ACTION_FK_TO_RULE).on(ruleRefIdColumn).references(ISU_CREATIONRULES.name())
                    .map("rule").reverseMap("actions").composition().onDelete(DeleteRule.CASCADE).add();
        }
    },
    ISU_RULEACTIONPARAM {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ActionParameter> table = dataModel.addTable(name(), ActionParameter.class);
            table.map(ActionParameterImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column(RULE_ACTION_PARAM_KEY).map("key").varChar(SHORT_DESCRIPTION_LENGTH).notNull().add();
            table.column(RULE_ACTION_PARAM_VALUE).map("value").varChar(1024).notNull().add();
            Column actionRefIdColumn = table.column(RULE_ACTION_PARAM_RULE_ACTION_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            table.addAuditColumns();

            table.primaryKey(RULE_ACTION_PARAM_PK_NAME).on(idColumn).add();
            table.foreignKey(RULE_ACTION_PARAM_FK_TO_ACTION_RULE).on(actionRefIdColumn).references(ISU_RULEACTION.name())
                    .map("action").reverseMap("parameters").reverseMapOrder("key").composition().onDelete(DeleteRule.CASCADE).add();
        }
    }
    ;

	public abstract void addTo(DataModel dataModel);

    private static class TableBuilder {
        private static final int EXPECTED_FK_KEYS_LENGTH = 5;

        static void buildIssueTable(Table table, Column idColumn, String pkKey, String... fkKeys){
            table.column(ISSUE_COLUMN_DUE_DATE).map("dueDate").type("number").conversion(NUMBER2INSTANT).add();
            Column reasonRefIdColumn = table.column(ISSUE_COLUMN_REASON_ID).varChar(NAME_LENGTH).notNull().add();
            Column statusRefIdColumn = table.column(ISSUE_COLUMN_STATUS_ID).varChar(NAME_LENGTH).notNull().add();
            Column deviceRefIdColumn = table.column(ISSUE_COLUMN_DEVICE_ID).type("number").conversion(NUMBER2LONG).add();
            table.column(ISSUE_COLUMN_ASSIGNEE_TYPE).map("assigneeType").type("number").conversion(NUMBER2ENUM).add();
            Column userRefIdColumn = table.column(ISSUE_COLUMN_USER_ID).type("number").conversion(NUMBER2LONG).add();
            table.column(ISSUE_COLUMN_OVERDUE).map("overdue").type("number").conversion(NUMBER2BOOLEAN).notNull().add();
            Column ruleRefIdColumn = table.column(ISSUE_COLUMN_RULE_ID).type("number").conversion(NUMBER2LONG).notNull().add();


            table.primaryKey(pkKey).on(idColumn).add();
            if (fkKeys == null || fkKeys.length != EXPECTED_FK_KEYS_LENGTH){
                throw new IllegalArgumentException("Passed arguments don't match foreigen keys");
            }
            ListIterator<String> fkKeysIter = Arrays.asList(fkKeys).listIterator();
            table.foreignKey(fkKeysIter.next()).map("reason").on(reasonRefIdColumn).references(ISU_REASON.name()).add();
            table.foreignKey(fkKeysIter.next()).map("status").on(statusRefIdColumn).references(ISU_STATUS.name()).add();
            table.foreignKey(fkKeysIter.next()).map("device").on(deviceRefIdColumn).references(MeteringService.COMPONENTNAME, METERING_DEVICE_TABLE).add();
            table.foreignKey(fkKeysIter.next()).map("user").on(userRefIdColumn).references(UserService.COMPONENTNAME, USER_TABLE).add();
            table.foreignKey(fkKeysIter.next()).map("rule").on(ruleRefIdColumn).references(ISU_CREATIONRULES.name()).add();
        }
    }
}