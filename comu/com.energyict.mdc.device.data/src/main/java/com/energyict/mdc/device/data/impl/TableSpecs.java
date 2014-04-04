package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.impl.tasks.ConnectionMethod;
import com.energyict.mdc.device.data.impl.tasks.ConnectionMethodImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.PluggableService;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.DATE2DATE;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2UTCINSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBERINUTCSECONDS2DATE;
import static com.elster.jupiter.orm.DeleteRule.*;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum TableSpecs {

    EISRTU {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Device> table = dataModel.addTable(name(), Device.class);
            table.map(DeviceImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar(80).notNull().map("name").add();
            // TODO rename the column to serialNumber
            table.column("DEVICENAME").varChar(80).map("serialNumber").add();
            table.column("TIMEZONE").varChar(32).map("timeZoneId").add();
            Column externid = table.column("EXTERNID").varChar(255).map("externalName").add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
            Column deviceConfigId = table.column("DEVICECONFIGID").number().add();
            table.foreignKey("FK_EISRTU_DEVICECONFIG").on(deviceConfigId).references(DeviceConfigurationService.COMPONENTNAME, "EISDEVICECONFIG").map("deviceConfiguration").add();
            table.unique("UK_RTU_EXTID").on(externid).add();
            table.primaryKey("PK_RTU").on(id).add();
        }
    },

    EISINFOTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<InfoType> table = dataModel.addTable(name(), InfoType.class);
            table.map(InfoTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(80).notNull().map("name").add();
            table.primaryKey("PK_INFOTYPE").on(id).add();
            table.unique("UK_INFOTYPE").on(name).add();
        }
    },

    EISRTUINFO {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceProtocolProperty> table = dataModel.addTable(name(), DeviceProtocolProperty.class);
            table.map(DeviceProtocolPropertyImpl.class);
            Column deviceId = table.column("RTUID").number().notNull().conversion(NUMBER2LONG).add();
            Column infoTypeId = table.column("INFOTYPEID").map("infoTypeId").number().conversion(NUMBER2LONG).notNull().add();
            table.column("INFOVALUE").varChar(255).map("propertyValue").add();
            table.primaryKey("PK_RTUINFO").on(deviceId,infoTypeId).add();
            table.foreignKey("FK_RTUINFOTYPEID").on(infoTypeId).references(EISINFOTYPE.name()).map("infoTypeId").add();
            table.foreignKey("FK_RTUINFOTYPEDEVID").on(deviceId).references(EISRTU.name()).map("device").reverseMap("deviceProperties").composition().add();
        }
    },

    MDCPHYSICALGATEWAYREFERENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<PhysicalGatewayReference> table = dataModel.addTable(name(), PhysicalGatewayReference.class);
            table.map(PhysicalGatewayReferenceImpl.class);
            Column originId = table.column("ORIGINID").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            Column physicalGatewayId = table.column("GATEWAYID").notNull().number().conversion(NUMBER2LONG).add();
            table.primaryKey("DDC_U_PHY_GATEWAY").on(originId, intervalColumns.get(0)).add();
            table.foreignKey("DDC_FK_DEVPHYORIGIN").on(originId).references(EISRTU.name()).onDelete(CASCADE).map("origin").reverseMap("physicalGatewayReferenceDevice").composition().add();
            table.foreignKey("DDC_FK_DEVPHYGATEWAY").on(physicalGatewayId).references(EISRTU.name()).onDelete(CASCADE).map("gateway").add();
        }
    },

    MDCCOMGATEWAYREFERENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<CommunicationGatewayReference> table = dataModel.addTable(name(), CommunicationGatewayReference.class);
            table.map(CommunicationGatewayReferenceImpl.class);
            Column originId = table.column("ORIGINID").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            Column communicationGatewayId = table.column("GATEWAYID").notNull().number().conversion(NUMBER2LONG).add();
            table.primaryKey("DDC_U_COM_GATEWAY").on(originId, intervalColumns.get(0)).add();
            table.foreignKey("DDC_FK_DEVCOMORIGIN").on(originId).references(EISRTU.name()).onDelete(CASCADE).map("origin").reverseMap("communicationGatewayReferenceDevice").composition().add();
            table.foreignKey("DDC_FK_DEVCOMGATEWAY").on(communicationGatewayId).references(EISRTU.name()).onDelete(CASCADE).map("gateway").add();
        }
    },

    EISLOADPROFILE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfile> table = dataModel.addTable(name(), LoadProfile.class);
            table.map(LoadProfileImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceId = table.column("RTUID").number().notNull().add();
            table.column("LASTREADING").number().map("lastReading").conversion(ColumnConversion.NUMBER2UTCINSTANT).add();
            Column loadprofilespecid = table.column("LOADPROFILESPECID").number().add();
            table.primaryKey("PK_LOADPROFILE").on(id).add();
            table.foreignKey("FK_LOADPROFILE_LOADPROFILESPEC").on(loadprofilespecid).references(DeviceConfigurationService.COMPONENTNAME, "EISLOADPROFILESPEC").map("loadProfileSpec").add();
            table.foreignKey("FK_LOADPROFILE_RTU").on(deviceId).references(EISRTU.name()).map("device").reverseMap("loadProfiles").composition().add();

        }
    },

    EISLOGBOOK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LogBook> table = dataModel.addTable(name(), LogBook.class);
            table.map(LogBookImpl.class);
            Column id = table.addAutoIdColumn();
            Column logBookSpecId = table.column("LOGBOOKSPECID").number().notNull().add();
            Column deviceid = table.column("DEVICEID").number().notNull().add();
            table.column("LASTLOGBOOK").number().map("lastReading").conversion(ColumnConversion.NUMBER2UTCINSTANT).add();
            table.primaryKey("PK_EISLOGBOOKID").on(id).add();
            table.foreignKey("FK_EISLOGBOOK_LOGBOOKSPEC").on(logBookSpecId).references(DeviceConfigurationService.COMPONENTNAME, "EISLOGBOOKSPEC").map("logBookSpec").add();
            table.foreignKey("FK_EISLOGBOOK_DEVICE").on(deviceid).references(EISRTU.name()).map("device").reverseMap("logBooks").composition().add();
        }
    },

