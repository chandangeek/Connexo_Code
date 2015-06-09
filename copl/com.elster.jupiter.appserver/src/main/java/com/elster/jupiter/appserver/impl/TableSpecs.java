package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportFolderForAppServer;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

public enum TableSpecs {

    APS_APPSERVER {
        @Override
        void addTo(DataModel dataModel) {
        	Table<AppServer> table = dataModel.addTable(name(), AppServer.class);
            table.map(AppServerImpl.class);
            Column idColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.column("CRONSTRING").varChar(NAME_LENGTH).notNull().map("cronString").add();
            table.column("RECURRENTTASKSACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("recurrentTaskActive").add();
            table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map("active").add();
            table.primaryKey("APS_PK_APPSERVER").on(idColumn).add();
        }

    },
    APS_SUBSCRIBEREXECUTIONSPEC {
        @Override
        void addTo(DataModel dataModel) {
        	Table<SubscriberExecutionSpecImpl> table = dataModel.addTable(name(), SubscriberExecutionSpecImpl.class);
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
    APS_IMPORTSCHEDULEONSERVER {
        @Override
        void addTo(DataModel dataModel) {
        	Table<ImportScheduleOnAppServer> table = dataModel.addTable(name(), ImportScheduleOnAppServer.class);
            table.map(ImportScheduleOnAppServerImpl.class);
            Column appServerColumn = table.column("APPSERVER").varChar(NAME_LENGTH).notNull().map("appServerName").add();
            Column importScheduleColumn = table.column("IMPORTSCHEDULE").type("number").notNull().conversion(NUMBER2LONG).map("importScheduleId").add();
            table.foreignKey("APS_FKIMPORTSCHEDULEAPPSERVER").references(APS_APPSERVER.name()).onDelete(DeleteRule.CASCADE).map("appServer").on(appServerColumn).add();
            table.primaryKey("APS_PK_IMPORTSCHEDULEONSERVER").on(appServerColumn, importScheduleColumn).add();
        }
    },
    APS_IMPORTFOLDER() {
        @Override
        void addTo(DataModel dataModel) {
            Table<ImportFolderForAppServer> table = dataModel.addTable(name(), ImportFolderForAppServer.class);
            table.map(ImportFolderForAppServerImpl.class);
            Column appServerColumn = table.column("APPSERVER").varChar(NAME_LENGTH).notNull().map("appServerName").add();
            table.column("PATH").varChar(DESCRIPTION_LENGTH).conversion(CHAR2PATH).map("importFolderPath").add();
            table.primaryKey("APS_PK_IMPORTFOLDER").on(appServerColumn).add();
            table.foreignKey("APS_FK_IMPORTFOLDERAPPSERVER").references(APS_APPSERVER.name()).onDelete(DeleteRule.CASCADE).map("appServer").on(appServerColumn).add();
        }
    };
    
    abstract void addTo(DataModel dataModel);

}
