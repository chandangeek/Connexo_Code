package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2UTCINSTANT;

enum TableSpecs {

    TSK_RECURRENT_TASK {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(80)").notNull().map("name").add();
            table.column("CRONSTRING").type("varchar2(80)").notNull().map("cronString").add();
            table.column("NEXTEXECUTION").type("number").conversion(NUMBER2UTCINSTANT).map("nextExecution").add();
            table.column("PAYLOAD").type("varchar2(80)").notNull().map("payload").add();
            table.column("DESTINATION").type("varchar2(30)").notNull().map("destination").add();
            table.primaryKey("TSK_PK_RECURRENTTASK").on(idColumn).add();
        }
    },
    TSK_TASK_OCCURRENCE {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            Column recurrentIdColumn = table.addColumn("RECURRENTTASKID", "number", true, NUMBER2LONG, "recurrentTaskId");
            table.column("TRIGGERTIME").type("number").conversion(NUMBER2UTCINSTANT).map("triggerTime").add();
            table.foreignKey("TSK_FKOCCURRENCE_TASK").references(TSK_RECURRENT_TASK.name()).onDelete(DeleteRule.CASCADE).map("recurrentTask").on(recurrentIdColumn).add();
            table.primaryKey("TSK_PK_TASK_OCCURRENCE").on(idColumn).add();
        }
    };

    void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }

    abstract void describeTable(Table table);


}
