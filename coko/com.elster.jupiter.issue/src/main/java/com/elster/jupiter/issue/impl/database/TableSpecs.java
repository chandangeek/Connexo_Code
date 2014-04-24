package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.issue.impl.records.AssignmentRuleImpl;
import com.elster.jupiter.issue.impl.records.BaseIssueImpl;
import com.elster.jupiter.issue.impl.records.CreationRuleActionImpl;
import com.elster.jupiter.issue.impl.records.CreationRuleActionParameterImpl;
import com.elster.jupiter.issue.impl.records.CreationRuleActionTypeImpl;
import com.elster.jupiter.issue.impl.records.CreationRuleImpl;
import com.elster.jupiter.issue.impl.records.CreationRuleParameterImpl;
import com.elster.jupiter.issue.impl.records.HistoricalIssueImpl;
import com.elster.jupiter.issue.impl.records.IssueCommentImpl;
import com.elster.jupiter.issue.impl.records.IssueImpl;
import com.elster.jupiter.issue.impl.records.IssueReasonImpl;
import com.elster.jupiter.issue.impl.records.IssueStatusImpl;
import com.elster.jupiter.issue.impl.records.IssueTypeImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeRoleImpl;
import com.elster.jupiter.issue.impl.records.assignee.AssigneeTeamImpl;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.issue.share.entity.AssignmentRule;
import com.elster.jupiter.issue.share.entity.BaseIssue;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionParameter;
import com.elster.jupiter.issue.share.entity.CreationRuleActionType;
import com.elster.jupiter.issue.share.entity.CreationRuleParameter;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.UserService;

import java.util.Arrays;
import java.util.ListIterator;

import static com.elster.jupiter.orm.ColumnConversion.*;

public enum TableSpecs {

