package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import static com.elster.jupiter.orm.ColumnConversion.NUMBER2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.energyict.mdc.tasks.impl.BasicCheckTaskImpl.Fields.MAXIMUM_CLOCK_DIFFERENCE;
import static com.energyict.mdc.tasks.impl.BasicCheckTaskImpl.Fields.VERIFY_CLOCK_DIFFERENCE;
import static com.energyict.mdc.tasks.impl.BasicCheckTaskImpl.Fields.VERIFY_SERIAL_NUMBER;
import static com.energyict.mdc.tasks.impl.ClockTaskImpl.Fields.CLOCK_TASK_TYPE;
import static com.energyict.mdc.tasks.impl.ClockTaskImpl.Fields.MAXIMUM_CLOCK_DIFF;
import static com.energyict.mdc.tasks.impl.ClockTaskImpl.Fields.MAXIMUM_CLOCK_SHIFT;
import static com.energyict.mdc.tasks.impl.ClockTaskImpl.Fields.MINIMUM_CLOCK_DIFF;
import static com.energyict.mdc.tasks.impl.LoadProfilesTaskImpl.Fields.CREATE_METER_EVENTS_FROM_STATUS_FLAGS;
import static com.energyict.mdc.tasks.impl.LoadProfilesTaskImpl.Fields.FAIL_IF_CONFIGURATION_MISMATCH;
import static com.energyict.mdc.tasks.impl.LoadProfilesTaskImpl.Fields.MARK_INTERVALS_AS_BAD_TIME;
import static com.energyict.mdc.tasks.impl.LoadProfilesTaskImpl.Fields.MIN_CLOCK_DIFF_BEFORE_BAD_TIME;
import static com.energyict.mdc.tasks.impl.MessagesTaskImpl.Fields.ALL_CATEGORIES;
import static com.energyict.mdc.tasks.impl.TopologyTaskImpl.Fields.TOPOLOGY_ACTION;

