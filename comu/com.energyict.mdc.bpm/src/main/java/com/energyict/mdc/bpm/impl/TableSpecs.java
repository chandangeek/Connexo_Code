package com.energyict.mdc.bpm.impl;


import com.elster.jupiter.bpm.BpmService;
import com.energyict.mdc.bpm.BpmProcessDeviceState;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

public enum TableSpecs {

    DBP_PROCESS_DEVICESTATE(BpmProcessDeviceState.class) {
        @Override
        void describeTable(Table table) {
            table.map(BpmProcessDeviceStateImpl.class);
            Column processIdColumn = table.column("PROCESSID").number().notNull().conversion(NUMBER2LONG).map("processId").add();
            Column deviceStateId = table.column("DEVICESTATEID").number().notNull().conversion(NUMBER2LONG).map("deviceStateId").add();
            table.column("DEVICELIFECYCLEID").number().notNull().conversion(NUMBER2LONG).map("deviceLifeCycleId").add();
            table.column("NAME").type("varchar2(30)").notNull().map("name").add();
            table.column("DEVICESTATE").type("varchar2(30)").notNull().map("deviceState").add();
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.primaryKey("DBP_PK_DEVICESTATE").on(processIdColumn, deviceStateId).add();
            table.foreignKey("FK_DEVICESTATE").references(BpmService.COMPONENTNAME, "BPM_PROCESS").map("bpmProcessDefinition")
                    .on(processIdColumn).onDelete(CASCADE).add();
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
