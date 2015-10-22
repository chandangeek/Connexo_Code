package com.elster.jupiter.bpm.impl;


import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.Table.NAME_LENGTH;

public enum TableSpecs {

    BPM_PROCESS(BpmProcessDefinition.class) {
        @Override
        void describeTable(Table table) {
            table.map(BpmProcessDefinitionImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("PROCESSNAME").varChar(NAME_LENGTH).notNull().map("processName").add();
            table.column("ASSOCIATION").varChar(NAME_LENGTH).notNull().map("association").add();
            table.column("VERSION").varChar(NAME_LENGTH).notNull().map("version").add();
            table.column("STATE").bool().map("state").add();
            table.primaryKey("BPM_PK_PROCESS").on(idColumn).add();
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
