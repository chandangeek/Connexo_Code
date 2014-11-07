package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskLogEntry;
import com.elster.jupiter.tasks.TaskOccurrence;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

enum TableSpecs {

    TSK_RECURRENT_TASK(RecurrentTask.class) {
        @Override
        void describeTable(Table table) {
            table.map(RecurrentTaskImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("CRONSTRING").varChar(NAME_LENGTH).notNull().map("cronString").add();
            table.column("NEXTEXECUTION").type("number").conversion(NUMBER2INSTANT).map("nextExecution").add();
            table.column("PAYLOAD").varChar(NAME_LENGTH).notNull().map("payload").add();
            table.column("DESTINATION").type("varchar2(30)").notNull().map("destination").add();
            table.column("LASTRUN").number().conversion(NUMBER2INSTANT).map("lastRun").add();
            table.primaryKey("TSK_PK_RECURRENTTASK").on(idColumn).add();
        }
    },
    TSK_TASK_OCCURRENCE(TaskOccurrence.class) {
        @Override
        void describeTable(Table table) {
            table.map(TaskOccurrenceImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column recurrentIdColumn = table.column("RECURRENTTASKID").type("number").notNull().conversion(NUMBER2LONG).map("recurrentTaskId").add();
            table.column("TRIGGERTIME").type("number").conversion(NUMBER2INSTANT).map("triggerTime").add();
            table.foreignKey("TSK_FKOCCURRENCE_TASK").references(TSK_RECURRENT_TASK.name()).onDelete(DeleteRule.CASCADE).map("recurrentTask").on(recurrentIdColumn).add();
            table.primaryKey("TSK_PK_TASK_OCCURRENCE").on(idColumn).add();
        }
    },
    TSK_TASK_LOG(TaskLogEntry.class) {
        @Override
        void describeTable(Table table) {
            table.map(TaskLogEntryImpl.class);
            Column taskOccurrenceColumn = table.column("TASKOCCCURRENCE").number().notNull().conversion(NUMBER2LONG).add();
            Column position = table.column("POSITION").number().notNull().map("position").conversion(NUMBER2INT).add();
            table.column("TIMESTAMP").number().notNull().conversion(NUMBER2INSTANT).map("timeStamp").add();
            table.column("LOGLEVEL").number().notNull().conversion(NUMBER2INT).map("level").add();
            table.column("MESSAGE").varChar(DESCRIPTION_LENGTH).map("message").add();
            table.column("STACKTRACE").type("CLOB").conversion(CLOB2STRING).map("stackTrace").add();

            table.primaryKey("TSK_PK_LOG_ENTRY").on(taskOccurrenceColumn, position).add();
            table.foreignKey("TSK_FKTASKLOG_OCCURRENCE").references(TSK_TASK_OCCURRENCE.name()).on(taskOccurrenceColumn).onDelete(DeleteRule.CASCADE)
                    .map("taskOccurrence").reverseMap("logEntries").reverseMapOrder("position").composition().add();
        }
    };

    private final Class<?> api;

    TableSpecs(Class<?> api) {
        this.api = api;
    }

    void addTo(DataModel component) {
        Table table = component.addTable(name(), api);
        describeTable(table);
    }

    abstract void describeTable(Table table);


}
