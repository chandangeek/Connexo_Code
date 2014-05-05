package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.finders.DeviceFinder;
import com.energyict.mdc.device.data.finders.LoadProfileFinder;
import com.energyict.mdc.device.data.finders.LogBookFinder;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionInitiationTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionMethod;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.InboundConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus;
import com.energyict.mdc.device.data.impl.tasks.TimedOutTasksSqlBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.device.BaseDevice;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.inject.Inject;
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
@Component(name="com.energyict.mdc.device.data", service = {DeviceDataService.class, InstallService.class}, property = "name=" + DeviceDataService.COMPONENTNAME)
public class DeviceDataServiceImpl implements ServerDeviceDataService, InstallService {

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
    private volatile Environment environment;

    public DeviceDataServiceImpl() {
    }

    @Inject
    public DeviceDataServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, Clock clock, Environment environment, RelationService relationService, ProtocolPluggableService protocolPluggableService, EngineModelService engineModelService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService, SchedulingService schedulingService) {
        this(ormService, eventService, nlsService, clock, environment, relationService, protocolPluggableService, engineModelService, deviceConfigurationService, meteringService, false, schedulingService);
    }

    public DeviceDataServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, Clock clock, Environment environment, RelationService relationService, ProtocolPluggableService protocolPluggableService, EngineModelService engineModelService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService, boolean createMasterData, SchedulingService schedulingService) {
        super();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setEnvironment(environment);
        this.setRelationService(relationService);
        this.setClock(clock);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setEngineModelService(engineModelService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setMeteringService(meteringService);
        this.setSchedulingService(schedulingService);
        this.activate();
        if (!this.dataModel.isInstalled()) {
            this.install(true, createMasterData);
        }
    }

    @Override
    public void releaseInterruptedConnectionTasks(ComServer comServer) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.MDCCONNECTIONTASK.name() + " SET comserver = NULL WHERE comserver = ?");
        sqlBuilder.bindLong(comServer.getId());
        this.executeUpdate(sqlBuilder);
    }

    @Override
    public void releaseTimedOutConnectionTasks(ComServer outboundCapableComServer) {
        List<ComPortPool> containingComPortPoolsForComServer = this.engineModelService.findContainingComPortPoolsForComServer(outboundCapableComServer);
        for (ComPortPool comPortPool : containingComPortPoolsForComServer) {
            this.releaseTimedOutComTasks((OutboundComPortPool) comPortPool);
        }
    }

    private void releaseTimedOutComTasks(OutboundComPortPool outboundComPortPool) {
        long now = this.toSeconds(this.clock.now());
        int timeOutSeconds = outboundComPortPool.getTaskExecutionTimeout().getSeconds();
        this.executeUpdate(this.releaseTimedOutTasksSqlBuilder(outboundComPortPool, now, timeOutSeconds));
    }

    private SqlBuilder releaseTimedOutTasksSqlBuilder(OutboundComPortPool outboundComPortPool, long now, int timeOutSeconds) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.MDCCONNECTIONTASK.name());
        sqlBuilder.append("   set comserver = null");
        sqlBuilder.append(" where id in (select connectiontask from mdccomtaskexec");
        sqlBuilder.append(" where id in (");
        TimedOutTasksSqlBuilder.appendTimedOutComTaskExecutionSql(sqlBuilder, outboundComPortPool, now, timeOutSeconds);
        sqlBuilder.append("))");
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
    public InboundConnectionTask newInboundConnectionTask(Device device, PartialInboundConnectionTask partialConnectionTask, InboundComPortPool comPortPool) {
        InboundConnectionTaskImpl connectionTask = this.dataModel.getInstance(InboundConnectionTaskImpl.class);
        connectionTask.initialize(device, partialConnectionTask, comPortPool);
        return connectionTask;
    }

    @Override
    public ScheduledConnectionTask newAsapConnectionTask(Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool comPortPool) {
        ScheduledConnectionTaskImpl connectionTask = this.dataModel.getInstance(ScheduledConnectionTaskImpl.class);
        connectionTask.initializeWithAsapStrategy(device, partialConnectionTask, comPortPool);
        return connectionTask;
    }

    @Override
    public ScheduledConnectionTask newMinimizeConnectionTask(Device device, PartialScheduledConnectionTask partialConnectionTask, OutboundComPortPool comPortPool, TemporalExpression temporalExpression) {
        NextExecutionSpecs nextExecutionSpecs = null;
        if (temporalExpression != null) {
            nextExecutionSpecs = this.schedulingService.newNextExecutionSpecs(temporalExpression);
        }
        ScheduledConnectionTaskImpl connectionTask = this.dataModel.getInstance(ScheduledConnectionTaskImpl.class);
        connectionTask.initializeWithMinimizeStrategy(device, partialConnectionTask, comPortPool, nextExecutionSpecs);
        return connectionTask;
    }

    @Override
    public ConnectionInitiationTask newConnectionInitiationTask(Device device, PartialConnectionInitiationTask partialConnectionTask, OutboundComPortPool comPortPool) {
        ConnectionInitiationTaskImpl connectionTask = this.dataModel.getInstance(ConnectionInitiationTaskImpl.class);
        connectionTask.initialize(device, partialConnectionTask, comPortPool);
        return connectionTask;
    }

    @Override
    public Optional<ConnectionTask> findConnectionTask(long id) {
        return this.getDataModel().mapper(ConnectionTask.class).getUnique("id", id);
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
            ComTaskExecution.ComTaskExecutionUpdater comTaskExecutionUpdater = comTaskExecution.getDevice().getComTaskExecutionUpdater(comTaskExecution);
            comTaskExecutionUpdater.setUseDefaultConnectionTask(defaultConnectionTask);
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
        Condition query = Where.where(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName()).isEqualTo(true)
                .and(where(ComTaskExecutionFields.DEVICE.fieldName()).isEqualTo(device)
                        .and((where(ComTaskExecutionFields.CONNECTIONTASK.fieldName()).isNull())
                                .or(where(ComTaskExecutionFields.CONNECTIONTASK.fieldName()).isNotEqual(connectionTask))));
        List<ComTaskExecution> comTaskExecutions = this.dataModel.mapper(ComTaskExecution.class).select(query);
        scheduledComTasks.addAll(comTaskExecutions);
        for (Object physicalConnectedDevice : device.getPhysicalConnectedDevices()) {
            Device slave = (Device) physicalConnectedDevice;
            this.collectComTaskWithDefaultConnectionTaskForCompleteTopology(slave, scheduledComTasks, connectionTask);
        }
    }

    @Override
    public <T extends ConnectionTask> T attemptLockConnectionTask(T connectionTask, ComServer comServer) {
        Optional<ConnectionTask> lockResult = this.getDataModel().mapper(ConnectionTask.class).lockNoWait(connectionTask.getId());
        if (lockResult.isPresent()) {
            T lockedConnectionTask = (T) lockResult.get();
            if (lockedConnectionTask.getExecutingComServer() == null) {
                ((ConnectionTaskImpl) lockedConnectionTask).setExecutingComServer(comServer);
                ((ConnectionTaskImpl) lockedConnectionTask).save();
                return lockedConnectionTask;
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
        sqlBuilder.append(TableSpecs.MDCCOMTASKEXEC.name());
        sqlBuilder.append(" set ");
        sqlBuilder.append(ComTaskExecutionFields.CONNECTIONTASK.fieldName());
        sqlBuilder.append(" = null");
        sqlBuilder.append(" where ");
        sqlBuilder.append("comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and ");
        sqlBuilder.append(ComTaskExecutionFields.CONNECTIONTASK.fieldName());
        sqlBuilder.append(" = (select id from mdcconnectiontask");
        sqlBuilder.append("     where rtu = mdccomtaskexec.rtu");
        sqlBuilder.append("       and partialconnectiontask = ?");   // Match the connection task
        sqlBuilder.append("       and obsolete_date is null)");
        sqlBuilder.bindLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("   and rtu in (select id from eisrtu where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void switchFromDefaultConnectionTaskToPreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        this.executeUpdate(this.switchFromDefaultConnectionTaskToPreferredConnectionTaskSqlBuilder(comTask, deviceConfiguration, partialConnectionTask));
    }

    private SqlBuilder switchFromDefaultConnectionTaskToPreferredConnectionTaskSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.MDCCOMTASKEXEC.name());
        sqlBuilder.append(" set ");
        sqlBuilder.append(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName());
        sqlBuilder.append(" = 0, ");
        sqlBuilder.append(ComTaskExecutionFields.CONNECTIONTASK.fieldName());
        sqlBuilder.append(" = (select id from mdcconnectiontask");
        sqlBuilder.append("     where rtu = mdccomtaskexec.rtu");
        sqlBuilder.append("       and partialconnectiontask = ?");  //Match the connection task against the same device
        sqlBuilder.append("       and obsolete_date is null)");
        sqlBuilder.bindLong(partialConnectionTask.getId());
        sqlBuilder.append(" where comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and rtu in (select id from eisrtu where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void switchOnDefault(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.executeUpdate(this.switchOnDefaultSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder switchOnDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.MDCCOMTASKEXEC.name());
        sqlBuilder.append(" set ");
        sqlBuilder.append(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName());
        sqlBuilder.append(" = 1, ");
        sqlBuilder.append(ComTaskExecutionFields.CONNECTIONTASK.fieldName());
        sqlBuilder.append(" = (select id from mdcconnectiontask");
        sqlBuilder.append("     where rtu = mdccomtaskexec.rtu");
        sqlBuilder.append("       and isdefault = 1");  // Match the default connection task against the same device
        sqlBuilder.append("       and obsolete_date is null)");
        sqlBuilder.append(" where ");
        sqlBuilder.append("comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and rtu in (select id from eisrtu where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void switchOffDefault(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.executeUpdate(this.switchOffDefaultSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder switchOffDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.MDCCOMTASKEXEC.name());
        sqlBuilder.append(" set ");
        sqlBuilder.append(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName());
        sqlBuilder.append(" = 0");
        sqlBuilder.append(" where ");
        sqlBuilder.append("comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and rtu in (select id from eisrtu where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void switchFromPreferredConnectionTaskToDefault(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        this.executeUpdate(this.switchFromPreferredConnectionTaskToDefaultSqlBuilder(comTask, deviceConfiguration, previousPartialConnectionTask));
    }

    private SqlBuilder switchFromPreferredConnectionTaskToDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.MDCCOMTASKEXEC.name());
        sqlBuilder.append(" set ");
        sqlBuilder.append(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName());
        sqlBuilder.append(" = 1, ");
        sqlBuilder.append(ComTaskExecutionFields.CONNECTIONTASK.fieldName());
        sqlBuilder.append(" = (select id from mdcconnectiontask");
        sqlBuilder.append("     where rtu = mdccomtaskexec.rtu");
        sqlBuilder.append("       and isdefault = 1");  // Match the default connection task against the same device
        sqlBuilder.append("       and obsolete_date is null)");
        sqlBuilder.append(" where ");
        sqlBuilder.append("comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and ");
        sqlBuilder.append(ComTaskExecutionFields.CONNECTIONTASK.fieldName());
        sqlBuilder.append(" = (select id from mdcconnectiontask");
        sqlBuilder.append("     where rtu = mdccomtaskexec.rtu");
        sqlBuilder.append("       and partialconnectiontask = ?");   // Match the connection task
        sqlBuilder.append("       and obsolete_date is null)");
        sqlBuilder.bindLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("   and rtu in (select id from eisrtu where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void preferredConnectionTaskChanged(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask, PartialConnectionTask newPartialConnectionTask) {
        this.executeUpdate(this.preferredConnectionTaskChangedSqlBuilder(comTask, deviceConfiguration, previousPartialConnectionTask, newPartialConnectionTask));
    }

    private SqlBuilder preferredConnectionTaskChangedSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask, PartialConnectionTask newPartialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.MDCCOMTASKEXEC.name());
        sqlBuilder.append(" set ");
        sqlBuilder.append(ComTaskExecutionFields.CONNECTIONTASK.fieldName());
        sqlBuilder.append(" = (select id from mdcconnectiontask");
        sqlBuilder.append("     where rtu = mdccomtaskexec.rtu");
        sqlBuilder.append("       and partialconnectiontask = ?");  //Match the connection task against the same device
        sqlBuilder.append("       and obsolete_date is null)");
        sqlBuilder.bindLong(newPartialConnectionTask.getId());
        // Avoid comTaskExecutions that use the default connection
        sqlBuilder.append(" where ");
        sqlBuilder.append(ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName());
        sqlBuilder.append(" = 0");
        sqlBuilder.append("   and comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and ");
        sqlBuilder.append(ComTaskExecutionFields.CONNECTIONTASK.fieldName());
        sqlBuilder.append("   = (select id from mdcconnectiontask");
        sqlBuilder.append("       where rtu = mdccomtaskexec.rtu");
        sqlBuilder.append("         and partialconnectiontask = ?");   // Match the previous connection task
        sqlBuilder.append("         and obsolete_date is null)");
        sqlBuilder.bindLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("   and rtu in (select id from eisrtu where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public boolean hasComTaskExecutions(ComTaskEnablement comTaskEnablement) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("select count(*) from ");
        sqlBuilder.append(TableSpecs.MDCCOMTASKEXEC.name());
        sqlBuilder.append(" cte");
        sqlBuilder.append(" inner join eisrtu rtu on cte.rtu = rtu.id");
        sqlBuilder.append(" inner join eisdeviceconfig dcf on rtu.deviceconfigid = dcf.id");
        sqlBuilder.append(" inner join mdcdevicecommconfig dcc on dcf.id = dcc.deviceconfiguration");
        sqlBuilder.append(" inner join mdccomtaskenablement ctn on dcc.id = ctn.devicecomconfig and cte.comtask = ctn.comtask");
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
        sqlBuilder.append(TableSpecs.MDCCOMTASKEXEC.name());
        sqlBuilder.append(" set ");
        sqlBuilder.append(ComTaskExecutionFields.PRIORITY.fieldName());
        sqlBuilder.append(" = ?");
        sqlBuilder.bindInt(newPreferredPriority);
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append(ComTaskExecutionFields.CONNECTIONTASK.fieldName());
        sqlBuilder.append(" = ?");  // Match the previous priority
        sqlBuilder.bindInt(previousPreferredPriority);
        sqlBuilder.append("   and comtask = ?");  // Match the ComTask
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and rtu in (select id from eisrtu where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        return sqlBuilder;
    }

    @Override
    public void suspendAll(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.executeUpdate(this.suspendAllSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder suspendAllSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.MDCCOMTASKEXEC.name());
        sqlBuilder.append(" set ");
        sqlBuilder.append(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName());
        sqlBuilder.append(" = null");
        sqlBuilder.append(" where rtu in (select id from eisrtu where deviceConfigId = ?)");    // against devices of the specified configuration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        sqlBuilder.append("   and comtask = ?");
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and comport is null");    // exclude tasks that are currently executing
        sqlBuilder.append("   and ");
        sqlBuilder.append(ComTaskExecutionFields.PLANNEDNEXTEXECUTIONTIMESTAMP.fieldName());
        sqlBuilder.append(" is not null");  // Exclude tasks that have been put on hold manually
        return sqlBuilder;
    }

    @Override
    public void resumeAll(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.executeUpdate(this.resumeAllSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder resumeAllSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.MDCCOMTASKEXEC.name());
        sqlBuilder.append(" set ");
        sqlBuilder.append(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName());
        sqlBuilder.append(" = ");
        sqlBuilder.append(ComTaskExecutionFields.PLANNEDNEXTEXECUTIONTIMESTAMP.fieldName());
        sqlBuilder.append(" where rtu in (select id from eisrtu where deviceConfigId = ?)");    // against devices of the specified configuration
        sqlBuilder.bindLong(deviceConfiguration.getId());
        sqlBuilder.append("   and comtask = ?");
        sqlBuilder.bindLong(comTask.getId());
        sqlBuilder.append("   and ");
        sqlBuilder.append(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName());
        sqlBuilder.append(" is null");      // Only tasks that were suspended
        sqlBuilder.append("   and ");
        sqlBuilder.append(ComTaskExecutionFields.PLANNEDNEXTEXECUTIONTIMESTAMP.fieldName());
        sqlBuilder.append(" is not null");  // Only tasks that were suspended
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
    public void setEnvironment (Environment environment) {
        this.environment = environment;
    }

    @Reference
    public void setSchedulingService(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                bind(DeviceDataService.class).toInstance(DeviceDataServiceImpl.this);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(RelationService.class).toInstance(relationService);
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(Clock.class).toInstance(clock);
                bind(MeteringService.class).toInstance(meteringService);
                bind(SchedulingService.class).toInstance(schedulingService);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
        registerFinders();
    }

    private void registerFinders() {
        environment.registerFinder(new DeviceFinder(this.dataModel));
        environment.registerFinder(new LoadProfileFinder(this.dataModel));
        environment.registerFinder(new LogBookFinder(this.dataModel));
        environment.registerFinder(new ConnectionMethodFinder());
        environment.registerFinder(new ProtocolDialectPropertiesFinder());
    }

    @Override
    public void install() {
        this.install(false, true);
    }

    private void install(boolean exeuteDdl, boolean createMasterData) {
        new Installer(this.dataModel, this.eventService, this.thesaurus).install(exeuteDdl, createMasterData);
    }

    private class ConnectionMethodFinder implements CanFindByLongPrimaryKey<ConnectionMethod> {
        @Override
        public FactoryIds registrationKey() {
            return FactoryIds.CONNECTION_METHOD;
        }

        @Override
        public Class<ConnectionMethod> valueDomain() {
            return ConnectionMethod.class;
        }

        @Override
        public Optional<ConnectionMethod> findByPrimaryKey(long id) {
            return dataModel.mapper(this.valueDomain()).getUnique("id", id);
        }

    }

    private class ProtocolDialectPropertiesFinder implements CanFindByLongPrimaryKey<ProtocolDialectProperties> {
        @Override
        public FactoryIds registrationKey() {
            return FactoryIds.DEVICE_PROTOCOL_DIALECT;
        }

        @Override
        public Class<ProtocolDialectProperties> valueDomain() {
            return ProtocolDialectProperties.class;
        }

        @Override
        public Optional<ProtocolDialectProperties> findByPrimaryKey(long id) {
            return dataModel.mapper(this.valueDomain()).getUnique("id", id);
        }

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
    public List<BaseDevice<Channel, LoadProfile, Register>> findPhysicalConnectedDevicesFor(Device device) {
        Condition condition = where("gateway").isEqualTo(device).and(where("interval").isEffective());
        List<PhysicalGatewayReference> physicalGatewayReferences = this.dataModel.mapper(PhysicalGatewayReference.class).select(condition);
        if(!physicalGatewayReferences.isEmpty()){
            List<BaseDevice<Channel, LoadProfile, Register>> baseDevices = new ArrayList<>();
            for (PhysicalGatewayReference physicalGatewayReference : physicalGatewayReferences) {
                baseDevices.add(physicalGatewayReference.getOrigin());
            }
            return baseDevices;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<BaseDevice<Channel, LoadProfile, Register>> findCommunicationReferencingDevicesFor(Device device) {
        Condition condition = where("gateway").isEqualTo(device).and(where("interval").isEffective());
        List<CommunicationGatewayReference> communicationGatewayReferences = this.dataModel.mapper(CommunicationGatewayReference.class).select(condition);
        if(!communicationGatewayReferences.isEmpty()){
            List<BaseDevice<Channel, LoadProfile, Register>> baseDevices = new ArrayList<>();
            for (CommunicationGatewayReference communicationGatewayReference : communicationGatewayReferences) {
                baseDevices.add(communicationGatewayReference.getOrigin());
            }
            return baseDevices;
        } else {
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
    public List<ComTaskExecution> findComTaskExecutionsByComSchedule(ComSchedule comSchedule) {
        return this.dataModel.query(ComTaskExecution.class)
                .select(Where.where(ComTaskExecutionFields.COM_SCHEDULE_REFERENCE.fieldName()).isEqualTo(comSchedule)
                        .and(Where.where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull()));
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByComScheduleWithinRange(ComSchedule comSchedule, long minId, long maxId) {
        return this.dataModel.query(ComTaskExecution.class)
                .select(Where.where(ComTaskExecutionFields.COM_SCHEDULE_REFERENCE.fieldName()).isEqualTo(comSchedule)
                    .and(Where.where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull())
                    .and(Where.where(ComTaskExecutionFields.ID.fieldName()).between(minId).and(maxId)));
    }


    @Override
    public boolean isLinkedToDevices(ComSchedule comSchedule) {
        return !this.dataModel.query(DeviceInComScheduleImpl.class)
                .select(Where.where(DeviceInComScheduleImpl.Fields.COM_SCHEDULE_REFERENCE.fieldName()).isEqualTo(comSchedule),new Order[0], false, new String[0], 0,1)
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
                .select(Where.where(ComTaskExecutionFields.COM_SCHEDULE_REFERENCE.fieldName()).isEqualTo(comSchedule), new Order[0], false, new String[0], 0, 1);
        if (comTaskExecutions.isEmpty()) {
            return null;
        }
        return comTaskExecutions.get(0).getPlannedNextExecutionTimestamp();
    }

    @Override
    public List<ComTaskExecution> findComTasksByDefaultConnectionTask(Device device) {
        return this.dataModel.mapper(ComTaskExecution.class).find(ComTaskExecutionFields.DEVICE.fieldName(), device, ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName(), true);
    }
}