public enum TableSpecs {
    MDCCOMTASK {
        @Override
        void addTo(DataModel dataModel) {
            Table<ComTask> table = dataModel.addTable(name(), ComTask.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(256)").map(ComTaskImpl.Fields.NAME.fieldName()).add();
            table.column("STOREDATA").number().conversion(NUMBER2INT).map(ComTaskImpl.Fields.STORE_DATE.fieldName()).add();
            table.column("MAXNROFTRIES").number().conversion(NUMBER2INT).map(ComTaskImpl.Fields.MAX_NR_OF_TRIES.fieldName()).add();
            table.column("MOD_DATE").type("DATE").conversion(ColumnConversion.DATE2DATE).map(ComTaskImpl.Fields.STORE_DATE.fieldName()).insert("sysdate").update("sysdate").add();
            table.primaryKey("TSK_PK_COMTASK").on(idColumn).add();
        }
    },
    MDCPROTOCOLTASK {

        @Override
        void addTo(DataModel dataModel) {
            Table<ProtocolTask> table = dataModel.addTable(name(), ProtocolTask.class);
            table.map(ProtocolTaskImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            Column comTaskId = table.column("COMTASKID").number().conversion(NUMBER2LONG).add(); // DO NOT MAP

            table.column("CLOCKTASKTYPE").number().conversion(NUMBER2ENUM).map(CLOCK_TASK_TYPE.fieldName()).add();
            table.column("MINCLOCKDIFFVALUE").number().conversion(NUMBER2INT).map(MINIMUM_CLOCK_DIFF.fieldName() + ".count").add();
            table.column("MINCLOCKDIFFUNIT").number().conversion(NUMBER2INT).map(MINIMUM_CLOCK_DIFF.fieldName()+".timeUnitCode").add();
            table.column("MAXCLOCKDIFFVALUE").number().conversion(NUMBER2INT).map(MAXIMUM_CLOCK_DIFF.fieldName()+".count").add();
            table.column("MAXCLOCKDIFFUNIT").number().conversion(NUMBER2INT).map(MAXIMUM_CLOCK_DIFF.fieldName()+".timeUnitCode").add();
            table.column("MAXCLOCKSHIFTVALUE").number().conversion(NUMBER2INT).map(MAXIMUM_CLOCK_SHIFT.fieldName()+".count").add();
            table.column("MAXCLOCKSHIFTUNIT").number().conversion(NUMBER2INT).map(MAXIMUM_CLOCK_SHIFT.fieldName()+".timeUnitCode").add();

            table.column("CHECKCLOCKDIFF").number().conversion(NUMBER2BOOLEAN).map(VERIFY_CLOCK_DIFFERENCE.fieldName()).add();
            table.column("VERIFYSERIAL").number().conversion(NUMBER2BOOLEAN).map(VERIFY_SERIAL_NUMBER.fieldName()).add();
            table.column("BASICMAXCLOCKDIFFVALUE").number().conversion(NUMBER2INT).map(MAXIMUM_CLOCK_DIFFERENCE.fieldName()+".count").add();
            table.column("BASICMAXCLOCKDIFFUNIT").number().conversion(NUMBER2INT).map(MAXIMUM_CLOCK_DIFFERENCE.fieldName()+".timeUnitCode").add();

            table.column("ALLCATEGORIES").number().conversion(NUMBER2BOOLEAN).map(ALL_CATEGORIES.fieldName()).add();

            table.column("TOPOLOGYACTION").number().conversion(NUMBER2ENUM).map(TOPOLOGY_ACTION.fieldName()).add();

            table.column("FAILIFCONFIGMISMATCH").number().conversion(NUMBER2BOOLEAN).map(FAIL_IF_CONFIGURATION_MISMATCH.fieldName()).add();
            table.column("MARKASBADTIME").number().conversion(NUMBER2BOOLEAN).map(MARK_INTERVALS_AS_BAD_TIME.fieldName()).add();
            table.column("CREATEMETEREVENTS").number().conversion(NUMBER2BOOLEAN).map(CREATE_METER_EVENTS_FROM_STATUS_FLAGS.fieldName()).add();
            table.column("MINCLOCKDIFFBADTIMEVALUE").number().conversion(NUMBER2INT).map(MIN_CLOCK_DIFF_BEFORE_BAD_TIME.fieldName()+".count").add();
            table.column("MINCLOCKDIFFBADTIMEUNIT").number().conversion(NUMBER2INT).map(MIN_CLOCK_DIFF_BEFORE_BAD_TIME.fieldName()+".timeUnitCode").add();

            table.foreignKey("FK_COM_TASK").on(comTaskId).references(MDCCOMTASK.name()).map(ProtocolTaskImpl.Fields.COM_TASK.fieldName()).reverseMap(ComTaskImpl.Fields.PROTOCOL_TASKS.fieldName()).add();
            table.primaryKey("TSK_PK_PROTOCOLTASK").on(idColumn).add();
        }
    },
    MDCRTUMSGTYPEUSAGE {
        @Override
        void addTo(DataModel dataModel) {
            Table<MessagesTaskTypeUsage> table = dataModel.addTable(name(), MessagesTaskTypeUsage.class);
            Column idColumn = table.addAutoIdColumn();
            Column messageTaskId = table.column("MESSAGETASK").number().conversion(NUMBER2INT).add(); // DO NOT MAP
            table.column("MESSAGECATEGORY").number().conversion(NUMBER2INT).map(MessagesTaskTypeUsageImpl.Fields.DEVICE_MESSAGE_CATEGORY.fieldName()).add();
            table.column("MESSAGESPEC").number().conversion(NUMBER2INT).map(MessagesTaskTypeUsageImpl.Fields.DEVICE_MESSAGE_SPEC.fieldName()).add();
            table.foreignKey("FK_COM_TASK").
                    on(messageTaskId).references(MDCPROTOCOLTASK.name()).
                    map(MessagesTaskTypeUsageImpl.Fields.PROTOCOL_TASK.fieldName()).
                    reverseMap(MessagesTaskImpl.Fields.DEVICE_MESSAGE_USAGES.fieldName()).
                    composition().
                    add();
            table.primaryKey("TSK_PK_MTTU").on(idColumn).add();
        }
    },
    MDCRTUREGISTERGROUPUSAGE {
        @Override
        void addTo(DataModel dataModel) {
            Table<RegisterGroupUsage> table = dataModel.addTable(name(), RegisterGroupUsage.class);
            Column registerTaskId = table.column("REGISTERTASK").number().conversion(NUMBER2INT).add(); // DO NOT MAP
            Column registerGroupId = table.column("REGISTERGROUP").number().conversion(NUMBER2INT).add(); // DO NOT MAP

            table.foreignKey("FK_PROTOCOLTASK").
                    on(registerTaskId).references(MDCPROTOCOLTASK.name()).
                    map(RegisterGroupUsageImpl.Fields.REGISTERS_TASK_REFERENCE.fieldName()).
                    reverseMap(RegistersTaskImpl.Fields.REGISTER_GROUP_USAGES.fieldName()).
                    composition().
                    add();
            table.foreignKey("FK_REGISTERGROUP").on(registerGroupId).map(RegisterGroupUsageImpl.Fields.REGISTERS_GROUP_REFERENCE.fieldName()).add();
            table.primaryKey("PK_REGISTERGROUPUSAGE").on(registerTaskId, registerGroupId).add();
        }
    };


    abstract void addTo(DataModel component);

}
