package com.elster.jupiter.issue.impl;

import com.elster.jupiter.issue.Issue;
import com.elster.jupiter.issue.IssueAssignee;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.*;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2UTCINSTANT;

public enum TableSpecs {
    ISU_ASSIGNEE {
        @Override
        void addTo(DataModel dataModel) {
            Table<IssueAssignee> table = dataModel.addTable(name(), IssueAssignee.class);
            table.map(IssueAssigneeImpl.class);
            table.setJournalTableName(DatabaseConst.ISSUE_ASSIGNEE_JOURNAL_TABLE_NAME);

            Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.ISSUE_ASSIGNEE_COLUMN_TYPE).map("assigneeType").type("number").conversion(NUMBER2ENUM).notNull().add();
            table.column(DatabaseConst.ISSUE_ASSIGNEE_COLUMN_ASSIGNEE_REF).map("assigneeRef").type("varchar(200)").notNull().add();

            table.addAuditColumns();
            table.primaryKey(DatabaseConst.ISSUE_ASSIGNEE_PK_NAME).on(idColumn).add();
        }
    },
    ISU_DOMAIN {
        @Override
		void addTo(DataModel dataModel) {
			Table<Issue> table = dataModel.addTable(name(), Issue.class);
			table.map(IssueImpl.class);
			table.setJournalTableName(DatabaseConst.ISSUE_JOURNAL_TABLE_NAME);

			Column idColumn = table.addAutoIdColumn();
            table.column(DatabaseConst.ISSUE_COLUMN_REASON).map("reason").type("varchar(200)").notNull().add();
            table.column(DatabaseConst.ISSUE_COLUMN_DUE_DATE).map("dueDate").type("number").conversion(NUMBER2UTCINSTANT).add();
			table.column(DatabaseConst.ISSUE_COLUMN_STATUS).map("status").type("number").conversion(NUMBER2ENUM).notNull().add();

            Column deviceRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_DEVICE_REF).type("number").conversion(NUMBER2LONG).add();
            Column assigneeRefIdColumn = table.column(DatabaseConst.ISSUE_COLUMN_ASSIGNEE_REF).type("number").conversion(NUMBER2LONG).add();

			table.addAuditColumns();
			table.primaryKey(DatabaseConst.ISSUE_PK_NAME).on(idColumn).add();
            table.foreignKey(DatabaseConst.ISSUE_FK_TO_DEVICE).map("device").on(deviceRefIdColumn).references(MeteringService.COMPONENTNAME, DatabaseConst.METERING_DEVICE_TABLE).
                    onDelete(DeleteRule.CASCADE).add();
            table.foreignKey(DatabaseConst.ISSUE_FK_TO_ASSIGNEE).map("assignee").on(assigneeRefIdColumn).references(ISU_ASSIGNEE.name())
                    .onDelete(DeleteRule.RESTRICT).composition().add();
		}
	}
    ;
	
	abstract void addTo(DataModel component);
}