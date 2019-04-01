/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.database;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.task.entity.HistoricalTaskIssue;
import com.elster.jupiter.issue.task.entity.OpenTaskIssue;
import com.elster.jupiter.issue.task.entity.TaskIssue;
import com.elster.jupiter.issue.task.impl.records.HistoricalTaskIssueImpl;
import com.elster.jupiter.issue.task.impl.records.OpenTaskIssueImpl;
import com.elster.jupiter.issue.task.impl.records.TaskIssueImpl;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.TaskService;

import java.util.Arrays;
import java.util.ListIterator;

import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_BASE_ISSUE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ERROR_MESSAGE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_FAILURE_TIME;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ID;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_FK_TO_ISSUE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_FK_TO_TASKOCCURRENCE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_HISTORY_FK_TO_ISSUE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_HISTORY_FK_TO_TASKOCCURRENCE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_HISTORY_PK;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_OPEN_FK_TO_ISSUE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_OPEN_FK_TO_TASKOCCURRENCE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_OPEN_PK;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_PK;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_TASKOCCURRENCE;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
    ITK_ISSUE_HISTORY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalTaskIssue> table = dataModel.addTable(name(), HistoricalTaskIssue.class);
            table.map(HistoricalTaskIssueImpl.class);
            table.setJournalTableName("ITK_ISSUE_HISTORY_JRNL").upTo(version(10, 2));
            Column idColumn = table.column(ITK_ID).map("id").number().conversion(NUMBER2LONG).notNull().add();

            TableSpecs.TableBuilder.buildIssueTable(table, idColumn, "ISU_ISSUE_HISTORY", ITK_ISSUE_HISTORY_PK,
                    // Foreign keys
                    ITK_ISSUE_HISTORY_FK_TO_ISSUE,
                    ITK_ISSUE_HISTORY_FK_TO_TASKOCCURRENCE);
            table.addAuditColumns();
        }
    },
    ITK_ISSUE_OPEN {
        @Override
        public void addTo(DataModel dataModel) {
            Table<OpenTaskIssue> table = dataModel.addTable(name(), OpenTaskIssue.class);
            table.map(OpenTaskIssueImpl.class);
            table.setJournalTableName("ITK_ISSUE_OPEN_JRNL").upTo(version(10, 2));
            Column idColumn = table.column(ITK_ID).map("id").number().conversion(NUMBER2LONG).notNull().add();

            TableSpecs.TableBuilder.buildIssueTable(table, idColumn, "ISU_ISSUE_OPEN", ITK_ISSUE_OPEN_PK,
                    // Foreign keys
                    ITK_ISSUE_OPEN_FK_TO_ISSUE,
                    ITK_ISSUE_OPEN_FK_TO_TASKOCCURRENCE);
            table.addAuditColumns();
        }
    },
    ITK_ISSUE_ALL {
        @Override
        public void addTo(DataModel dataModel) {
            Table<TaskIssue> table = dataModel.addTable(name(), TaskIssue.class);
            table.map(TaskIssueImpl.class);
            table.doNotAutoInstall();
            Column idColumn = table.column(ITK_ID).map("id").number().conversion(NUMBER2LONG).notNull().add();

            TableSpecs.TableBuilder.buildIssueTable(table, idColumn, "ISU_ISSUE_ALL", ITK_ISSUE_PK,
                    // Foreign keys
                    ITK_ISSUE_FK_TO_ISSUE,
                    ITK_ISSUE_FK_TO_TASKOCCURRENCE);
            table.addAuditColumns();
        }
    };

    public abstract void addTo(DataModel dataModel);

    private static class TableBuilder {
        private static final int EXPECTED_FK_KEYS_LENGTH = 2;

        static void buildIssueTable(Table<?> table, Column idColumn, String issueTable, String pkKey, String... fkKeys) {
            Column issueColRef = table.column(ITK_BASE_ISSUE).number().conversion(NUMBER2LONG).notNull().add();
            Column taskOccurence = table.column(ITK_TASKOCCURRENCE).number().conversion(NUMBER2LONG).add();
            table.column(ITK_ERROR_MESSAGE).varChar(DESCRIPTION_LENGTH).map("errorMsg").add();
            table.column(ITK_FAILURE_TIME).number().conversion(NUMBER2INSTANT).map("failureTime").add();


            table.primaryKey(pkKey).on(idColumn).add();
            if (fkKeys == null || fkKeys.length != EXPECTED_FK_KEYS_LENGTH) {
                throw new IllegalArgumentException("Passed arguments don't match foreign keys");
            }
            ListIterator<String> fkKeysIter = Arrays.asList(fkKeys).listIterator();
            table.foreignKey(fkKeysIter.next()).map("baseIssue").on(issueColRef).references(IssueService.COMPONENT_NAME, issueTable).add();
            table.foreignKey(fkKeysIter.next()).map("connectionTask").on(taskOccurence).references(TaskService.COMPONENTNAME, "TSK_TASK_OCCURRENCE").add();
        }
    }
}