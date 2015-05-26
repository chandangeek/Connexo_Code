package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.ConnectionTaskFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiImpl;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionJournalEntryImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionJournalEntryImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionSessionImpl;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.TaskService;

import static com.elster.jupiter.orm.ColumnConversion.CHAR2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.CLOB2STRING;
import static com.elster.jupiter.orm.ColumnConversion.DATE2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INSTANT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONGNULLZERO;
import static com.elster.jupiter.orm.ColumnConversion.NUMBERINUTCSECONDS2INSTANT;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;

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
            table.addAuditColumns();
            table.column("NAME").varChar().notNull().map(DeviceFields.NAME.fieldName()).add();
            table.column("SERIALNUMBER").varChar().map(DeviceFields.SERIALNUMBER.fieldName()).add();
            table.column("TIMEZONE").varChar().map(DeviceFields.TIMEZONE.fieldName()).add();
            Column mRID = table.column("MRID").varChar().map(DeviceFields.MRID.fieldName()).add();
            table.column("CERTIF_YEAR").number().map("yearOfCertification").conversion(ColumnConversion.NUMBER2INT).add();
            Column deviceType = table.column("DEVICETYPE").number().notNull().add();
            Column configuration = table.column("DEVICECONFIGID").number().notNull().add();
            table.foreignKey("FK_DDC_DEVICE_DEVICECONFIG").
                    on(configuration).
                    references(DeviceConfigurationService.COMPONENTNAME, "DTC_DEVICECONFIG").
                    map(DeviceFields.DEVICECONFIGURATION.fieldName()).
                    add();
            table.foreignKey("FK_DDC_DEVICE_DEVICETYPE").
                    on(deviceType).
                    references(DeviceConfigurationService.COMPONENTNAME, "DTC_DEVICETYPE").
                    map(DeviceFields.DEVICETYPE.fieldName()).
                    add();
            table.unique("UK_DDC_DEVICE_MRID").on(mRID).add();
            table.primaryKey("PK_DDC_DEVICE").on(id).add();
        }
    },

    DDC_DEVICEPROTOCOLPROPERTY {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceProtocolProperty> table = dataModel.addTable(name(), DeviceProtocolProperty.class);
            table.map(DeviceProtocolPropertyImpl.class);
            Column deviceId = table.column("DEVICEID").number().notNull().conversion(NUMBER2LONG).add();
            Column propertySpec = table.column("PROPERTYSPEC").map("propertySpec").varChar(256).notNull().add();
            table.column("INFOVALUE").varChar().map("propertyValue").add();
            table.addAuditColumns();
            table.primaryKey("PK_DDC_DEVICEPROTOCOLPROPERTY").on(deviceId, propertySpec).add();
            table.foreignKey("FK_DDC_DEVICEPROTPROP_DEVICE")
                    .on(deviceId)
                    .references(DDC_DEVICE.name())
                    .map("device").reverseMap("deviceProperties").composition()
                    .add();
        }
    },

    DDC_LOADPROFILE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LoadProfile> table = dataModel.addTable(name(), LoadProfile.class);
            table.map(LoadProfileImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column deviceId = table.column("DEVICEID").number().notNull().add();
            table.column("LASTREADING").number().map("lastReading").conversion(ColumnConversion.NUMBER2INSTANT).add();
            Column loadprofilespecid = table.column("LOADPROFILESPECID").number().add();
            table.primaryKey("PK_DDC_LOADPROFILE").on(id).add();
            table.foreignKey("FK_DDC_LOADPROFILE_LPSPEC")
                    .on(loadprofilespecid)
                    .references(DeviceConfigurationService.COMPONENTNAME, "DTC_LOADPROFILESPEC")
                    .map("loadProfileSpec")
                    .add();
            table.foreignKey("FK_DDC_LOADPROFILE_DEVICE")
                    .on(deviceId)
                    .references(DDC_DEVICE.name())
                    .map("device").reverseMap("loadProfiles").composition()
                    .add();

        }
    },

    DDC_LOGBOOK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<LogBook> table = dataModel.addTable(name(), LogBook.class);
            table.map(LogBookImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column logBookSpecId = table.column("LOGBOOKSPECID").number().notNull().add();
            Column deviceid = table.column("DEVICEID").number().notNull().add();
            table.column("LASTLOGBOOK").number().map(LogBookImpl.FieldNames.LATEST_EVENT_OCCURRENCE_IN_METER.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("LASTLOGBOOKCREATETIME").number().map(LogBookImpl.FieldNames.LATEST_EVENT_CREATED_IN_DB.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.primaryKey("PK_DDC_LOGBOOK").on(id).add();
            table.foreignKey("FK_DDC_LOGBOOK_LOGBOOKSPEC")
                    .on(logBookSpecId)
                    .references(DeviceConfigurationService.COMPONENTNAME, "DTC_LOGBOOKSPEC")
                    .map("logBookSpec")
                    .add();
            table.foreignKey("FK_DDC_LOGBOOK_DEVICE")
                    .on(deviceid).references(DDC_DEVICE.name())
                    .map("device").reverseMap("logBooks").composition()
                    .add();
        }
    },

    DDC_CONNECTIONTASK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ConnectionTask> table = dataModel.addTable(name(), ConnectionTask.class);
            table.map(ConnectionTaskImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
//            table.addAuditColumns();
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.addModTimeColumn("MODTIME", "modTime");
            table.addUserNameColumn("USERNAME", "userName");
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            // Common columns
            Column device = table.column("DEVICE").number().notNull().add();
            Column connectionTypePluggableClass = table.column("CONNECTIONTYPEPLUGGABLECLASS").number().conversion(NUMBER2LONG).map("pluggableClassId").notNull().add();
            table.column("OBSOLETE_DATE").type("DATE").conversion(DATE2INSTANT).map(ConnectionTaskFields.OBSOLETE_DATE.fieldName()).add();
            table.column("ISDEFAULT").number().conversion(NUMBER2BOOLEAN).map(ConnectionTaskFields.IS_DEFAULT.fieldName()).add();
            table.column("STATUS").number().conversion(NUMBER2ENUM).map(ConnectionTaskFields.STATUS.fieldName()).add();
            table.column("LASTCOMMUNICATIONSTART").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ConnectionTaskFields.LAST_COMMUNICATION_START.fieldName()).add();
            table.column("LASTSUCCESSFULCOMMUNICATIONEND").conversion(NUMBERINUTCSECONDS2INSTANT).number().map(ConnectionTaskFields.LAST_SUCCESSFUL_COMMUNICATION_END.fieldName()).add();
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
            table.column("NEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ConnectionTaskFields.NEXT_EXECUTION_TIMESTAMP.fieldName()).add();
            table.column("PLANNEDNEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ConnectionTaskFields.PLANNED_NEXT_EXECUTION_TIMESTAMP.fieldName()).add();
            table.column("CONNECTIONSTRATEGY").number().conversion(NUMBER2ENUM).map(ConnectionTaskFields.CONNECTION_STRATEGY.fieldName()).add();
            table.column("PRIORITY").number().conversion(NUMBER2INT).map(ConnectionTaskFields.PRIORITY.fieldName()).add();
            table.column("SIMULTANEOUSCONNECTIONS").number().conversion(NUMBER2BOOLEAN).map(ConnectionTaskFields.ALLOW_SIMULTANEOUS_CONNECTIONS.fieldName()).add();
            Column initiator = table.column("INITIATOR").number().add();
            // InboundConnectionTaskImpl columns: none at this moment
            // ConnectionInitiationTaskImpl columns: none at this moment
            table.primaryKey("PK_DDC_CONNECTIONTASK").on(id).add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_DEVICE").
                    on(device).
                    references(DDC_DEVICE.name()).
                    map(ConnectionTaskFields.DEVICE.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_CLASS").
                    on(connectionTypePluggableClass).
                    references(PluggableService.COMPONENTNAME, "CPC_PLUGGABLECLASS").
                    map("pluggableClass").add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_CPP").
                    on(comPortPool).
                    references(EngineConfigurationService.COMPONENT_NAME, "MDC_COMPORTPOOL").
                    map(ConnectionTaskFields.COM_PORT_POOL.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_COMSRVER").
                    on(comServer).
                    references(EngineConfigurationService.COMPONENT_NAME, "MDC_COMSERVER").
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
            table.addAuditColumns();
            table.column("NAME").varChar().map("name").add();
            Column deviceProtocolId = table.column("DEVICEPROTOCOLID").number().conversion(NUMBER2LONG).notNull().map("pluggableClassId").add();
            Column device = table.column("DEVICEID").number().conversion(NUMBER2LONG).notNull().add();
            Column configurationProperties = table.column("CONFIGURATIONPROPERTIESID").number().add();
            table.primaryKey("PK_DDC_PROTOCOLDIALECTPROPS").on(id).add();
            table.foreignKey("FK_DDC_PROTDIALECTPROPS_PC")
                    .on(deviceProtocolId)
                    .references(PluggableService.COMPONENTNAME, "CPC_PLUGGABLECLASS")
                    .map("deviceProtocolPluggableClass")
                    .add();
            table.foreignKey("FK_DDC_PROTDIALECTPROPS_DEV")
                    .on(device)
                    .references(DDC_DEVICE.name())
                    .map("device").reverseMap("dialectPropertiesList")
                    .add();
            table.foreignKey("FK_DDC_PROTDIALECTPROPS_PDCP")
                    .on(configurationProperties)
                    .references(DeviceConfigurationService.COMPONENTNAME, "DTC_DIALECTCONFIGPROPERTIES")
                    .map("configurationProperties")
                    .add();
        }
    },

    DDC_COMTASKEXEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComTaskExecution> table = dataModel.addTable(name(), ComTaskExecution.class);
            table.map(ComTaskExecutionImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
//            table.addAuditColumns();
            table.addCreateTimeColumn("CREATETIME", "createTime");
            table.addModTimeColumn("MODTIME", "modTime");
            table.addUserNameColumn("USERNAME", "userName");
            table.addDiscriminatorColumn("DISCRIMINATOR", "number");
            Column device = table.column("DEVICE").number().notNull().add();
            Column comTask = table.column("COMTASK").number().add();
            Column comSchedule = table.column("COMSCHEDULE").number().add();
            Column nextExecutionSpecs = table.column("NEXTEXECUTIONSPECS").number().add();
            table.column("LASTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ComTaskExecutionFields.LASTEXECUTIONTIMESTAMP.fieldName()).add();
            Column nextExecutionTimestamp = table.column("NEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).add();
            Column comPort = table.column("COMPORT").number().add();
            Column obsoleteDate = table.column("OBSOLETE_DATE").type("DATE").conversion(DATE2INSTANT).map(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).add();
            Column priority = table.column("PRIORITY").number().conversion(NUMBER2INT).map(ComTaskExecutionFields.PLANNED_PRIORITY.fieldName()).add();
            table.column("USEDEFAULTCONNECTIONTASK").number().conversion(NUMBER2BOOLEAN).map(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName()).add();
            table.column("CURRENTRETRYCOUNT").number().conversion(NUMBER2INT).map(ComTaskExecutionFields.CURRENTRETRYCOUNT.fieldName()).add();
            table.column("PLANNEDNEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ComTaskExecutionFields.PLANNEDNEXTEXECUTIONTIMESTAMP.fieldName()).add();
            table.column("EXECUTIONPRIORITY").number().conversion(NUMBER2INT).map(ComTaskExecutionFields.EXECUTION_PRIORITY.fieldName()).add();
            table.column("EXECUTIONSTART").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ComTaskExecutionFields.EXECUTIONSTART.fieldName()).add();
            table.column("LASTSUCCESSFULCOMPLETION").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ComTaskExecutionFields.LASTSUCCESSFULCOMPLETIONTIMESTAMP.fieldName()).add();
            table.column("LASTEXECUTIONFAILED").number().conversion(NUMBER2BOOLEAN).map(ComTaskExecutionFields.LASTEXECUTIONFAILED.fieldName()).add();
            Column connectionTask = table.column("CONNECTIONTASK").number().conversion(NUMBER2LONGNULLZERO).map("connectionTaskId").add();
            Column protocolDialectConfigurationProperties = table.column("PROTOCOLDIALECTCONFIGPROPS").number().add();
            table.column("IGNORENEXTEXECSPECS").number().conversion(NUMBER2BOOLEAN).notNull().map(ComTaskExecutionFields.IGNORENEXTEXECUTIONSPECSFORINBOUND.fieldName()).add();
            table.primaryKey("PK_DDC_COMTASKEXEC").on(id).add();
            table.foreignKey("FK_DDC_COMTASKEXEC_COMPORT")
                    .on(comPort)
                    .references(EngineConfigurationService.COMPONENT_NAME, "MDC_COMPORT")
                    .map(ComTaskExecutionFields.COMPORT.fieldName())
                    .add();
            table.foreignKey("FK_DDC_COMTASKEXEC_COMTASK")
                    .on(comTask)
                    .references(TaskService.COMPONENT_NAME, "CTS_COMTASK")
                    .map(ComTaskExecutionFields.COMTASK.fieldName())
                    .add();
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
            table.foreignKey("FK_DDC_COMTASKEXEC_CONNECTTASK")
                    .on(connectionTask)
                    .references(DDC_CONNECTIONTASK.name())
                    .map(ComTaskExecutionFields.CONNECTIONTASK.fieldName())
                    .add();
            table.foreignKey("FK_DDC_COMTASKEXEC_DIALECT")
                    .on(protocolDialectConfigurationProperties)
                    .references(DeviceConfigurationService.COMPONENTNAME, "DTC_DIALECTCONFIGPROPERTIES")
                    .map(ComTaskExecutionFields.PROTOCOLDIALECTCONFIGURATIONPROPERTIES.fieldName())
                    .add();
            table.foreignKey("FK_DDC_COMTASKEXEC_DEVICE")
                    .on(device).references(DDC_DEVICE.name())
                    .map(ComTaskExecutionFields.DEVICE.fieldName())
                    .add();
            table.index("IX_DDCCOMTASKEXEC_NXTEXEC").on(nextExecutionTimestamp, priority, connectionTask, obsoleteDate, comPort).add();
        }
    },

    DDC_COMSESSION {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComSession> table = dataModel.addTable(name(), ComSession.class);
            table.map(ComSessionImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column connectionTask = table.column("CONNECTIONTASK").number().notNull().add();
            Column comport = table.column("COMPORT").number().notNull().add();
            Column comportPool = table.column("COMPORTPOOL").number().notNull().add();
            table.column("STARTDATE").number().conversion(NUMBER2INSTANT).notNull().map(ComSessionImpl.Fields.START_DATE.fieldName()).add();
            table.column("STOPDATE").number().conversion(NUMBER2INSTANT).notNull().map(ComSessionImpl.Fields.STOP_DATE.fieldName()).add();
            table.column("TOTALMILLIS").number().conversion(NUMBER2LONG).map(ComSessionImpl.Fields.TOTAL_TIME.fieldName()).add();
            table.column("CONNECTMILLIS").number().conversion(NUMBER2LONG).map(ComSessionImpl.Fields.CONNECT_MILLIS.fieldName()).add();
            table.column("TALKMILLIS").number().conversion(NUMBER2LONG).map(ComSessionImpl.Fields.TALK_MILLIS.fieldName()).add();
            table.column("STOREMILLIS").number().conversion(NUMBER2LONG).map(ComSessionImpl.Fields.STORE_MILLIS.fieldName()).add();
            table.column("SUCCESSINDICATOR").number().conversion(NUMBER2ENUM).notNull().map(ComSessionImpl.Fields.SUCCESS_INDICATOR.fieldName()).add();
            table.column("BYTESSENT").number().conversion(NUMBER2LONG).notNull().map(ComSessionImpl.Fields.NUMBER_OF_BYTES_SENT.fieldName()).add();
            table.column("BYTESREAD").number().conversion(NUMBER2LONG).notNull().map(ComSessionImpl.Fields.NUMBER_OF_BYTES_READ.fieldName()).add();
            table.column("PACKETSSENT").number().conversion(NUMBER2LONG).notNull().map(ComSessionImpl.Fields.NUMBER_OF_PACKETS_SENT.fieldName()).add();
            table.column("PACKETSREAD").number().conversion(NUMBER2LONG).notNull().map(ComSessionImpl.Fields.NUMBER_OF_PACKETS_READ.fieldName()).add();
            table.column("TASKSUCCESSCOUNT").number().conversion(NUMBER2INT).notNull().map(ComSessionImpl.Fields.TASK_SUCCESS_COUNT.fieldName()).add();
            table.column("TASKFAILURECOUNT").number().conversion(NUMBER2INT).notNull().map(ComSessionImpl.Fields.TASK_FAILURE_COUNT.fieldName()).add();
            table.column("TASKNOTEXECUTEDCOUNT").number().conversion(NUMBER2INT).notNull().map(ComSessionImpl.Fields.TASK_NOT_EXECUTED_COUNT.fieldName()).add();
            table.column("STATUS").number().conversion(NUMBER2BOOLEAN).notNull().map(ComSessionImpl.Fields.STATUS.fieldName()).add();
            table.foreignKey("FK_DDC_COMSESSION_COMPORTPOOL").
                    on(comportPool).
                    references(EngineConfigurationService.COMPONENT_NAME, "MDC_COMPORTPOOL").
                    onDelete(CASCADE).
                    map(ComSessionImpl.Fields.COMPORT_POOL.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMSESSION_COMPORT").
                    on(comport).
                    references(EngineConfigurationService.COMPONENT_NAME, "MDC_COMPORT").
                    onDelete(CASCADE).
                    map(ComSessionImpl.Fields.COMPORT.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMSESSION_CONNTASK").
                    on(connectionTask).
                    references(DDC_CONNECTIONTASK.name()).
                    onDelete(CASCADE).
                    map(ComSessionImpl.Fields.CONNECTION_TASK.fieldName()).
                    add();
            table.primaryKey("PK_DDC_COMSESSION").on(id).add();
        }
    },
    ADD_LAST_SESSION_TO_CONNECTION_TASK {
        @Override
        void addTo(DataModel dataModel) {
            Table<?> table = dataModel.getTable(DDC_CONNECTIONTASK.name());
            Column lastSession = table.column("LASTSESSION").number().add();
            table.column("LASTSESSIONSUCCESSINDICATOR").number().conversion(NUMBER2ENUM).map(ConnectionTaskFields.LAST_SESSION_SUCCESS_INDICATOR.fieldName()).add();
            table.column("LASTSESSIONSTATUS").number().conversion(NUMBER2BOOLEAN).map(ConnectionTaskFields.LAST_SESSION_STATUS.fieldName()).add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_LASTCS").
                    on(lastSession).
                    references(DDC_COMSESSION.name()).
                    map(ConnectionTaskFields.LAST_SESSION.fieldName()).
                    add();
        }
    },
    DDC_COMTASKEXECSESSION {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComTaskExecutionSession> table = dataModel.addTable(name(), ComTaskExecutionSession.class);
            table.map(ComTaskExecutionSessionImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column device = table.column("DEVICE").number().notNull().add();
            Column session = table.column("COMSESSION").number().notNull().add();
            table.column("STARTDATE").number().conversion(NUMBER2INSTANT).notNull().map(ComTaskExecutionSessionImpl.Fields.START_DATE.fieldName()).add();
            table.column("STOPDATE").number().notNull().conversion(NUMBER2INSTANT).map(ComTaskExecutionSessionImpl.Fields.STOP_DATE.fieldName()).add();
            Column successIndicator = table.column("SUCCESSINDICATOR").number().conversion(NUMBER2ENUM).notNull().map(ComTaskExecutionSessionImpl.Fields.SUCCESS_INDICATOR.fieldName()).add();
            table.column("BYTESSENT").number().conversion(NUMBER2LONG).notNull().map(ComTaskExecutionSessionImpl.Fields.NUMBER_OF_BYTES_SENT.fieldName()).add();
            table.column("BYTESREAD").number().conversion(NUMBER2LONG).notNull().map(ComTaskExecutionSessionImpl.Fields.NUMBER_OF_BYTES_READ.fieldName()).add();
            table.column("PACKETSSENT").number().conversion(NUMBER2LONG).notNull().map(ComTaskExecutionSessionImpl.Fields.NUMBER_OF_PACKETS_SENT.fieldName()).add();
            table.column("PACKETSREAD").number().conversion(NUMBER2LONG).notNull().map(ComTaskExecutionSessionImpl.Fields.NUMBER_OF_PACKETS_READ.fieldName()).add();
            Column comTaskExecution = table.column("COMTASKEXEC").number().notNull().add();
            Column comTask = table.column("COMTASK").number().notNull().add();
            table.column("HIGHESTPRIOCOMPLETIONCODE").number().conversion(NUMBER2ENUM).map(ComTaskExecutionSessionImpl.Fields.HIGHEST_PRIORITY_COMPLETION_CODE.fieldName()).add();
            table.column("HIGHESTPRIOERRORDESCRIPTION").type("CLOB").conversion(CLOB2STRING).map(ComTaskExecutionSessionImpl.Fields.HIGHEST_PRIORITY_ERROR_DESCRIPTION.fieldName()).add();
            table.primaryKey("PK_DDC_COMTASKEXECSESSION").on(id).add();
            table.foreignKey("FK_DDC_COMTSKEXECSESSION_SESS").
                    on(session).
                    references(DDC_COMSESSION.name()).
                    onDelete(CASCADE).
                    map("comSession").
                    composition().
                    reverseMap("comTaskExecutionSessions").add();
            table.foreignKey("FK_DDC_COMTASKSESSION_CTEXEC").
                    on(comTaskExecution).
                    references(DDC_COMTASKEXEC.name()).
                    onDelete(CASCADE).
                    map(ComTaskExecutionSessionImpl.Fields.COM_TASK_EXECUTION.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMTASKSESSION_COMTASK").
                    on(comTask).
                    references(TaskService.COMPONENT_NAME, "CTS_COMTASK").
                    onDelete(CASCADE).
                    map(ComTaskExecutionSessionImpl.Fields.COM_TASK.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMTSKEXECSESSION_DEVIC").
                    on(device).
                    references(DDC_DEVICE.name()).
                    onDelete(CASCADE).
                    map(ComTaskExecutionSessionImpl.Fields.DEVICE.fieldName()).
                    add();
            table.index("DDC_CTES_CS_SUCCESS").on(session, successIndicator).compress(1).add();
        }
    },
    ADD_LAST_SESSION_TO_COM_TASK_EXECUTION {
        @Override
        void addTo(DataModel dataModel) {
            Table<?> table = dataModel.getTable(DDC_COMTASKEXEC.name());
            Column lastSession = table.column("LASTSESSION").number().add();
            table.column("LASTSESS_HIGHESTPRIOCOMPLCODE").number().conversion(NUMBER2ENUM).map(ComTaskExecutionFields.LAST_SESSION_HIGHEST_PRIORITY_COMPLETION_CODE.fieldName()).add();
            table.column("LASTSESS_SUCCESSINDICATOR").number().conversion(NUMBER2ENUM).map(ComTaskExecutionFields.LAST_SESSION_SUCCESSINDICATOR.fieldName()).add();
            table.foreignKey("FK_DDC_COMTASKEXEC_LASTSESS").
                    on(lastSession).
                    references(DDC_COMTASKEXECSESSION.name()).
                    map(ComTaskExecutionFields.LAST_SESSION.fieldName()).
                    add();
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
            table.column("TIMESTAMP").number().conversion(NUMBER2INSTANT).notNull().map("timestamp").add();
            table.column("ERRORDESCRIPTION").type("CLOB").conversion(CLOB2STRING).map("errorDescription").add();
            table.column("COMMANDDESCRIPTION").type("CLOB").conversion(CLOB2STRING).map("commandDescription").add();
            table.column("COMPLETIONCODE").number().conversion(NUMBER2ENUM).map("completionCode").add();
            table.column("MOD_DATE").type("DATE").conversion(DATE2INSTANT).map("modDate").add();
            table.column("MESSAGE").type("CLOB").conversion(CLOB2STRING).map("message").add();
            table.column("LOGLEVEL").number().notNull().conversion(NUMBER2ENUM).map("logLevel").add();
            table.foreignKey("FK_DDC_COMTASKJENTRY_SESSION").
                    on(comtaskexecsession).
                    references(DDC_COMTASKEXECSESSION.name()).
                    onDelete(CASCADE).
                    map("comTaskExecutionSession").
                    composition().
                    reverseMap("comTaskExecutionJournalEntries").
                    add();
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
            table.column("LOGLEVEL").number().conversion(NUMBER2ENUM).map("logLevel").add();
            table.column("MESSAGE").type("CLOB").conversion(CLOB2STRING).notNull().map("message").add();
            table.column("TIMESTAMP").number().conversion(NUMBER2INSTANT).notNull().map("timestamp").add();
            table.column("MOD_DATE").type("DATE").conversion(DATE2INSTANT).map("modDate").add();
            table.column("STACKTRACE").type("CLOB").conversion(CLOB2STRING).map("stackTrace").add();
            table.foreignKey("FK_DDC_COMSESSIONJENTR_SESSION").
                    on(comsession).
                    references(DDC_COMSESSION.name()).
                    onDelete(CASCADE).
                    map("comSession").
                    composition().
                    reverseMap("journalEntries").
                    add();
            table.primaryKey("PK_DDC_COMSESSIONJOURNALENTRY").on(id).add();
        }
    },

    DDC_DATA_COLLECTION_KPI {
        @Override
        void addTo(DataModel dataModel) {
            Table<DataCollectionKpi> table = dataModel.addTable(name(), DataCollectionKpi.class);
            table.map(DataCollectionKpiImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.column("DISPLAYRANGEVALUE").number().conversion(ColumnConversion.NUMBER2INT).map(DataCollectionKpiImpl.Fields.DISPLAY_PERIOD.fieldName()+".count").notNull().add();
            table.column("DISPLAYRANGEUNIT").number().conversion(ColumnConversion.NUMBER2INT).map(DataCollectionKpiImpl.Fields.DISPLAY_PERIOD.fieldName()+".timeUnitCode").notNull().add();
            Column endDeviceGroup = table.column("ENDDEVICEGROUP").number().notNull().add();
            Column connectionKpi = table.column("CONNECTIONKPI").number().add();
            Column comTaskExecKpi = table.column("COMMUNICATIONKPI").number().add();
            Column connectionKpiTask = table.column("CONNECTIONKPI_TASK").number().add();
            Column communicationKpiTask = table.column("COMMUNICATIONKPI_TASK").number().add();
            table.primaryKey("PK_DDC_DATA_COLLECTION_KPI").on(id).add();
            table.foreignKey("FK_DDC_ENDDEVICEGROUP").
                    on(endDeviceGroup).
                    references(MeteringGroupsService.COMPONENTNAME, "MTG_ED_GROUP").
                    map(DataCollectionKpiImpl.Fields.END_DEVICE_GROUP.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONNECTIONKPI").
                    on(connectionKpi).
                    references(KpiService.COMPONENT_NAME, "KPI_KPI").
                    map(DataCollectionKpiImpl.Fields.CONNECTION_KPI.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMTASKEXECKPI").
                    on(comTaskExecKpi).
                    references(KpiService.COMPONENT_NAME, "KPI_KPI").
                    map(DataCollectionKpiImpl.Fields.COMMUNICATION_KPI.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONN_KPI_TASK").
                    on(connectionKpiTask).
                    references(com.elster.jupiter.tasks.TaskService.COMPONENTNAME, "TSK_RECURRENT_TASK").
                    map(DataCollectionKpiImpl.Fields.CONNECTION_RECURRENT_TASK.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMM_KPI_TASK").
                    on(communicationKpiTask).
                    references(com.elster.jupiter.tasks.TaskService.COMPONENTNAME, "TSK_RECURRENT_TASK").
                    map(DataCollectionKpiImpl.Fields.COMMUNICATION_RECURRENT_TASK.fieldName()).
                    add();
        }
    },

    DDC_DEVICEMESSAGE {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceMessage> table = dataModel.addTable(name(), DeviceMessage.class);
            table.map(DeviceMessageImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column device = table.column("DEVICEID").number().conversion(NUMBER2LONG).notNull().add();
            table.column("DEVICEMESSAGEID").number().conversion(NUMBER2LONG).map(DeviceMessageImpl.Fields.DEVICEMESSAGEID.fieldName()).notNull().add();
            table.column("STATUS").number().conversion(NUMBER2ENUM).map(DeviceMessageImpl.Fields.DEVICEMESSAGESTATUS.fieldName()).notNull().add();
            table.column("TRACKINGID").varChar(Table.DESCRIPTION_LENGTH).map(DeviceMessageImpl.Fields.TRACKINGID.fieldName()).add();
            table.column("PROTOCOLINFO").varChar(Table.DESCRIPTION_LENGTH).map(DeviceMessageImpl.Fields.PROTOCOLINFO.fieldName()).add();
            table.column("RELEASEDATE").number().map(DeviceMessageImpl.Fields.RELEASEDATE.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("SENTDATE").number().map(DeviceMessageImpl.Fields.SENTDATE.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.primaryKey("PK_DDC_DEVICEMESSAGE").on(id).add();
            table.foreignKey("FK_DDC_DEVMESSAGE_DEV")
                    .on(device).references(DDC_DEVICE.name())
                    .map("device").reverseMap("deviceMessages")
                    .add();
        }
    },

    DDC_DEVICEMESSAGEATTR {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceMessageAttribute> table = dataModel.addTable(name(), DeviceMessageAttribute.class);
            table.map(DeviceMessageAttributeImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column deviceMessage = table.column("DEVICEMESSAGE").number().conversion(NUMBER2LONG).notNull().add();
            Column name = table.column("NAME").varChar().map("name").notNull().add();
            table.column("VALUE").varChar().map("stringValue").notNull().add();

            table.primaryKey("PK_DDC_DEVMESATTR").on(id).add();
            table.foreignKey("FK_DDC_DEVMESATTR_DEV")
                    .on(deviceMessage)
                    .references(DDC_DEVICEMESSAGE.name())
                    .map("deviceMessage")
                    .composition()
                    .reverseMap(DeviceMessageImpl.Fields.DEVICEMESSAGEATTRIBUTES.fieldName())
                    .add();
            table.unique("UK_DDC_DEVMESATTR_NAME").on(deviceMessage, name).add();
        }
    },

    DDC_DEVICEESTACTIVATION {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceEstimation> table = dataModel.addTable(name(), DeviceEstimation.class);
            table.map(DeviceEstimationImpl.class);
            Column device = table.column("DEVICE").number().conversion(NUMBER2LONG).notNull().add();
            table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map(DeviceEstimationImpl.Fields.ACTIVE.fieldName()).add();
            table.addAuditColumns();

            table.primaryKey("PK_DDC_DEVESTACTIVATION").on(device).add();
            table.foreignKey("FK_DDC_DEVESTACTIVATION_DEVICE")
                 .on(device)
                 .references(DDC_DEVICE.name())
                 .map(DeviceEstimationImpl.Fields.DEVICE.fieldName())
                 .reverseMap("deviceEstimation")
                 .onDelete(CASCADE)
                 .add();
        }
    },

    DDC_DEVICEESTRULESETACTIVATION {
        @Override
        void addTo(DataModel dataModel) {
            Table<DeviceEstimationRuleSetActivation> table = dataModel.addTable(name(), DeviceEstimationRuleSetActivation.class);
            table.map(DeviceEstimationRuleSetActivationImpl.class);

            Column estimationActivationColumn = table.column("ESTIMATIONACTIVATION").number().conversion(NUMBER2LONG).notNull().add();
            Column estimationRuleSetColumn = table.column("ESTIMATIONRULESET").number().conversion(NUMBER2LONG).notNull().add();
            table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map(DeviceEstimationRuleSetActivationImpl.Fields.ACTIVE.fieldName()).add();
            table.addAuditColumns();

            table.primaryKey("PK_DDC_DEVICEESTRULESETACT").on(estimationActivationColumn, estimationRuleSetColumn).add();
            table.foreignKey("FK_DDC_ESTRSACTIVATION_RULESET")
                 .on(estimationRuleSetColumn)
                 .references(EstimationService.COMPONENTNAME, "EST_ESTIMATIONRULESET")
                 .map(DeviceEstimationRuleSetActivationImpl.Fields.ESTIMATIONRULESET.fieldName())
                 .add();
            table.foreignKey("FK_DDC_ESTRSACTIVATION_ESTACT")
                 .on(estimationActivationColumn)
                 .references(DDC_DEVICEESTACTIVATION.name())
                 .map(DeviceEstimationRuleSetActivationImpl.Fields.ESTIMATIONACTIVATION.fieldName())
                 .reverseMap(DeviceEstimationImpl.Fields.ESTRULESETACTIVATIONS.fieldName())
                 .composition()
                 .onDelete(CASCADE)
                 .add();
        }
    },

    ;

    abstract void addTo(DataModel component);

}