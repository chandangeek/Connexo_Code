package com.elster.jupiter.bpm.impl;


import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmProcessDeviceState;
import com.elster.jupiter.bpm.BpmProcessPrivilege;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;

public enum TableSpecs {

    BPM_PROCESS(BpmProcessDefinition.class) {
        @Override
        void describeTable(Table table) {
            table.map(BpmProcessDefinitionImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("PROCESSNAME").varChar(NAME_LENGTH).notNull().map("processName").add();
            table.column("ASSOCIATION").varChar(NAME_LENGTH).notNull().map("association").add();
            table.column("VERSION").varChar(NAME_LENGTH).notNull().map("version").add();
            table.column("STATUS").varChar(NAME_LENGTH).notNull().map("status").add();
            table.primaryKey("BPM_PK_PROCESS").on(idColumn).add();
        }
    },
    BPM_PROCESS_PRIVILEGE(BpmProcessPrivilege.class) {
        @Override
        void describeTable(Table table) {
            table.map(BpmProcessPrivilegeImpl.class);
            Column processIdColumn = table.column("PROCESSID").number().notNull().conversion(NUMBER2LONG).map("processId").add();
            Column applicationColumn = table.column("APPLICATION").type("varchar2(10)").notNull().map("application").add();
            Column privilegeIdColumn = table.column("PRIVILEGENAME").varChar(NAME_LENGTH).notNull().map("privilegeName").add();
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.primaryKey("BPM_PK_PROCESSPRIVILEGE").on(processIdColumn, applicationColumn, privilegeIdColumn).add();
            table.foreignKey("FK_PROCESSPRIVILEGE").references(BPM_PROCESS.name()).onDelete(CASCADE).map("bpmProcessDefinition")
                    .reverseMap("processPrivileges").on(processIdColumn).add();
        }
    },
    BPM_PROCESS_DEVICESTATE(BpmProcessDeviceState.class) {
        @Override
        void describeTable(Table table) {
            table.map(BpmProcessDeviceStateImpl.class);
            Column processIdColumn = table.column("PROCESSID").number().notNull().conversion(NUMBER2LONG).map("processId").add();
            Column deviceStateId = table.column("DEVICESTATEID").number().notNull().conversion(NUMBER2LONG).map("deviceStateId").add();
            table.column("DEVICELIFECYCLEID").number().notNull().conversion(NUMBER2LONG).map("deviceLifeCycleId").add();
            table.column("NAME").type("varchar2(30)").notNull().map("name").add();
            table.column("DEVICESTATE").type("varchar2(30)").notNull().map("deviceState").add();
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.primaryKey("BPM_PK_DEVICESTATE").on(processIdColumn, deviceStateId).add();
            table.foreignKey("FK_DEVICESTATE").references(BPM_PROCESS.name()).onDelete(CASCADE).map("bpmProcessDefinition")
                    .reverseMap("processDeviceStates").on(processIdColumn).add();
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
