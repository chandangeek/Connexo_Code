package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.AssociationMapping;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NOCONVERSION;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2UTCINSTANT;

enum TableSpecs {

    TSK_RECURRENT_TASK {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            table.addColumn("NAME", "varchar2(80)", true, NOCONVERSION, "name");
            table.addColumn("CRONSTRING", "varchar2(80)", true, NOCONVERSION, "cronString");
            table.addColumn("NEXTEXECUTION", "number", false, NUMBER2UTCINSTANT, "nextExecution");
            table.addColumn("PAYLOAD", "varchar2(80)", true, NOCONVERSION, "payload");
            table.addColumn("DESTINATION", "varchar2(30)", true, NOCONVERSION, "destination");
            table.addPrimaryKeyConstraint("TSK_PK_RECURRENTTASK", idColumn);
        }
    },
    TSK_TASK_OCCURRENCE {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            Column recurrentIdColumn = table.addColumn("RECURRENTTASKID", "number", true, NUMBER2LONG, "recurrentTaskId");
            table.addColumn("TRIGGERTIME", "number", false, NUMBER2UTCINSTANT, "triggerTime");
            table.addForeignKeyConstraint("TSK_FKOCCURRENCE_TASK", TSK_RECURRENT_TASK.name(), DeleteRule.CASCADE, new AssociationMapping("recurrentTask"), recurrentIdColumn);
            table.addPrimaryKeyConstraint("TSK_PK_TASK_OCCURRENCE", idColumn);
        }
    };

    void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }

    abstract void describeTable(Table table);


}
