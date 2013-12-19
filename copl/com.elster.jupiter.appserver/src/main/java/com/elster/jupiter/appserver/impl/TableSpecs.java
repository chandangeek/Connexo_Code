package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.*;

public enum TableSpecs {

    APS_APPSERVER {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.column("NAME").type("varchar2(80)").notNull().map("name").add();
            table.column("CRONSTRING").type("varchar2(80)").notNull().map("cronString").add();
            table.column("RECURRENTTASKSACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("recurrentTaskActive").add();
            table.primaryKey("APS_PK_APPSERVER").on(idColumn).add();
        }

    },
    APS_SUBSCRIBEREXECUTIONSPEC {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            table.column("THREADCOUNT").type("NUMBER").notNull().conversion(NUMBER2INT).map("threadCount").add();
            table.column("SUBSCRIBERSPEC").type("varchar2(80)").notNull().map("subscriberSpecName").add();
            table.column("DESTINATIONSPEC").type("varchar2(80)").notNull().map("destinationSpecName").add();
            Column appServerColumn = table.column("APPSERVER").type("varchar2(80)").notNull().map("appServerName").add();
            table.foreignKey("APS_FKEXECUTIONSPECAPPSERVER").references(APS_APPSERVER.name()).onDelete(DeleteRule.CASCADE).map("appServer").on(appServerColumn).add();
            table.primaryKey("APS_PK_SUBSCRIBEREXECUTIONSPEC").on(idColumn).add();
        }
    },
    APS_IMPORTSCHEDULEONSERVER {
        @Override
        void describeTable(Table table) {
            Column appServerColumn = table.column("APPSERVER").type("varchar2(80)").notNull().map("appServerName").add();
            Column importScheduleColumn = table.column("IMPORTSCHEDULE").type("number").notNull().conversion(NUMBER2LONG).map("importScheduleId").add();
            table.foreignKey("APS_FKIMPORTSCHEDULEAPPSERVER").references(APS_APPSERVER.name()).onDelete(DeleteRule.CASCADE).map("appServer").on(appServerColumn).add();
            table.primaryKey("APS_PK_IMPORTSCHEDULEONSERVER").on(appServerColumn, importScheduleColumn).add();
        }
    };

    public void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }

    abstract void describeTable(Table table);

}
