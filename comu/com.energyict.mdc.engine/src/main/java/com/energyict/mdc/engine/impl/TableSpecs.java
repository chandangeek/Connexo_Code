/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.orm.*;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionJournalEntryImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionJournalEntryImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionSessionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFields;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionJournalEntry;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.cache.DeviceCache;
import com.energyict.mdc.engine.impl.cache.DeviceCacheImpl;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2BOOLEAN;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.DeleteRule.CASCADE;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {

    CES_DEVICECACHE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<DeviceCache> table = dataModel.addTable(name(), DeviceCache.class);
            table.map(DeviceCacheImpl.class);
            Column device = table.column("DEVICEID").number().notNull().add();
            table.addAuditColumns();
            table.column("CONTENT").type("BLOB").conversion(ColumnConversion.BLOB2BYTE).map("simpleCache").add();
            table.primaryKey("PK_CES_DEVICECACHE").on(device).add();
            table.foreignKey("FK_CES_DEVICECACHE_DEVICE")
                    .on(device)
                    .references(Device.class)
                    .map("device")
                    .add();
        }
    },
    DDC_DEVICE {
        @Override
        public void addTo(DataModel dataModel) {
            Table<Device> table = dataModel.addTable(name(), Device.class).alsoReferredToAs(com.energyict.mdc.upl.meterdata.Device.class);
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
            table.audit(DDC_DEVICE.name())
                    .domainContext(AuditDomainContextType.DEVICE_ATTRIBUTES.ordinal())
                    .domainReferences("FK_DDC_DEVICE_ENDDEVICE")
                    .reverseReferenceMap("amrId")
                    .build();
        }
    },
    DDC_CONNECTIONTASK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ConnectionTask> table = dataModel.addTable(name(), ConnectionTask.class).alsoReferredToAs(ConnectionProvider.class);
            table.map(ConnectionTaskImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.setJournalTableName("DDC_CONNECTIONTASKJRNL", true).since(version(10, 6));
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            // Common columns
            Column device = table.column("DEVICE").number().notNull().add();
            Column connectionTypePluggableClass = table.column("CONNECTIONTYPEPLUGGABLECLASS").number().conversion(NUMBER2LONG).map("pluggableClassId").notNull().add();
            table.column("OBSOLETE_DATE").type("DATE").conversion(DATE2INSTANT).map(ConnectionTaskFields.OBSOLETE_DATE.fieldName()).add();
            table.column("ISDEFAULT").number().conversion(NUMBER2BOOLEAN).map(ConnectionTaskFields.IS_DEFAULT.fieldName()).add();
            table.column("STATUS").number().conversion(NUMBER2ENUM).map(ConnectionTaskFields.STATUS.fieldName()).add();
            table.column("LASTCOMMUNICATIONSTART").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ConnectionTaskFields.LAST_COMMUNICATION_START.fieldName()).notAudited().add();
            table.column("LASTSUCCESSFULCOMMUNICATIONEND")
                    .conversion(NUMBERINUTCSECONDS2INSTANT)
                    .number()
                    .map(ConnectionTaskFields.LAST_SUCCESSFUL_COMMUNICATION_END.fieldName())
                    .notAudited()
                    .add();
            Column comServer = table.column("COMSERVER").number().notAudited().add();
            Column comPortPool = table.column("COMPORTPOOL").number().add();
            Column partialConnectionTask = table.column("PARTIALCONNECTIONTASK").number().add();
            // Common columns for sheduled connection tasks
            table.column("CURRENTRETRYCOUNT").number().conversion(NUMBER2INT).map(ConnectionTaskFields.CURRENT_RETRY_COUNT.fieldName()).notAudited().add();
            table.column("LASTEXECUTIONFAILED").number().conversion(NUMBER2BOOLEAN).map(ConnectionTaskFields.LAST_EXECUTION_FAILED.fieldName()).notAudited().add();
            // ScheduledConnectionTaskImpl columns
            table.column("COMWINDOWSTART").number().conversion(NUMBER2INT).map("comWindow.start.millis").notAudited().add();
            table.column("COMWINDOWEND").number().conversion(NUMBER2INT).map("comWindow.end.millis").notAudited().add();
            Column nextExecutionSpecs = table.column("NEXTEXECUTIONSPECS").number().notAudited().add();
            table.column("NEXTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ConnectionTaskFields.NEXT_EXECUTION_TIMESTAMP.fieldName()).notAudited().add();
            table.column("PLANNEDNEXTEXECUTIONTIMESTAMP")
                    .number()
                    .conversion(NUMBERINUTCSECONDS2INSTANT)
                    .map(ConnectionTaskFields.PLANNED_NEXT_EXECUTION_TIMESTAMP.fieldName())
                    .notAudited()
                    .add();
            table.column("CONNECTIONSTRATEGY").number().conversion(NUMBER2ENUM).map(ConnectionTaskFields.CONNECTION_STRATEGY.fieldName()).add();
            table.column("PRIORITY").number().conversion(NUMBER2INT).map(ConnectionTaskFields.PRIORITY.fieldName()).add();
            table.column("SIMULTANEOUSCONNECTIONS").number().conversion(NUMBER2INT).map(ConnectionTaskFields.ALLOW_SIMULTANEOUS_CONNECTIONS.fieldName()).add();
            Column initiator = table.column("INITIATOR").number().add();
            Column protocolDialectConfigurationProperties = table.column("PROTOCOLDIALECTCONFIGPROPS").number().add().since(Version.version(10, 3));
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
            table.foreignKey("FK_DDC_CONNECTIONTASK_DIALECT")
                    .on(protocolDialectConfigurationProperties)
                    .onDelete(DeleteRule.CASCADE)
                    .since(Version.version(10, 3))
                    .references(ProtocolDialectConfigurationProperties.class)
                    .map(ConnectionTaskFields.PROTOCOLDIALECTCONFIGURATIONPROPERTIES.fieldName())
                    .add();
            table.audit("")
                    .domainContext(AuditDomainContextType.DEVICE_CONNECTION_METHODS.ordinal())
                    .domainReferences("FK_DDC_CONNECTIONTASK_DEVICE", "FK_DDC_DEVICE_ENDDEVICE")
                    .contextReferenceColumn("ID")
                    .forceReverseReferenceMap(false)
                    .build();
        }
    },
    DDC_COMTASKEXEC {
        @Override
        public void addTo(DataModel dataModel) {
            Table<ComTaskExecution> table = dataModel.addTable(name(), ComTaskExecution.class);
            table.map(ComTaskExecutionImpl.class);
            Column id = table.addAutoIdColumn();
            table.addAuditColumns();
            table.setJournalTableName("DDC_COMTASKEXECJRNL", true).since(version(10, 6));
            table.column("DISCRIMINATOR").number().conversion(NUMBER2ENUM).map(ComTaskExecutionFields.COMTASKEXECTYPE.fieldName()).notNull().add();
            Column device = table.column("DEVICE").number().notNull().add();
            Column comTask = table.column("COMTASK").number().add();
            Column comSchedule = table.column("COMSCHEDULE").number().add();
            Column nextExecutionSpecs = table.column("NEXTEXECUTIONSPECS").number().add();
            table.column("LASTEXECUTIONTIMESTAMP").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ComTaskExecutionFields.LASTEXECUTIONTIMESTAMP.fieldName()).notAudited().add();
            Column nextExecutionTimestamp = table.column("NEXTEXECUTIONTIMESTAMP")
                    .number()
                    .conversion(NUMBERINUTCSECONDS2INSTANT)
                    .map(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName())
                    .notAudited()
                    .add();
            Column comPort = table.column("COMPORT").number().add();
            Column obsoleteDate = table.column("OBSOLETE_DATE").type("DATE").conversion(DATE2INSTANT).map(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).add();
            Column priority = table.column("PRIORITY").number().conversion(NUMBER2INT).map(ComTaskExecutionFields.PLANNED_PRIORITY.fieldName()).add();
            table.column("USEDEFAULTCONNECTIONTASK").number().conversion(NUMBER2BOOLEAN).map(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName()).add();
            table.column("CURRENTRETRYCOUNT").number().conversion(NUMBER2INT).map(ComTaskExecutionFields.CURRENTRETRYCOUNT.fieldName()).notAudited().add();
            table.column("PLANNEDNEXTEXECUTIONTIMESTAMP")
                    .number()
                    .conversion(NUMBERINUTCSECONDS2INSTANT)
                    .map(ComTaskExecutionFields.PLANNEDNEXTEXECUTIONTIMESTAMP.fieldName())
                    .notAudited()
                    .add();
            table.column("EXECUTIONPRIORITY").number().conversion(NUMBER2INT).map(ComTaskExecutionFields.EXECUTION_PRIORITY.fieldName()).add();
            table.column("EXECUTIONSTART").number().conversion(NUMBERINUTCSECONDS2INSTANT).map(ComTaskExecutionFields.EXECUTIONSTART.fieldName()).notAudited().add();
            table.column("LASTSUCCESSFULCOMPLETION")
                    .number()
                    .conversion(NUMBERINUTCSECONDS2INSTANT)
                    .map(ComTaskExecutionFields.LASTSUCCESSFULCOMPLETIONTIMESTAMP.fieldName())
                    .notAudited()
                    .add();
            table.column("LASTEXECUTIONFAILED").number().conversion(NUMBER2BOOLEAN).map(ComTaskExecutionFields.LASTEXECUTIONFAILED.fieldName()).notAudited().add();
            table.column("ONHOLD").number().conversion(NUMBER2BOOLEAN).map(ComTaskExecutionFields.ONHOLD.fieldName()).since(version(10, 2)).add();
            Column connectionTask = table.column("CONNECTIONTASK").number().conversion(NUMBER2LONGNULLZERO).map("connectionTaskId").add();
            Column protocolDialectConfigurationProperties = table.column("PROTOCOLDIALECTCONFIGPROPS").number().add().upTo(Version.version(10, 2));
            table.column("IGNORENEXTEXECSPECS").number().conversion(NUMBER2BOOLEAN).notNull().map(ComTaskExecutionFields.IGNORENEXTEXECUTIONSPECSFORINBOUND.fieldName()).add();
            table.column("CONNECTIONFUNCTION").number().conversion(NUMBER2LONG).map("connectionFunctionId").since(Version.version(10, 4)).add();
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
                    .upTo(Version.version(10, 2))
                    .references(ProtocolDialectConfigurationProperties.class)
                    .map("protocolDialectConfigurationProperties")
                    .add();
            table.foreignKey("FK_DDC_COMTASKEXEC_DEVICE")
                    .on(device).references(DDC_DEVICE.name())
                    .map(ComTaskExecutionFields.DEVICE.fieldName())
                    .reverseMap("comTaskExecutions").composition()
                    .add();
            table.index("IX_DDCCOMTASKEXEC_NXTEXEC").on(nextExecutionTimestamp, priority, connectionTask, obsoleteDate, comPort).add();
            table.audit(DDC_COMTASKEXEC.name())
                    .domainContext(AuditDomainContextType.DEVICE_COMTASKS.ordinal())
                    .domainReferences("FK_DDC_COMTASKEXEC_DEVICE", "FK_DDC_DEVICE_ENDDEVICE")
                    .contextReferenceColumn("COMTASK")
                    .build();
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
            table.column("STARTDATE").number().conversion(NUMBER2INSTANT).notNull().map(ComSessionImpl.Fields.START_DATE.fieldName()).add();
            Column stopDate = table.column("STOPDATE").number().conversion(NUMBER2INSTANT).notNull().map(ComSessionImpl.Fields.STOP_DATE.fieldName()).add();
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
            table.autoPartitionOn(stopDate, LifeCycleClass.LOGGING);
        }
    },
    ADD_LAST_SESSION_TO_CONNECTION_TASK {
        @Override
        public void addTo(DataModel dataModel) {
            Table<?> table = dataModel.getTable(DDC_CONNECTIONTASK.name());
            Column lastSession = table.column("LASTSESSION").number().notAudited().add();
            table.column("LASTSESSIONSUCCESSINDICATOR").number().conversion(NUMBER2ENUM).map(ConnectionTaskFields.LAST_SESSION_SUCCESS_INDICATOR.fieldName()).notAudited().add();
            table.column("LASTSESSIONSTATUS").number().conversion(NUMBER2BOOLEAN).map(ConnectionTaskFields.LAST_SESSION_STATUS.fieldName()).notAudited().add();
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
            Column device = table.column("DEVICE").number().notNull().add();
            Column session = table.column("COMSESSION").number().notNull().add();
            table.column("STARTDATE").number().conversion(NUMBER2INSTANT).notNull().map(ComTaskExecutionSessionImpl.Fields.START_DATE.fieldName()).add();
            Column stopDate = table.column("STOPDATE").number().notNull().conversion(NUMBER2INSTANT).map(ComTaskExecutionSessionImpl.Fields.STOP_DATE.fieldName()).add();
            Column successIndicator = table.column("SUCCESSINDICATOR")
                    .number()
                    .conversion(NUMBER2ENUM)
                    .notNull()
                    .map(ComTaskExecutionSessionImpl.Fields.SUCCESS_INDICATOR.fieldName())
                    .add();
            table.column("BYTESSENT").number().conversion(NUMBER2LONG).notNull().map(ComTaskExecutionSessionImpl.Fields.NUMBER_OF_BYTES_SENT.fieldName()).add();
            table.column("BYTESREAD").number().conversion(NUMBER2LONG).notNull().map(ComTaskExecutionSessionImpl.Fields.NUMBER_OF_BYTES_READ.fieldName()).add();
            table.column("PACKETSSENT").number().conversion(NUMBER2LONG).notNull().map(ComTaskExecutionSessionImpl.Fields.NUMBER_OF_PACKETS_SENT.fieldName()).add();
            table.column("PACKETSREAD").number().conversion(NUMBER2LONG).notNull().map(ComTaskExecutionSessionImpl.Fields.NUMBER_OF_PACKETS_READ.fieldName()).add();
            Column comTaskExecution = table.column("COMTASKEXEC").number().notNull().add();
            Column comTask = table.column("COMTASK").number().notNull().add();
            table.column("HIGHESTPRIOCOMPLETIONCODE").number().conversion(NUMBER2ENUM).map(ComTaskExecutionSessionImpl.Fields.HIGHEST_PRIORITY_COMPLETION_CODE.fieldName()).add();
            table.column("HIGHESTPRIOERRORDESCRIPTION")
                    .type("CLOB")
                    .conversion(CLOB2STRING)
                    .map(ComTaskExecutionSessionImpl.Fields.HIGHEST_PRIORITY_ERROR_DESCRIPTION.fieldName())
                    .add();
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
            table.autoPartitionOn(stopDate, LifeCycleClass.LOGGING);
        }
    },
    ADD_LAST_SESSION_TO_COM_TASK_EXECUTION {
        @Override
        public void addTo(DataModel dataModel) {
            Table<?> table = dataModel.getTable(DDC_COMTASKEXEC.name());
            Column lastSession = table.column("LASTSESSION").number().notAudited().add();
            table.column("LASTSESS_HIGHESTPRIOCOMPLCODE")
                    .number()
                    .conversion(NUMBER2ENUM)
                    .map(ComTaskExecutionFields.LAST_SESSION_HIGHEST_PRIORITY_COMPLETION_CODE.fieldName())
                    .add();
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
            table.addDiscriminatorColumn("DISCRIMINATOR", "varchar2(1 char)");
            Column comtaskexecsession = table.column("COMTASKEXECSESSION").number().notNull().add();
            Column timestamp = table.column("TIMESTAMP").number().conversion(NUMBER2INSTANT).notNull().map("timestamp").add();
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
            table.autoPartitionOn(timestamp, LifeCycleClass.LOGGING);
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
            Column timestamp = table.column("TIMESTAMP").number().conversion(NUMBER2INSTANT).notNull().map("timestamp").add();
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
            table.autoPartitionOn(timestamp, LifeCycleClass.LOGGING);
        }
    };

    public abstract void addTo(DataModel component);

}