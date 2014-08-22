package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.CommunicationTopologyEntry;
import com.energyict.mdc.device.data.ConnectionTaskFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.finders.ConnectionTaskFinder;
import com.energyict.mdc.device.data.impl.finders.DeviceFinder;
import com.energyict.mdc.device.data.impl.finders.LoadProfileFinder;
import com.energyict.mdc.device.data.impl.finders.LogBookFinder;
import com.energyict.mdc.device.data.impl.finders.ProtocolDialectPropertiesFinder;
import com.energyict.mdc.device.data.impl.finders.SecuritySetFinder;
import com.energyict.mdc.device.data.impl.security.SecurityPropertyService;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionFilterMatchCounterSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskFilterMatchCounterSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskFilterSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.ConnectionTaskImpl;
import com.energyict.mdc.device.data.impl.tasks.ServerComTaskStatus;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskStatus;
import com.energyict.mdc.device.data.impl.tasks.TimedOutTasksSqlBuilder;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionBuilderImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionSessionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;
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
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

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
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.joda.time.DateTimeConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link DeviceDataService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:27)
 */
@Component(name="com.energyict.mdc.device.data", service = {DeviceDataService.class, ReferencePropertySpecFinderProvider.class, InstallService.class}, property = "name=" + DeviceDataService.COMPONENTNAME, immediate = true)
public class DeviceDataServiceImpl implements ServerDeviceDataService, ReferencePropertySpecFinderProvider, InstallService {

    private static final Logger LOGGER = Logger.getLogger(DeviceDataServiceImpl.class.getName());

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;

    private volatile Clock clock;
    private volatile RelationService relationService;
    private volatile ProtocolPluggableService protocolPluggableService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile EngineModelService engineModelService;
    private volatile MeteringService meteringService;
    private volatile ValidationService validationService;
    private volatile SchedulingService schedulingService;
    private volatile MessageService messagingService;
    private volatile SecurityPropertyService securityPropertyService;

    public DeviceDataServiceImpl() {
    }

