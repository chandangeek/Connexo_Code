package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.ConnectionTaskFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionJournalEntryImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComStatisticsImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionJournalEntryImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionSessionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComStatistics;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import java.util.List;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Table.DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Table.NAME_LENGTH;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum TableSpecs {

    DDC_DEVICE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Device> table = dataModel.addTable(name(), Device.class);
            table.map(DeviceImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar().notNull().map(DeviceFields.NAME.fieldName()).add();
            table.column("SERIALNUMBER").varChar().map(DeviceFields.SERIALNUMBER.fieldName()).add();
            table.column("TIMEZONE").varChar(32).map(DeviceFields.TIMEZONE.fieldName()).add();
            Column externid = table.column("MRID").varChar().map(DeviceFields.MRID.fieldName()).add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).map("modificationDate").add();
            table.column("CERTIF_DATE").type("DATE").conversion(ColumnConversion.DATE2DATE).map("yearOfCertification").add();
            Column deviceType = table.column("DEVICETYPE").number().notNull().add();
            Column configuration = table.column("DEVICECONFIGID").number().notNull().add();
            table.
                foreignKey("FK_DDC_DEVICE_DEVICECONFIG").
                on(configuration).
                references(DeviceConfigurationService.COMPONENTNAME, "DTC_DEVICECONFIG").
                map(DeviceFields.DEVICECONFIGURATION.fieldName()).
                add();
            table.
                foreignKey("FK_DDC_DEVICE_DEVICETYPE").
                on(deviceType).
                references(DeviceConfigurationService.COMPONENTNAME, "DTC_DEVICETYPE").
                map(DeviceFields.DEVICETYPE.fieldName()).
                add();
            table.unique("UK_DDC_DEVICE_MRID").on(externid).add();
            table.primaryKey("PK_DDC_DEVICE").on(id).add();
        }
    },

    DDC_INFOTYPE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<InfoType> table = dataModel.addTable(name(), InfoType.class);
            table.map(InfoTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            table.primaryKey("PK_DDC_INFOTYPE").on(id).add();
            table.unique("UK_DDC_INFOTYPE").on(name).add();
        }
    },

    DDC_DEVICEPROTOCOLPROPERTY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceProtocolProperty> table = dataModel.addTable(name(), DeviceProtocolProperty.class);
            table.map(DeviceProtocolPropertyImpl.class);
            Column deviceId = table.column("DEVICEID").number().notNull().conversion(NUMBER2LONG).add();
            Column infoTypeId = table.column("INFOTYPEID").map("infoTypeId").number().conversion(NUMBER2LONG).notNull().add();
            table.column("INFOVALUE").varChar().map("propertyValue").add();
            table.primaryKey("PK_DDC_DEVICEPROTOCOLPROPERTY").on(deviceId,infoTypeId).add();
            table.foreignKey("FK_DDC_DEVICEPROTPROP_INFOTYPE").on(infoTypeId).references(DDC_INFOTYPE.name()).map("infoTypeId").add();
            table.foreignKey("FK_DDC_DEVICEPROTPROP_DEVICE").on(deviceId).references(DDC_DEVICE.name()).map("device").reverseMap("deviceProperties").composition().add();
        }
    },

    DDC_PHYSICALGATEWAYREFERENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<PhysicalGatewayReference> table = dataModel.addTable(name(), PhysicalGatewayReference.class);
            table.map(PhysicalGatewayReferenceImpl.class);
            Column originId = table.column("ORIGINID").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            Column physicalGatewayId = table.column("GATEWAYID").notNull().number().conversion(NUMBER2LONG).add();
            table.primaryKey("PK_DDC_PHYSICALGATEWAYREF").on(originId, intervalColumns.get(0)).add();
            table.foreignKey("FK_DDC_PHYSGATEWAYREF_ORIGIN").
                    on(originId).
                    references(DDC_DEVICE.name()).
                    onDelete(CASCADE).map("origin").
                    reverseMap("physicalGatewayReferenceDevice").
                    composition().
                    add();
            table.foreignKey("FK_DDC_PHYSGATEWAYREF_GATEWAY").
                    on(physicalGatewayId).
                    references(DDC_DEVICE.name()).
                    onDelete(CASCADE).
                    map("gateway").
                    add();
        }
    },

    DDC_COMGATEWAYREFERENCE {
        @Override
        void addTo(DataModel dataModel) {
            Table<CommunicationGatewayReference> table = dataModel.addTable(name(), CommunicationGatewayReference.class);
            table.map(CommunicationGatewayReferenceImpl.class);
            Column originId = table.column("ORIGINID").notNull().number().conversion(NUMBER2LONG).add();
            List<Column> intervalColumns = table.addIntervalColumns("interval");
            Column communicationGatewayId = table.column("GATEWAYID").notNull().number().conversion(NUMBER2LONG).add();
            table.primaryKey("PK_DDC_COMMUNICATIONGATEWAYREF").on(originId, intervalColumns.get(0)).add();
            table.foreignKey("FK_DDC_COMGATEWAYREF_ORIGIN").
                    on(originId).references(DDC_DEVICE.name()).
                    onDelete(CASCADE).
                    map("origin").
                    reverseMap("communicationGatewayReferenceDevice").
                    composition().
                    add();
            table.foreignKey("FK_DDC_COMGATEWAYREF_GATEWAY").
                    on(communicationGatewayId).
                    references(DDC_DEVICE.name()).
                    onDelete(CASCADE).
                    map("gateway").
                    add();
        }
    },

    DDC_LOADPROFILE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfile> table = dataModel.addTable(name(), LoadProfile.class);
            table.map(LoadProfileImpl.class);
            Column id = table.addAutoIdColumn();
            Column deviceId = table.column("DEVICEID").number().notNull().add();
            table.column("LASTREADING").number().map("lastReading").conversion(ColumnConversion.NUMBER2UTCINSTANT).add();
            Column loadprofilespecid = table.column("LOADPROFILESPECID").number().add();
            table.primaryKey("PK_DDC_LOADPROFILE").on(id).add();
            table.foreignKey("FK_DDC_LOADPROFILE_LPSPEC").on(loadprofilespecid).references(DeviceConfigurationService.COMPONENTNAME, "DTC_LOADPROFILESPEC").map("loadProfileSpec").add();
            table.foreignKey("FK_DDC_LOADPROFILE_DEVICE").on(deviceId).references(DDC_DEVICE.name()).map("device").reverseMap("loadProfiles").composition().add();

        }
    },

    DDC_LOGBOOK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LogBook> table = dataModel.addTable(name(), LogBook.class);
            table.map(LogBookImpl.class);
            Column id = table.addAutoIdColumn();
            Column logBookSpecId = table.column("LOGBOOKSPECID").number().notNull().add();
            Column deviceid = table.column("DEVICEID").number().notNull().add();
            table.column("LASTLOGBOOK").number().map("lastReading").conversion(ColumnConversion.NUMBER2UTCINSTANT).add();
            table.primaryKey("PK_DDC_LOGBOOK").on(id).add();
            table.foreignKey("FK_DDC_LOGBOOK_LOGBOOKSPEC").on(logBookSpecId).references(DeviceConfigurationService.COMPONENTNAME, "DTC_LOGBOOKSPEC").map("logBookSpec").add();
            table.foreignKey("FK_DDC_LOGBOOK_DEVICE").on(deviceid).references(DDC_DEVICE.name()).map("device").reverseMap("logBooks").composition().add();
        }
    },

    DDC_CONNECTIONTASK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ConnectionTask> table = dataModel.addTable(name(), ConnectionTask.class);
            table.map(ConnectionTaskImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            // Common columns
            table.column("DEVICE").number().conversion(NUMBER2LONG).map(ConnectionTaskFields.DEVICE.fieldName()).add();
            Column connectionTypePluggableClass = table.column("CONNECTIONTYPEPLUGGABLECLASS").number().conversion(NUMBER2LONG).map("pluggableClassId").notNull().add();
            table.column("MOD_DATE").type("DATE").conversion(DATE2DATE).map(ConnectionTaskFields.MODIFICATION_DATE.fieldName()).add();
            table.column("OBSOLETE_DATE").type("DATE").conversion(DATE2DATE).map(ConnectionTaskFields.OBSOLETE_DATE.fieldName()).add();
            table.column("ISDEFAULT").number().conversion(NUMBER2BOOLEAN).map(ConnectionTaskFields.IS_DEFAULT.fieldName()).add();
            table.column("STATUS").number().conversion(NUMBER2ENUM).map(ConnectionTaskFields.STATUS.fieldName()).add();
            table.column("LASTCOMMUNICATIONSTART").number().conversion(NUMBERINUTCSECONDS2DATE).map(ConnectionTaskFields.LAST_COMMUNICATION_START.fieldName()).add();
            table.column("LASTSUCCESSFULCOMMUNICATIONEND").conversion(NUMBERINUTCSECONDS2DATE).number().map(ConnectionTaskFields.LAST_SUCCESSFUL_COMMUNICATION_END.fieldName()).add();
            Column comServer = table.column("COMSERVER").number().add();
            Column comPortPool = table.column("COMPORTPOOL").number().add();
            Column partialConnectionTask = table.column("PARTIALCONNECTIONTASK").number().add();
            // Common columns for sheduled connection tasks
            table.column("CURRENTRETRYCOUNT").number().conversion(NUMBER2INT).map(ConnectionTaskFields.CURRENT_RETRY_COUNT.fieldName()).add();
            table.column("LASTEXECUTIONFAILED").number().conversion(NUMBER2BOOLEAN).map(ConnectionTaskFields.LAST_EXECUTION_FAILED.fieldName()).add();
            // ScheduledConnectionTaskImpl columns
            table.column("COMWINDOWSTART").number().conversion(NUMBER2INT).map("comWindow.start.millis").add();
            table.column("COMWINDOWEND").number().conversion(NUMBER2INT).map("comWindow.end.millis").add();
            Column nextExecutionSpecs = table.column("NEXTEXECUTIONSPECS").number().add();
            table.column("NEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2DATE).map(ConnectionTaskFields.NEXT_EXECUTION_TIMESTAMP.fieldName()).add();
            table.column("PLANNEDNEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2DATE).map(ConnectionTaskFields.PLANNED_NEXT_EXECUTION_TIMESTAMP.fieldName()).add();
            table.column("CONNECTIONSTRATEGY").number().conversion(NUMBER2ENUM).map(ConnectionTaskFields.CONNECTION_STRATEGY.fieldName()).add();
            table.column("PRIORITY").number().conversion(NUMBER2INT).map(ConnectionTaskFields.PRIORITY.fieldName()).add();
            table.column("SIMULTANEOUSCONNECTIONS").number().conversion(NUMBER2BOOLEAN).map(ConnectionTaskFields.ALLOW_SIMULTANEOUS_CONNECTIONS.fieldName()).add();
            Column initiator = table.column("INITIATOR").number().add();
            // InboundConnectionTaskImpl columns: none at this moment
            // ConnectionInitiationTaskImpl columns: none at this moment
            table.primaryKey("PK_DDC_CONNECTIONTASK").on(id).add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_CLASS").
                    on(connectionTypePluggableClass).
                    references(PluggableService.COMPONENTNAME, "CPC_PLUGGABLECLASS").
                    map("pluggableClass").add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_CPP").
                    on(comPortPool).
                    references(EngineModelService.COMPONENT_NAME, "MDC_COMPORTPOOL").
                    map(ConnectionTaskFields.COM_PORT_POOL.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_COMSRVER").
                    on(comServer).
                    references(EngineModelService.COMPONENT_NAME, "MDC_COMSERVER").
                    map(ConnectionTaskFields.COM_SERVER.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_INITIATR").
                    on(initiator).
                    references(DDC_CONNECTIONTASK.name()).
                    map("initiationTask").
                    add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_NEXTEXEC").
                    on(nextExecutionSpecs).
                    references(SchedulingService.COMPONENT_NAME, "SCH_NEXTEXECUTIONSPEC").
                    map(ConnectionTaskFields.NEXT_EXECUTION_SPECS.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_PARTIAL").
                    on(partialConnectionTask).
                    references(DeviceConfigurationService.COMPONENTNAME, "DTC_PARTIALCONNECTIONTASK").
                    map(ConnectionTaskFields.PARTIAL_CONNECTION_TASK.fieldName()).
                    add();
        }
    },

    DDC_PROTOCOLDIALECTPROPS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ProtocolDialectProperties> table = dataModel.addTable(name(), ProtocolDialectProperties.class);
            table.map(ProtocolDialectPropertiesImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME").varChar().map("name").add();
            Column deviceProtocolId = table.column("DEVICEPROTOCOLID").number().conversion(NUMBER2LONG).notNull().map("pluggableClassId").add();
            Column device = table.column("DEVICEID").number().conversion(NUMBER2LONG).notNull().add();
            table.column("MOD_DATE").type("DATE").conversion(DATE2DATE).map("modificationDate").add();
            Column configurationProperties = table.column("CONFIGURATIONPROPERTIESID").number().add();
            table.foreignKey("FK_DDC_PROTDIALECTPROPS_PC").on(deviceProtocolId).references(PluggableService.COMPONENTNAME, "CPC_PLUGGABLECLASS").map("deviceProtocolPluggableClass").add();
            table.foreignKey("FK_DDC_PROTDIALECTPROPS_DEV").on(device).references(DDC_DEVICE.name()).map("device").reverseMap("dialectPropertiesList").add();
            table.foreignKey("FK_DDC_PROTDIALECTPROPS_PDCP").on(configurationProperties).references(DeviceConfigurationService.COMPONENTNAME, "DTC_DIALECTCONFIGPROPERTIES").map("configurationProperties").add();
            table.primaryKey("PK_DDC_PROTOCOLDIALECTPROPS").on(id).add();
        }
    },

    DDC_COMTASKEXEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComTaskExecution> table = dataModel.addTable(name(), ComTaskExecution.class);
            table.map(ComTaskExecutionImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "number");
            Column device = table.column("DEVICE").number().notNull().add();
            Column comtask = table.column("COMTASK").number().add();
            Column comSchedule = table.column("COMSCHEDULE").number().add();
            Column nextExecutionSpecs = table.column("NEXTEXECUTIONSPECS").number().add();
            table.column("LASTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2DATE).map(ComTaskExecutionFields.LASTEXECUTIONTIMESTAMP.fieldName()).add();
            Column nextexecutiontimestamp = table.column("NEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2DATE).map(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).add();
            Column comport = table.column("COMPORT").number().add();
            table.column("MOD_DATE").type("DATE").conversion(DATE2DATE).map(ComTaskExecutionFields.MODIFICATIONDATE.fieldName()).add();
            Column obsoleteDate = table.column("OBSOLETE_DATE").type("DATE").conversion(DATE2DATE).map(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).add();
            Column priority = table.column("PRIORITY").number().conversion(NUMBER2INT).map(ComTaskExecutionFields.PLANNED_PRIORITY.fieldName()).add();
            table.column("USEDEFAULTCONNECTIONTASK").number().conversion(NUMBER2BOOLEAN).map(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName()).add();
            table.column("CURRENTRETRYCOUNT").number().conversion(NUMBER2INT).map(ComTaskExecutionFields.CURRENTRETRYCOUNT.fieldName()).add();
            table.column("PLANNEDNEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2DATE).map(ComTaskExecutionFields.PLANNEDNEXTEXECUTIONTIMESTAMP.fieldName()).add();
            table.column("EXECUTIONPRIORITY").number().conversion(NUMBER2INT).map(ComTaskExecutionFields.EXECUTION_PRIORITY.fieldName()).add();
            table.column("EXECUTIONSTART").number().conversion(NUMBERINUTCSECONDS2DATE).map(ComTaskExecutionFields.EXECUTIONSTART.fieldName()).add();
            table.column("LASTSUCCESSFULCOMPLETION").number().conversion(NUMBERINUTCSECONDS2DATE).map(ComTaskExecutionFields.LASTSUCCESSFULCOMPLETIONTIMESTAMP.fieldName()).add();
            table.column("LASTEXECUTIONFAILED").number().conversion(NUMBER2BOOLEAN).map(ComTaskExecutionFields.LASTEXECUTIONFAILED.fieldName()).add();
            Column connectionTask = table.column("CONNECTIONTASK").number().conversion(NUMBER2LONGNULLZERO).map("connectionTaskId").add();
            Column protocolDialectConfigurationProperties = table.column("PROTOCOLDIALECTCONFIGPROPS").number().add();
            table.column("IGNORENEXTEXECSPECS").number().conversion(NUMBER2BOOLEAN).notNull().map(ComTaskExecutionFields.IGNORENEXTEXECUTIONSPECSFORINBOUND.fieldName()).add();
            table.foreignKey("FK_DDC_COMTASKEXEC_COMPORT").on(comport).references(EngineModelService.COMPONENT_NAME, "MDC_COMPORT").map(ComTaskExecutionFields.COMPORT.fieldName()).add();
            table.foreignKey("FK_DDC_COMTASKEXEC_COMTASK").on(comtask).references(TaskService.COMPONENT_NAME, "CTS_COMTASK").map(ComTaskExecutionFields.COMTASK.fieldName()).add();
            table.foreignKey("FK_DDC_COMTASKEXEC_COMSCHEDULE").
                    on(comSchedule).
                    references(SchedulingService.COMPONENT_NAME, "SCH_COMSCHEDULE").
                    onDelete(CASCADE).
                    map(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMTASKEXEC_NEXTEXEC").
                    on(nextExecutionSpecs).
                    references(SchedulingService.COMPONENT_NAME, "SCH_NEXTEXECUTIONSPEC").
                    map(ComTaskExecutionFields.NEXTEXECUTIONSPEC.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMTASKEXEC_CONNECTTASK").on(connectionTask).references(DDC_CONNECTIONTASK.name()).map(ComTaskExecutionFields.CONNECTIONTASK.fieldName()).add();
            table.foreignKey("FK_DDC_COMTASKEXEC_DIALECT").on(protocolDialectConfigurationProperties).references(DeviceConfigurationService.COMPONENTNAME, "DTC_DIALECTCONFIGPROPERTIES").map(ComTaskExecutionFields.PROTOCOLDIALECTCONFIGURATIONPROPERTIES.fieldName()).add();
            table.foreignKey("FK_DDC_COMTASKEXEC_DEVICE").on(device).references(DDC_DEVICE.name()).map(ComTaskExecutionFields.DEVICE.fieldName()).add();
            table.primaryKey("PK_DDC_COMTASKEXEC").on(id).add();
            table.index("IX_DDCCOMTASKEXEC_NXTEXEC").on(nextexecutiontimestamp, priority, connectionTask, obsoleteDate).add();
        }
    },

    DDC_COMSTATISTICS {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComStatistics> table = dataModel.addTable(name(), ComStatistics.class);
            table.map(ComStatisticsImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("BYTESSENT").number().conversion(NUMBER2LONG).notNull().map("nrOfBytesSent").add();
            table.column("BYTESREAD").number().conversion(NUMBER2LONG).notNull().map("nrOfBytesReceived").add();
            table.column("PACKETSSENT").number().conversion(NUMBER2LONG).notNull().map("nrOfPacketsSent").add();
            table.column("PACKETSREAD").number().conversion(NUMBER2LONG).notNull().map("nrOfPacketsReceived").add();
            table.column("MOD_DATE").type("DATE").map("modDate").add();
            table.primaryKey("PK_DDC_COMSTATISTICS").on(id).add();
        }
    },
    DDC_COMSESSION {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComSession> table = dataModel.addTable(name(), ComSession.class);
            table.map(ComSessionImpl.class);
            Column id = table.addAutoIdColumn();
            Column connectionTask = table.column("CONNECTIONTASK").number().notNull().add();
            Column comport = table.column("COMPORT").number().notNull().add();
            Column comportPool = table.column("COMPORTPOOL").number().notNull().add();
            Column statistics = table.column("COMSTATISTICS").number().notNull().add();
            table.column("STARTDATE").number().conversion(NUMBERINUTCSECONDS2DATE).notNull().map(ComSessionImpl.Fields.START_DATE.fieldName()).add();
            table.column("STOPDATE").number().conversion(NUMBERINUTCSECONDS2DATE).notNull().map(ComSessionImpl.Fields.STOP_DATE.fieldName()).add();
            table.column("TOTALTIME").number().conversion(NUMBER2LONG).map(ComSessionImpl.Fields.TOTAL_TIME.fieldName()).add();
            table.column("CONNECTMILLIS").number().conversion(NUMBER2LONG).map(ComSessionImpl.Fields.CONNECT_MILLIS.fieldName()).add();
            table.column("TALKMILLIS").number().conversion(NUMBER2LONG).map(ComSessionImpl.Fields.TALK_MILLIS.fieldName()).add();
            table.column("STOREMILLIS").number().conversion(NUMBER2LONG).map(ComSessionImpl.Fields.STORE_MILLIS.fieldName()).add();
            table.column("SUCCESSINDICATOR").number().conversion(NUMBER2ENUM).notNull().map(ComSessionImpl.Fields.SUCCESS_INDICATOR.fieldName()).add();
            table.column("MOD_DATE").type("DATE").map(ComSessionImpl.Fields.MODIFICATION_DATE.fieldName()).add();
            table.column("TASKSUCCESSCOUNT").number().conversion(NUMBER2INT).notNull().map(ComSessionImpl.Fields.TASK_SUCCESS_COUNT.fieldName()).add();
            table.column("TASKFAILURECOUNT").number().conversion(NUMBER2INT).notNull().map(ComSessionImpl.Fields.TASK_FAILURE_COUNT.fieldName()).add();
            table.column("TASKNOTEXECUTEDCOUNT").number().conversion(NUMBER2INT).notNull().map(ComSessionImpl.Fields.TASK_NOT_EXECUTED_COUNT.fieldName()).add();
            table.column("STATUS").number().conversion(NUMBER2BOOLEAN).notNull().map(ComSessionImpl.Fields.STATUS.fieldName()).add();
            table.foreignKey("FK_DDC_COMSESSION_STATS").on(statistics).references(DDC_COMSTATISTICS.name()).map(ComSessionImpl.Fields.STATISTICS.fieldName()).add();
            table.foreignKey("FK_DDC_COMSESSION_COMPORTPOOL").on(comportPool).references("MDC", "MDC_COMPORTPOOL").map(ComSessionImpl.Fields.COMPORT_POOL.fieldName()).add();
            table.foreignKey("FK_DDC_COMSESSION_COMPORT").on(comport).references("MDC", "MDC_COMPORT").map(ComSessionImpl.Fields.COMPORT.fieldName()).add();
            table.foreignKey("FK_DDC_COMSESSION_CONNTASK").on(connectionTask).references(DDC_CONNECTIONTASK.name()).map(ComSessionImpl.Fields.CONNECTION_TASK.fieldName()).add();
            table.primaryKey("PK_DDC_COMSESSION").on(id).add();
        }
    },
    DDC_COMTASKEXECSESSION {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComTaskExecutionSession> table = dataModel.addTable(name(), ComTaskExecutionSession.class);
            table.map(ComTaskExecutionSessionImpl.class);
            Column id = table.addAutoIdColumn();
            Column device = table.column("DEVICE").number().notNull().add();
            Column session = table.column("COMSESSION").number().notNull().add();
            Column statistics = table.column("COMSTATISTICS").number().notNull().add();
            table.column("STARTDATE").number().conversion(NUMBERINUTCSECONDS2DATE).notNull().map(ComTaskExecutionSessionImpl.Fields.START_DATE.fieldName()).add();
            table.column("STOPDATE").number().notNull().conversion(NUMBERINUTCSECONDS2DATE).map(ComTaskExecutionSessionImpl.Fields.STOP_DATE.fieldName()).add();
            table.column("SUCCESSINDICATOR").number().conversion(NUMBER2ENUM).notNull().map(ComTaskExecutionSessionImpl.Fields.SUCCESS_INDICATOR.fieldName()).add();
            table.column("MOD_DATE").type("DATE").map(ComTaskExecutionSessionImpl.Fields.MODIFICATION_DATE.fieldName()).add();
            Column comTaskExecution = table.column("COMTASKEXEC").number().notNull().add();
            table.column("HIGHESTPRIOCOMPLETIONCODE").number().conversion(NUMBER2ENUM).map(ComTaskExecutionSessionImpl.Fields.HIGHEST_PRIORITY_COMPLETION_CODE.fieldName()).add();
            table.column("HIGHESTPRIOERRORDESCRIPTION").type("CLOB").conversion(CLOB2STRING).map(ComTaskExecutionSessionImpl.Fields.HIGHEST_PRIORITY_ERROR_DESCRIPTION.fieldName()).add();
            table.foreignKey("FK_DDC_COMTSKEXECSESSION_SESS").on(session).references(DDC_COMSESSION.name()).map("comSession").composition().reverseMap("comTaskExecutionSessions").add();
            table.foreignKey("FK_DDC_COMTASKSESSION_COMTASK").on(comTaskExecution).references(DDC_COMTASKEXEC.name()).map(ComTaskExecutionSessionImpl.Fields.COM_TASK_EXECUTION.fieldName()).onDelete(CASCADE).add();
            table.foreignKey("FK_DDC_COMTSKEXECSESSION_STATS").on(statistics).references(DDC_COMSTATISTICS.name()).map(ComTaskExecutionSessionImpl.Fields.STATISTICS.fieldName()).add();
            table.foreignKey("FK_DDC_COMTSKEXECSESSION_DEVIC").on(device).references(DDC_DEVICE.name()).map(ComTaskExecutionSessionImpl.Fields.DEVICE.fieldName()).add();
            table.primaryKey("PK_DDC_COMTASKEXECSESSION").on(id).add();
        }
    },
    DDC_COMTASKEXECJOURNALENTRY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComTaskExecutionJournalEntry> table = dataModel.addTable(name(), ComTaskExecutionJournalEntry.class);
            table.map(ComTaskExecutionJournalEntryImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "varchar(1)");
            Column comtaskexecsession = table.column("COMTASKEXECSESSION").number().notNull().add();
            table.column("TIMESTAMP").number().conversion(NUMBER2UTCINSTANT).notNull().map("timestamp").add();
            table.column("ERRORDESCRIPTION").type("CLOB").conversion(CLOB2STRING).map("errorDescription").add();
            table.column("COMMANDDESCRIPTION").type("CLOB").conversion(CLOB2STRING).map("commandDescription").add();
            table.column("COMPLETIONCODE").number().conversion(NUMBER2ENUM).map("completionCode").add();
            table.column("MOD_DATE").type("DATE").map("modDate").add();
            table.column("MESSAGE").type("CLOB").conversion(CLOB2STRING).map("message").add();
            table.foreignKey("FK_DDC_COMTASKJENTRY_SESSION").on(comtaskexecsession).references(DDC_COMTASKEXECSESSION.name()).map("comTaskExecutionSession").composition().reverseMap("comTaskExecutionJournalEntries").onDelete(CASCADE).add();
            table.primaryKey("PK_DDC_COMTASKJOURNALENTRY").on(id).add();
        }
    },
    DDC_COMSESSIONJOURNALENTRY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComSessionJournalEntry> table = dataModel.addTable(name(), ComSessionJournalEntry.class);
            table.map(ComSessionJournalEntryImpl.class);
            Column id = table.addAutoIdColumn();
            Column comsession = table.column("COMSESSION").number().notNull().add();
            table.column("MESSAGE").varChar(DESCRIPTION_LENGTH).notNull().map("message").add();
            table.column("TIMESTAMP").number().conversion(NUMBER2UTCINSTANT).notNull().map("timestamp").add();
            table.column("MOD_DATE").type("DATE").map("modDate").add();
            table.column("STACKTRACE").type("CLOB").conversion(CLOB2STRING).map("stackTrace").add();
            table.foreignKey("FK_DDC_COMSESSIONJENTR_SESSION").on(comsession).references(DDC_COMSESSION.name()).map("comSession").composition().reverseMap("journalEntries").add();
            table.primaryKey("PK_DDC_COMSESSIONJOURNALENTRY").on(id).add();
        }
    },
    ;

    abstract void addTo(DataModel component);

}