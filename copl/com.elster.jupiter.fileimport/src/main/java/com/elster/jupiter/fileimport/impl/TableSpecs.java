package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImport;
import com.elster.jupiter.fileimport.FileImporterProperty;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.Table.*;

enum TableSpecs {

    FIM_IMPORT_SCHEDULE(ImportSchedule.class) {
        @Override
        void describeTable(Table table) {
            table.map(ImportScheduleImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("IMPORTERNAME").varChar(NAME_LENGTH).notNull().map("importerName").add();
            table.column("CRONSTRING").varChar(NAME_LENGTH).notNull().map("cronString").add();
            table.column("IMPORTDIR").varChar(NAME_LENGTH).notNull().conversion(CHAR2FILE).map("importDirectory").add();
            table.column("PATHMATCHER").varChar(NAME_LENGTH).notNull().map("pathMatcher").add();
            table.column("INPROCESSDIR").varChar(NAME_LENGTH).notNull().conversion(CHAR2FILE).map("inProcessDirectory").add();
            table.column("SUCCESSDIR").varChar(NAME_LENGTH).notNull().conversion(CHAR2FILE).map("successDirectory").add();
            table.column("FAILDIR").varChar(NAME_LENGTH).notNull().conversion(CHAR2FILE).map("failureDirectory").add();
            table.primaryKey("FIM_PK_IMPORT_SCHEDULE").on(idColumn).add();
        }

    },
    FIM_FILE_IMPORT(FileImport.class) {
        @Override
        void describeTable(Table table) {
            table.map(FileImportImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column importScheduleColumn = table.column("IMPORTSCHEDULE").type("number").notNull().conversion(NUMBER2LONG).map("importScheduleId").add();
            table.column("FILENAME").varChar(NAME_LENGTH).notNull().conversion(CHAR2FILE).map("file").add();
            table.column("STATE").type("number").notNull().conversion(NUMBER2ENUM).map("state").add();
            table.primaryKey("FIM_PK_FILE_IMPORT").on(idColumn).add();
            table.foreignKey("FIM_FKFILEIMPORT_SCHEDULE").references(FIM_IMPORT_SCHEDULE.name()).onDelete(DeleteRule.CASCADE).map("importSchedule").on(importScheduleColumn).add();
        }
    },

    FIM_IMPORTER_PROPERTY(FileImporterProperty.class) {
        @Override
        void describeTable(Table table) {
            table.map(FileImporterPropertyImpl.class);
            table.setJournalTableName("FIM_IMPORTER_PROPERTY_JRNL");
            Column importScheduleColumn = table.column("IMPORTSCHEDULE").type("number").notNull().conversion(NUMBER2LONG).map("importScheduleId").add();
            Column nameColumn = table.column("NAME").varChar(Table.NAME_LENGTH).notNull().map("name").add();
            table.column("VALUE").varChar(Table.DESCRIPTION_LENGTH).map("stringValue").add();
            table.addAuditColumns();

            table.primaryKey("FIM_PK_IMPORTER_PROPERTY").on(importScheduleColumn, nameColumn).add();
            table.foreignKey("DES_FK_IMPPROP_IMPORT_SCHEDULE").on(importScheduleColumn).references(FIM_IMPORT_SCHEDULE.name())
                    .map("importSchedule").reverseMap("properties").composition().add();
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