    @Inject
    public DeviceDataServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, Clock clock,
                                 RelationService relationService, ProtocolPluggableService protocolPluggableService,
                                 EngineModelService engineModelService, DeviceConfigurationService deviceConfigurationService,
                                 MeteringService meteringService, ValidationService validationService,
                                 SchedulingService schedulingService, MessageService messageService,
                                 SecurityPropertyService securityPropertyService) {
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
        this.setValidationService(validationService);
        this.setSchedulingService(schedulingService);
        this.setMessagingService(messageService);
        this.setSecurityPropertyService(securityPropertyService);
        this.activate();
        this.install(true);
    }

    @Override
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
        finders.add(new DeviceFinder(this.dataModel));
        finders.add(new LoadProfileFinder(this.dataModel));
        finders.add(new LogBookFinder(this.dataModel));
        finders.add(new ConnectionTaskFinder(this.dataModel));
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
                        paged(0, 1);
        List<Device> allDevices = page.find();
        return !allDevices.isEmpty();
    }

    @Override
    public void releaseInterruptedConnectionTasks(ComServer comServer) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_CONNECTIONTASK.name() + " SET comserver = NULL WHERE comserver = ");
        sqlBuilder.addLong(comServer.getId());
        this.executeUpdate(sqlBuilder);
    }

    @Override
    public void releaseInterruptedComTasks(ComServer comServer) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_COMTASKEXEC.name() + " SET comport = NULL, executionStart = null WHERE comport in (select id from mdc_comport where COMSERVERID = ");
        sqlBuilder.addLong(comServer.getId());
        sqlBuilder.append(")");
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
        if (waitTime <= 0) {
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
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                statement.executeUpdate();
                // Don't care about how many rows were updated and if that matches the expected number of updates
            }
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
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
        Condition condition = where("deviceId").isEqualTo(device.getId()).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull()).and(where("partialConnectionTask").isEqualTo(partialConnectionTask));
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
        Condition condition = where("deviceId").isEqualTo(device.getId()).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.getDataModel().mapper(ConnectionTask.class).select(condition);
    }

    @Override
    public List<ConnectionTask> findAllConnectionTasksByDevice(Device device) {
        return this.getDataModel().mapper(ConnectionTask.class).find("deviceId", device.getId());
    }

    @Override
    public List<InboundConnectionTask> findInboundConnectionTasksByDevice(Device device) {
        Condition condition = where("deviceId").isEqualTo(device.getId()).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.getDataModel().mapper(InboundConnectionTask.class).select(condition);
    }

    @Override
    public List<ScheduledConnectionTask> findScheduledConnectionTasksByDevice(Device device) {
        Condition condition = where("deviceId").isEqualTo(device.getId()).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.getDataModel().mapper(ScheduledConnectionTask.class).select(condition);
    }

    @Override
    public ConnectionTask findDefaultConnectionTaskForDevice(Device device) {
        Condition condition = where("deviceId").isEqualTo(device.getId()).and(where("isDefault").isEqualTo(true)).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
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
    public List<ConnectionTask> findConnectionTasksByStatus(TaskStatus status) {
        return this.getDataModel().mapper(ConnectionTask.class).select(ServerConnectionTaskStatus.forTaskStatus(status).condition());
    }

    @Override
    public Map<TaskStatus, Long> getComTaskExecutionStatusCount() {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = EnumSet.allOf(TaskStatus.class);
        return this.getComTaskExecutionStatusCount(filter);
    }

    @Override
    public Map<TaskStatus, Long> getComTaskExecutionStatusCount(ComTaskExecutionFilterSpecification filter) {
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerComTaskStatus taskStatus : this.taskStatusesForCounting(filter)) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = new ClauseAwareSqlBuilder(new SqlBuilder());
                this.countByFilterAndTaskStatusSqlBuilder(sqlBuilder, filter, taskStatus);
            }
            else {
                sqlBuilder.unionAll();
                this.countByFilterAndTaskStatusSqlBuilder(sqlBuilder, filter, taskStatus);
            }
        }
        return this.addMissingTaskStatusCounters(this.fetchTaskStatusCounters(sqlBuilder));
    }

    private Set<ServerComTaskStatus> taskStatusesForCounting (ComTaskExecutionFilterSpecification filter) {
        Set<ServerComTaskStatus> taskStatuses = EnumSet.noneOf(ServerComTaskStatus.class);
        for (TaskStatus taskStatus : filter.taskStatuses) {
            taskStatuses.add(ServerComTaskStatus.forTaskStatus(taskStatus));
        }
        return taskStatuses;
    }

    public void countByFilterAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ComTaskExecutionFilterSpecification filter, ServerComTaskStatus taskStatus) {
        ComTaskExecutionFilterMatchCounterSqlBuilder countingFilter = new ComTaskExecutionFilterMatchCounterSqlBuilder(taskStatus, this.clock);
        countingFilter.appendTo(sqlBuilder);
    }

    @Override
    public Map<TaskStatus, Long> getConnectionTaskStatusCount() {
        ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
        filter.useLastComSession = false;
        filter.taskStatuses=EnumSet.allOf(TaskStatus.class);
        return this.getConnectionTaskStatusCount(filter);
    }

    @Override
    public Map<TaskStatus, Long> getConnectionTaskStatusCount(ConnectionTaskFilterSpecification filter) {
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerConnectionTaskStatus taskStatus : this.taskStatusesForCounting(filter)) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = new ClauseAwareSqlBuilder(new SqlBuilder());
                this.countByFilterAndTaskStatusSqlBuilder(sqlBuilder, filter, taskStatus);
            }
            else {
                sqlBuilder.unionAll();
                this.countByFilterAndTaskStatusSqlBuilder(sqlBuilder, filter, taskStatus);
            }
        }
        return this.addMissingTaskStatusCounters(this.fetchTaskStatusCounters(sqlBuilder));
    }

    private Set<ServerConnectionTaskStatus> taskStatusesForCounting (ConnectionTaskFilterSpecification filter) {
        Set<ServerConnectionTaskStatus> taskStatuses = EnumSet.noneOf(ServerConnectionTaskStatus.class);
        for (TaskStatus taskStatus : filter.taskStatuses) {
            taskStatuses.add(ServerConnectionTaskStatus.forTaskStatus(taskStatus));
        }
        return taskStatuses;
    }

    public void countByFilterAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ConnectionTaskFilterSpecification filter, ServerConnectionTaskStatus taskStatus) {
        ConnectionTaskFilterMatchCounterSqlBuilder countingFilter = new ConnectionTaskFilterMatchCounterSqlBuilder(taskStatus, filter, this.clock);
        countingFilter.appendTo(sqlBuilder);
    }

    private Map<TaskStatus, Long> fetchTaskStatusCounters(ClauseAwareSqlBuilder builder) {
        Map<TaskStatus, Long> counters = new HashMap<>();
        try (PreparedStatement stmnt = builder.prepare(this.dataModel.getConnection(false))) {
            this.fetchTaskStatusCounters(stmnt, counters);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return counters;
    }

    private void fetchTaskStatusCounters(PreparedStatement statement, Map<TaskStatus, Long> counters) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String taskStatusName = resultSet.getString(1);
                long counter = resultSet.getLong(2);
                counters.put(TaskStatus.valueOf(taskStatusName), counter);
            }
        }
    }

    private Map<TaskStatus, Long> addMissingTaskStatusCounters(Map<TaskStatus, Long> counters) {
        for (TaskStatus missing : this.taskStatusComplement(counters.keySet())) {
            counters.put(missing, 0L);
        }
        return counters;
    }

    private EnumSet<TaskStatus> taskStatusComplement(Set<TaskStatus> taskStatuses) {
        if (taskStatuses.isEmpty()) {
            return EnumSet.allOf(TaskStatus.class);
        }
        else {
            return EnumSet.complementOf(EnumSet.copyOf(taskStatuses));
        }
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByFilter(ConnectionTaskFilterSpecification filter, int pageStart, int pageSize) {
        ConnectionTaskFilterSqlBuilder sqlBuilder = new ConnectionTaskFilterSqlBuilder(filter, this.clock);
        DataMapper<ConnectionTask> dataMapper = this.dataModel.mapper(ConnectionTask.class);
        return this.fetchConnectionTasks(dataMapper, sqlBuilder.build(dataMapper, pageStart, pageSize));
    }

    private List<ConnectionTask> fetchConnectionTasks(DataMapper<ConnectionTask> dataMapper, SqlBuilder sqlBuilder) {
        LOGGER.finest(sqlBuilder.getText());    // My impression is that ORM is not logging the sql as an event when using the fetcher
        Iterator<ConnectionTask> connectionTaskIterator = dataMapper.fetcher(sqlBuilder).iterator();
        List<ConnectionTask> connectionTasks = new ArrayList<>();
        while (connectionTaskIterator.hasNext()) {
            connectionTasks.add(connectionTaskIterator.next());
        }
        return connectionTasks;
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
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append("  where device = " + TableSpecs.DDC_COMTASKEXEC.name() + ".device");
        sqlBuilder.append("    and partialconnectiontask = ?");   // Match the connection task
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.addLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
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
        sqlBuilder.addLong(partialConnectionTask.getId());
        sqlBuilder.append(" where comtask = ?");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
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
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
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
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
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
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append("  where device = " + TableSpecs.DDC_COMTASKEXEC.name() + ".device");
        sqlBuilder.append("    and partialconnectiontask = ?");   // Match the connection task
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.addLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
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
        sqlBuilder.addLong(newPartialConnectionTask.getId());
        // Avoid comTaskExecutions that use the default connection
        sqlBuilder.append(" where useDefaultConnectionTask = 0");
        sqlBuilder.append("   and comtask = ?");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and connectionTask = ");
        sqlBuilder.append("     (select id from " + TableSpecs.DDC_CONNECTIONTASK.name() + "");
        sqlBuilder.append("       where device = " + TableSpecs.DDC_COMTASKEXEC.name() + ".device");
        sqlBuilder.append("         and partialconnectiontask = ?");   // Match the previous connection task
        sqlBuilder.append("         and obsolete_date is null)");
        sqlBuilder.addLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
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
        sqlBuilder.append(" where ctn.id = ");
        sqlBuilder.addLong(comTaskEnablement.getId());
        sqlBuilder.append(" and cte.obsolete_date is null");
        try (PreparedStatement statement = sqlBuilder.prepare(this.dataModel.getConnection(true))) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count != 0;
                }
            }
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return false;
    }

    @Override
    public Optional<ScheduledComTaskExecutionIdRange> getScheduledComTaskExecutionIdRange(long comScheduleId) {
        try (PreparedStatement preparedStatement = this.getMinMaxComTaskExecutionIdPreparedStatement()) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.first();  // There is always at least one row since we are counting
                long minId = resultSet.getLong(0);
                if (resultSet.wasNull()) {
                    return Optional.absent();    // There were not ComTaskExecutions
                }
                else {
                    long maxId = resultSet.getLong(1);
                    return Optional.of(new ScheduledComTaskExecutionIdRange(comScheduleId, minId, maxId));
                }
            }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private PreparedStatement getMinMaxComTaskExecutionIdPreparedStatement() throws SQLException {
        return this.dataModel.getConnection(true).prepareStatement(this.getMinMaxComTaskExecutionIdStatement());
    }

    private String getMinMaxComTaskExecutionIdStatement() {
        return "SELECT MIN(id), MAX(id) FROM " + TableSpecs.DDC_COMTASKEXEC.name() + " WHERE comschedule = ? AND obsolete_date IS NULL";
    }

    @Override
    public void obsoleteComTaskExecutionsInRange(ScheduledComTaskExecutionIdRange idRange) {
        try (PreparedStatement preparedStatement = this.getObsoleteComTaskExecutionInRangePreparedStatement()) {
            preparedStatement.setDate(1, this.toSqlDate());
            preparedStatement.setLong(2, idRange.comScheduleId);
            preparedStatement.setLong(3, idRange.minId);
            preparedStatement.setLong(4, idRange.maxId);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private java.sql.Date toSqlDate() {
        return new java.sql.Date(this.clock.now().getTime());
    }

    private PreparedStatement getObsoleteComTaskExecutionInRangePreparedStatement() throws SQLException {
        return this.dataModel.getConnection(true).prepareStatement(this.getObsoleteComTaskExecutionInRangeStatement());
    }

    private String getObsoleteComTaskExecutionInRangeStatement() {
        return "UPDATE " + TableSpecs.DDC_COMTASKEXEC.name() + " SET OBSOLETE_DATE = ? WHERE comschedule = ? AND id BETWEEN ? AND ?";
    }

    @Override
    public boolean hasComTaskExecutions(ComSchedule comSchedule) {
        Condition condition = where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        List<ComTaskExecution> comTaskExecutions = this.getDataModel().query(ComTaskExecution.class).select(condition, new Order[0], false, new String[0], 1, 1);
        return !comTaskExecutions.isEmpty();
    }

    @Override
    public boolean hasConnectionTasks(PartialConnectionTask partialConnectionTask) {
        Condition condition = where(ConnectionTaskFields.PARTIAL_CONNECTION_TASK.fieldName()).isEqualTo(partialConnectionTask).and(where(ConnectionTaskFields.OBSOLETE_DATE.fieldName()).isNull());
        List<ConnectionTask> connectionTasks = this.getDataModel().query(ConnectionTask.class).select(condition, new Order[0], false, new String[0], 1, 1);
        return !connectionTasks.isEmpty();
    }

    @Override
    public void preferredPriorityChanged(ComTask comTask, DeviceConfiguration deviceConfiguration, int previousPreferredPriority, int newPreferredPriority) {
        this.executeUpdate(this.preferredPriorityChangedSqlBuilder(comTask, deviceConfiguration, previousPreferredPriority, newPreferredPriority));
    }

    private SqlBuilder preferredPriorityChangedSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, int previousPreferredPriority, int newPreferredPriority) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set priority = ?");
        sqlBuilder.addInt(newPreferredPriority);
        sqlBuilder.append("   and priority = ?");  // Match the previous priority
        sqlBuilder.addInt(previousPreferredPriority);
        sqlBuilder.append("   and comtask = ?");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from " + TableSpecs.DDC_DEVICE.name() + " where deviceConfigId = ?)");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
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
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append("   and comtask = ?");
        sqlBuilder.addLong(comTask.getId());
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
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append("   and comtask = ?");
        sqlBuilder.addLong(comTask.getId());
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

    @Override
    public Thesaurus getThesaurus() {
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
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
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
                bind(ValidationService.class).toInstance(validationService);
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
        Condition condition = where("device").isEqualTo(device).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
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
        return !this.dataModel.mapper(ScheduledComTaskExecution.class).find(ComTaskExecutionFields.COM_SCHEDULE.fieldName(), comSchedule).isEmpty();
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
            com.elster.jupiter.util.sql.SqlBuilder sqlBuilder = mapper.builder("cte", "LEADING(cte) USE_NL(ct)");
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

    @Override
    public Finder<Device> findDevicesByDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        return DefaultFinder.of(Device.class, where("deviceConfiguration").isEqualTo(deviceConfiguration), this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public List<ComSession> findAllFor(ConnectionTask<?, ?> connectionTask) {
        return dataModel.mapper(ComSession.class).select(where("connectionTask").isEqualTo(connectionTask));
    }

    @Override
    public Optional<ComSession> getLastComSession(ConnectionTask<?, ?> connectionTask) {
        Condition condition = Where.where(ComSessionImpl.Fields.CONNECTION_TASK.fieldName()).isEqualTo(connectionTask);
        Finder<ComSession> page =
                DefaultFinder.
                        of(ComSession.class, condition, this.dataModel).
                        sorted(ComSessionImpl.Fields.MODIFICATION_DATE.fieldName(), false).
                        paged(1, 1);
        List<ComSession> allComSessions = page.find();
        if (allComSessions.isEmpty()) {
            return Optional.absent();
        }
        else {
            return Optional.of(allComSessions.get(0));
        }
    }

    @Override
    public Optional<ComTaskExecutionSession> findLastSessionFor(ComTaskExecution comTaskExecution) {
        Condition condition = Where.where(ComTaskExecutionSessionImpl.Fields.COM_TASK_EXECUTION.fieldName()).isEqualTo(comTaskExecution);
        Finder<ComTaskExecutionSession> page =
                DefaultFinder.
                        of(ComTaskExecutionSession.class, condition, this.dataModel).
                        sorted(ComTaskExecutionSessionImpl.Fields.COM_TASK_EXECUTION.fieldName(), false).
                        paged(1, 1);
        List<ComTaskExecutionSession> allSessions = page.find();
        if (allSessions.isEmpty()) {
            return Optional.absent();
        }
        else {
            return Optional.of(allSessions.get(0));
        }
    }

    @Override
    public ComSessionBuilder buildComSession(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Date startTime) {
        return new ComSessionBuilderImpl(dataModel, connectionTask, comPortPool, comPort, startTime);
    }

    @Override
    public Optional<ComSession> findComSession(long id) {
        return dataModel.mapper(ComSession.class).getOptional(id);
    }

    @Override
    public List<ComSession> findComSessions(ComPort comPort) {
        return this.dataModel.mapper(ComSession.class).find("comPort", comPort);
    }

    @Override
    public List<ComSession> findComSessions(ComPortPool comPortPool) {
        return this.dataModel.mapper(ComSession.class).find("comPortPool", comPortPool);
    }

    @Override
    public long countConnectionTasksLastComSessionsWithAtLeastOneFailedTask() {
        SqlBuilder sqlBuilder = new SqlBuilder("select count(*) from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator from ");
        sqlBuilder.append(TableSpecs.DDC_COMSESSION.name());
        sqlBuilder.append(" cs where cs.SUCCESSINDICATOR = 0 and exists (select * from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
        sqlBuilder.append(" ctes where ctes.comsession = cs.id and ctes.successindicator <> 0) group by connectiontask) t");
        try (PreparedStatement stmnt = sqlBuilder.prepare(this.dataModel.getConnection(false))) {
            try (ResultSet resultSet = stmnt.executeQuery()) {
                while (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return 0;
    }

    @Override
    public Map<ComSession.SuccessIndicator, Long> getConnectionTaskLastComSessionSuccessIndicatorCount() {
        SqlBuilder sqlBuilder = new SqlBuilder("select t.successIndicator, count(*) from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator from ");
        sqlBuilder.append(TableSpecs.DDC_COMSESSION.name());
        sqlBuilder.append(" cs group by connectiontask) t group by t.successIndicator");
        return this.addMissingSuccessIndicatorCounters(this.fetchSuccessIndicatorCounters(sqlBuilder));
    }

    private Map<ComSession.SuccessIndicator, Long> fetchSuccessIndicatorCounters(SqlBuilder builder) {
        Map<ComSession.SuccessIndicator, Long> counters = new HashMap<>();
        try (PreparedStatement stmnt = builder.prepare(this.dataModel.getConnection(false))) {
            this.fetchSuccessIndicatorCounters(stmnt, counters);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return counters;
    }

    private void fetchSuccessIndicatorCounters(PreparedStatement statement, Map<ComSession.SuccessIndicator, Long> counters) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int successIndicatorOrdinal = resultSet.getInt(1);
                long counter = resultSet.getLong(2);
                counters.put(ComSession.SuccessIndicator.fromOrdinal(successIndicatorOrdinal), counter);
            }
        }
    }

    private Map<ComSession.SuccessIndicator, Long> addMissingSuccessIndicatorCounters(Map<ComSession.SuccessIndicator, Long> counters) {
        for (ComSession.SuccessIndicator missing : this.successIndicatorComplement(counters.keySet())) {
            counters.put(missing, 0L);
        }
        return counters;
    }

    private EnumSet<ComSession.SuccessIndicator> successIndicatorComplement(Set<ComSession.SuccessIndicator> successIndicators) {
        if (successIndicators.isEmpty()) {
            return EnumSet.allOf(ComSession.SuccessIndicator.class);
        }
        else {
            return EnumSet.complementOf(EnumSet.copyOf(successIndicators));
        }
    }

    @Override
    public Map<ConnectionTypePluggableClass, List<Long>> getConnectionTypeHeatMap() {
        /* For clarity's sake, here is the formatted SQL:
         * select ct.CONNECTIONTYPEPLUGGABLECLASS, cst.successIndicator, count(*)
         *   from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator
         *           from DDC_COMSESSION cs
         *          where not exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0)
         *          group by connectiontask) cst,
         *         DDC_CONNECTIONTASK ct
         *  where ct.id = cst.connectiontask
         *  group by cm.CONNECTIONTYPEPLUGGABLECLASS, cst.successIndicator
         */
        SqlBuilder sqlBuilder = new SqlBuilder("select ct.CONNECTIONTYPEPLUGGABLECLASS, cst.successIndicator, count(*) from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator from ");
        sqlBuilder.append(TableSpecs.DDC_COMSESSION.name());
        sqlBuilder.append(" cs where not exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0) group by connectiontask) cst, DDC_CONNECTIONTASK ct where ct.id = cst.connectiontask group by cm.CONNECTIONTYPEPLUGGABLECLASS, cst.successIndicator");
        Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters = this.fetchHeatMapCounters(sqlBuilder);
        /* Need another similar query that selects the successful last com sessions that have at least one failing task.
         * Again for clarity's sake, the formatted SQL
         * select ct.CONNECTIONTYPEPLUGGABLECLASS, cst.successIndicator, count(*)
         *   from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator
         *           from DDC_COMSESSION cs
         *          where cs.successIndicator = 0
          *           and exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0)
         *          group by connectiontask) cst,
         *         DDC_CONNECTIONTASK ct
         *  where ct.id = cst.connectiontask
         *  group by cm.CONNECTIONTYPEPLUGGABLECLASS, cst.successIndicator
         * Stricto sensu, we do not need to select 'cst.successIndicator' because it will always be 0
         * but that allows us to reuse the fetchConnectionTypeHeatMapCounters method.
         */
        SqlBuilder failingComTasksSqlBuilder = new SqlBuilder("select ct.CONNECTIONTYPEPLUGGABLECLASS, cst.successIndicator, count(*) from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator from ");
        failingComTasksSqlBuilder.append(TableSpecs.DDC_COMSESSION.name());
        failingComTasksSqlBuilder.append(" cs where cs.successIndicator = 0 and exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0) group by connectiontask) cst, DDC_CONNECTIONTASK ct where ct.id = cst.connectiontask group by cm.CONNECTIONTYPEPLUGGABLECLASS, cst.successIndicator");
        Map<Long, Map<ComSession.SuccessIndicator, Long>> remainingCounters = this.fetchHeatMapCounters(failingComTasksSqlBuilder);
        return this.buildConnectionTypeHeatMap(partialCounters, remainingCounters);
    }

    private Map<ConnectionTypePluggableClass, List<Long>> buildConnectionTypeHeatMap(Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters, Map<Long, Map<ComSession.SuccessIndicator, Long>> remainingCounters) {
        Map<ConnectionTypePluggableClass, List<Long>> heatMap = new HashMap<>();
        Set<Long> allConnectionTypePluggableClassIds = this.union(partialCounters.keySet(), remainingCounters.keySet());
        for (Long connectionTypePluggableClassId : allConnectionTypePluggableClassIds) {
            ConnectionTypePluggableClass connectionTypePluggableClass = this.protocolPluggableService.findConnectionTypePluggableClass(connectionTypePluggableClassId);
            heatMap.put(connectionTypePluggableClass, this.orderCounters(partialCounters.get(connectionTypePluggableClassId), remainingCounters.get(connectionTypePluggableClassId)));
        }
        return heatMap;
    }

    @Override
    public Map<DeviceType, List<Long>> getDeviceTypeHeatMap() {
        /* For clarity's sake, here is the formatted SQL:
         * select dev.DEVICETYPE, cst.successIndicator, count(*)
         *   from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator
         *           from DDC_COMSESSION cs
         *          where not exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0)
         *          group by connectiontask) cst,
         *        DDC_CONNECTIONTASK ct, DDC_DEVICE dev
         *  where ct.id = cst.connectiontask
         *    and ct.DEVICE = dev.id
         *  group by dev.DEVICETYPE, cst.successIndicator
         */
        SqlBuilder sqlBuilder = new SqlBuilder("select dev.DEVICETYPE, cst.successIndicator, count(*) from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator from ");
        sqlBuilder.append(TableSpecs.DDC_COMSESSION.name());
        sqlBuilder.append(" cs where not exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0) group by connectiontask) cst, DDC_CONNECTIONTASK ct, DDC_DEVICE dev where ct.id = cst.connectiontask and ct.device = dev.id group by dev.devicetype, cst.successIndicator");
        Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters = this.fetchHeatMapCounters(sqlBuilder);
        /* Need another similar query that selects the successful last com sessions that have at least one failing task.
         * Again for clarity's sake, the formatted SQL
         * select dev.DEVICETYPE, cst.successIndicator, count(*)
         *   from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator
         *           from DDC_COMSESSION cs
         *          where cs.successIndicator = 0
          *           and exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0)
         *          group by connectiontask) cst,
         *        DDC_CONNECTIONTASK ct, DDC_DEVICE dev
         *  where ct.id = cst.connectiontask
         *    and ct.DEVICE = dev.id
         *  group by dev.DEVICETYPE, cst.successIndicator
         * Stricto sensu, we do not need to select 'cst.successIndicator' because it will always be 0
         * but that allows us to reuse the fetchConnectionTypeHeatMapCounters method.
         */
        SqlBuilder failingComTasksSqlBuilder = new SqlBuilder("select dev.DEVICETYPE, cst.successIndicator, count(*) from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator from ");
        failingComTasksSqlBuilder.append(TableSpecs.DDC_COMSESSION.name());
        failingComTasksSqlBuilder.append(" cs where cs.successIndicator = 0 and exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0) group by connectiontask) cst, DDC_CONNECTIONTASK ct, DDC_DEVICE dev where ct.id = cst.connectiontask and ct.device = dev.id group by dev.devicetype, cst.successIndicator");
        Map<Long, Map<ComSession.SuccessIndicator, Long>> remainingCounters = this.fetchHeatMapCounters(failingComTasksSqlBuilder);
        return this.buildDeviceTypeHeatMap(partialCounters, remainingCounters);
    }

    private Map<DeviceType, List<Long>> buildDeviceTypeHeatMap(Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters, Map<Long, Map<ComSession.SuccessIndicator, Long>> remainingCounters) {
        Map<DeviceType, List<Long>> heatMap = new HashMap<>();
        Set<Long> allDeviceTypeIds = this.union(partialCounters.keySet(), remainingCounters.keySet());
        for (Long deviceTypeId : allDeviceTypeIds) {
            DeviceType deviceType = this.deviceConfigurationService.findDeviceType(deviceTypeId);
            heatMap.put(deviceType, this.orderCounters(partialCounters.get(deviceTypeId), remainingCounters.get(deviceTypeId)));
        }
        return heatMap;
    }

    @Override
    public Map<ComPortPool, List<Long>> getComPortPoolHeatMap() {
        /* For clarity's sake, here is the formatted SQL:
         * select ct.COMPORTPOOL, cst.successIndicator, count(*)
         *   from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator
         *           from DDC_COMSESSION cs
         *          where not exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0)
         *          group by connectiontask) cst,
         *        DDC_CONNECTIONTASK ct, DDC_DEVICE dev
         *  where ct.id = cst.connectiontask
         *  group by ct.COMPORTPOOL, cst.successIndicator
         */
        SqlBuilder sqlBuilder = new SqlBuilder("select ct.COMPORTPOOL, cst.successIndicator, count(*) from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator from ");
        sqlBuilder.append(TableSpecs.DDC_COMSESSION.name());
        sqlBuilder.append(" cs where not exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0) group by connectiontask) cst, DDC_CONNECTIONTASK ct where ct.id = cst.connectiontask group by ct.devicetype, cst.successIndicator");
        Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters = this.fetchHeatMapCounters(sqlBuilder);
        /* Need another similar query that selects the successful last com sessions that have at least one failing task.
         * Again for clarity's sake, the formatted SQL
         * select ct.COMPORTPOOL, cst.successIndicator, count(*)
         *   from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator
         *           from DDC_COMSESSION cs
         *          where cs.successIndicator = 0
          *           and exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0)
         *        DDC_CONNECTIONTASK ct, DDC_DEVICE dev
         *  where ct.id = cst.connectiontask
         *  group by ct.COMPORTPOOL, cst.successIndicator
         * Stricto sensu, we do not need to select 'cst.successIndicator' because it will always be 0
         * but that allows us to reuse the fetchConnectionTypeHeatMapCounters method.
         */
        SqlBuilder failingComTasksSqlBuilder = new SqlBuilder("select ct.COMPORTPOOL, cst.successIndicator, count(*) from (select connectiontask, MAX(successindicator) KEEP (DENSE_RANK LAST ORDER BY cs.startdate) successIndicator from ");
        failingComTasksSqlBuilder.append(TableSpecs.DDC_COMSESSION.name());
        failingComTasksSqlBuilder.append(" cs where cs.successIndicator = 0 and exists (select * from DDC_COMTASKEXECSESSION cte where cte.COMSESSION = cs.id and cte.SUCCESSINDICATOR <> 0) group by connectiontask) cst, DDC_CONNECTIONTASK ct where ct.id = cst.connectiontask group by ct.devicetype, cst.successIndicator");
        Map<Long, Map<ComSession.SuccessIndicator, Long>> remainingCounters = this.fetchHeatMapCounters(failingComTasksSqlBuilder);
        return this.buildComPortPoolHeatMap(partialCounters, remainingCounters);
    }

    private Map<ComPortPool, List<Long>> buildComPortPoolHeatMap(Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters, Map<Long, Map<ComSession.SuccessIndicator, Long>> remainingCounters) {
        Map<ComPortPool, List<Long>> heatMap = new HashMap<>();
        Set<Long> allComPortPoolIds = this.union(partialCounters.keySet(), remainingCounters.keySet());
        for (Long comPortPoolId : allComPortPoolIds) {
            ComPortPool comPortPool = this.engineModelService.findComPortPool(comPortPoolId);
            heatMap.put(comPortPool, this.orderCounters(partialCounters.get(comPortPoolId), remainingCounters.get(comPortPoolId)));
        }
        return heatMap;
    }

    private Set<Long> union(Set<Long> businessObjectIds, Set<Long> moreBusinessObjectIds) {
        Set<Long> union = new HashSet<>(businessObjectIds);
        union.addAll(moreBusinessObjectIds);
        return union;
    }

    private List<Long> orderCounters(Map<ComSession.SuccessIndicator, Long> successIndicatorCounters, Map<ComSession.SuccessIndicator, Long> failingTaskCounters) {
        List<Long> counters = new ArrayList<>(ComSession.SuccessIndicator.values().length + 1);
        counters.add(failingTaskCounters.get(ComSession.SuccessIndicator.Success));
        counters.add(successIndicatorCounters.get(ComSession.SuccessIndicator.Success));
        counters.add(successIndicatorCounters.get(ComSession.SuccessIndicator.SetupError));
        counters.add(successIndicatorCounters.get(ComSession.SuccessIndicator.Broken));
        return counters;
    }

    private Map<Long, Map<ComSession.SuccessIndicator, Long>> fetchHeatMapCounters(SqlBuilder builder) {
        try (PreparedStatement stmnt = builder.prepare(this.dataModel.getConnection(false))) {
            return this.fetchHeatMapCounters(stmnt);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private Map<Long, Map<ComSession.SuccessIndicator, Long>> fetchHeatMapCounters(PreparedStatement statement) throws SQLException {
        Map<Long, Map<ComSession.SuccessIndicator, Long>> counters = new HashMap<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long businessObjectId = resultSet.getLong(1);
                int successIndicatorOrdinal = resultSet.getInt(2);
                long counter = resultSet.getLong(3);
                Map<ComSession.SuccessIndicator, Long> successIndicatorCounters = this.getOrPutSuccessIndicatorCounters(businessObjectId, counters);
                successIndicatorCounters.put(ComSession.SuccessIndicator.fromOrdinal(successIndicatorOrdinal), counter);
            }
        }
        return counters;
    }

    private Map<ComSession.SuccessIndicator, Long> getOrPutSuccessIndicatorCounters(long businessObjectId, Map<Long, Map<ComSession.SuccessIndicator, Long>> counters) {
        Map<ComSession.SuccessIndicator, Long> successIndicatorCounters = counters.get(businessObjectId);
        if (successIndicatorCounters == null) {
            successIndicatorCounters = new HashMap<>();
            for (ComSession.SuccessIndicator missing : EnumSet.allOf(ComSession.SuccessIndicator.class)) {
                successIndicatorCounters.put(missing, 0L);
            }
            counters.put(businessObjectId, successIndicatorCounters);
        }
        return successIndicatorCounters;
    }

    @Override
    public List<ComTaskExecutionSession> findByComTaskExecution(ComTaskExecution comTaskExecution) {
        return this.dataModel.
                mapper(ComTaskExecutionSession.class).
                find(
                        ComTaskExecutionSessionImpl.Fields.COM_TASK_EXECUTION.fieldName(), comTaskExecution,
                        Order.descending(ComTaskExecutionSessionImpl.Fields.START_DATE.fieldName()));
    }

    @Override
    public int countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType errorType, Device device, Interval interval) {
        if (CommunicationErrorType.CONNECTION_SETUP_FAILURE.equals(errorType)) {
            /* Slaves always setup the connection via the master.
             * The logging records the failure against the master
             * so there is no way to count the number of slave devices
             * that had a connection setup failure. */
            return 0;
        }
        else {
            int numberOfDevices = 0;
            List<CommunicationTopologyEntry> communicationTopologies = device.getAllCommunicationTopologies(interval);
            for (CommunicationTopologyEntry communicationTopologyEntry : communicationTopologies) {
                List<Device> devices = new ArrayList<>(communicationTopologyEntry.getDevices());
                devices.add(device);
                numberOfDevices = numberOfDevices + this.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(errorType, devices, communicationTopologyEntry.getInterval());
            }
            return numberOfDevices;
        }
    }

    private int countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(CommunicationErrorType errorType, List<Device> devices, Interval interval) {
        switch (errorType) {
            case CONNECTION_FAILURE: {
                return this.countNumberOfDevicesWithConnectionFailures(interval, devices);
            }
            case COMMUNICATION_FAILURE: {
                return this.countNumberOfDevicesWithCommunicationFailuresInGatewayTopology(devices, interval);
            }
            case CONNECTION_SETUP_FAILURE: {
                // Intended fall-through
            }
            default: {
                throw new RuntimeException("Unsupported CommunicationErrorType " + errorType);
            }
        }
    }

    private int countNumberOfDevicesWithConnectionFailures(Interval interval, List<Device> devices) {
        return this.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(
                devices,
                interval,
                where(this.comSessionSuccessIndicatorFieldName()).isEqualTo(ComSession.SuccessIndicator.Broken));
    }

    private int countNumberOfDevicesWithCommunicationFailuresInGatewayTopology(List<Device> devices, Interval interval) {
        return this.countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(
                devices,
                interval,
                where(this.comSessionSuccessIndicatorFieldName()).isNotEqual(ComSession.SuccessIndicator.Success));
    }

    private String comSessionSuccessIndicatorFieldName() {
        return ComTaskExecutionSessionImpl.Fields.SESSION.fieldName() + "." + ComSessionImpl.Fields.SUCCESS_INDICATOR.fieldName();
    }

    private int countNumberOfDevicesWithCommunicationErrorsInGatewayTopology(List<Device> devices, Interval interval, Condition successIndicatorCondition) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(successIndicatorCondition);
        conditions.add(where(ComTaskExecutionSessionImpl.Fields.DEVICE.fieldName()).in(devices));
        if (interval.getStart() != null) {
            conditions.add(where(ComTaskExecutionSessionImpl.Fields.SESSION.fieldName() + "." + ComSessionImpl.Fields.START_DATE.fieldName()).isGreaterThanOrEqual(interval.getStart()));
        }
        if (interval.getEnd() != null) {
            conditions.add(where(ComTaskExecutionSessionImpl.Fields.SESSION.fieldName() + "." + ComSessionImpl.Fields.STOP_DATE.fieldName()).isLessThanOrEqual(interval.getEnd()));
        }
        Condition execSessionCondition = this.andAll(conditions);
        List<ComTaskExecutionSession> comTaskExecutionSessions = this.dataModel.query(ComTaskExecutionSession.class, ComSession.class, Device.class).select(execSessionCondition);
        Set<Long> uniqueDeviceIds = new HashSet<>();
        for (ComTaskExecutionSession comTaskExecutionSession : comTaskExecutionSessions) {
            uniqueDeviceIds.add(comTaskExecutionSession.getDevice().getId());
        }
        return uniqueDeviceIds.size();
    }

    private Condition andAll(List<Condition> conditions) {
        Condition superCondition = null;
        for (Condition condition : conditions) {
            if (superCondition == null) {
                superCondition = condition;
            }
            else {
                superCondition = superCondition.and(condition);
            }
        }
        return superCondition;
    }

}