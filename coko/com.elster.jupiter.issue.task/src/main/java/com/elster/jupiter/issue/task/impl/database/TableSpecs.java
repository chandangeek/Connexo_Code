/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.impl.database;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.issue.task.HistoricalRelatedTaskOccurrence;
import com.elster.jupiter.issue.task.HistoricalTaskIssue;
import com.elster.jupiter.issue.task.OpenRelatedTaskOccurrence;
import com.elster.jupiter.issue.task.OpenTaskIssue;
import com.elster.jupiter.issue.task.TaskIssue;
import com.elster.jupiter.issue.task.entity.HistoricalRelatedTaskOccurrenceImpl;
import com.elster.jupiter.issue.task.entity.HistoricalTaskIssueImpl;
import com.elster.jupiter.issue.task.entity.OpenRelatedTaskOccurrenceImpl;
import com.elster.jupiter.issue.task.entity.OpenTaskIssueImpl;
import com.elster.jupiter.issue.task.entity.RelatedTaskOccurrenceImpl;
import com.elster.jupiter.issue.task.entity.TaskIssueImpl;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;

import java.util.Arrays;
import java.util.ListIterator;

import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_BASE_ISSUE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ERROR_MESSAGE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_FAILURE_TIME;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ID;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_FK_TO_ISSUE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_HISTORY_FK_TO_ISSUE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_HISTORY_PK;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_OPEN_FK_TO_ISSUE;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_OPEN_PK;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_ISSUE_PK;
import static com.elster.jupiter.issue.task.impl.database.DatabaseConst.ITK_TASKOCCURRENCE;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;

public enum TableSpecs {
    ITK_ISSUE_HISTORY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalTaskIssue> table = dataModel.addTable(name(), HistoricalTaskIssue.class);
            table.map(HistoricalTaskIssueImpl.class);
            Column idColumn = table.column(ITK_ID).map("id").number().conversion(NUMBER2LONG).notNull().add();

            TableSpecs.TableBuilder.buildIssueTable(table, idColumn, "ISU_ISSUE_HISTORY", ITK_ISSUE_HISTORY_PK,
                    // Foreign keys
                    ITK_ISSUE_HISTORY_FK_TO_ISSUE);
            table.addAuditColumns();
        }
    },
    ITK_ISSUE_OPEN {
        @Override
        public void addTo(DataModel dataModel) {
            Table<OpenTaskIssue> table = dataModel.addTable(name(), OpenTaskIssue.class);
            table.map(OpenTaskIssueImpl.class);
            Column idColumn = table.column(ITK_ID).map("id").number().conversion(NUMBER2LONG).notNull().add();

            TableSpecs.TableBuilder.buildIssueTable(table, idColumn, "ISU_ISSUE_OPEN", ITK_ISSUE_OPEN_PK,
                    // Foreign keys
                    ITK_ISSUE_OPEN_FK_TO_ISSUE);
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
                    ITK_ISSUE_FK_TO_ISSUE);
            table.addAuditColumns();
        }
    },
    ITK_OPEN_RELATED_TSKOCCURRENCE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<OpenRelatedTaskOccurrence> table = dataModel.addTable(name(), OpenRelatedTaskOccurrence.class);
            table.map(OpenRelatedTaskOccurrenceImpl.class);
            Column issueColumn = table.column(ITK_BASE_ISSUE).number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column taskOccurence = table.column(ITK_TASKOCCURRENCE).number().conversion(NUMBER2LONG).notNull().add();
            table.column(ITK_ERROR_MESSAGE).varChar(DESCRIPTION_LENGTH).map(RelatedTaskOccurrenceImpl.Fields.ERROR_MESSAGE.fieldName()).add();
            table.column(ITK_FAILURE_TIME).number().conversion(NUMBER2INSTANT).map(RelatedTaskOccurrenceImpl.Fields.FAIL_TIME.fieldName()).add();

            table.primaryKey("ITK_PK_OPEN_RL_TSK_OCCURRENCE").on(issueColumn, taskOccurence).add();
            table.foreignKey("ITK_FK_OPN_RL_TSKO_ISSUE")
                    .on(issueColumn)
                    .references(ITK_ISSUE_OPEN.name())
                    .map(RelatedTaskOccurrenceImpl.Fields.ISSUE.fieldName())
                    .reverseMap(TaskIssueImpl.Fields.TASK_OCCURRENCES.fieldName())
                    .composition()
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("ITK_FK_OPN_RL_TSKOCCURRENCE")
                    .map((RelatedTaskOccurrenceImpl.Fields.TASK_OCCURRENCE.fieldName()))
                    .on(taskOccurence)
                    .references(TaskService.COMPONENTNAME, "TSK_TASK_OCCURRENCE")
                    .add();
        }
    },

    ITK_HIST_RELATED_TSKOCCURRENCE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<HistoricalRelatedTaskOccurrence> table = dataModel.addTable(name(), HistoricalRelatedTaskOccurrence.class);
            table.map(HistoricalRelatedTaskOccurrenceImpl.class);
            Column issueColumn = table.column(ITK_BASE_ISSUE).number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            Column taskOccurence = table.column(ITK_TASKOCCURRENCE).number().conversion(NUMBER2LONG).notNull().add();
            table.column(ITK_ERROR_MESSAGE).varChar(DESCRIPTION_LENGTH).map(RelatedTaskOccurrenceImpl.Fields.ERROR_MESSAGE.fieldName()).add();
            table.column(ITK_FAILURE_TIME).number().conversion(NUMBER2INSTANT).map(RelatedTaskOccurrenceImpl.Fields.FAIL_TIME.fieldName()).add();

            table.primaryKey("ITK_PK_HIST_RL_TSK_OCCURRENCE").on(issueColumn, taskOccurence).add();
            table.foreignKey("ITK_FK_HST_RL_TSKO_ISSUE")
                    .on(issueColumn)
                    .references(ITK_ISSUE_HISTORY.name())
                    .map(RelatedTaskOccurrenceImpl.Fields.ISSUE.fieldName())
                    .reverseMap(TaskIssueImpl.Fields.TASK_OCCURRENCES.fieldName())
                    .composition()
                    .onDelete(DeleteRule.CASCADE)
                    .add();

            table.foreignKey("ITK_FK_HST_RL_TSKOCCURRENCE")
                    .map((RelatedTaskOccurrenceImpl.Fields.TASK_OCCURRENCE.fieldName()))
                    .on(taskOccurence)
                    .references(TaskService.COMPONENTNAME, "TSK_TASK_OCCURRENCE")
                    .add();
        }
    };

    public abstract void addTo(DataModel dataModel);

    private static class TableBuilder {
        private static final int EXPECTED_FK_KEYS_LENGTH = 1;

        static void buildIssueTable(Table<?> table, Column idColumn, String issueTable, String pkKey, String... fkKeys) {
            Column issueColRef = table.column(ITK_BASE_ISSUE).number().conversion(NUMBER2LONG).notNull().add();

            table.primaryKey(pkKey).on(idColumn).add();
            if (fkKeys == null || fkKeys.length != EXPECTED_FK_KEYS_LENGTH) {
                throw new IllegalArgumentException("Passed arguments don't match foreign keys");
            }
            ListIterator<String> fkKeysIter = Arrays.asList(fkKeys).listIterator();
            table.foreignKey(fkKeysIter.next()).map("baseIssue").on(issueColRef).references(IssueService.COMPONENT_NAME, issueTable).add();
        }
    }
}