//
//    EISDEVICECACHE {
//        @Override
//        public void addTo(DataModel dataModel) {
//            Table<DeviceCache> table = dataModel.addTable(name(), DeviceCache.class);
//            table.map(DeviceCacheImpl.class);
//            Column deviceId = table.column("RTUID").number().notNull().add();
//            table.column("CONTENT").type("BLOB(4000)").map("simpleCache").add();
//            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
//            table.primaryKey("PK_EISDEVICECACHE").on(deviceId).add();
//            table.foreignKey("FK_EISDEVICECACHE_RTU").on(deviceId).references(EISRTU.name()).map("device").add();
//        }
//    },
    
    
    MDCCONNECTIONMETHOD {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ConnectionMethod> table = dataModel.addTable(name(), ConnectionMethod.class);
            table.map(ConnectionMethodImpl.class);
            Column id = table.addAutoIdColumn();
            Column connectionTypePluggableClass = table.column("CONNECTIONTYPEPLUGGABLECLASS").number().conversion(NUMBER2LONG).map("pluggableClassId").notNull().add();
            table.column("NAME").varChar(255).map("name").add();
            Column comPortPool = table.column("COMPORTPOOL").number().notNull().add();
            table.primaryKey("PK_MDCCONNECTIONMETHOD").on(id).add();
            table.foreignKey("FK_MDCCONNMETHOD_CLASS").on(connectionTypePluggableClass).references(PluggableService.COMPONENTNAME, "EISPLUGGABLECLASS").map("pluggableClass").add();
            table.foreignKey("FK_MDCCONNTASKUSAGE_CPP").on(comPortPool).references(EngineModelService.COMPONENT_NAME, "MDCCOMPORTPOOL").map("comPortPool").add();
        }
    },

    MDCCONNECTIONTASK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ConnectionTask> table = dataModel.addTable(name(), ConnectionTask.class);
            table.map(ConnectionTaskImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            // Common columns
            // Todo: change to FK and reference once Device (JP-1122) is properly moved to the mdc.device.data bundle
            table.column("RTU").number().conversion(NUMBER2LONG).map("deviceId").add();
            Column connectionMethod = table.column("CONNECTIONMETHOD").number().add();
            table.column("MOD_DATE").type("DATE").map("modificationDate").add();
            table.column("OBSOLETE_DATE").type("DATE").conversion(DATE2DATE).map("obsoleteDate").add();
            table.column("ISDEFAULT").number().conversion(NUMBER2BOOLEAN).map("isDefault").add();
            table.column("PAUSED").number().conversion(NUMBER2BOOLEAN).map("paused").add();
            table.column("LASTCOMMUNICATIONSTART").number().conversion(NUMBERINUTCSECONDS2DATE).map("lastCommunicationStart").add();
            table.column("LASTSUCCESSFULCOMMUNICATIONEND").conversion(NUMBERINUTCSECONDS2DATE).number().map("lastSuccessfulCommunicationEnd").add();
            Column comServer = table.column("COMSERVER").number().add();
            Column comPortPool = table.column("COMPORTPOOL").number().add();
            // Todo: change to FK and reference once PartialConnectionTask (JP-809) is properly moved to the mdc.device.config bundle
            table.column("PARTIALCONNECTIONTASK").number().conversion(NUMBER2LONG).map("partialConnectionTaskId").add();
            // Common columns for sheduled connection tasks
            table.column("CURRENTRETRYCOUNT").number().conversion(NUMBER2INT).map("currentRetryCount").add();
            table.column("LASTEXECUTIONFAILED").number().conversion(NUMBER2BOOLEAN).map("lastExecutionFailed").add();
            // ScheduledConnectionTaskImpl columns
            table.column("COMWINDOWSTART").number().conversion(NUMBER2INT).map("comWindow.start.millis").add();
            table.column("COMWINDOWEND").number().conversion(NUMBER2INT).map("comWindow.end.millis").add();
            Column nextExecutionSpecs = table.column("NEXTEXECUTIONSPECS").number().add();
            table.column("NEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2DATE).map("nextExecutionTimestamp").add();
            table.column("PLANNEDNEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2DATE).map("plannedNextExecutionTimestamp").add();
            table.column("CONNECTIONSTRATEGY").number().conversion(NUMBER2ENUM).map("connectionStrategy").add();
            table.column("PRIORITY").number().conversion(NUMBER2INT).map("priority").add();
            table.column("SIMULTANEOUSCONNECTIONS").number().conversion(NUMBER2BOOLEAN).map("allowSimultaneousConnections").add();
            Column initiator = table.column("INITIATOR").number().add();
            // InboundConnectionTaskImpl columns: none at this moment
            // ConnectionInitiationTaskImpl columns: none at this moment
            table.primaryKey("PK_MDCCONNTASK").on(id).add();
            table.foreignKey("FK_MDCCONNTASK_METHOD").on(connectionMethod).references(MDCCONNECTIONMETHOD.name()).map("connectionMethod").add();
            table.foreignKey("FK_MDCCONNTASK_CPP").on(comPortPool).references(EngineModelService.COMPONENT_NAME, "MDCCOMPORTPOOL").map("comPortPool").add();
            table.foreignKey("FK_MDCCONNTASK_COMSERVER").on(comServer).references(EngineModelService.COMPONENT_NAME, "MDCCOMSERVER").map("comServer").add();
            table.foreignKey("FK_MDCCONNTASK_INITIATOR").on(initiator).references(MDCCONNECTIONTASK.name()).map("initiationTask").add();
            table.foreignKey("FK_MDCCONNTASK_NEXTEXEC").on(nextExecutionSpecs).references(DeviceConfigurationService.COMPONENTNAME, "MDCNEXTEXECUTIONSPEC").map("nextExecutionSpecs").add();
        }
    };

    abstract void addTo(DataModel component);

}