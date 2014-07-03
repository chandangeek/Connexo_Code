package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.CommunicationTopologyEntry;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.finders.ConnectionMethodFinder;
import com.energyict.mdc.device.data.impl.finders.DeviceFinder;
import com.energyict.mdc.device.data.impl.finders.LoadProfileFinder;
import com.energyict.mdc.device.data.impl.finders.LogBookFinder;
import com.energyict.mdc.device.data.impl.finders.ProtocolDialectPropertiesFinder;
import com.energyict.mdc.device.data.impl.finders.SecuritySetFinder;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus;
import com.energyict.mdc.device.data.impl.tasks.TimedOutTasksSqlBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import javax.inject.Inject;
import javax.validation.MessageInterpolator;

import org.joda.time.DateTimeConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link DeviceDataService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:27)
 */
@Component(name="com.energyict.mdc.device.data", service = {DeviceDataService.class, ReferencePropertySpecFinderProvider.class, InstallService.class}, property = "name=" + DeviceDataService.COMPONENTNAME, immediate = true)
public class DeviceDataServiceImpl implements ServerDeviceDataService, ReferencePropertySpecFinderProvider, InstallService {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;

    private volatile Clock clock;
    private volatile RelationService relationService;
    private volatile PluggableService pluggableService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile EngineModelService engineModelService;
    private volatile MeteringService meteringService;
    private volatile SchedulingService schedulingService;
    private volatile MessageService messagingService;
    private volatile SecurityPropertyService securityPropertyService;

    public DeviceDataServiceImpl() {
    }

