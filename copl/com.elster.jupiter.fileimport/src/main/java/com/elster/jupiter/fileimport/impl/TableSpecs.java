package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.*;

enum TableSpecs {

    FIM_IMPORT_SCHEDULE {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            table.column("DESTINATION").type("varchar2(80)").notNull().map("destinationName").add();
            table.column("CRONSTRING").type("varchar2(80)").notNull().map("cronString").add();
            table.column("IMPORTDIR").type("varchar2(80)").notNull().conversion(CHAR2FILE).map("importDirectory").add();
            table.column("INPROCESSDIR").type("varchar2(80)").notNull().conversion(CHAR2FILE).map("inProcessDirectory").add();
            table.column("SUCCESSDIR").type("varchar2(80)").notNull().conversion(CHAR2FILE).map("successDirectory").add();
            table.column("FAILDIR").type("varchar2(80)").notNull().conversion(CHAR2FILE).map("failureDirectory").add();
            table.primaryKey("FIM_PK_IMPORT_SCHEDULE").on(idColumn).add();
        }

    },
    FIM_FILE_IMPORT {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            Column importScheduleColumn = table.column("IMPORTSCHEDULE").type("number").notNull().conversion(NUMBER2LONG).map("importScheduleId").add();
            table.column("FILENAME").type("varchar2(80)").notNull().conversion(CHAR2FILE).map("file").add();
            table.column("STATE").type("number").notNull().conversion(NUMBER2ENUM).map("state").add();
            table.primaryKey("FIM_PK_FILE_IMPORT").on(idColumn).add();
            table.foreignKey("FIM_FKFILEIMPORT_SCHEDULE").references(FIM_IMPORT_SCHEDULE.name()).onDelete(DeleteRule.CASCADE).map("importSchedule").on(importScheduleColumn).add();
        }
    };

    public void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }


    abstract void describeTable(Table table);
}
