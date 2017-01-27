package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Encrypter;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.tasks.RecurrentTask;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.PassiveCalendar;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeInAction;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeInActionImpl;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeRequest;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeRequestImpl;
import com.energyict.mdc.device.data.impl.kpi.DataCollectionKpiImpl;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionTriggerImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionJournalEntryImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionJournalEntryImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionSessionImpl;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionTrigger;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.util.List;

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
import static com.elster.jupiter.orm.Table.NAME_LENGTH;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Version.version;

/**
 * Models the database tables that hold the data of the
 * entities that are managed by this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-07 (14:28)
 */
public enum TableSpecs {

    DDC_BATCH {
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<Batch> table = dataModel.addTable(name(), Batch.class);
            table.map(BatchImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column nameColumn = table.column("NAME").varChar(NAME_LENGTH).notNull().map(BatchImpl.Fields.BATCH_NAME.fieldName()).add();
            table.addAuditColumns();
            table.primaryKey("DDC_PK_BATCH").on(idColumn).add();
            table.unique("DDC_U_BATCH_NAME").on(nameColumn).add();
        }
    },
    DDC_DEVICE {
        @Override
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<Device> table = dataModel.addTable(name(), Device.class).alsoReferredToAs(BaseDevice.class);
            table.map(DeviceImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.setJournalTableName("DDC_DEVICEJRNL").since(version(10, 2));
            Column name = table.column("NAME").varChar().notNull().map(DeviceFields.NAME.fieldName()).add();
            table.column("SERIALNUMBER").varChar().map(DeviceFields.SERIALNUMBER.fieldName()).add();
            table.column("TIMEZONE").varChar().map(DeviceFields.TIMEZONE.fieldName()).add();
            Column mRID_10_2 = table.column("MRID").varChar(SHORT_DESCRIPTION_LENGTH).upTo(version(10, 2, 1)).add();
            Column mRID = table.column("MRID").varChar().notNull().map(DeviceFields.MRID.fieldName()).since(version(10, 2, 1)).previously(mRID_10_2).add();
            table.column("CERTIF_YEAR").number().map("yearOfCertification").conversion(ColumnConversion.NUMBER2INT).add();
            Column deviceType = table.column("DEVICETYPE").number().notNull().add();
            Column configuration = table.column("DEVICECONFIGID").number().notNull().add();
            Column meterId = table.column("METERID").number().since(version(10, 2)).add();
            Column batchId = table.column("BATCH_ID").number().since(version(10, 2)).add();
            table.column("ESTIMATION_ACTIVE").bool().map("estimationActive").since(version(10, 2)).installValue("'N'").add();
            table.foreignKey("FK_DDC_DEVICE_DEVICECONFIG")
                    .on(configuration)
                    .references(DeviceConfiguration.class)
                    .map(DeviceFields.DEVICECONFIGURATION.fieldName(), DeviceType.class)
                    .add();
            table.foreignKey("FK_DDC_DEVICE_DEVICETYPE")
                    .on(deviceType)
                    .references(DeviceType.class)
                    .map(DeviceFields.DEVICETYPE.fieldName())
                    .add();
            table.foreignKey("FK_DDC_DEVICE_ENDDEVICE")
                    .on(meterId)
                    .references(EndDevice.class)
                    .map(DeviceFields.METER.fieldName())
                    .since(version(10, 2))
                    .add();
            table.foreignKey("FK_DDC_DEVICE_BATCH")
                    .on(batchId)
                    .references(Batch.class)
                    .map(DeviceFields.BATCH.fieldName())
                    .since(version(10, 2))
                    .add();

            table.unique("UK_DDC_DEVICE_MRID").on(mRID).add();
            table.unique("UK_DDC_DEVICE_NAME").on(name).since(version(10, 2, 1)).add();
            table.primaryKey("PK_DDC_DEVICE").on(id).add();
        }
    },

