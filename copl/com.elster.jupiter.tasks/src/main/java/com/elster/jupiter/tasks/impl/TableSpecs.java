package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskLogEntry;
import com.elster.jupiter.tasks.TaskOccurrence;

import static com.elster.jupiter.orm.ColumnConversion.CLOB2STRING;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

enum TableSpecs {

    TSK_RECURRENT_TASK {
        @Override
        void addTo(DataModel dataModel) {
            Table<RecurrentTask> table = dataModel.addTable(name(), RecurrentTask.class);
            table.map(RecurrentTaskImpl.class);
            table.setJournalTableName("TSK_RECURRENT_TASKJRNL");
            Column idColumn = table.addAutoIdColumn();
            Column applicationColumn = table.column("APPLICATION").varChar(NAME_LENGTH).notNull().map("application").add();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("CRONSTRING").varChar(NAME_LENGTH).notNull().map("cronString").add();
            table.column("NEXTEXECUTION").number().conversion(NUMBER2INSTANT).map("nextExecution").notAudited().add();
            table.column("PAYLOAD").varChar(NAME_LENGTH).notNull().map("payload").add();
            Column destination = table.column("DESTINATION").varChar(30).notNull().map("destination").add();
            table.column("LASTRUN").number().conversion(NUMBER2INSTANT).map("lastRun").notAudited().add();
            table.addAuditColumns();
            table.primaryKey("TSK_PK_RECURRENTTASK").on(idColumn).add();
            table.unique("TSK_UK_RECURRENTTASK").on(applicationColumn, nameColumn).upTo(Version.version(10,2)).add();
            table.unique("TSK_UK_RECURRENTTASK").on(applicationColumn, nameColumn, destination).since(Version.version(10,2)).add();
            table.column("LOGLEVEL").number().conversion(ColumnConversion.NUMBER2ENUM).map("logLevel").since(Version.version(10,3)).add();
        }
    },
    TSK_TASK_OCCURRENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<TaskOccurrence> table = dataModel.addTable(name(), TaskOccurrence.class);
            table.map(TaskOccurrenceImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column recurrentIdColumn = table.column("RECURRENTTASKID").number().notNull().conversion(NUMBER2LONG).map("recurrentTaskId").add();
            Column trigger = table.column("TRIGGERTIME").number().conversion(NUMBER2INSTANT).map("triggerTime").add();
            table.column("SCHEDULED").bool().map("scheduled").add();
            table.column("STARTDATE").number().conversion(ColumnConversion.NUMBER2INSTANT).map("startDate").add();
            table.column("ENDDATE").number().conversion(ColumnConversion.NUMBER2INSTANT).map("endDate").add();
            table.column("STATUS").number().notNull().conversion(ColumnConversion.NUMBER2ENUM).map("status").add();

            table.foreignKey("TSK_FKOCCURRENCE_TASK").references(TSK_RECURRENT_TASK.name()).map("recurrentTask").on(recurrentIdColumn).add();
            table.primaryKey("TSK_PK_TASK_OCCURRENCE").on(idColumn).add();
            table.partitionOn(trigger);
        }
    },
    TSK_TASK_LOG {
        @Override
        void addTo(DataModel dataModel) {
            Table<TaskLogEntry> table = dataModel.addTable(name(), TaskLogEntry.class);
            table.map(TaskLogEntryImpl.class);
            Column taskOccurrenceColumn = table.column("TASKOCCURRENCE").number().notNull().conversion(NUMBER2LONG).add();
            Column position = table.column("POSITION").number().notNull().map("position").conversion(NUMBER2INT).add();
            table.column("TIMESTAMP").number().notNull().conversion(NUMBER2INSTANT).map("timeStamp").add();
            table.column("LOGLEVEL").number().notNull().conversion(NUMBER2INT).map("level").add();
            table.column("MESSAGE").varChar(DESCRIPTION_LENGTH).map("message").add();
            table.column("STACKTRACE").type("CLOB").conversion(CLOB2STRING).map("stackTrace").add();
            table.primaryKey("TSK_PK_LOG_ENTRY").on(taskOccurrenceColumn, position).add();
            table.foreignKey("TSK_FKTASKLOG_OCCURRENCE").references(TSK_TASK_OCCURRENCE.name()).on(taskOccurrenceColumn).onDelete(DeleteRule.CASCADE)
                    .map("taskOccurrence").reverseMap("logEntries").reverseMapOrder("position").composition().refPartition().add();
        }
    };

    abstract void addTo(DataModel dataModel);


}
