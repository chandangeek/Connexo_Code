package com.energyict.mdc.task.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.task.ComTask;
import com.energyict.mdc.task.ProtocolTask;

public enum TableSpecs {

    MDCPROTOCOLTASK {

        @Override
        void addTo(DataModel dataModel) {
            Table<ProtocolTask> table = dataModel.addTable(name(), ProtocolTask.class);
            table.map(ProtocolTaskImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            Column comTaskId = table.column("COMTASKID").number().conversion(ColumnConversion.NUMBER2INT).add(); // DO NOT MAP
            table.column("CLOCKTASKTYPE").number().conversion(ColumnConversion.NUMBER2ENUM).map("clockTaskType").add();
            table.column("MINCLOCKDIFFVALUE").number().conversion(ColumnConversion.NUMBER2INT).map("minimumClockDiff.count").add();
            table.column("MINCLOCKDIFFUNIT").number().conversion(ColumnConversion.NUMBER2INT).map("minimumClockDiff.timeUnitCode").add();
            table.column("MAXCLOCKDIFFVALUE").number().conversion(ColumnConversion.NUMBER2INT).map("maximumClockDiff.count").add();
            table.column("MAXCLOCKDIFFUNIT").number().conversion(ColumnConversion.NUMBER2INT).map("maximumClockDiff.timeUnitCode").add();
            table.column("MAXCLOCKSHIFTVALUE").number().conversion(ColumnConversion.NUMBER2INT).map("maximumClockShift.count").add();
            table.column("MAXCLOCKSHIFTUNIT").number().conversion(ColumnConversion.NUMBER2INT).map("maximumClockShift.timeUnitCode").add();

            table.foreignKey("FK_REMOTE_ONLINE").on(comTaskId).references(MDCCOMTASK.name()).map("comTask").add();
            table.primaryKey("TSK_PK_PROTOCOLTASK").on(idColumn).add();
        }
    },
    MDCCOMTASK {
        @Override
        void addTo(DataModel dataModel) {
            Table<ComTask> table = dataModel.addTable(name(), ComTask.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(256)").map("name").add();
            table.column("STOREDATA").number().conversion(ColumnConversion.NUMBER2INT).map("").add();
            table.column("MAXNROFTRIES").number().conversion(ColumnConversion.NUMBER2INT).map("").add();
            table.column("MOD_DATE").type("DATE").conversion(ColumnConversion.DATE2DATE).map("modificationDate").insert("sysdate").update("sysdate").add();
            table.primaryKey("TSK_PK_COMTASK").on(idColumn).add();
        }
    };


    abstract void addTo(DataModel component);

}