    @Inject
    public DeviceDataServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, Clock clock, RelationService relationService, ProtocolPluggableService protocolPluggableService, EngineModelService engineModelService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService, SchedulingService schedulingService, MessageService messageService, SecurityPropertyService securityPropertyService) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setRelationService(relationService);
        this.setClock(clock);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setEngineModelService(engineModelService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setMeteringService(meteringService);
        this.setSchedulingService(schedulingService);
        this.setMessagingService(messageService);
        this.setSecurityPropertyService(securityPropertyService);
        this.activate();
        if (!this.dataModel.isInstalled()) {
            this.install(true);
        }
    }

    @Override
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
        finders.add(new DeviceFinder(this.dataModel));
        finders.add(new LoadProfileFinder(this.dataModel));
        finders.add(new LogBookFinder(this.dataModel));
        finders.add(new ConnectionMethodFinder(this.dataModel));
        finders.add(new ProtocolDialectPropertiesFinder(this.dataModel));
        finders.add(new SecuritySetFinder(this.dataModel));
        return finders;
    }

    @Override
    public boolean hasDevices (DeviceConfiguration deviceConfiguration) {
        Condition condition = Where.where(DeviceFields.DEVICECONFIGURATION.fieldName()).isEqualTo(deviceConfiguration);
        Finder<Device> page =
                DefaultFinder.
                        of(Device.class, condition, this.dataModel).
                        paged(1, 1);
        List<Device> allDevices = page.find();
        return !allDevices.isEmpty();
    }

    @Override
    public void releaseInterruptedConnectionTasks(ComServer comServer) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_CONNECTIONTASK.name() + " SET comserver = NULL WHERE comserver = ?");
        sqlBuilder.bindLong(comServer.getId());
        this.executeUpdate(sqlBuilder);
    }

    @Override
    public void releaseInterruptedComTasks(ComServer comServer) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_COMTASKEXEC.name() + " SET comport = NULL, executionStart = null WHERE comport in (select id from mdc_comport where COMSERVERID = ?)");
        sqlBuilder.bindLong(comServer.getId());
        this.executeUpdate(sqlBuilder);
    }

    @Override
    public void releaseTimedOutConnectionTasks(ComServer outboundCapableComServer) {
        List<ComPortPool> containingComPortPoolsForComServer = this.engineModelService.findContainingComPortPoolsForComServer(outboundCapableComServer);
        for (ComPortPool comPortPool : containingComPortPoolsForComServer) {
            this.releaseTimedOutConnectionTasks((OutboundComPortPool) comPortPool);
        }
    }

    @Override
    public TimeDuration releaseTimedOutComTasks(ComServer comServer) {
        int waitTime = -1;
        List<ComPortPool> containingComPortPoolsForComServer = this.engineModelService.findContainingComPortPoolsForComServer(comServer);
        for (ComPortPool comPortPool : containingComPortPoolsForComServer) {
            this.releaseTimedOutComTasks((OutboundComPortPool) comPortPool);
            waitTime = this.minimumWaitTime(waitTime, ((OutboundComPortPool)comPortPool).getTaskExecutionTimeout().getSeconds());
        }
        if (waitTime < 0) {
            return new TimeDuration(1, TimeDuration.DAYS);
        } else {
            return new TimeDuration(waitTime, TimeDuration.SECONDS);
        }
    }

    private int minimumWaitTime(int currentWaitTime, int comPortPoolTaskExecutionTimeout) {
        if (currentWaitTime < 0) {
            return comPortPoolTaskExecutionTimeout;
        } else {
            return Math.min(currentWaitTime, comPortPoolTaskExecutionTimeout);
        }
    }

    private void releaseTimedOutComTasks(OutboundComPortPool outboundComPortPool){
        long now = this.toSeconds(this.clock.now());
        int timeOutSeconds = outboundComPortPool.getTaskExecutionTimeout().getSeconds();
        this.executeUpdate(this.releaseTimedOutComTaskExecutionsSqlBuilder(outboundComPortPool, now, timeOutSeconds));
    }

    private void releaseTimedOutConnectionTasks(OutboundComPortPool outboundComPortPool) {
        long now = this.toSeconds(this.clock.now());
        int timeOutSeconds = outboundComPortPool.getTaskExecutionTimeout().getSeconds();
        this.executeUpdate(this.releaseTimedOutConnectionTasksSqlBuilder(outboundComPortPool, now, timeOutSeconds));
    }

    private SqlBuilder releaseTimedOutConnectionTasksSqlBuilder(OutboundComPortPool outboundComPortPool, long now, int timeOutSeconds) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append("   set comserver = null");
        sqlBuilder.append(" where id in (select connectiontask from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" where id in (");
        TimedOutTasksSqlBuilder.appendTimedOutComTaskExecutionSql(sqlBuilder, outboundComPortPool, now, timeOutSeconds);
        sqlBuilder.append("))");
        return sqlBuilder;
    }

    private SqlBuilder releaseTimedOutComTaskExecutionsSqlBuilder(OutboundComPortPool outboundComPortPool, long now, int timeOutSeconds) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append("   set comport = null, executionStart = null");
        sqlBuilder.append(" where id in (");
        TimedOutTasksSqlBuilder.appendTimedOutComTaskExecutionSql(sqlBuilder, outboundComPortPool, now, timeOutSeconds);
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    private long toSeconds(Date time) {
        return time.getTime() / DateTimeConstants.MILLIS_PER_SECOND;
    }

    private void executeUpdate(SqlBuilder sqlBuilder) {
        try (Connection connection = this.dataModel.getConnection(true)) {
            try (PreparedStatement statement = sqlBuilder.getStatement(connection)) {
                statement.executeUpdate();
                // Don't care about how many rows were updated and if that matches the expected number of updates
            }
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public InboundConnectionTask newInboundConnectionTask(Device device, PartialInboundConnectionTask partialConnectionTask, InboundComPortPool comPortPool, ConnectionTask.ConnectionTaskLifecycleStatus status) {
        InboundConnectionTaskImpl connectionTask = this.dataModel.getInstance(InboundConnectionTaskImpl.class);
        connectionTask.initialize(device, partialConnectionTask, comPortPool, status);
        return connectionTask;
    }

    @Override
    public ScheduledConnectionTask newAsapConnectionTask(Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool comPortPool, ConnectionTask.ConnectionTaskLifecycleStatus status) {
        ScheduledConnectionTaskImpl connectionTask = this.dataModel.getInstance(ScheduledConnectionTaskImpl.class);
        connectionTask.initializeWithAsapStrategy(device, partialConnectionTask, comPortPool, status);
        return connectionTask;
    }

    @Override
    public ScheduledConnectionTask newMinimizeConnectionTask(Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool comPortPool, TemporalExpression temporalExpression, ConnectionTask.ConnectionTaskLifecycleStatus status) {
        NextExecutionSpecs nextExecutionSpecs = null;
        if (temporalExpression != null) {
            nextExecutionSpecs = this.schedulingService.newNextExecutionSpecs(temporalExpression);
            nextExecutionSpecs.save();
        }
        ScheduledConnectionTaskImpl connectionTask = this.dataModel.getInstance(ScheduledConnectionTaskImpl.class);
        connectionTask.initializeWithMinimizeStrategy(device, partialConnectionTask, comPortPool, nextExecutionSpecs, status);
        return connectionTask;
    }

    @Override
    public ConnectionInitiationTask newConnectionInitiationTask(Device device, PartialConnectionInitiationTask partialConnectionTask, OutboundComPortPool comPortPool, ConnectionTask.ConnectionTaskLifecycleStatus status) {
        ConnectionInitiationTaskImpl connectionTask = this.dataModel.getInstance(ConnectionInitiationTaskImpl.class);
        connectionTask.initialize(device, partialConnectionTask, comPortPool, status);
        return connectionTask;
    }

    @Override
    public Optional<ConnectionTask> findConnectionTask(long id) {
        return this.getDataModel().mapper(ConnectionTask.class).getUnique("id", id);
    }

    @Override
    public Optional<OutboundConnectionTask> findOutboundConnectionTask(long id) {
        return this.getDataModel().mapper(OutboundConnectionTask.class).getUnique("id", id);
    }

    @Override
    public Optional<InboundConnectionTask> findInboundConnectionTask(long id) {
        return this.getDataModel().mapper(InboundConnectionTask.class).getUnique("id", id);
    }

    @Override
    public Optional<ScheduledConnectionTask> findScheduledConnectionTask(long id) {
        return this.getDataModel().mapper(ScheduledConnectionTask.class).getUnique("id", id);
    }

    @Override
    public Optional<ConnectionInitiationTask> findConnectionInitiationTask(long id) {
        return this.getDataModel().mapper(ConnectionInitiationTask.class).getUnique("id", id);
    }

    @Override
    public Optional<ConnectionTask> findConnectionTaskForPartialOnDevice(PartialConnectionTask partialConnectionTask, Device device) {
        Condition condition = where("deviceId").isEqualTo(device.getId()).and(where("obsoleteDate").isNull()).and(where("partialConnectionTask").isEqualTo(partialConnectionTask));
        List<ConnectionTask> connectionTasks = this.getDataModel().mapper(ConnectionTask.class).select(condition);
        if (connectionTasks.isEmpty()) {
            return Optional.absent();
        }
        else {
            return Optional.of(connectionTasks.get(0));
        }
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByDevice(Device device) {
        Condition condition = where("deviceId").isEqualTo(device.getId()).and(where("obsoleteDate").isNull());
        return this.getDataModel().mapper(ConnectionTask.class).select(condition);
    }

    @Override
    public List<ConnectionTask> findAllConnectionTasksByDevice(Device device) {
        return this.getDataModel().mapper(ConnectionTask.class).find("deviceId", device.getId());
    }

    @Override
    public List<InboundConnectionTask> findInboundConnectionTasksByDevice(Device device) {
        Condition condition = where("deviceId").isEqualTo(device.getId()).and(where("obsoleteDate").isNull());
        return this.getDataModel().mapper(InboundConnectionTask.class).select(condition);
    }

    @Override
    public List<ScheduledConnectionTask> findScheduledConnectionTasksByDevice(Device device) {
        Condition condition = where("deviceId").isEqualTo(device.getId()).and(where("obsoleteDate").isNull());
        return this.getDataModel().mapper(ScheduledConnectionTask.class).select(condition);
    }

    @Override
    public ConnectionTask findDefaultConnectionTaskForDevice(Device device) {
        Condition condition = where("deviceId").isEqualTo(device.getId()).and(where("isDefault").isEqualTo(true)).and(where("obsoleteDate").isNull());
        List<ConnectionTask> connectionTasks = this.getDataModel().mapper(ConnectionTask.class).select(condition);
        if (connectionTasks != null && connectionTasks.size() == 1) {
            return connectionTasks.get(0);
        }
        else {
            if (device.getPhysicalGateway() != null) {
                return this.findDefaultConnectionTaskForDevice(device.getPhysicalGateway());
            }
        }
        return null;  //if no default is found, null is returned
    }

    @Override
    public List<ConnectionTask> findByStatus(TaskStatus status) {
        return this.getDataModel().mapper(ConnectionTask.class).select(ServerConnectionTaskStatus.forTaskStatus(status).condition(this.getDataModel()));
    }

    @Override
    public void setDefaultConnectionTask(ConnectionTask newDefaultConnectionTask) {
        this.doSetDefaultConnectionTask(newDefaultConnectionTask.getDevice(), (ConnectionTaskImpl) newDefaultConnectionTask);
    }

    public void doSetDefaultConnectionTask(final Device device, final ConnectionTaskImpl newDefaultConnectionTask) {
        List<ConnectionTask> connectionTasks = getDataModel().mapper(ConnectionTask.class).find("deviceId", device.getId());
        for (ConnectionTask connectionTask : connectionTasks) {
            if (isPreviousDefault(newDefaultConnectionTask, connectionTask)) {
                ((ConnectionTaskImpl) connectionTask).clearDefault();
            }
        }
        setOrUpdateDefaultConnectionTaskOnComTaskInDeviceTopology(device, newDefaultConnectionTask);
        if (newDefaultConnectionTask != null) {
            newDefaultConnectionTask.setAsDefault();
        }
    }

    @Override
    public void clearDefaultConnectionTask(Device device) {
        this.doSetDefaultConnectionTask(device, null);
    }

    private boolean isPreviousDefault(ConnectionTask newDefaultConnectionTask, ConnectionTask connectionTask) {
        return connectionTask.isDefault()
            && (   (newDefaultConnectionTask == null)
                || (connectionTask.getId() != newDefaultConnectionTask.getId()));
    }

    @Override
    public void setOrUpdateDefaultConnectionTaskOnComTaskInDeviceTopology(Device device, ConnectionTask defaultConnectionTask) {
        List<ComTaskExecution> comTaskExecutions = this.findComTaskExecutionsWithDefaultConnectionTaskForCompleteTopologyButNotLinkedYet(device, defaultConnectionTask);
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?,?>, ? extends ComTaskExecution> comTaskExecutionUpdater = comTaskExecution.getUpdater();
            comTaskExecutionUpdater.useDefaultConnectionTask(defaultConnectionTask);
            comTaskExecutionUpdater.update();
        }
    }

    /**
     * Constructs a list of {@link ComTaskExecution} which are linked to
     * the default {@link ConnectionTask} for the entire topology of the specified Device,
     * but are not linked yet to the given Default connectionTask
     *
     * @param device the Device for which we need to search the ComTaskExecution
     * @param connectionTask the 'new' default ConnectionTask
     * @return The List of ComTaskExecution
     */
    private List<ComTaskExecution> findComTaskExecutionsWithDefaultConnectionTaskForCompleteTopologyButNotLinkedYet(Device device, ConnectionTask connectionTask) {
        List<ComTaskExecution> scheduledComTasks = new ArrayList<>();
        this.collectComTaskWithDefaultConnectionTaskForCompleteTopology(device, scheduledComTasks, connectionTask);
        return scheduledComTasks;
    }

    private void collectComTaskWithDefaultConnectionTaskForCompleteTopology(Device device, List<ComTaskExecution> scheduledComTasks, ConnectionTask connectionTask) {
        Condition query = where(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName()).isEqualTo(true)
                .and(where(ComTaskExecutionFields.DEVICE.fieldName()).isEqualTo(device)
                        .and((where(ComTaskExecutionFields.CONNECTIONTASK.fieldName()).isNull())
                                .or(where(ComTaskExecutionFields.CONNECTIONTASK.fieldName()).isNotEqual(connectionTask))));
        List<ComTaskExecution> comTaskExecutions = this.dataModel.mapper(ComTaskExecution.class).select(query);
        scheduledComTasks.addAll(comTaskExecutions);
        for (Device slave : device.getPhysicalConnectedDevices()) {
            this.collectComTaskWithDefaultConnectionTaskForCompleteTopology(slave, scheduledComTasks, connectionTask);
        }
    }

    @Override
    public <T extends ConnectionTask> T attemptLockConnectionTask(T connectionTask, ComServer comServer) {
        Optional<ConnectionTask> lockResult = this.getDataModel().mapper(ConnectionTask.class).lockNoWait(connectionTask.getId());
        if (lockResult.isPresent()) {
            T lockedConnectionTask = (T) lockResult.get();
            if (lockedConnectionTask.getExecutingComServer() == null) {
                try {
                    ((ConnectionTaskImpl) lockedConnectionTask).setExecutingComServer(comServer);
                    lockedConnectionTask.save();
                    return lockedConnectionTask;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
            else {
                // No database lock but business lock is already set
                return null;
            }
        }
        else {
            // ConnectionTask no longer exists, attempt to lock fails
            return null;
        }
    }

    @Override
    public void unlockConnectionTask(ConnectionTask connectionTask) {
        this.unlockConnectionTask((ConnectionTaskImpl) connectionTask);
    }
     private void unlockConnectionTask (ConnectionTaskImpl connectionTask) {
        connectionTask.setExecutingComServer(null);
        connectionTask.save();
    }

    @Override
    public void removePreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        this.executeUpdate(this.removePreferredConnectionTaskSqlBuilder(comTask, deviceConfiguration, partialConnectionTask));
    }

    private SqlBuilder removePreferredConnectionTaskSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set connectionTask = null");
        sqlBuilder.append(" where comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append("  where device = " + TableSpecs.DDC_COMTASKEXEC.name() + ".device");
        sqlBuilder.append("    and partialconnectiontask = ?");   // Match the connection task
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.bindLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void switchFromDefaultConnectionTaskToPreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        this.executeUpdate(this.switchFromDefaultConnectionTaskToPreferredConnectionTaskSqlBuilder(comTask, deviceConfiguration, partialConnectionTask));
    }

    private SqlBuilder switchFromDefaultConnectionTaskToPreferredConnectionTaskSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set useDefaultConnectionTask = 0, connectionTask = ");
        sqlBuilder.append("select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" where device = " + TableSpecs.DDC_COMTASKEXEC.name() + ".device");
        sqlBuilder.append("   and partialconnectiontask = ?");  //Match the connection task against the same device
        sqlBuilder.append("   and obsolete_date is null)");
        sqlBuilder.bindLong(partialConnectionTask.getId());
        sqlBuilder.append(" where comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void switchOnDefault(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.executeUpdate(this.switchOnDefaultSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder switchOnDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set useDefaultConnectionTask = 1, connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append("  where device = " + TableSpecs.DDC_COMTASKEXEC.name() + ".device");
        sqlBuilder.append("    and isdefault = 1");  // Match the default connection task against the same device
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.append(" where comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void switchOffDefault(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.executeUpdate(this.switchOffDefaultSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder switchOffDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set useDefaultConnectionTask = 0");
        sqlBuilder.append(" where comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void switchFromPreferredConnectionTaskToDefault(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        this.executeUpdate(this.switchFromPreferredConnectionTaskToDefaultSqlBuilder(comTask, deviceConfiguration, previousPartialConnectionTask));
    }

    private SqlBuilder switchFromPreferredConnectionTaskToDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set useDefaultConnectionTask = 1, connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append("  where device = " + TableSpecs.DDC_COMTASKEXEC.name() + ".device");
        sqlBuilder.append("    and isdefault = 1");  // Match the default connection task against the same device
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.append(" where comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append("  where device = " + TableSpecs.DDC_COMTASKEXEC.name() + ".device");
        sqlBuilder.append("    and partialconnectiontask = ?");   // Match the connection task
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.bindLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void preferredConnectionTaskChanged(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask, PartialConnectionTask newPartialConnectionTask) {
        this.executeUpdate(this.preferredConnectionTaskChangedSqlBuilder(comTask, deviceConfiguration, previousPartialConnectionTask, newPartialConnectionTask));
    }

    private SqlBuilder preferredConnectionTaskChangedSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask, PartialConnectionTask newPartialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set connectionTask = ");
        sqlBuilder.append("   (select id from " + TableSpecs.DDC_CONNECTIONTASK.name() + "");
        sqlBuilder.append("     where device = " + TableSpecs.DDC_COMTASKEXEC.name() + ".device");
        sqlBuilder.append("       and partialconnectiontask = ?");  //Match the connection task against the same device
        sqlBuilder.append("       and obsolete_date is null)");
        sqlBuilder.bindLong(newPartialConnectionTask.getId());
        // Avoid comTaskExecutions that use the default connection
        sqlBuilder.append(" where useDefaultConnectionTask = 0");
        sqlBuilder.append("   and comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and connectionTask = ");
        sqlBuilder.append("     (select id from " + TableSpecs.DDC_CONNECTIONTASK.name() + "");
        sqlBuilder.append("       where device = " + TableSpecs.DDC_COMTASKEXEC.name() + ".device");
        sqlBuilder.append("         and partialconnectiontask = ?");   // Match the previous connection task
        sqlBuilder.append("         and obsolete_date is null)");
        sqlBuilder.bindLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public boolean hasComTaskExecutions(ComTaskEnablement comTaskEnablement) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("select count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" cte");
        sqlBuilder.append(" inner join ddc_device device on cte.device = device.id");
        sqlBuilder.append(" inner join dtc_deviceconfig dcf on device.deviceconfigid = dcf.id");
        sqlBuilder.append(" inner join dtc_devicecommconfig dcc on dcf.id = dcc.deviceconfiguration");
        sqlBuilder.append(" inner join dtc_comtaskenablement ctn on dcc.id = ctn.devicecomconfig and cte.comtask = ctn.comtask");
        sqlBuilder.append(" where ctn.id = ?  and cte.obsolete_date is null");
        sqlBuilder.bindLong(comTaskEnablement.getId());
        try (PreparedStatement statement = sqlBuilder.getStatement(this.dataModel.getConnection(true))) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count != 0;
            }
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return false;
    }

    @Override
    public void preferredPriorityChanged(ComTask comTask, DeviceConfiguration deviceConfiguration, int previousPreferredPriority, int newPreferredPriority) {
        this.executeUpdate(this.preferredPriorityChangedSqlBuilder(comTask, deviceConfiguration, previousPreferredPriority, newPreferredPriority));
    }

    private SqlBuilder preferredPriorityChangedSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, int previousPreferredPriority, int newPreferredPriority) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set priority = ?");
        sqlBuilder.bindInt(newPreferredPriority);
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append("priority = ?");  // Match the previous priority
        sqlBuilder.bindInt(previousPreferredPriority);
        sqlBuilder.append("   and comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void suspendAll(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.executeUpdate(this.suspendAllSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder suspendAllSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set nextExecutionTimestamp = null");
        sqlBuilder.append(" where device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");    // against devices of the specified configuration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        sqlBuilder.append("   and comtask = ?");
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and comport is null");    // exclude tasks that are currently executing
        sqlBuilder.append("   and plannedNextExecutionTimestamp is not null");  // Exclude tasks that have been put on hold manually
        return sqlBuilder;
    }

    @Override
    public void resumeAll(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.executeUpdate(this.resumeAllSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder resumeAllSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set nextExecutionTimestamp = plannedNextExecutionTimestamp");
        sqlBuilder.append(" where device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");    // against devices of the specified configuration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        sqlBuilder.append("   and comtask = ?");
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and nextExecutionTimestamp is null");      // Only tasks that were suspended
        sqlBuilder.append("   and plannedNextExecutionTimestamp is not null");  // Only tasks that were suspended
        return sqlBuilder;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "Device data");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public void setRelationService(RelationService relationService) {
        this.relationService = relationService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setPluggableService(PluggableService pluggableService) {
        // Not actively used but required for foreign keys in TableSpecs
        this.pluggableService = pluggableService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @Reference
    public void setMessagingService(MessageService messagingService) {
        this.messagingService = messagingService;
    }

    @Reference
    public void setSecurityPropertyService(SecurityPropertyService securityPropertyService) {
        this.securityPropertyService = securityPropertyService;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(DeviceDataService.class).toInstance(DeviceDataServiceImpl.this);
                bind(SecurityPropertyService.class).toInstance(DeviceDataServiceImpl.this.securityPropertyService);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(RelationService.class).toInstance(relationService);
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(Clock.class).toInstance(clock);
                bind(MeteringService.class).toInstance(meteringService);
                bind(SchedulingService.class).toInstance(schedulingService);
                bind(MessageService.class).toInstance(messagingService);
                bind(MessageInterpolator.class).toInstance(thesaurus);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        this.install(true);
    }

    private void install(boolean exeuteDdl) {
        new Installer(this.dataModel, this.eventService, this.thesaurus, messagingService).install(exeuteDdl);
    }

    @Override
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String mRID) {
        return dataModel.getInstance(DeviceImpl.class).initialize(deviceConfiguration, name, mRID);
    }

    @Override
    public Device findDeviceById(long id) {
        return dataModel.mapper(Device.class).getUnique("id", id).orNull();
    }

    @Override
    public Device findByUniqueMrid(String mrId) {
        return dataModel.mapper(Device.class).getUnique(DeviceFields.MRID.fieldName(), mrId).orNull();
    }

    @Override
    public Device getPrototypeDeviceFor(DeviceConfiguration deviceConfiguration) {
        return null;
    }

    @Override
    public List<Device> findPhysicalConnectedDevicesFor(Device device) {
        Condition condition = where("gateway").isEqualTo(device).and(where("interval").isEffective());
        List<PhysicalGatewayReference> physicalGatewayReferences = this.dataModel.mapper(PhysicalGatewayReference.class).select(condition);
        if (!physicalGatewayReferences.isEmpty()) {
            List<Device> devices = new ArrayList<>();
            for (PhysicalGatewayReference physicalGatewayReference : physicalGatewayReferences) {
                devices.add(physicalGatewayReference.getOrigin());
            }
            return devices;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Device> findCommunicationReferencingDevicesFor(Device device) {
        Condition condition = where("gateway").isEqualTo(device).and(where("interval").isEffective());
        return this.findCommunicationReferencingDevicesFor(condition);
    }

    private List<Device> findCommunicationReferencingDevicesFor(Condition condition) {
        List<CommunicationGatewayReference> communicationGatewayReferences = this.dataModel.mapper(CommunicationGatewayReference.class).select(condition);
        if (!communicationGatewayReferences.isEmpty()) {
            List<Device> devices = new ArrayList<>();
            for (CommunicationGatewayReference communicationGatewayReference : communicationGatewayReferences) {
                devices.add(communicationGatewayReference.getOrigin());
            }
            return devices;
        }
        else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Device> findCommunicationReferencingDevicesFor(Device device, Date timestamp) {
        Condition condition = where("gateway").isEqualTo(device).and(where("interval").isEffective(timestamp));
        return this.findCommunicationReferencingDevicesFor(condition);
    }

    @Override
    public List<CommunicationTopologyEntry> findCommunicationReferencingDevicesFor(Device device, Interval interval) {
        Condition condition = where("gateway").isEqualTo(device).and(where("interval").isEffective(interval));
        List<CommunicationGatewayReference> communicationGatewayReferences = this.dataModel.mapper(CommunicationGatewayReference.class).select(condition);
        if (!communicationGatewayReferences.isEmpty()) {
            List<CommunicationTopologyEntry> entries = new ArrayList<>(communicationGatewayReferences.size());
            for (CommunicationGatewayReference communicationGatewayReference : communicationGatewayReferences) {
                entries.add(
                        new SimpleCommunicationTopologyEntryImpl(
                                communicationGatewayReference.getOrigin(),
                                communicationGatewayReference.getInterval()));
            }
            return entries;
        }
        else {
            return Collections.emptyList();
        }
    }

    @Override
    public LoadProfile findLoadProfileById(long id) {
        return dataModel.mapper(LoadProfile.class).getUnique("id", id).orNull();
    }

    @Override
    public List<Device> findDevicesBySerialNumber(String serialNumber) {
        return this.dataModel.mapper(Device.class).find("serialNumber", serialNumber);
    }

    @Override
    public List<Device> findAllDevices() {
        return this.dataModel.mapper(Device.class).find();
    }

    @Override
    public Finder<Device> findAllDevices(Condition condition) {
        return DefaultFinder.of(Device.class, condition, this.getDataModel(), DeviceConfiguration.class, DeviceType.class);
    }

    @Override
    public List<Device> findDevicesByTimeZone(TimeZone timeZone) {
        return this.dataModel.mapper(Device.class).find("timeZoneId", timeZone.getID());
    }

    @Override
    public InfoType newInfoType(String name) {
        return this.dataModel.getInstance(InfoTypeImpl.class).initialize(name);
    }

    @Override
    public InfoType findInfoType(String name) {
        return this.dataModel.mapper(InfoType.class).getUnique("name", name).orNull();
    }

    @Override
    public InfoType findInfoTypeById(long infoTypeId) {
        return this.dataModel.mapper(InfoType.class).getUnique("id", infoTypeId).orNull();
    }

    @Override
    public LogBook findLogBookById(long id) {
        return this.dataModel.mapper(LogBook.class).getUnique("id", id).orNull();
    }

    @Override
    public List<LogBook> findLogBooksByDevice(Device device) {
        return this.dataModel.mapper(LogBook.class).find("device", device);
    }

    @Override
    public ComTaskExecution findComTaskExecution(long id) {
        return this.dataModel.mapper(ComTaskExecution.class).getUnique("id", id).orNull();
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByDevice(Device device) {
        Condition condition = where("device").isEqualTo(device).and(where("obsoleteDate").isNull());
        return this.getDataModel().mapper(ComTaskExecution.class).select(condition);
    }

    @Override
    public List<ComTaskExecution> findAllComTaskExecutionsIncludingObsoleteForDevice(Device device) {
        return this.getDataModel().mapper(ComTaskExecution.class).find("device", device);
    }

    @Override
    public ComTaskExecution attemptLockComTaskExecution(ComTaskExecution comTaskExecution, ComPort comPort) {
        Optional<ComTaskExecution> lockResult = this.getDataModel().mapper(ComTaskExecution.class).lockNoWait(comTaskExecution.getId());
        if (lockResult.isPresent()) {
            ComTaskExecution lockedComTaskExecution = lockResult.get();
            if (lockedComTaskExecution.getExecutingComPort() == null) {
                ((ServerComTaskExecution) lockedComTaskExecution).setLockedComPort(comPort);
                return lockedComTaskExecution;
            } else {
                // No database lock but business lock is already set
                return null;
            }
        } else {
            // ComTaskExecution no longer exists, attempt to lock fails
            return null;
        }
    }

    @Override
    public void unlockComTaskExecution(ComTaskExecution comTaskExecution) {
        ((ComTaskExecutionImpl) comTaskExecution).setLockedComPort(null);
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByConnectionTask(ConnectionTask<?, ?> connectionTask) {
        return this.dataModel.mapper(ComTaskExecution.class).find(ComTaskExecutionFields.CONNECTIONTASK.fieldName(), connectionTask);
    }

    @Override
    public List<ComTaskExecution> findComTasksByDefaultConnectionTask(Device device) {
        return this.dataModel.mapper(ComTaskExecution.class).find(ComTaskExecutionFields.DEVICE.fieldName(), device, ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName(), true);
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByComSchedule(ComSchedule comSchedule) {
        return this.dataModel.query(ComTaskExecution.class)
                .select(where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule)
                        .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull()));
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByComScheduleWithinRange(ComSchedule comSchedule, long minId, long maxId) {
        return this.dataModel.query(ComTaskExecution.class)
                .select(where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule)
                    .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull())
                    .and(where(ComTaskExecutionFields.ID.fieldName()).between(minId).and(maxId)));
    }


    @Override
    public boolean isLinkedToDevices(ComSchedule comSchedule) {
        return !this.dataModel.query(DeviceInComScheduleImpl.class)
                .select(where(DeviceInComScheduleImpl.Fields.COM_SCHEDULE_REFERENCE.fieldName()).isEqualTo(comSchedule),new Order[0], false, new String[0], 0,1)
                .isEmpty();
    }

    @Override
    public List<ComTask> findAvailableComTasksForComSchedule(ComSchedule comSchedule) {
//        return this.dataModel.query(ComTask.class)
//                .select(Where.where(ComTask));
        return null; // TODO complete me
    }

    @Override
    public Date getPlannedDate(ComSchedule comSchedule) {
        List<ComTaskExecution> comTaskExecutions = dataModel.query(ComTaskExecution.class)
                .select(where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule), new Order[0], false, new String[0], 0, 1);
        if (comTaskExecutions.isEmpty()) {
            return null;
        }
        return comTaskExecutions.get(0).getPlannedNextExecutionTimestamp();
    }

    @Override
    public Fetcher<ComTaskExecution> getPlannedComTaskExecutionsFor(ComPort comPort) {
        List<OutboundComPortPool> comPortPools = this.engineModelService.findContainingComPortPoolsForComPort((OutboundComPort) comPort);
        if (!comPortPools.isEmpty()) {
            long nowInSeconds = this.clock.now().getTime() / DateTimeConstants.MILLIS_PER_SECOND;
            DataMapper<ComTaskExecution> mapper = this.dataModel.mapper(ComTaskExecution.class);
            com.elster.jupiter.util.sql.SqlBuilder sqlBuilder = mapper.builder("cte", "FIRST_ROWS(1)");
            sqlBuilder.append(", ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(" ct");
            sqlBuilder.append(" where ct.status = 0");
            sqlBuilder.append("   and ct.comserver is null");
            sqlBuilder.append("   and ct.obsolete_date is null");
            sqlBuilder.append("   and cte.obsolete_date is null");
            sqlBuilder.append("   and cte.connectiontask = ct.id");
            sqlBuilder.append("   and cte.nextexecutiontimestamp <=");
            sqlBuilder.addLong(nowInSeconds);
            sqlBuilder.append("   and cte.comport is null");
            sqlBuilder.append("   and ct.nextExecutionTimestamp <=");
            sqlBuilder.addLong(nowInSeconds);
            sqlBuilder.append("   and ct.comportpool in (");
            int count = 1;
            for (ComPortPool comPortPool : comPortPools) {
                sqlBuilder.addLong(comPortPool.getId());
                if (count < comPortPools.size()) {
                    sqlBuilder.append(", ");
                }
                count++;
            }
            sqlBuilder.append(") order by cte.nextexecutiontimestamp, cte.priority, cte.connectiontask");
            return mapper.fetcher(sqlBuilder);
        } else {
            return new NoComTaskExecutions();
        }
    }

    @Override
    public List<ComTaskExecution> getPlannedComTaskExecutionsFor(InboundComPort comPort, Device device) {
        if (comPort.isActive()) {
            InboundComPortPool inboundComPortPool = comPort.getComPortPool();
            Date now = this.clock.now();
            Condition condition = Where.where("connectionTask.paused").isEqualTo(false)
                    .and(where("connectionTask.comServer").isNull())
                    .and(where("connectionTask.obsoleteDate").isNull())
                    .and(where("connectionTask.device").isEqualTo(device))
                    .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull())
                    .and(where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(now))
                    .and(where("connectionTask.nextExecutionTimestamp").isLessThanOrEqual(now)
                            .or(where(ComTaskExecutionFields.IGNORENEXTEXECUTIONSPECSFORINBOUND.fieldName()).isEqualTo(true)))
                    .and(where(ComTaskExecutionFields.COMPORT.fieldName()).isNull())
                    .and(where("connectionTask.comPortPool").isEqualTo(inboundComPortPool));
            return this.dataModel.query(ComTaskExecution.class, ConnectionTask.class).select(condition,
                    Order.ascending(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()),
                    Order.ascending(ComTaskExecutionFields.PLANNED_PRIORITY.fieldName()),
                    Order.ascending(ComTaskExecutionFields.CONNECTIONTASK.fieldName()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean areComTasksStillPending(Collection<Long> comTaskExecutionIds) {
        Date now = this.clock.now();
        Condition condition = where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(now)
                .and(ListOperator.IN.contains("id", new ArrayList<>(comTaskExecutionIds)))
                .and(where("connectionTask.comServer").isNull());
        return !this.dataModel.query(ComTaskExecution.class, ConnectionTask.class).select(condition).isEmpty();
    }

    /**
     * Provides an implementation for the Fetcher interface
     * that never returns any {@link ComTaskExecution}.
     */
    private class NoComTaskExecutions implements Fetcher<ComTaskExecution> {
        @Override
        public void close() {
            // Nothing to close because there was nothing to read from.
        }

        @Override
        public Iterator<ComTaskExecution> iterator() {
            return Collections.emptyIterator();
        }

    }

}