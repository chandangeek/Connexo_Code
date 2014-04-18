package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.issue.share.entity.BaseIssue;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueComment;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.Rule;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.UserService;

import static com.elster.jupiter.orm.ColumnConversion.*;

public enum TableSpecs {

    ISU_REASON {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueReason> table = dataModel.addTable(name(), IssueReason.class);
            table.map(IssueReason.class);
            table.setJournalTableName(DatabaseConst.ISSUE_REASON_JOURNAL_TABLE_NAME);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.ISSUE_REASON_COLUMN_NAME).map("name").type("varchar(200)").notNull().add();
            table.column(DatabaseConst.ISSUE_REASON_COLUMN_TOPIC).map("topic").type("varchar(200)").notNull().add();
            table.addAuditColumns();
            table.primaryKey(DatabaseConst.ISSUE_REASON_PK_NAME).on(idColumn).add();
        }
    },

    ISU_STATUS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueStatus> table = dataModel.addTable(name(), IssueStatus.class);
            table.map(IssueStatus.class);
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
            table.map(AssigneeTeam.class);

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
            table.map(AssigneeRole.class);

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
            table.map(IssueComment.class);
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
    ISU_ISSUEHISTORY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalIssue> table = dataModel.addTable(name(), HistoricalIssue.class);
            table.map(HistoricalIssue.class);
            Column idColumn = table.column(DatabaseConst.ISSUE_HIST_COLUMN_ID).notNull().map("id").type("number").conversion(NUMBER2LONG).add();

            TableBuilder.buildIssueTable(table, idColumn, DatabaseConst.ISSUE_HIST_PK_NAME,
                    // Foreign keys
                    DatabaseConst.ISSUE_HIST_FK_TO_REASON,
                    DatabaseConst.ISSUE_HIST_FK_TO_STATUS,
                    DatabaseConst.ISSUE_HIST_FK_TO_DEVICE,
                    DatabaseConst.ISSUE_HIST_FK_TO_USER,
                    DatabaseConst.ISSUE_HIST_FK_TO_TEAM,
                    DatabaseConst.ISSUE_HIST_FK_TO_ROLE);
            table.addAuditColumns();
        }
    },
    ISU_ISSUE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Issue> table = dataModel.addTable(name(), Issue.class);
            table.map(Issue.class);
            table.setJournalTableName(DatabaseConst.ISSUE_JOURNAL_TABLE_NAME);
            Column idColumn = table.addAutoIdColumn();

            TableBuilder.buildIssueTable(table, idColumn, DatabaseConst.ISSUE_PK_NAME,
                    // Foreign keys
                    DatabaseConst.ISSUE_FK_TO_REASON,
                    DatabaseConst.ISSUE_FK_TO_STATUS,
                    DatabaseConst.ISSUE_FK_TO_DEVICE,
                    DatabaseConst.ISSUE_FK_TO_USER,
                    DatabaseConst.ISSUE_FK_TO_TEAM,
                    DatabaseConst.ISSUE_FK_TO_ROLE);
            table.addAuditColumns();
        }
    },
    ISU_BASE_ISSUES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<BaseIssue> table = dataModel.addTable(DatabaseConst.ALL_ISSUES_VIEW_NAME, BaseIssue.class);
            table.map(BaseIssue.class);
            Column idColumn = table.addAutoIdColumn();
            TableBuilder.buildIssueTable(table, idColumn, DatabaseConst.ALL_ISSUES_PK_NAME,
                    // Foreign keys
                    DatabaseConst.ALL_ISSUES_FK_TO_REASON,
                    DatabaseConst.ALL_ISSUES_FK_TO_STATUS,
                    DatabaseConst.ALL_ISSUES_FK_TO_DEVICE,
                    DatabaseConst.ALL_ISSUES_FK_TO_USER,
                    DatabaseConst.ALL_ISSUES_FK_TO_TEAM,
                    DatabaseConst.ALL_ISSUES_FK_TO_ROLE);
            table.addAuditColumns();
        }
    },
    ISU_ASSIGMENTRULES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Rule> table = dataModel.addTable(name(), Rule.class);
            table.map(Rule.class);
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
    }
    ;

	public abstract void addTo(DataModel dataModel);

    private static class TableBuilder{
        static void buildIssueTable(Table table, Column idColumn, String pkKey, String... fkKeys){
            table.column(DatabaseConst.ISSUE_COLUMN_DUE_DATE).map("dueDate").type("number").conversion(NUMBER2UTCINSTANT).add();
            Column reasonRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_REASON_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            Column statusRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_STATUS_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            Column deviceRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_DEVICE_ID).type("number").conversion(NUMBER2LONG).add();
            table.column(DatabaseConst.ISSUE_COLUMN_ASSIGNEE_TYPE).map("type").type("number").conversion(NUMBER2ENUM).add();
            Column userRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_USER_ID).type("number").conversion(NUMBER2LONG).add();
            Column teamRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_TEAM_ID).type("number").conversion(NUMBER2LONG).add();
            Column roleRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_ROLE_ID).type("number").conversion(NUMBER2LONG).add();
            table.column(DatabaseConst.ISSUE_COLUMN_DISCRIMINATOR).map("issueType").type("varchar2(1)").add();

            table.primaryKey(pkKey).on(idColumn).add();
            if (fkKeys == null || fkKeys.length != 6){
                throw new IllegalArgumentException("Passed arguments don't match foreigen keys");
            }
            // Changes due to d625ee6 in ORM module (TODO re-check)
            table.foreignKey(fkKeys[0]).map("reason").on(reasonRefIdColumn).references(ISU_REASON.name()).add();
            table.foreignKey(fkKeys[1]).map("status").on(statusRefIdColumn).references(ISU_STATUS.name()).add();
            table.foreignKey(fkKeys[2]).map("device").on(deviceRefIdColumn).references(MeteringService.COMPONENTNAME, DatabaseConst.METERING_DEVICE_TABLE).add();
            table.foreignKey(fkKeys[3]).map("user").on(userRefIdColumn).references(UserService.COMPONENTNAME, DatabaseConst.USER_TABLE).add();
            table.foreignKey(fkKeys[4]).map("team").on(teamRefIdColumn).references(ISU_ASSIGNEETEAM.name()).add();
            table.foreignKey(fkKeys[5]).map("role").on(roleRefIdColumn).references(ISU_ASSIGNEEROLE.name()).add();
        }
    }
}