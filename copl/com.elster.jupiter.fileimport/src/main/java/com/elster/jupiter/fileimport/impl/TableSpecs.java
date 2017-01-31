/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportOccurrence;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.fileimport.ImportLogEntry;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.CHAR2PATH;
import static com.elster.jupiter.orm.ColumnConversion.CLOB2STRING;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

enum TableSpecs {

    FIM_IMPORT_SCHEDULE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ImportSchedule> table = dataModel.addTable(name(), ImportSchedule.class);
            table.map(ImportScheduleImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.setJournalTableName("FIM_IMPORT_SCHEDULEJRNL");
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("active").add();
            table.column("APPLICATION").varChar(NAME_LENGTH).notNull().map("applicationName").add();
            table.column("IMPORTERNAME").varChar(NAME_LENGTH).notNull().map("importerName").add();
            table.column("DESTINATION").varChar(NAME_LENGTH).notNull().map("destinationName").add();
            table.column("CRONSTRING").varChar(NAME_LENGTH).notNull().map("cronString").add();
            table.column("IMPORTDIR").varChar(DESCRIPTION_LENGTH).notNull().conversion(CHAR2PATH).map("importDirectory").add();
            table.column("PATHMATCHER").varChar(NAME_LENGTH).map("pathMatcher").add();
            table.column("INPROCESSDIR").varChar(DESCRIPTION_LENGTH).notNull().conversion(CHAR2PATH).map("inProcessDirectory").add();
            table.column("SUCCESSDIR").varChar(DESCRIPTION_LENGTH).notNull().conversion(CHAR2PATH).map("successDirectory").add();
            table.column("FAILDIR").varChar(DESCRIPTION_LENGTH).notNull().conversion(CHAR2PATH).map("failureDirectory").add();
            Column obsoleteColumn = table.column("OBSOLETE_TIME").map("obsoleteTime").number().conversion(NUMBER2INSTANT).add();
            table.addAuditColumns();
            table.primaryKey("FIM_PK_IMPORT_SCHEDULE").on(idColumn).add();
            table.unique("FIM_UQ_SCHEDULE_NAME").on(nameColumn, obsoleteColumn).add();
        }

    },
    FIM_FILE_IMPORT_OCCURRENCE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<FileImportOccurrence> table = dataModel.addTable(name(), FileImportOccurrence.class);
            table.map(FileImportOccurrenceImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column importScheduleColumn = table.column("IMPORTSCHEDULE").number().notNull().conversion(NUMBER2LONG).map("importScheduleId").add();
            Column trigger = table.column("TRIGGERTIME").number().conversion(NUMBER2INSTANT).map("triggerTime").add();
            table.column("FILENAME").varChar(DESCRIPTION_LENGTH).notNull().conversion(CHAR2PATH).map("path").add();
            table.column("STATUS").number().notNull().conversion(NUMBER2ENUM).map("status").add();
            table.column("STARTDATE").number().conversion(ColumnConversion.NUMBER2INSTANT).map("startDate").add();
            table.column("ENDDATE").number().conversion(ColumnConversion.NUMBER2INSTANT).map("endDate").add();
            table.column("MESSAGE").varChar(Table.DESCRIPTION_LENGTH).map("message").add();
            table.primaryKey("FIM_PK_FILE_IMPORT").on(idColumn).add();
            table
                .foreignKey("FIM_FKFILEIMPORT_SCHEDULE")
                .references(FIM_IMPORT_SCHEDULE.name())
                .onDelete(DeleteRule.CASCADE)
                .map("importSchedule")
                .on(importScheduleColumn)
                .add();
            table.partitionOn(trigger);
        }
    },

    FIM_FILE_IMPORT_LOG {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ImportLogEntry> table = dataModel.addTable(name(), ImportLogEntry.class);
            table.map(ImportLogEntryImpl.class);
            Column fileImportOccurrenceColumn = table.column("FILEIMPORTOCCURRENCE").number().notNull().conversion(NUMBER2LONG).add();
            Column position = table.column("POSITION").number().notNull().map("position").conversion(NUMBER2INT).add();
            table.column("TIMESTAMP").number().notNull().conversion(NUMBER2INSTANT).map("timeStamp").add();
            table.column("LOGLEVEL").number().notNull().conversion(NUMBER2INT).map("level").add();
            table.column("MESSAGE").varChar(DESCRIPTION_LENGTH).map("message").add();
            table.column("STACKTRACE").type("CLOB").conversion(CLOB2STRING).map("stackTrace").add();
            table.primaryKey("FIM_PK_LOG_ENTRY").on(fileImportOccurrenceColumn, position).add();
            table
                .foreignKey("FIM_PKFILOG_OCCURRENCE")
                .references(FIM_FILE_IMPORT_OCCURRENCE.name())
                .on(fileImportOccurrenceColumn)
                .onDelete(DeleteRule.CASCADE)
                .map("fileImportOccurrenceReference")
                .reverseMap("logEntries")
                .reverseMapOrder("position")
                .composition()
                .refPartition()
                .add();
        }
    },

    FIM_PROPERTY_IN_SCHEDULE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<FileImporterProperty> table = dataModel.addTable(name(), FileImporterProperty.class);
            table.map(FileImporterPropertyImpl.class);
            table.setJournalTableName("FIM_PROPERTY_IN_SCHEDULEJRNL");
            Column importScheduleColumn = table.column("IMPORTSCHEDULE").number().notNull().add();
            Column nameColumn = table.column("NAME").varChar(Table.NAME_LENGTH).notNull().map("name").add();
            table.column("VALUE").varChar(Table.DESCRIPTION_LENGTH).map("stringValue").add();
            table.addAuditColumns();
            table.primaryKey("FIM_PK_PROPERTY").on(importScheduleColumn, nameColumn).add();
            table
                .foreignKey("FIM_FK_PROPERTY")
                .on(importScheduleColumn)
                .references(FIM_IMPORT_SCHEDULE.name())
                .map("importScheduleReference")
                .reverseMap("properties")
                .composition()
                .add();
        }
    };

    public abstract void addTo(DataModel dataModel);

}