    ISU_TYPE{
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueType> table = dataModel.addTable(name(), IssueType.class);
            table.map(IssueTypeImpl.class);

            Column idColumn = table.column(DatabaseConst.ISSUE_TYPE_COLUMN_UUID).map("uuid").type("varchar2(80)").notNull().add();
            table.column(DatabaseConst.ISSUE_TYPE_COLUMN_NAME).map("name").type("varchar2(80)").notNull().add();
            table.addAuditColumns();

            table.primaryKey(DatabaseConst.ISSUE_TYPE_PK_NAME).on(idColumn).add();
        }
    },
    ISU_REASON {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueReason> table = dataModel.addTable(name(), IssueReason.class);
            table.map(IssueReasonImpl.class);
            table.setJournalTableName(DatabaseConst.ISSUE_REASON_JOURNAL_TABLE_NAME);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.ISSUE_REASON_COLUMN_NAME).map("name").type("varchar(200)").notNull().add();
            Column typeRefIdColumn = table.column(DatabaseConst.ISSUE_REASON_COLUMN_TYPE).type("varchar2(80)").notNull().add();
            table.addAuditColumns();

            table.primaryKey(DatabaseConst.ISSUE_REASON_PK_NAME).on(idColumn).add();
            table.foreignKey(DatabaseConst.ISSUE_REASON_FK_TO_ISSUE_TYPE).map("issueType").on(typeRefIdColumn).references(ISU_TYPE.name()).add();
        }
    },

    ISU_STATUS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueStatus> table = dataModel.addTable(name(), IssueStatus.class);
            table.map(IssueStatusImpl.class);
            table.cache();
            table.setJournalTableName(DatabaseConst.ISSUE_STATUS_JOURNAL_TABLE_NAME);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.ISSUE_STATUS_COLUMN_NAME).map("name").type("varchar(200)").notNull().add();
            table.column(DatabaseConst.ISSUE_STATUS_COLUMN_IS_FINAL).map("isFinal").type("char(1)").conversion(CHAR2BOOLEAN).notNull().add();
            table.addAuditColumns();

            table.primaryKey(DatabaseConst.ISSUE_STATUS_PK_NAME).on(idColumn).add();
        }
    },

    ISU_ASSIGNEETEAM { // No journalling
        @Override
        public void addTo(DataModel dataModel) {
            Table<AssigneeTeam> table = dataModel.addTable(name(), AssigneeTeam.class);
            table.map(AssigneeTeamImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.ISSUE_ASSIGNEE_NAME).map("name").type("varchar(80)").notNull().add();
            table.addAuditColumns();

            table.primaryKey(DatabaseConst.ISSUE_ASSIGNEE_TEAM_PK_NAME).on(idColumn).add();
        }
    },

    ISU_ASSIGNEEROLE { // No journalling
        @Override
        public void addTo(DataModel dataModel) {
            Table<AssigneeRole> table = dataModel.addTable(name(), AssigneeRole.class);
            table.map(AssigneeRoleImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.ISSUE_ASSIGNEE_NAME).map("name").type("varchar(80)").notNull().add();
            table.column(DatabaseConst.ISSUE_ASSIGNEE_DESCRIPTION).map("description").type("varchar(256)").notNull().add();
            table.addAuditColumns();

            table.primaryKey(DatabaseConst.ISSUE_ASSIGNEE_ROLE_PK_NAME).on(idColumn).add();
        }
    },

    ISU_COMMENT {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueComment> table = dataModel.addTable(name(), IssueComment.class);
            table.map(IssueCommentImpl.class);
            table.setJournalTableName(DatabaseConst.ISSUE_COMMENT_JOURNAL_TABLE_NAME);
            Column idColumn = table.addAutoIdColumn();

            table.column(DatabaseConst.ISSUE_COMMENT_COMMENT).type("CLOB").map("comment").conversion(ColumnConversion.CLOB2STRING).add();
            table.column(DatabaseConst.ISSUE_COMMENT_ISSUE_ID).map("issueId").type("number").conversion(NUMBER2LONG).notNull().add();
            Column userRefIdColumn = table.column(DatabaseConst.ISSUE_COMMENT_USER_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            table.primaryKey(DatabaseConst.ISSUE_COMMENT_PK_NAME).on(idColumn).add();
            table.addAuditColumns();

            table.foreignKey(DatabaseConst.ISSUE_COMMENT_FK_TO_USER).map("user").on(userRefIdColumn).references(UserService.COMPONENTNAME, DatabaseConst.USER_TABLE).add();
        }
    },
    ISU_CREATIONRULES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<CreationRule> table = dataModel.addTable(name(), CreationRule.class);
            table.map(CreationRuleImpl.class);
            table.setJournalTableName(DatabaseConst.CREATION_RULE_JOURNAL_TABLE_NAME);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.CREATION_RULE_NAME).map("name").type("varchar2(256)").notNull().add();
            table.column(DatabaseConst.CREATION_RULE_COMMENT).map("comment").type("clob").conversion(CLOB2STRING).add();
            table.column(DatabaseConst.CREATION_RULE_CONTENT).map("content").type("clob").conversion(CLOB2STRING).notNull().add();
            Column reasonRefIdColumn = table.column(DatabaseConst.CREATION_RULE_REASON_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            table.column(DatabaseConst.CREATION_RULE_DUE_IN_VALUE).map("dueInValue").type("number").conversion(NUMBER2LONG).add();
            table.column(DatabaseConst.CREATION_RULE_DUE_IN_TYPE).map("dueInType").type("number").conversion(NUMBER2ENUM).add();
            table.column(DatabaseConst.CREATION_RULE_TEMPLATE_NAME).map("templateUuid").type("varchar2(128)").notNull().add();
            table.column(DatabaseConst.CREATION_RULE_OBSOLETE_TIME).map("obsoleteTime").type("number").conversion(NUMBER2UTCINSTANT).add();
            table.addAuditColumns();

            table.primaryKey(DatabaseConst.CREATION_RULE_PK_NAME).on(idColumn).add();
            table.foreignKey(DatabaseConst.CREATION_RULE_FK_TO_REASON).map("reason").on(reasonRefIdColumn).references(ISU_REASON.name()).add();
        }
    },
    ISU_RULEPARAM {
        @Override
        public void addTo(DataModel dataModel) {
            Table<CreationRuleParameter> table = dataModel.addTable(name(), CreationRuleParameter.class);
            table.map(CreationRuleParameterImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.CREATION_PARAMETER_KEY).map("key").type("varchar2(256)").notNull().add();
            table.column(DatabaseConst.CREATION_PARAMETER_VALUE).map("value").type("varchar2(1024)").notNull().add();
            Column ruleRefIdColumn = table.column(DatabaseConst.CREATION_PARAMETER_RULE_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            table.addAuditColumns();

            table.primaryKey(DatabaseConst.CREATION_PARAMETER_PK_NAME).on(idColumn).add();
            table.foreignKey(DatabaseConst.CREATION_PARAMETER_FK_TO_RULE).on(ruleRefIdColumn).references(ISU_CREATIONRULES.name())
                    .map("rule").reverseMap("parameters").reverseMapOrder("key").composition().onDelete(DeleteRule.CASCADE).add();
        }
    },
    ISU_ISSUEHISTORY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalIssue> table = dataModel.addTable(name(), HistoricalIssue.class);
            table.map(HistoricalIssueImpl.class);
            Column idColumn = table.column(DatabaseConst.ISSUE_HIST_COLUMN_ID).map("id").type("number").conversion(NUMBER2LONG).notNull().add();

            TableBuilder.buildIssueTable(table, idColumn, DatabaseConst.ISSUE_HIST_PK_NAME,
                    // Foreign keys
                    DatabaseConst.ISSUE_HIST_FK_TO_REASON,
                    DatabaseConst.ISSUE_HIST_FK_TO_STATUS,
                    DatabaseConst.ISSUE_HIST_FK_TO_DEVICE,
                    DatabaseConst.ISSUE_HIST_FK_TO_USER,
                    DatabaseConst.ISSUE_HIST_FK_TO_TEAM,
                    DatabaseConst.ISSUE_HIST_FK_TO_ROLE,
                    DatabaseConst.ISSUE_HIST_FK_TO_RULE);
            table.addAuditColumns();
        }
    },
    ISU_ISSUE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Issue> table = dataModel.addTable(name(), Issue.class);
            table.map(IssueImpl.class);
            table.setJournalTableName(DatabaseConst.ISSUE_JOURNAL_TABLE_NAME);
            Column idColumn = table.addAutoIdColumn();

            TableBuilder.buildIssueTable(table, idColumn, DatabaseConst.ISSUE_PK_NAME,
                    // Foreign keys
                    DatabaseConst.ISSUE_FK_TO_REASON,
                    DatabaseConst.ISSUE_FK_TO_STATUS,
                    DatabaseConst.ISSUE_FK_TO_DEVICE,
                    DatabaseConst.ISSUE_FK_TO_USER,
                    DatabaseConst.ISSUE_FK_TO_TEAM,
                    DatabaseConst.ISSUE_FK_TO_ROLE,
                    DatabaseConst.ISSUE_FK_TO_RULE);
            table.addAuditColumns();
        }
    },
    ISU_BASE_ISSUES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<BaseIssue> table = dataModel.addTable(DatabaseConst.ALL_ISSUES_VIEW_NAME, BaseIssue.class);
            table.map(BaseIssueImpl.class);
            table.doNotAutoInstall();
            Column idColumn = table.addAutoIdColumn();
            TableBuilder.buildIssueTable(table, idColumn, DatabaseConst.ALL_ISSUES_PK_NAME,
                    // Foreign keys
                    DatabaseConst.ALL_ISSUES_FK_TO_REASON,
                    DatabaseConst.ALL_ISSUES_FK_TO_STATUS,
                    DatabaseConst.ALL_ISSUES_FK_TO_DEVICE,
                    DatabaseConst.ALL_ISSUES_FK_TO_USER,
                    DatabaseConst.ALL_ISSUES_FK_TO_TEAM,
                    DatabaseConst.ALL_ISSUES_FK_TO_ROLE,
                    DatabaseConst.ALL_ISSUES_FK_TO_RULE);
            table.addAuditColumns();
        }
    },
    ISU_ASSIGMENTRULES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<AssignmentRule> table = dataModel.addTable(name(), AssignmentRule.class);
            table.map(AssignmentRuleImpl.class);
            table.setJournalTableName(DatabaseConst.ASSIGNEE_RULE_JOURNAL_TABLE_NAME);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.ASSIGNMENT_RULES_PRIORITY).map("priority").type("number").conversion(NUMBER2INT).add();
            table.column(DatabaseConst.ASSIGNMENT_RULES_DESCRIPTION).map("description").type("varchar(400)").add();
            table.column(DatabaseConst.ASSIGNMENT_RULES_TITLE).map("title").type("varchar(400)").notNull().add();
            table.column(DatabaseConst.ASSIGNMENT_RULES_ENABLED).map("enabled").type("number").conversion(NUMBER2BOOLEAN).add();
            table.column(DatabaseConst.ASSIGNMENT_RULES_RULE_DATA).map("ruleData").type("clob").conversion(CLOB2STRING).notNull().add();
            table.addAuditColumns();

            table.primaryKey(DatabaseConst.ASSIGNMENT_RULES_PK).on(idColumn).add();
        }
    },
    ISU_ACTIONTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<CreationRuleActionType> table = dataModel.addTable(name(), CreationRuleActionType.class);
            table.map(CreationRuleActionTypeImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.RULE_ACTION_TYPE_NAME).map("name").type("varchar2(256)").notNull().add();
            Column typeRefIdColumn = table.column(DatabaseConst.RULE_ACTION_TYPE_ISSUE_TYPE).type("varchar2(80)").add();
            table.column(DatabaseConst.RULE_ACTION_TYPE_DESCRIPTION).map("description").type("varchar2(256)").add();
            table.column(DatabaseConst.RULE_ACTION_TYPE_CLASS_NAME).map("className").type("varchar2(1024)").notNull().add();
            table.addAuditColumns();

            table.primaryKey(DatabaseConst.RULE_ACTION_TYPE_PK_NAME).on(idColumn).add();
            table.foreignKey(DatabaseConst.RULE_ACTION_TYPE_FK_TO_ISSUE_TYPE).map("issueType").on(typeRefIdColumn).references(ISU_TYPE.name()).add();
        }
    },
    ISU_RULEACTION {
        @Override
        public void addTo(DataModel dataModel) {
            Table<CreationRuleAction> table = dataModel.addTable(name(), CreationRuleAction.class);
            table.map(CreationRuleActionImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.RULE_ACTION_PHASE).map("phase").type("number").conversion(NUMBER2ENUM).notNull().add();
            Column ruleRefIdColumn = table.column(DatabaseConst.RULE_ACTION_RULE_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            Column typeRefIdColumn = table.column(DatabaseConst.RULE_ACTION_TYPE_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            table.addAuditColumns();

            table.primaryKey(DatabaseConst.RULE_ACTION_PK_NAME).on(idColumn).add();
            table.foreignKey(DatabaseConst.RULE_ACTION_FK_TO_ACTION_TYPE).map("type").on(typeRefIdColumn).references(ISU_ACTIONTYPE.name()).add();
            table.foreignKey(DatabaseConst.RULE_ACTION_FK_TO_RULE).on(ruleRefIdColumn).references(ISU_CREATIONRULES.name())
                    .map("rule").reverseMap("actions").composition().onDelete(DeleteRule.CASCADE).add();
        }
    },
    ISU_RULEACTIONPARAM {
        @Override
        public void addTo(DataModel dataModel) {
            Table<CreationRuleActionParameter> table = dataModel.addTable(name(), CreationRuleActionParameter.class);
            table.map(CreationRuleActionParameterImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.RULE_ACTION_PARAM_KEY).map("key").type("varchar2(256)").notNull().add();
            table.column(DatabaseConst.RULE_ACTION_PARAM_VALUE).map("value").type("varchar2(1024)").notNull().add();
            Column actionRefIdColumn = table.column(DatabaseConst.RULE_ACTION_PARAM_RULE_ACTION_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            table.addAuditColumns();

            table.primaryKey(DatabaseConst.RULE_ACTION_PARAM_PK_NAME).on(idColumn).add();
            table.foreignKey(DatabaseConst.RULE_ACTION_PARAM_FK_TO_ACTION_RULE).on(actionRefIdColumn).references(ISU_RULEACTION.name())
                    .map("action").reverseMap("parameters").reverseMapOrder("key").composition().onDelete(DeleteRule.CASCADE).add();
        }
    }
    ;

	public abstract void addTo(DataModel dataModel);

    private static class TableBuilder{
        private static final int EXPECTED_FK_KEYS_LENGTH = 7;

        static void buildIssueTable(Table table, Column idColumn, String pkKey, String... fkKeys){
            table.column(DatabaseConst.ISSUE_COLUMN_DUE_DATE).map("dueDate").type("number").conversion(NUMBER2UTCINSTANT).add();
            Column reasonRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_REASON_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            Column statusRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_STATUS_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            Column deviceRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_DEVICE_ID).type("number").conversion(NUMBER2LONG).add();
            table.column(DatabaseConst.ISSUE_COLUMN_ASSIGNEE_TYPE).map("assigneeType").type("number").conversion(NUMBER2ENUM).add();
            Column userRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_USER_ID).type("number").conversion(NUMBER2LONG).add();
            Column teamRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_TEAM_ID).type("number").conversion(NUMBER2LONG).add();
            Column roleRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_ROLE_ID).type("number").conversion(NUMBER2LONG).add();
            table.column(DatabaseConst.ISSUE_COLUMN_OVERDUE).map("overdue").type("number").conversion(NUMBER2BOOLEAN).notNull().add();
            Column ruleRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_RULE_ID).type("number").conversion(NUMBER2LONG).notNull().add();


            table.primaryKey(pkKey).on(idColumn).add();
            if (fkKeys == null || fkKeys.length != EXPECTED_FK_KEYS_LENGTH){
                throw new IllegalArgumentException("Passed arguments don't match foreigen keys");
            }
            ListIterator<String> fkKeysIter = Arrays.asList(fkKeys).listIterator();
            table.foreignKey(fkKeysIter.next()).map("reason").on(reasonRefIdColumn).references(ISU_REASON.name()).add();
            table.foreignKey(fkKeysIter.next()).map("status").on(statusRefIdColumn).references(ISU_STATUS.name()).add();
            table.foreignKey(fkKeysIter.next()).map("device").on(deviceRefIdColumn).references(MeteringService.COMPONENTNAME, DatabaseConst.METERING_DEVICE_TABLE).add();
            table.foreignKey(fkKeysIter.next()).map("user").on(userRefIdColumn).references(UserService.COMPONENTNAME, DatabaseConst.USER_TABLE).add();
            table.foreignKey(fkKeysIter.next()).map("group").on(teamRefIdColumn).references(ISU_ASSIGNEETEAM.name()).add();
            table.foreignKey(fkKeysIter.next()).map("role").on(roleRefIdColumn).references(ISU_ASSIGNEEROLE.name()).add();
            table.foreignKey(fkKeysIter.next()).map("rule").on(ruleRefIdColumn).references(ISU_CREATIONRULES.name()).add();
        }
    }
}