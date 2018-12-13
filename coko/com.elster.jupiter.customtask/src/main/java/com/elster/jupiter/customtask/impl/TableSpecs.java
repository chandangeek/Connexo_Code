/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.impl;

import com.elster.jupiter.customtask.CustomTaskOccurrence;
import com.elster.jupiter.customtask.CustomTaskProperty;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;

import static com.elster.jupiter.orm.ColumnConversion.CLOB2STRING;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Version.version;

enum TableSpecs {

    CTK_CUSTOMTASK(ICustomTask.class) {
        @Override
        void describeTable(Table table) {
            table.map(CustomTaskImpl.class);
            table.since(version(10, 4));
            table.setJournalTableName("CTK_CUSTOMTASKJRNL");
            Column idColumn = table.addAutoIdColumn();
            table.column("TASKTYPE").varChar(NAME_LENGTH).notNull().map("taskType").add();
            table.column("LASTRUN").number().conversion(NUMBER2INSTANT).map("lastRun").notAudited().add();
            table.addAuditColumns();

            Column recurrentTaskId = table.column("RECURRENTTASK").number().notNull().conversion(ColumnConversion.NUMBER2LONG).add();
            table.foreignKey("CTK_FK_RECURRENTTASK")
                    .on(recurrentTaskId)
                    .references(RecurrentTask.class)
                    .map("recurrentTask")
                    .add();
            table.primaryKey("CTK_PK_CUSTOMTASK").on(idColumn).add();
        }
    },
    CTK_OCCURRENCE(CustomTaskOccurrence.class) {
        @Override
        void describeTable(Table table) {
            table.map(CustomTaskOccurrenceImpl.class);
            table.since(version(10, 4));
            Column taskOccurrence = table.column("TASKOCC").number().notNull().add();
            Column customTask = table.column("CUSTOMTASK").number().notNull().add();
            table.column("STATUS").number().conversion(ColumnConversion.NUMBER2ENUM).map("status").add();
            table.column("DETAILS").varChar(Table.DESCRIPTION_LENGTH).map("occurrenceDetails").add();
            table.column("MESSAGE").varChar(Table.DESCRIPTION_LENGTH).map("failureReason").add();
            table.column("SUMMARY").type("CLOB").conversion(CLOB2STRING).map("summary").add();

            table.primaryKey("CTK_PK_TASKOCC").on(taskOccurrence).add();

            table.foreignKey("CTK_FK_CTKOCC_TSKOCC")
                    .on(taskOccurrence)
                    .references(TaskOccurrence.class)
                    .map("taskOccurrence")
                    .add();
            table.foreignKey("CTK_FK_CTKOCC_CUSTOMTASK").on(customTask).references(CTK_CUSTOMTASK.name())
                    .map("customTask").add();
        }
    },
    CTK_PROPERTY_IN_TASK(CustomTaskProperty.class) {
        @Override
        void describeTable(Table table) {
            table.map(CustomTaskPropertyImpl.class);
            table.since(version(10, 5));
            table.setJournalTableName("CTK_PROPERTY_IN_TASKJRNL");
            Column taskColumn = table.column("TASK").number().notNull().add();
            Column nameColumn = table.column("NAME").varChar(Table.NAME_LENGTH).notNull().map("name").add();
            table.column("VALUE").varChar(Table.DESCRIPTION_LENGTH).map("stringValue").add();
            table.addAuditColumns();

            table.primaryKey("CTK_PK_TASKPROPERTY").on(taskColumn, nameColumn).add();
            table.foreignKey("CTK_FK_CTK_TASKPROPERTY").on(taskColumn).references(CTK_CUSTOMTASK.name())
                    .map("task").reverseMap("properties").composition().add();
        }
    };

    private final Class<?> api;

    TableSpecs(Class<?> api) {
        this.api = api;
    }

    public void addTo(DataModel component) {
        Table table = component.addTable(name(), api);
        describeTable(table);
    }

    abstract void describeTable(Table table);

}
