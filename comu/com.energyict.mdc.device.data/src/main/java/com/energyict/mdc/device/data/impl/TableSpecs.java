package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionMethod;
import com.energyict.mdc.device.data.impl.tasks.ConnectionMethodImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.tasks.TaskService;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

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
            Column partialConnectionTaskColumn = table.column("PARTIALCONNECTIONTASK").number().conversion(NUMBER2LONG).add();
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
            table.foreignKey("FK_MDCCONNTASK_PARTIAL").on(partialConnectionTaskColumn).references("DTC", "MDCPARTIALCONNECTIONTASK").map("partialConnectionTask").add();
        }
    },

    MDCPROTOCOLDIALECTPROPERTIES {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ProtocolDialectProperties> table = dataModel.addTable(name(), ProtocolDialectProperties.class);
            table.map(ProtocolDialectPropertiesImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar(255).map("name").add();
            Column deviceProtocolId = table.column("DEVICEPROTOCOLID").number().conversion(NUMBER2LONG).notNull().map("pluggableClassId").add();
            Column device = table.column("RTUID").number().conversion(NUMBER2LONG).notNull().add();
            table.column("MOD_DATE").type("DATE").conversion(DATE2DATE).map("modificationDate").add();
            Column configurationProperties = table.column("CONFIGURATIONPROPERTIESID").number().add();
            table.foreignKey("FK_MDCPRTDIALECTPROPS_PCID").on(deviceProtocolId).references(PluggableService.COMPONENTNAME, "EISPLUGGABLECLASS").map("deviceProtocolPluggableClass").add();
            table.foreignKey("FK_MDCPRTDIALECTPROPS_RTU").on(device).references(EISRTU.name()).map("device").reverseMap("dialectPropertiesList").add();
            table.foreignKey("FK_MDCPRTDIALECTPROPS_PDCP").on(configurationProperties).references(DeviceConfigurationService.COMPONENTNAME, "MDCDIALECTCONFIGPROPERTIES").map("configurationProperties").add();
            table.primaryKey("PK_MDCPRTDIALECTPROPS").on(id).add();
        }
    },

    MDCCOMTASKEXEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComTaskExecution> table = dataModel.addTable(name(), ComTaskExecution.class);
            table.map(ComTaskExecutionImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceId = table.column("RTU").number().notNull().add();
            Column comtask = table.column("COMTASK").number().notNull().add();
            table.column("NEXTEXECUTIONSPECS").number().conversion(NUMBER2LONG).map("nextExecutionSpecId").add();
            table.column("MYNEXTEXECSPEC").number().conversion(NUMBER2BOOLEAN).map("myNextExecutionSpec").add();
            table.column("LASTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2DATE).map("lastExecutionTimeStamp").add();
            table.column("NEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2DATE).map("nextExecutionTimeStamp").add();
            Column comport = table.column("COMPORT").number().add();
            table.column("MOD_DATE").type("DATE").map("modificationDate").add();
            table.column("OBSOLETE_DATE").type("DATE").conversion(DATE2DATE).map("obsoleteDate").add();
            table.column("PRIORITY").number().conversion(NUMBER2INT).map("priority").add();
            table.column("USEDEFAULTCONNECTIONTASK").number().conversion(NUMBER2BOOLEAN).map("useDefaultConnectionTask").add();
            table.column("CURRENTRETRYCOUNT").number().conversion(NUMBER2INT).map("currentRetryCount").add();
            table.column("PLANNEDNEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2DATE).map("plannedNextExecutionTimeStamp").add();
            table.column("EXECUTIONPRIORITY").number().conversion(NUMBER2INT).map("executionPriority").add();
            table.column("EXECUTIONSTART").number().conversion(NUMBERINUTCSECONDS2DATE).map("executionStart").add();
            table.column("LASTSUCCESSFULCOMPLETION").number().conversion(NUMBERINUTCSECONDS2DATE).map("lastSuccessfulCompletionTimestamp").add();
            table.column("LASTEXECUTIONFAILED").number().conversion(NUMBER2BOOLEAN).map("lastExecutionFailed").add();
            Column connectionTask = table.column("CONNECTIONTASK").number().add();
            Column protocolDialectConfigurationProperties = table.column("PROTOCOLDIALECTCONFIGPROPS").number().notNull().add();
            table.column("IGNORENEXTEXECSPECS").number().conversion(NUMBER2BOOLEAN).notNull().map("ignoreNextExecutionSpecsForInbound").add();
            table.foreignKey("FK_MDCCOMTASKEXEC_COMPORT").on(comport).references(EngineModelService.COMPONENT_NAME, "MDCCOMPORT").map("comPort").add();
            table.foreignKey("FK_MDCCOMTASKEXEC_COMTASK").on(comtask).references(TaskService.COMPONENT_NAME, "MDCCOMTASK").map("comTask").add();
            table.foreignKey("FK_MDCCOMTASKEXEC_CONNECTTASK").on(connectionTask).references(MDCCONNECTIONTASK.name()).map("connectionTask").add();
            table.foreignKey("FK_MDCCOMTASKEXEC_DIALECT").on(protocolDialectConfigurationProperties).references(DeviceConfigurationService.COMPONENTNAME, "MDCDIALECTCONFIGPROPERTIES").map("protocolDialectConfigurationProperties").add();
            table.foreignKey("FK_MDCCOMTASKEXEC_RTU").on(deviceId).references(EISRTU.name()).map("device").add();
            table.primaryKey("PK_MDCCOMTASKEXEC").on(id).add();
        }
    }
    ;

    abstract void addTo(DataModel component);

}