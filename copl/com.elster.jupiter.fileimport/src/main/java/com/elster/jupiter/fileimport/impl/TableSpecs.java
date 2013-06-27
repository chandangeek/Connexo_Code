package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.orm.AssociationMapping;
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
            table.addColumn("DESTINATION", "varchar2(80)", true, NOCONVERSION, "destinationName");
            table.addColumn("CRONSTRING", "varchar2(80)", true, NOCONVERSION, "cronString");
            table.addColumn("IMPORTDIR", "varchar2(80)", true, CHAR2FILE, "importDirectory");
            table.addColumn("INPROCESSDIR", "varchar2(80)", true, CHAR2FILE, "inProcessDirectory");
            table.addColumn("SUCCESSDIR", "varchar2(80)", true, CHAR2FILE, "successDirectory");
            table.addColumn("FAILDIR", "varchar2(80)", true, CHAR2FILE, "failureDirectory");
            table.addPrimaryKeyConstraint("FIM_PK_IMPORT_SCHEDULE", idColumn);
        }

    },
    FIM_FILE_IMPORT {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            Column importScheduleColumn = table.addColumn("IMPORTSCHEDULE", "number", true, NUMBER2LONG, "importScheduleId");
            table.addColumn("FILENAME", "varchar2(80)", true, CHAR2FILE, "file");
            table.addColumn("STATE", "number", true, NUMBER2ENUM, "state");
            table.addPrimaryKeyConstraint("FIM_PK_FILE_IMPORT", idColumn);
            table.addForeignKeyConstraint("FIM_FKFILEIMPORT_SCHEDULE", FIM_IMPORT_SCHEDULE.name(), DeleteRule.CASCADE, new AssociationMapping("importSchedule"), importScheduleColumn);
        }
    };

    public void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }


    abstract void describeTable(Table table);
}