    DDC_DEVICEPROTOCOLPROPERTY {
        @Override
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<DeviceProtocolProperty> table = dataModel.addTable(name(), DeviceProtocolProperty.class);
            table.map(DeviceProtocolPropertyImpl.class);
            Column deviceId = table.column("DEVICEID").number().notNull().conversion(NUMBER2LONG).add();
            Column propertySpec = table.column("PROPERTYSPEC").map("propertyName").varChar().notNull().add();
            table.column("INFOVALUE").varChar().map("propertyValue").add();
            table.addAuditColumns();
            table.setJournalTableName("DDC_DEV_PROTOCOL_PROP_JRNL").since(version(10, 2));
            table.primaryKey("PK_DDC_DEVICEPROTOCOLPROP").on(deviceId, propertySpec).add();
            table.foreignKey("FK_DDC_DEVICEPROTPROP_DEVICE")
                    .on(deviceId)
                    .references(DDC_DEVICE.name())
                    .map("device")
                    .reverseMap("deviceProperties")
                    .composition()
                    .add();
        }
    },

    DDC_LOADPROFILE {
        @Override
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<LoadProfile> table = dataModel.addTable(name(), LoadProfile.class).alsoReferredToAs(BaseLoadProfile.class);
            table.map(LoadProfileImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column deviceId = table.column("DEVICEID").number().notNull().add();
            table.column("LASTREADING").number().map("lastReading").conversion(ColumnConversion.NUMBER2INSTANT).add();
            Column loadProfileSpec = table.column("LOADPROFILESPECID").number().add();
            table.primaryKey("PK_DDC_LOADPROFILE").on(id).add();
            table.foreignKey("FK_DDC_LOADPROFILE_LPSPEC")
                    .on(loadProfileSpec)
                    .references(LoadProfileSpec.class)
                    .map("loadProfileSpec")
                    .add();
            table.foreignKey("FK_DDC_LOADPROFILE_DEVICE")
                    .on(deviceId)
                    .references(DDC_DEVICE.name())
                    .map("device")
                    .reverseMap("loadProfiles")
                    .composition()
                    .add();

        }
    },

    DDC_LOGBOOK {
        @Override
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<LogBook> table = dataModel.addTable(name(), LogBook.class);
            table.map(LogBookImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column logBookSpec = table.column("LOGBOOKSPECID").number().notNull().add();
            Column deviceid = table.column("DEVICEID").number().notNull().add();
            table.column("LASTLOGBOOK").number().map(LogBookImpl.FieldNames.LATEST_EVENT_OCCURRENCE_IN_METER.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("LASTLOGBOOKCREATETIME").number().map(LogBookImpl.FieldNames.LATEST_EVENT_CREATED_IN_DB.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.primaryKey("PK_DDC_LOGBOOK").on(id).add();
            table.foreignKey("FK_DDC_LOGBOOK_LOGBOOKSPEC")
                    .on(logBookSpec)
                    .references(LogBookSpec.class)
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
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<ConnectionTask> table = dataModel.addTable(name(), ConnectionTask.class).alsoReferredToAs(ConnectionProvider.class);
            table.map(ConnectionTaskImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
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
            table.column("SIMULTANEOUSCONNECTIONS").number().conversion(NUMBER2INT).map(ConnectionTaskFields.ALLOW_SIMULTANEOUS_CONNECTIONS.fieldName()).add();
            Column initiator = table.column("INITIATOR").number().add();
            // InboundConnectionTaskImpl columns: none at this moment
            // ConnectionInitiationTaskImpl columns: none at this moment
            table.primaryKey("PK_DDC_CONNECTIONTASK").on(id).add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_DEVICE")
                    .on(device)
                    .references(DDC_DEVICE.name())
                    .map(ConnectionTaskFields.DEVICE.fieldName())
                    .reverseMap("connectionTasks").composition()
                    .add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_CLASS").
                    on(connectionTypePluggableClass).
                    references(PluggableClass.class).
                    map("pluggableClass").add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_CPP").
                    on(comPortPool).
                    references(ComPortPool.class).
                    map(ConnectionTaskFields.COM_PORT_POOL.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_COMSRVER").
                    on(comServer).
                    references(ComServer.class).
                    map(ConnectionTaskFields.COM_SERVER.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_INITIATR").
                    on(initiator).
                    references(DDC_CONNECTIONTASK.name()).
                    map("initiationTask").
                    add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_NEXTEXEC").
                    on(nextExecutionSpecs).
                    references(NextExecutionSpecs.class).
                    map(ConnectionTaskFields.NEXT_EXECUTION_SPECS.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONNECTIONTASK_PARTIAL").
                    on(partialConnectionTask).
                    references(PartialConnectionTask.class).
                    map(ConnectionTaskFields.PARTIAL_CONNECTION_TASK.fieldName()).
                    add();
        }
    },

    DDC_PROTOCOLDIALECTPROPS {
        @Override
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<ProtocolDialectProperties> table = dataModel.addTable(name(), ProtocolDialectProperties.class).alsoReferredToAs(DeviceProtocolDialectPropertyProvider.class);
            table.map(ProtocolDialectPropertiesImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns().forEach(column -> column.upTo(version(10, 2)));
            table.column("NAME").varChar().map("name").add();
            Column deviceProtocolId = table.column("DEVICEPROTOCOLID").number().conversion(NUMBER2LONG).notNull().map("pluggableClassId").add();
            Column device = table.column("DEVICEID").number().conversion(NUMBER2LONG).notNull().add();
            Column configurationProperties = table.column("CONFIGURATIONPROPERTIESID").number().add();
            table.primaryKey("PK_DDC_PROTOCOLDIALECTPROPS").on(id).add();
            table.foreignKey("FK_DDC_PROTDIALECTPROPS_PC")
                    .on(deviceProtocolId)
                    .references(PluggableClass.class)
                    .map("deviceProtocolPluggableClass")
                    .add();
            table.foreignKey("FK_DDC_PROTDIALECTPROPS_DEV")
                    .on(device)
                    .references(DDC_DEVICE.name())
                    .map("device").reverseMap("dialectPropertiesList")
                    .add();
            table.foreignKey("FK_DDC_PROTDIALECTPROPS_PDCP")
                    .on(configurationProperties)
                    .references(ProtocolDialectConfigurationProperties.class)
                    .map("configurationProperties")
                    .add();
        }
    },

    DDC_COMTASKEXEC {
        @Override
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<ComTaskExecution> table = dataModel.addTable(name(), ComTaskExecution.class);
            table.map(ComTaskExecutionImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.column("DISCRIMINATOR").number().conversion(NUMBER2ENUM).map(ComTaskExecutionFields.COMTASKEXECTYPE.fieldName()).notNull().add();
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
            table.column("ONHOLD").number().conversion(NUMBER2BOOLEAN).map(ComTaskExecutionFields.ONHOLD.fieldName()).since(version(10, 2)).add();
            Column connectionTask = table.column("CONNECTIONTASK").number().conversion(NUMBER2LONGNULLZERO).map("connectionTaskId").add();
            Column protocolDialectConfigurationProperties = table.column("PROTOCOLDIALECTCONFIGPROPS").number().add();
            table.column("IGNORENEXTEXECSPECS").number().conversion(NUMBER2BOOLEAN).notNull().map(ComTaskExecutionFields.IGNORENEXTEXECUTIONSPECSFORINBOUND.fieldName()).add();
            table.primaryKey("PK_DDC_COMTASKEXEC").on(id).add();
            table.foreignKey("FK_DDC_COMTASKEXEC_COMPORT")
                    .on(comPort)
                    .references(ComPort.class)
                    .map(ComTaskExecutionFields.COMPORT.fieldName())
                    .add();
            table.foreignKey("FK_DDC_COMTASKEXEC_COMTASK")
                    .on(comTask)
                    .references(ComTask.class)
                    .map(ComTaskExecutionFields.COMTASK.fieldName())
                    .add();
            table.foreignKey("FK_DDC_COMTASKEXEC_COMSCHEDULE").
                    on(comSchedule).
                    references(ComSchedule.class).
                    onDelete(CASCADE).
                    map(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMTASKEXEC_NEXTEXEC").
                    on(nextExecutionSpecs).
                    references(NextExecutionSpecs.class).
                    map(ComTaskExecutionFields.NEXTEXECUTIONSPEC.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMTASKEXEC_CONNECTTASK")
                    .on(connectionTask)
                    .references(DDC_CONNECTIONTASK.name())
                    .map(ComTaskExecutionFields.CONNECTIONTASK.fieldName())
                    .add();
            table.foreignKey("FK_DDC_COMTASKEXEC_DIALECT")
                    .on(protocolDialectConfigurationProperties)
                    .references(ProtocolDialectConfigurationProperties.class)
                    .map(ComTaskExecutionFields.PROTOCOLDIALECTCONFIGURATIONPROPERTIES.fieldName())
                    .add();
            table.foreignKey("FK_DDC_COMTASKEXEC_DEVICE")
                    .on(device).references(DDC_DEVICE.name())
                    .map(ComTaskExecutionFields.DEVICE.fieldName())
                    .reverseMap("comTaskExecutions").composition()
                    .add();
            table.index("IX_DDCCOMTASKEXEC_NXTEXEC").on(nextExecutionTimestamp, priority, connectionTask, obsoleteDate, comPort).add();
        }
    },

    DDC_COMSESSION {
        @Override
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<ComSession> table = dataModel.addTable(name(), ComSession.class);
            table.map(ComSessionImpl.class);
            Column id = table.addAutoIdColumn();
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
                    references(ComPortPool.class).
                    onDelete(CASCADE).
                    map(ComSessionImpl.Fields.COMPORT_POOL.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMSESSION_COMPORT").
                    on(comport).
                    references(ComPort.class).
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
        void addTo(DataModel dataModel, Encrypter encrypter) {
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
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<ComTaskExecutionSession> table = dataModel.addTable(name(), ComTaskExecutionSession.class);
            table.map(ComTaskExecutionSessionImpl.class);
            Column id = table.addAutoIdColumn();
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
            table.index("DDC_CTES_CS_COMSESSION").on(session).add();
            table.foreignKey("FK_DDC_COMTASKSESSION_CTEXEC").
                    on(comTaskExecution).
                    references(DDC_COMTASKEXEC.name()).
                    onDelete(CASCADE).
                    map(ComTaskExecutionSessionImpl.Fields.COM_TASK_EXECUTION.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMTASKSESSION_COMTASK").
                    on(comTask).
                    references(ComTask.class).
                    onDelete(CASCADE).
                    map(ComTaskExecutionSessionImpl.Fields.COM_TASK.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMTSKEXECSESSION_DEVIC").
                    on(device).
                    references(DDC_DEVICE.name()).
                    onDelete(CASCADE).
                    map(ComTaskExecutionSessionImpl.Fields.DEVICE.fieldName()).
                    add();
            table.index("DDC_CTES_CS_SUCCESS").on(successIndicator, session).compress(1).add();
        }
    },
    ADD_LAST_SESSION_TO_COM_TASK_EXECUTION {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
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
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<ComTaskExecutionJournalEntry> table = dataModel.addTable(name(), ComTaskExecutionJournalEntry.class);
            table.map(ComTaskExecutionJournalEntryImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "varchar2(1 char)");
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
        public void addTo(DataModel dataModel, Encrypter encrypter) {
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
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<DataCollectionKpi> table = dataModel.addTable(name(), DataCollectionKpi.class);
            table.map(DataCollectionKpiImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.column("DISPLAYRANGEVALUE").number().conversion(ColumnConversion.NUMBER2INT).map(DataCollectionKpiImpl.Fields.DISPLAY_PERIOD.fieldName() + ".count").notNull().add();
            table.column("DISPLAYRANGEUNIT").number().conversion(ColumnConversion.NUMBER2INT).map(DataCollectionKpiImpl.Fields.DISPLAY_PERIOD.fieldName() + ".timeUnitCode").notNull().add();
            Column endDeviceGroup = table.column("ENDDEVICEGROUP").number().notNull().add();
            Column connectionKpi = table.column("CONNECTIONKPI").number().add();
            Column comTaskExecKpi = table.column("COMMUNICATIONKPI").number().add();
            Column connectionKpiTask = table.column("CONNECTIONKPI_TASK").number().add();
            Column communicationKpiTask = table.column("COMMUNICATIONKPI_TASK").number().add();
            table.primaryKey("PK_DDC_DATA_COLLECTION_KPI").on(id).add();
            table.foreignKey("FK_DDC_ENDDEVICEGROUP").
                    on(endDeviceGroup).
                    references(EndDeviceGroup.class).
                    map(DataCollectionKpiImpl.Fields.END_DEVICE_GROUP.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONNECTIONKPI").
                    on(connectionKpi).
                    references(Kpi.class).
                    map(DataCollectionKpiImpl.Fields.CONNECTION_KPI.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMTASKEXECKPI").
                    on(comTaskExecKpi).
                    references(Kpi.class).
                    map(DataCollectionKpiImpl.Fields.COMMUNICATION_KPI.fieldName()).
                    add();
            table.foreignKey("FK_DDC_CONN_KPI_TASK").
                    on(connectionKpiTask).
                    references(RecurrentTask.class).
                    map(DataCollectionKpiImpl.Fields.CONNECTION_RECURRENT_TASK.fieldName()).
                    add();
            table.foreignKey("FK_DDC_COMM_KPI_TASK").
                    on(communicationKpiTask).
                    references(RecurrentTask.class).
                    map(DataCollectionKpiImpl.Fields.COMMUNICATION_RECURRENT_TASK.fieldName()).
                    add();
        }
    },

    DDC_DEVICEMESSAGE {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<DeviceMessage> table = dataModel.addTable(name(), DeviceMessage.class);
            table.map(DeviceMessageImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            Column device = table.column("DEVICEID").number().conversion(NUMBER2LONG).notNull().add();
            table.column("DEVICEMESSAGEID").number().conversion(NUMBER2LONG).map(DeviceMessageImpl.Fields.DEVICEMESSAGEID.fieldName()).notNull().add();
            table.column("STATUS").number().conversion(NUMBER2ENUM).map(DeviceMessageImpl.Fields.DEVICEMESSAGESTATUS.fieldName()).notNull().add();
            table.column("TRACKINGID").varChar(Table.DESCRIPTION_LENGTH).map(DeviceMessageImpl.Fields.TRACKINGID.fieldName()).add();
            table.column("TRACKINGCATEGORY")
                    .number()
                    .conversion(NUMBER2ENUM)
                    .map(DeviceMessageImpl.Fields.TRACKINGCATEGORY.fieldName())
                    .since(version(10, 2))
                    .add();
            table.column("PROTOCOLINFO").varChar(Table.DESCRIPTION_LENGTH).map(DeviceMessageImpl.Fields.PROTOCOLINFO.fieldName()).add();
            table.column("RELEASEDATE").number().map(DeviceMessageImpl.Fields.RELEASEDATE.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("SENTDATE").number().map(DeviceMessageImpl.Fields.SENTDATE.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.column("CREATEUSERNAME").varChar(80).notNull().map(DeviceMessageImpl.Fields.CREATEDBYUSER.fieldName()).since(version(10, 2)).installValue("'install/upgrade'").add();
            table.addMessageAuthenticationCodeColumn(encrypter).since(version(10, 3));
            table.primaryKey("PK_DDC_DEVICEMESSAGE").on(id).add();
            table.foreignKey("FK_DDC_DEVMESSAGE_DEV")
                    .on(device).references(DDC_DEVICE.name())
                    .map("device").reverseMap("deviceMessages")
                    .add();
        }
    },

    DDC_DEVICEMESSAGEATTR {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<DeviceMessageAttribute> table = dataModel.addTable(name(), DeviceMessageAttribute.class);
            table.map(DeviceMessageAttributeImpl.class);
            Column id = table.addAutoIdColumn();
            table.addCreateTimeColumn("CREATETIME", "createTime").since(version(10, 2));
            table.addModTimeColumn("MODTIME", "modTime").since(version(10, 2));
            table.addUserNameColumn("USERNAME", "userName").since(version(10, 2));
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
                    .upTo(version(10, 2))
                    .add();
            table.foreignKey("FK_DDC_DEVMESATTR_DEV")
                    .on(deviceMessage)
                    .references(DDC_DEVICEMESSAGE.name())
                    .onDelete(CASCADE)
                    .map("deviceMessage")
                    .composition()
                    .reverseMap(DeviceMessageImpl.Fields.DEVICEMESSAGEATTRIBUTES.fieldName())
                    .since(version(10, 2))
                    .add();
            table.unique("UK_DDC_DEVMESATTR_NAME").on(deviceMessage, name).add();
        }
    },

    DDC_DEVICEESTACTIVATION {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<DeviceEstimation> table = dataModel.addTable(name(), DeviceEstimation.class);
            table.upTo(version(10, 2));
            table.map(DeviceImpl.DeviceEstimationImpl.class);
            Column device = table.column("DEVICE").number().conversion(NUMBER2LONG).notNull().add();
            table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).add();
            table.addAuditColumns();

            table.primaryKey("PK_DDC_DEVESTACTIVATION").on(device).add();
            table.foreignKey("FK_DDC_DEVESTACTIVATION_DEVICE")
                    .on(device)
                    .references(DDC_DEVICE.name())
                    .map("device")
                    .add();
        }
    },

    DDC_DEVICEESTRULESETACTIVATION {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<DeviceEstimationRuleSetActivation> table = dataModel.addTable(name(), DeviceEstimationRuleSetActivation.class);
            table.map(DeviceEstimationRuleSetActivationImpl.class);

            Column deviceColumn = table.column("DEVICE").number().conversion(NUMBER2LONG).notNull().since(version(10, 2)).add();
            Column estimationRuleSetColumn = table.column("ESTIMATIONRULESET").number().conversion(NUMBER2LONG).notNull().add();
            Column estimationActivationColumn = table.column("ESTIMATIONACTIVATION").number().conversion(NUMBER2LONG).notNull().upTo(version(10, 2)).add();
            table.column("ACTIVE").type("char(1)").notNull().conversion(CHAR2BOOLEAN).map(DeviceEstimationRuleSetActivationImpl.Fields.ACTIVE.fieldName()).add();
            table.addAuditColumns();
            table.setJournalTableName("DDC_DEVRULESETACTJRNL").since(version(10, 2));
            table.primaryKey("PK_EST_RS_ACTIVATION").on(deviceColumn, estimationRuleSetColumn).since(version(10, 2)).add();
            table.primaryKey("PK_DDC_DEVESTRULESETACTIV").on(estimationActivationColumn, estimationRuleSetColumn).upTo(version(10, 2)).add();
            table.foreignKey("FK_DDC_ESTRSACTIVATION_RULESET")
                    .on(estimationRuleSetColumn)
                    .references(EstimationRuleSet.class)
                    .map(DeviceEstimationRuleSetActivationImpl.Fields.ESTIMATIONRULESET.fieldName())
                    .add();
            table.foreignKey("FK_DDC_ESTRSACTIVATION_ESTACT")
                    .on(estimationActivationColumn)
                    .map("estimationActivation")
                    .upTo(version(10, 2))
                    .references(DDC_DEVICEESTACTIVATION.name())
                    .add();
            table.foreignKey("FK_EST_RS_2_DEVICE")
                    .on(deviceColumn)
                    .references(Device.class)
                    .map(DeviceEstimationRuleSetActivationImpl.Fields.DEVICE.fieldName())
                    .reverseMap("estimationRuleSetActivations")
                    .composition()
                    .since(version(10, 2))
                    .add();
        }
    },

    DDC_DEVICEINBATCH {
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<DeviceInBatch> table = dataModel.addTable(name(), DeviceInBatch.class);
            table.map(DeviceInBatch.class);
            table.upTo(version(10, 2));
            Column deviceColumn = table.column("DEVICEID").number().notNull().add();
            Column batchColumn = table.column("BATCHID").number().notNull().add();
            table.addCreateTimeColumn("CREATETIME", DeviceInBatch.Fields.CREATE_TIME.fieldName());
            table.primaryKey("DDC_PK_DEVICEINBATCH").on(deviceColumn).add();
            table.foreignKey("DDC_FK_DEVICEINBATCH2BATCH").references(DDC_BATCH.name()).onDelete(CASCADE).map(DeviceInBatch.Fields.BATCH.fieldName()).on(batchColumn).add();
            table.foreignKey("DDC_FK_DEVICEINBATCH2DEVICE").references(DDC_DEVICE.name()).onDelete(CASCADE).map(DeviceInBatch.Fields.DEVICE.fieldName()).on(deviceColumn).add();
        }
    },

    DDC_CONFIGCHANGEREQUEST {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            final Table<DeviceConfigChangeRequest> table = dataModel.addTable(name(), DeviceConfigChangeRequest.class);
            table.map(DeviceConfigChangeRequestImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column config = table.column("DEVICECONFIG").number().notNull().add();
            table.addAuditColumns().forEach(each -> each.upTo(Version.version(10, 2)));

            table.primaryKey("PK_DDC_DCCREQUEST").on(idColumn).add();
            table.foreignKey("FK_DDC_DCCREQUEST_CONF").
                    on(config).
                    references(DeviceConfiguration.class).
                    map(DeviceConfigChangeRequestImpl.Fields.DEVICE_CONFIG_REFERENCE.fieldName()).
                    onDelete(CASCADE).
                    add();
        }
    },

    DDC_CONFIGCHANGEINACTION {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            final Table<DeviceConfigChangeInAction> table = dataModel.addTable(name(), DeviceConfigChangeInAction.class);
            table.map(DeviceConfigChangeInActionImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column device = table.column("DEVICE").number().notNull().add();
            Column configRequest = table.column("DEVICECONFIGREQUEST").number().notNull().add();
            table.addAuditColumns().forEach(each -> each.upTo(Version.version(10, 2)));

            table.primaryKey("PK_DDC_CONFIGCHANGEINACTION").on(idColumn).add();
            table.foreignKey("FK_DDC_CONFCHANGACT_DEV").
                    on(device).
                    references(DDC_DEVICE.name()).
                    map(DeviceConfigChangeInActionImpl.Fields.DEVICE_REFERENCE.fieldName()).
                    onDelete(CASCADE).
                    add();
            table.foreignKey("FK_DDC_CONFCHANGREQ_CONF").
                    on(configRequest).
                    references(DDC_CONFIGCHANGEREQUEST.name()).
                    map(DeviceConfigChangeInActionImpl.Fields.DEVICE_CONFIG_REQUEST_REFERENCE.fieldName()).
                    reverseMap(DeviceConfigChangeRequestImpl.Fields.DEVICE_CONFIG_CHANGE_IN_ACTION.fieldName()).
                    composition().
                    onDelete(CASCADE).
                    add();
            table.unique("UK_DDC_CONFIGCHIA_REQ").on(device, configRequest).add();

        }
    },

    DDC_BREAKER_STATUS {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<ActivatedBreakerStatus> table = dataModel.addTable(name(), ActivatedBreakerStatus.class);
            table.since(version(10, 2));
            table.map(ActivatedBreakerStatusImpl.class);
            Column idColumn = table.addAutoIdColumn();
            Column deviceColumn = table.column("DEVICEID").number().notNull().add();
            table.column("BREAKERSTATUS").number().map(ActivatedBreakerStatusImpl.Fields.BREAKER_STATUS.fieldName()).conversion(NUMBER2ENUM).notNull().add();
            table.column("LASTCHECKED").number().map(ActivatedBreakerStatusImpl.Fields.LAST_CHECKED.fieldName()).conversion(ColumnConversion.NUMBER2INSTANT).add();
            table.addIntervalColumns(ActivatedBreakerStatusImpl.Fields.INTERVAL.fieldName());
            table.addAuditColumns();
            table.primaryKey("PK_DDC_BREAKER_STATUS").on(idColumn).add();
            table.foreignKey("FK_DDC_BREAKER_STATUS_DEVICE")
                    .on(deviceColumn)
                    .map(ActivatedBreakerStatusImpl.Fields.DEVICE.fieldName())
                    .references(DDC_DEVICE.name())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
        }
    },

    DDC_PASSIVE_CALENDAR {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<PassiveCalendar> table = dataModel.addTable(name(), PassiveCalendar.class);
            table.map(PassiveCalendarImpl.class);
            table.since(version(10, 2));

            Column idColumn = table.addAutoIdColumn();
            Column calendar = table.column("ALLOWED_CALENDAR").number().notNull().add();
            table.column("ACTIVATION_DATE").number().conversion(NUMBER2INSTANT).map(PassiveCalendarImpl.Fields.ACTIVATIONDATE.fieldName()).add();
            Column deviceMessage = table.column("DEVICE_MESSAGE").number().conversion(NUMBER2LONG).add();

            table.primaryKey("PK_DDC_PASSIVE_CAL").on(idColumn).add();
            table.foreignKey("FK_DDC_PASSIVECAL_ALLOWEDCAL")
                    .references(AllowedCalendar.class)
                    .on(calendar)
                    .onDelete(CASCADE)
                    .map(PassiveCalendarImpl.Fields.CALENDAR.fieldName())
                    .add();
            table.foreignKey("FK_DDC_PASSCAL_DEVICEMSG")
                    .on(deviceMessage)
                    .references(DDC_DEVICEMESSAGE.name())
                    .map(PassiveCalendarImpl.Fields.DEVICEMESSAGE.fieldName())
                    .add();
        }
    },
    ADD_DDC_PASSIVE_CALENDAR_TO_DEVICE {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<?> table = dataModel.getTable(DDC_DEVICE.name());
            Column passiveCalendar = table.column("PASSIVE_CAL").number().conversion(NUMBER2LONG).add();
            passiveCalendar.since(version(10, 2));
            table.foreignKey("FK_DDC_DEVICE_PASSIVECAL")
                    .references(DDC_PASSIVE_CALENDAR.name())
                    .on(passiveCalendar)
                    .map("passiveCalendar")
                    .add();
            Column plannedPassiveCalendar = table.column("PLANNED_PASSIVE_CAL").number().conversion(NUMBER2LONG).add();
            table.foreignKey("FK_DDC_DEVICE_PLANNEDPASSCAL")
                    .references(DDC_PASSIVE_CALENDAR.name())
                    .on(plannedPassiveCalendar)
                    .map("plannedPassiveCalendar")
                    .since(version(10, 2))
                    .add();
        }
    },

    DDC_ACTIVE_CALENDAR {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<ActiveEffectiveCalendar> table = dataModel.addTable(name(), ActiveEffectiveCalendar.class);
            table.since(version(10, 2));
            table.map(ActiveEffectiveCalendarImpl.class);

            Column device = table.column("DEVICE").number().conversion(NUMBER2LONG).notNull().add();
            List<Column> intervalColumns = table.addIntervalColumns(ActiveEffectiveCalendarImpl.Fields.INTERVAL.fieldName());
            Column calendar = table.column("ALLOWED_CALENDAR").number().notNull().add();
            table.column("LAST_VERIFIED_DATE").number().conversion(NUMBER2INSTANT).map(ActiveEffectiveCalendarImpl.Fields.LASTVERIFIEDDATE.fieldName()).add();

            table.primaryKey("DDC_PK_ACTIVE_CAL").on(device, intervalColumns.get(0)).add();
            table.foreignKey("FK_DDC_ACTIVECAL_ALLOWEDCAL")
                    .references(AllowedCalendar.class)
                    .on(calendar)
                    .onDelete(CASCADE)
                    .map(ActiveEffectiveCalendarImpl.Fields.CALENDAR.fieldName())
                    .add();
            table.foreignKey("FK_DDC_ACTIVECAL_DEVICE")
                    .on(device)
                    .references(DDC_DEVICE.name())
                    .map(ActiveEffectiveCalendarImpl.Fields.DEVICE.fieldName())
                    .reverseMap("activeCalendar")
                    .composition()
                    .onDelete(CASCADE)
                    .add();
        }
    },

    DDC_OVERRULEDOBISCODE {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<ReadingTypeObisCodeUsage> table = dataModel.addTable(name(), ReadingTypeObisCodeUsage.class);
            table.since(version(10, 2));
            table.map(ReadingTypeObisCodeUsageImpl.class);
            Column readingType = table.column("READINGTYPEMRID").varChar(NAME_LENGTH).notNull().add();
            Column device = table.column("DEVICEID").number().notNull().add();
            table.column("OBISCODE").varChar(NAME_LENGTH).notNull().map("obisCodeString").add();
            table.addAuditColumns();
            table.setJournalTableName("DDC_OVERRULEDOBISCODEJRNL").since(version(10, 2));
            table.primaryKey("PK_DDC_OVERRULEDOBISCODE").on(readingType, device).add();
            table.foreignKey("FK_DDC_OVEROBIS_DEVICE").
                    on(device).
                    references(DDC_DEVICE.name())
                    .map("device").
                    reverseMap("readingTypeObisCodeUsages").
                    composition().
                    add();
            table.foreignKey("FK_DDC_OVEROBIS_RDNGTYPE").
                    on(readingType).
                    references(ReadingType.class).
                    map("readingType").
                    add();
        }
    },
    DDC_COMTASKEXEC_TRIGGERS {
        @Override
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<ComTaskExecutionTrigger> table = dataModel.addTable(name(), ComTaskExecutionTrigger.class);
            table.since(version(10, 2));
            table.map(ComTaskExecutionTriggerImpl.class);

            Column comTaskExecution = table.column("COMTASKEXEC").number().notNull().add();
            Column triggerTimestamp = table.column("TIMESTAMP")
                    .number()
                    .notNull()
                    .conversion(NUMBERINUTCSECONDS2INSTANT)
                    .map(ComTaskExecutionTriggerImpl.Fields.TRIGGER_TIMESTAMP.fieldName())
                    .add();
            table.primaryKey("PK_DDC_CTEXECTRIGGER").on(comTaskExecution, triggerTimestamp).add();
            table.foreignKey("FK_DDC_CTEXECTRIGGER_CTEXEC")
                    .on(comTaskExecution)
                    .references(DDC_COMTASKEXEC.name())
                    .map(ComTaskExecutionTriggerImpl.Fields.COMTASK_EXECUTION.fieldName())
                    .reverseMap("comTaskExecutionTriggers")
                    .composition()
                    .onDelete(CASCADE)
                    .add();
        }
    };

    abstract void addTo(DataModel component, Encrypter encrypter);

}
