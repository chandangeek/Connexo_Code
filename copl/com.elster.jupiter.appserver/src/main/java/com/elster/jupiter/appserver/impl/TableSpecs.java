package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.appserver.SubscriberExecutionSpec;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.Table.*;

public enum TableSpecs {

    APS_APPSERVER(AppServer.class) {
        @Override
        void describeTable(Table table) {
            table.map(AppServerImpl.class);
            Column idColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("CRONSTRING").varChar(NAME_LENGTH).notNull().map("cronString").add();
            table.column("RECURRENTTASKSACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("recurrentTaskActive").add();
            table.primaryKey("APS_PK_APPSERVER").on(idColumn).add();
        }

    },
    APS_SUBSCRIBEREXECUTIONSPEC(SubscriberExecutionSpec.class) {
        @Override
        void describeTable(Table table) {
            table.map(SubscriberExecutionSpecImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("THREADCOUNT").type("NUMBER").notNull().conversion(NUMBER2INT).map("threadCount").add();
            table.column("SUBSCRIBERSPEC").varChar(NAME_LENGTH).notNull().map("subscriberSpecName").add();
            table.column("DESTINATIONSPEC").varChar(NAME_LENGTH).notNull().map("destinationSpecName").add();
            Column appServerColumn = table.column("APPSERVER").varChar(NAME_LENGTH).notNull().map("appServerName").add();
            table.foreignKey("APS_FKEXECUTIONSPECAPPSERVER").references(APS_APPSERVER.name()).onDelete(DeleteRule.CASCADE).map("appServer").on(appServerColumn).add();
            table.primaryKey("APS_PK_SUBSCRIBEREXECUTIONSPEC").on(idColumn).add();
        }
    },
    APS_IMPORTSCHEDULEONSERVER(ImportScheduleOnAppServer.class) {
        @Override
        void describeTable(Table table) {
            table.map(ImportScheduleOnAppServerImpl.class);
            Column appServerColumn = table.column("APPSERVER").varChar(NAME_LENGTH).notNull().map("appServerName").add();
            Column importScheduleColumn = table.column("IMPORTSCHEDULE").type("number").notNull().conversion(NUMBER2LONG).map("importScheduleId").add();
            table.foreignKey("APS_FKIMPORTSCHEDULEAPPSERVER").references(APS_APPSERVER.name()).onDelete(DeleteRule.CASCADE).map("appServer").on(appServerColumn).add();
            table.primaryKey("APS_PK_IMPORTSCHEDULEONSERVER").on(appServerColumn, importScheduleColumn).add();
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
