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

            table.column("CLOCKTASKTYPE").number().conversion(ColumnConversion.NUMBER2ENUM).map(ClockTaskImpl.Fields.CLOCK_TASK_TYPE.fieldName()).add();
            table.column("MINCLOCKDIFFVALUE").number().conversion(ColumnConversion.NUMBER2INT).map(ClockTaskImpl.Fields.MINIMUM_CLOCK_DIFF.fieldName() + ".count").add();
            table.column("MINCLOCKDIFFUNIT").number().conversion(ColumnConversion.NUMBER2INT).map(ClockTaskImpl.Fields.MINIMUM_CLOCK_DIFF.fieldName()+".timeUnitCode").add();
            table.column("MAXCLOCKDIFFVALUE").number().conversion(ColumnConversion.NUMBER2INT).map(ClockTaskImpl.Fields.MAXIMUM_CLOCK_DIFF.fieldName()+".count").add();
            table.column("MAXCLOCKDIFFUNIT").number().conversion(ColumnConversion.NUMBER2INT).map(ClockTaskImpl.Fields.MAXIMUM_CLOCK_DIFF.fieldName()+".timeUnitCode").add();
            table.column("MAXCLOCKSHIFTVALUE").number().conversion(ColumnConversion.NUMBER2INT).map(ClockTaskImpl.Fields.MAXIMUM_CLOCK_SHIFT.fieldName()+".count").add();
            table.column("MAXCLOCKSHIFTUNIT").number().conversion(ColumnConversion.NUMBER2INT).map(ClockTaskImpl.Fields.MAXIMUM_CLOCK_SHIFT.fieldName()+".timeUnitCode").add();

            table.column("CHECKCLOCKDIFF").bool().conversion(ColumnConversion.NUMBER2BOOLEAN).map(BasicCheckTaskImpl.Fields.VERIFY_CLOCK_DIFFERENCE.fieldName()).add();
            table.column("VERIFYSERIAL").bool().conversion(ColumnConversion.NUMBER2BOOLEAN).map(BasicCheckTaskImpl.Fields.VERIFY_SERIAL_NUMBER.fieldName()).add();
            table.column("BASICMAXCLOCKDIFFVALUE").number().conversion(ColumnConversion.NUMBER2INT).map(BasicCheckTaskImpl.Fields.MAXIMUM_CLOCK_DIFFERENCE.fieldName()+".count").add();
            table.column("BASICMAXCLOCKDIFFUNIT").number().conversion(ColumnConversion.NUMBER2INT).map(BasicCheckTaskImpl.Fields.MAXIMUM_CLOCK_DIFFERENCE.fieldName()+".timeUnitCode").add();

            table.column("ALLCATEGORIES").number().conversion(ColumnConversion.NUMBER2BOOLEAN).map(MessagesTaskImpl.Fields.ALL_CATEGORIES.fieldName()).add();
            table.foreignKey("FK_COM_TASK").on(comTaskId).references(MDCCOMTASK.name()).map("comTask").add();
            table.primaryKey("TSK_PK_PROTOCOLTASK").on(idColumn).add();
        }
    },
    MDCCOMTASK {
        @Override
        void addTo(DataModel dataModel) {
            Table<ComTask> table = dataModel.addTable(name(), ComTask.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(256)").map("name").add();
            table.column("STOREDATA").number().conversion(ColumnConversion.NUMBER2INT).map("").add(); // TODO complete
            table.column("MAXNROFTRIES").number().conversion(ColumnConversion.NUMBER2INT).map("").add();// TODO complete
            table.column("MOD_DATE").type("DATE").conversion(ColumnConversion.DATE2DATE).map("modificationDate").insert("sysdate").update("sysdate").add();
            table.primaryKey("TSK_PK_COMTASK").on(idColumn).add();
        }
    },
    MDCRTUMESSAGETYPEUSAGE {
        @Override
        void addTo(DataModel dataModel) {
            Table<MessagesTaskTypeUsage> table = dataModel.addTable(name(), MessagesTaskTypeUsage.class);
            Column idColumn = table.addAutoIdColumn();
            Column messageTaskId = table.column("MESSAGETASK").number().conversion(ColumnConversion.NUMBER2INT).add(); // DO NOT MAP
            table.column("MESSAGECATEGORY").number().conversion(ColumnConversion.NUMBER2INT).map(MessagesTaskTypeUsageImpl.Fields.DEVICE_MESSAGE_CATEGORY.fieldName()).add();
            table.column("MESSAGESPEC").number().conversion(ColumnConversion.NUMBER2INT).map(MessagesTaskTypeUsageImpl.Fields.DEVICE_MESSAGE_SPEC.fieldName()).add();
            table.foreignKey("FK_COM_TASK").
                    on(messageTaskId).references(MDCPROTOCOLTASK.name()).
                    map(MessagesTaskTypeUsageImpl.Fields.PROTOCOL_TASK.fieldName()).
                    reverseMap(MessagesTaskImpl.Fields.DEVICE_MESSAGE_USAGES.fieldName()).
                    composition().
                    add();
            table.primaryKey("TSK_PK_MTTU").on(idColumn).add();
        }
    };


    abstract void addTo(DataModel component);

}
