package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.orm.AssociationMapping;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;

import static com.elster.jupiter.orm.ColumnConversion.NOCONVERSION;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;

public enum TableSpecs {

    APS_APPSERVER {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addColumn("NAME", "varchar2(80)", true, NOCONVERSION, "name");
            table.addColumn("CRONSTRING", "varchar2(80)", true, NOCONVERSION, "cronString");
            table.addPrimaryKeyConstraint("APS_PK_APPSERVER", idColumn);
        }

    },
    APS_SUBSCRIBEREXECUTIONSPEC {
        @Override
        void describeTable(Table table) {
            Column idColumn = table.addAutoIdColumn();
            table.addColumn("THREADCOUNT", "NUMBER", true, NUMBER2INT, "threadCount");
            table.addColumn("SUBSCRIBERSPEC", "varchar2(80)", true, NOCONVERSION, "subscriberSpecName");
            table.addColumn("DESTINATIONSPEC", "varchar2(80)", true, NOCONVERSION, "destinationSpecName");
            Column appServerColumn = table.addColumn("APPSERVER", "varchar2(80)", true, NOCONVERSION, "appServerName");
            table.addForeignKeyConstraint("APS_FKEXECUTIONSPECAPPSERVER", APS_APPSERVER.name(), DeleteRule.CASCADE, new AssociationMapping("appServer"), appServerColumn);
            table.addPrimaryKeyConstraint("APS_PK_SUBSCRIBEREXECUTIONSPEC", idColumn);
        }
    },
    APS_IMPORTSCHEDULEONSERVER {
        @Override
        void describeTable(Table table) {
            Column appServerColumn = table.addColumn("APPSERVER", "varchar2(80)", true, NOCONVERSION, "appServerName");
            Column importScheduleColumn = table.addColumn("IMPORTSCHEDULE", "number", true, NUMBER2LONG, "importScheduleId");
            table.addForeignKeyConstraint("APS_FKIMPORTSCHEDULEAPPSERVER", APS_APPSERVER.name(), DeleteRule.CASCADE, new AssociationMapping("appServer"), appServerColumn);
            table.addPrimaryKeyConstraint("APS_PK_IMPORTSCHEDULEONSERVER", appServerColumn, importScheduleColumn);
        }
    };

    public void addTo(DataModel component) {
        Table table = component.addTable(name());
        describeTable(table);
    }

    abstract void describeTable(Table table);

}
