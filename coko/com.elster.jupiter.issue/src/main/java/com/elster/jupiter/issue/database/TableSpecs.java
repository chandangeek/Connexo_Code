package com.elster.jupiter.issue.database;

import com.elster.jupiter.issue.*;
import com.elster.jupiter.issue.impl.*;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.users.UserService;

import static com.elster.jupiter.orm.ColumnConversion.*;

public enum TableSpecs {

    ISU_REASON {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueReason> table = dataModel.addTable(name(), IssueReason.class);
            table.map(IssueReasonImpl.class);
            table.setJournalTableName(DatabaseConst.ISSUE_REASON_JOURNAL_TABLE_NAME);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.ISSUE_REASON_COLUMN_NAME).map("name").type("varchar(200)").notNull().add();

            table.addAuditColumns();
            table.primaryKey(DatabaseConst.ISSUE_REASON_PK_NAME).on(idColumn).add();
        }
    },

    ISU_STATUS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<IssueStatus> table = dataModel.addTable(name(), IssueStatus.class);
            table.map(IssueStatusImpl.class);
            table.setJournalTableName(DatabaseConst.ISSUE_STATUS_JOURNAL_TABLE_NAME);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.ISSUE_STATUS_COLUMN_NAME).map("name").type("varchar(200)").notNull().add();

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

    ISU_ISSUE {
        @Override
        public void addTo(DataModel dataModel) {
			Table<Issue> table = dataModel.addTable(name(), Issue.class);
			table.map(IssueImpl.IMPLEMENTERS);
			table.setJournalTableName(DatabaseConst.ISSUE_JOURNAL_TABLE_NAME);

			Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn(DatabaseConst.ISSUE_COLUMN_DISCRIMINATOR, "char(1)");
            table.column(DatabaseConst.ISSUE_COLUMN_DUE_DATE).map("dueDate").type("number").conversion(NUMBER2UTCINSTANT).add();
            Column reasonRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_REASON_ID).type("number").conversion(NUMBER2LONG).notNull().add();
			Column statusRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_STATUS_ID).type("number").conversion(NUMBER2LONG).notNull().add();
            Column deviceRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_DEVICE_ID).type("number").conversion(NUMBER2LONG).add();
            Column meterRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_METER_ID).type("number").conversion(NUMBER2LONG).add();
            table.column(DatabaseConst.ISSUE_COLUMN_ASSIGNEE_TYPE).map("type").type("number").conversion(NUMBER2ENUM).add();
            Column userRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_USER_ID).type("number").conversion(NUMBER2LONG).add();
            Column teamRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_TEAM_ID).type("number").conversion(NUMBER2LONG).add();
            Column roleRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_ROLE_ID).type("number").conversion(NUMBER2LONG).add();


			table.addAuditColumns();
			table.primaryKey(DatabaseConst.ISSUE_PK_NAME).on(idColumn).add();
            table.foreignKey(DatabaseConst.ISSUE_FK_TO_REASON).map("reason").on(reasonRefIdColumn).references(ISU_REASON.name())
                    .onDelete(DeleteRule.CASCADE).add();
            table.foreignKey(DatabaseConst.ISSUE_FK_TO_STATUS).map("status").on(statusRefIdColumn).references(ISU_STATUS.name())
                    .onDelete(DeleteRule.CASCADE).add();
            table.foreignKey(DatabaseConst.ISSUE_FK_TO_DEVICE).map("device").on(deviceRefIdColumn).references(MeteringService.COMPONENTNAME, DatabaseConst.METERING_DEVICE_TABLE).
                    onDelete(DeleteRule.RESTRICT).add();
            table.foreignKey(DatabaseConst.ISSUE_FK_TO_METER).map("meter").on(meterRefIdColumn).references(MeteringService.COMPONENTNAME, DatabaseConst.METERING_DEVICE_TABLE).
                    onDelete(DeleteRule.RESTRICT).add();
            table.foreignKey(DatabaseConst.ISSUE_FK_TO_USER).map("user").on(userRefIdColumn).references(UserService.COMPONENTNAME, DatabaseConst.USER_DEVICE_TABLE).add();
            table.foreignKey(DatabaseConst.ISSUE_FK_TO_TEAM).map("team").on(teamRefIdColumn).references(ISU_ASSIGNEETEAM.name()).add();
            table.foreignKey(DatabaseConst.ISSUE_FK_TO_ROLE).map("role").on(roleRefIdColumn).references(ISU_ASSIGNEEROLE.name()).add();
		}
	},
    ISU_ISSUEHISTORY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalIssue> table = dataModel.addTable(name(), HistoricalIssue.class);
            table.map(HistoricalIssueImpl.class);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.ISSUE_COLUMN_DISCRIMINATOR).map("type").type("varchar(1)").notNull().add();
            table.column(DatabaseConst.ISSUE_HIST_COLUMN_CREATE_TIME).map("createTime").type("number").conversion(NUMBER2UTCINSTANT).add();
            table.column(DatabaseConst.ISSUE_COLUMN_DUE_DATE).map("dueDate").type("number").conversion(NUMBER2UTCINSTANT).add();
            table.column(DatabaseConst.ISSUE_COLUMN_REASON_ID).map("reasonId").type("number").conversion(NUMBER2LONG).notNull().add();
            table.column(DatabaseConst.ISSUE_COLUMN_STATUS_ID).map("statusId").type("number").conversion(NUMBER2LONG).notNull().add();
            table.column(DatabaseConst.ISSUE_COLUMN_DEVICE_ID).map("deviceId").type("number").conversion(NUMBER2LONG).add();
            table.column(DatabaseConst.ISSUE_COLUMN_ASSIGNEE_TYPE).map("assigneeType").type("number").conversion(NUMBER2ENUM).add();
            table.column(DatabaseConst.ISSUE_COLUMN_USER_ID).map("assigneeUserId").type("number").conversion(NUMBER2LONG).add();
            table.column(DatabaseConst.ISSUE_COLUMN_TEAM_ID).map("assigneeTeamId").type("number").conversion(NUMBER2LONG).add();
            table.column(DatabaseConst.ISSUE_COLUMN_ROLE_ID).map("assigneeRoleId").type("number").conversion(NUMBER2LONG).add();

            table.primaryKey(DatabaseConst.ISSUE_HIST_PK_NAME).on(idColumn).add();
        }
    }
    ;
	
	public abstract void addTo(DataModel component);
}