package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.ProtocolTask;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.energyict.mdc.tasks.impl.BasicCheckTaskImpl.Fields.*;
import static com.energyict.mdc.tasks.impl.ClockTaskImpl.Fields.*;
import static com.energyict.mdc.tasks.impl.LoadProfilesTaskImpl.Fields.*;
import static com.energyict.mdc.tasks.impl.MessagesTaskImpl.Fields.ALL_CATEGORIES;
import static com.energyict.mdc.tasks.impl.TopologyTaskImpl.Fields.TOPOLOGY_ACTION;

public enum TableSpecs {
    MDCCOMTASK {
        @Override
        void addTo(DataModel dataModel) {
            Table<ComTask> table = dataModel.addTable(name(), ComTask.class);
            table.map(ComTaskImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.column("NAME").type("varchar2(256)").map(ComTaskImpl.Fields.NAME.fieldName()).add();
            table.column("STOREDATA").number().conversion(NUMBER2BOOLEAN).map(ComTaskImpl.Fields.STORE_DATE.fieldName()).add();
            table.column("MAXNROFTRIES").number().conversion(NUMBER2INT).map(ComTaskImpl.Fields.MAX_NR_OF_TRIES.fieldName()).add();
            table.column("MOD_DATE").type("DATE").conversion(ColumnConversion.DATE2DATE).map(ComTaskImpl.Fields.MOD_DATE.fieldName()).insert("sysdate").update("sysdate").add();
            table.primaryKey("PK_COMTASK").on(idColumn).add();
        }
    },
    MDCPROTOCOLTASK {
        @Override
        void addTo(DataModel dataModel) {
            Table<ProtocolTask> table = dataModel.addTable(name(), ProtocolTask.class);
            table.map(ProtocolTaskImpl.IMPLEMENTERS);
            Column idColumn = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "number");
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

            table.foreignKey("FK_MDCPROTOCOLTASK_COMTASK").on(comTaskId).
                    references(MDCCOMTASK.name()).
                    map(ProtocolTaskImpl.Fields.COM_TASK.fieldName()).
                    reverseMap(ComTaskImpl.Fields.PROTOCOL_TASKS.fieldName()).composition().add();
            table.primaryKey("PK_MDCPROTOCOLTASK").on(idColumn).add();
        }
    },
    MDCRTUMSGTYPEUSAGE {
        @Override
        void addTo(DataModel dataModel) {
            Table<MessagesTaskTypeUsage> table = dataModel.addTable(name(), MessagesTaskTypeUsage.class);
            table.map(MessagesTaskTypeUsageImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column messageTaskId = table.column("MESSAGETASK").number().conversion(NUMBER2INT).add(); // DO NOT MAP
            table.column("MESSAGECATEGORY").type("varchar2(256)").map(MessagesTaskTypeUsageImpl.Fields.DEVICE_MESSAGE_CATEGORY.fieldName()).add();
            table.column("MESSAGESPEC").type("varchar2(256)").map(MessagesTaskTypeUsageImpl.Fields.DEVICE_MESSAGE_SPEC.fieldName()).add();
            table.foreignKey("FK_COM_TASK").
                    on(messageTaskId).references(MDCPROTOCOLTASK.name()).
                    map(MessagesTaskTypeUsageImpl.Fields.PROTOCOL_TASK.fieldName()).
                    reverseMap(MessagesTaskImpl.Fields.DEVICE_MESSAGE_USAGES.fieldName()).
                    composition().
                    add();
            table.primaryKey("PK_MTTU").on(idColumn).add();
        }
    },
    MDCRTUREGISTERGROUPUSAGE {
        @Override
        void addTo(DataModel dataModel) {
            Table<RegisterGroupUsage> table = dataModel.addTable(name(), RegisterGroupUsage.class);
            table.map(RegisterGroupUsageImpl.class);
            Column registerTaskId = table.column("REGISTERSTASK").number().conversion(NUMBER2INT).notNull().add(); // DO NOT MAP
            Column registerGroupId = table.column("REGISTERGROUP").number().conversion(NUMBER2LONG).notNull().add(); // DO NOT MAP

            table.foreignKey("FK_PROTOCOLTASK").
                    on(registerTaskId).references(MDCPROTOCOLTASK.name()).
                    map(RegisterGroupUsageImpl.Fields.REGISTERS_TASK_REFERENCE.fieldName()).
                    reverseMap(RegistersTaskImpl.Fields.REGISTER_GROUP_USAGES.fieldName()).
                    composition().
                    add();
            table.foreignKey("FK_REGISTERGROUP").on(registerGroupId).references(MasterDataService.COMPONENTNAME, "EISRTUREGISTERGROUP").map(RegisterGroupUsageImpl.Fields.REGISTERS_GROUP_REFERENCE.fieldName()).add();
            table.primaryKey("PK_REGISTERGROUPUSAGE").on(registerTaskId, registerGroupId).add();
        }
    },

    MDCLOADPROFILETYPEUSAGE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfileTypeUsageInProtocolTask> table = dataModel.addTable(name(), LoadProfileTypeUsageInProtocolTask.class);
            table.map(LoadProfileTypeUsageInProtocolTaskImpl.class);
            Column loadprofiletask = table.column("LOADPROFILETASK").number().notNull().add(); // DO NOT MAP
            Column loadprofiletype = table.column("LOADPROFILETYPE").number().notNull().add(); // DO NOT MAP

            table.primaryKey("PK_MDCLOADPRFLTYPEUSAGE").on(loadprofiletask,loadprofiletype).add();

            table.foreignKey("FK_MDCLOADPRFLTYPEUSAGE_TASK")
                    .on(loadprofiletask).references(MDCPROTOCOLTASK.name())
                    .map(LoadProfileTypeUsageInProtocolTaskImpl.Fields.LOADPROFILE_TASK_REFERENCE.fieldName())
                    .reverseMap(LoadProfilesTaskImpl.Fields.LOAD_PROFILE_TYPE_USAGES.fieldName())
                    .composition()
                    .add();
            table.foreignKey("FK_MDCLOADPRFLTYPEUSAGE_TYPE").on(loadprofiletype).references(MasterDataService.COMPONENTNAME, "EISLOADPROFILETYPE")
                    .map(LoadProfileTypeUsageInProtocolTaskImpl.Fields.LOADPROFILE_TYPE_REFERENCE.fieldName()).add();
        }
    },

    MDCLOGBOOKTYPEUSAGE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LogBookTypeUsageInProtocolTask> table = dataModel.addTable(name(), LogBookTypeUsageInProtocolTask.class);
            table.map(LogBookTypeUsageInProtocolTaskImpl.class);
            Column logbooksTask = table.column("LOGBOOKSTASK").number().notNull().add(); // DO NOT MAP
            Column logbookType = table.column("LOGBOOKTYPE").number().notNull().add(); // DO NOT MAP

            table.primaryKey("PK_MDCLOGBOOKTYPEUSAGE").on(logbooksTask,logbookType).add();

            table.foreignKey("FK_MDCLOGBOOKTYPEUSAGE_TASK")
                    .on(logbooksTask).references(MDCPROTOCOLTASK.name())
                    .map(LogBookTypeUsageInProtocolTaskImpl.Fields.LOGBOOK_TASK_REFERENCE.fieldName())
                    .reverseMap(LogBooksTaskImpl.Fields.LOGBOOK_TYPE_USAGES.fieldName())
                    .composition()
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("FK_MDCLOGBOOKTYPEUSAGE_TYPE").
                    on(logbookType).references(MasterDataService.COMPONENTNAME, "EISLOGBOOKTYPE").
                    map(LogBookTypeUsageInProtocolTaskImpl.Fields.LOGBOOK_TYPE_REFERENCE.fieldName()).
                    add();
        }
    },
    ;

    abstract void addTo(DataModel component);

}
