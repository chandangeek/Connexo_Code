package com.elster.jupiter.bpm.impl;


import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmProcessPrivilege;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
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
    BPM_PROCESS_PROPERTIES(BpmProcessProperty.class) {
        @Override
        void describeTable(Table table) {
            table.map(BpmProcessPropertyImpl.class);

            Column nameColumn = table.column("NAME").map("name").varChar(NAME_LENGTH).notNull().add();
            Column processColumn = table.column("PROCESSID").type("number").conversion(NUMBER2LONG).notNull().add();
            table.column("VALUE").map("value").varChar(DESCRIPTION_LENGTH).notNull().add();
            table.addAuditColumns();

            table.primaryKey("BPM_PROPS_PK_NAME").on(nameColumn, processColumn).add();
            table.foreignKey("BPM_PROPS_FK_TO_PROCESS").on(processColumn).references(BPM_PROCESS.name())
                    .map("processDefinition").reverseMap("properties").composition().onDelete(DeleteRule.CASCADE).add();
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
