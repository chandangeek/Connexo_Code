package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.ConnectionTaskFields;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.EventType;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.finders.ConnectionTaskFinder;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionBuilderImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionImpl;
import com.energyict.mdc.device.data.tasks.ConnectionInitiationTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.OutboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComSessionBuilder;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import org.joda.time.DateTimeConstants;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link ConnectionTaskService} interface.
 * Implementation note: no need for @Component annotation as this
 * component is dynamically registered as part of the activation of the
 * {@link com.energyict.mdc.device.data.impl.DeviceDataModelServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (09:01)
 */
public class ConnectionTaskServiceImpl implements ServerConnectionTaskService {

    private final DeviceDataModelService deviceDataModelService;
    private final EventService eventService;
    private final MeteringService meteringService;
    private final ProtocolPluggableService protocolPluggableService;
    private final Clock clock;

    @Inject
    public ConnectionTaskServiceImpl(DeviceDataModelService deviceDataModelService, EventService eventService, MeteringService meteringService, ProtocolPluggableService protocolPluggableService, Clock clock) {
        super();
        this.deviceDataModelService = deviceDataModelService;
        this.eventService = eventService;
        this.meteringService = meteringService;
        this.protocolPluggableService = protocolPluggableService;
        this.clock = clock;
    }

    @Override
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
        finders.add(new ConnectionTaskFinder(this.deviceDataModelService.dataModel()));
        return finders;
    }

    @Override
    public void releaseInterruptedConnectionTasks(ComServer comServer) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_CONNECTIONTASK.name() + " SET comserver = NULL WHERE comserver = ");
        sqlBuilder.addLong(comServer.getId());
        this.deviceDataModelService.executeUpdate(sqlBuilder);
    }

    @Override
    public void releaseTimedOutConnectionTasks(ComServer outboundCapableComServer) {
        List<ComPortPool> containingComPortPoolsForComServer = this.deviceDataModelService.engineConfigurationService().findContainingComPortPoolsForComServer(outboundCapableComServer);
        for (ComPortPool comPortPool : containingComPortPoolsForComServer) {
            this.releaseTimedOutConnectionTasks((OutboundComPortPool) comPortPool);
        }
    }

    private void releaseTimedOutConnectionTasks(OutboundComPortPool outboundComPortPool) {
        long now = this.toSeconds(this.deviceDataModelService.clock().instant());
        int timeOutSeconds = outboundComPortPool.getTaskExecutionTimeout().getSeconds();
        this.deviceDataModelService.executeUpdate(this.releaseTimedOutConnectionTasksSqlBuilder(outboundComPortPool, now, timeOutSeconds));
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

    private long toSeconds(Instant time) {
        return time.toEpochMilli() / DateTimeConstants.MILLIS_PER_SECOND;
    }

    @Override
    public Optional<ConnectionTask> findConnectionTask(long id) {
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<OutboundConnectionTask> findOutboundConnectionTask(long id) {
        return this.deviceDataModelService.dataModel().mapper(OutboundConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<InboundConnectionTask> findInboundConnectionTask(long id) {
        return this.deviceDataModelService.dataModel().mapper(InboundConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<ScheduledConnectionTask> findScheduledConnectionTask(long id) {
        return this.deviceDataModelService.dataModel().mapper(ScheduledConnectionTask.class).getOptional(id);
    }

    @Override
    public Optional<ConnectionInitiationTask> findConnectionInitiationTask(long id) {
        return this.deviceDataModelService.dataModel().mapper(ConnectionInitiationTask.class).getOptional(id);
    }

    @Override
    public Optional<ConnectionTask> findConnectionTaskForPartialOnDevice(PartialConnectionTask partialConnectionTask, Device device) {
        Condition condition =
                    where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).
                and(where(ConnectionTaskFields.OBSOLETE_DATE.fieldName()).isNull()).
                and(where(ConnectionTaskFields.PARTIAL_CONNECTION_TASK.fieldName()).isEqualTo(partialConnectionTask));
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition).stream().findFirst();
    }

    @Override
    public List<Long> findConnectionTasksForPartialId(long partialConnectionTaskId) {
        List<Long> connectionTaskIds = new ArrayList<>();
        SqlBuilder sqlBuilder = new SqlBuilder("select id from " + TableSpecs.DDC_CONNECTIONTASK + " where OBSOLETE_DATE is null and PARTIALCONNECTIONTASK =");
        sqlBuilder.addLong(partialConnectionTaskId);
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(true)) {
            try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        connectionTaskIds.add(resultSet.getLong(1));
                    }
                }
            }
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return connectionTaskIds;
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByDevice(Device device) {
        Condition condition =
                    where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).
                and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition);
    }

    @Override
    public List<ConnectionTask> findAllConnectionTasksByDevice(Device device) {
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).find(ConnectionTaskFields.DEVICE.fieldName(), device.getId());
    }

    @Override
    public List<InboundConnectionTask> findInboundConnectionTasksByDevice(Device device) {
        Condition condition = where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.deviceDataModelService.dataModel().mapper(InboundConnectionTask.class).select(condition);
    }

    @Override
    public List<ScheduledConnectionTask> findScheduledConnectionTasksByDevice(Device device) {
        Condition condition = where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.deviceDataModelService.dataModel().mapper(ScheduledConnectionTask.class).select(condition);
    }

    @Override
    public Optional<ConnectionTask> findDefaultConnectionTaskForDevice(Device device) {
        Condition condition = where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).
                          and(where("isDefault").isEqualTo(true)).
                          and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        List<ConnectionTask> connectionTasks = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition);
        if (connectionTasks.size() == 1) {
            return Optional.of(connectionTasks.get(0));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByStatus(TaskStatus status) {
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(ServerConnectionTaskStatus.forTaskStatus(status).condition());
    }

    @Override
    public Map<TaskStatus, Long> getConnectionTaskStatusCount() {
        return this.doGetConnectionTaskStatusCount(null);
    }

    @Override
    public Map<TaskStatus, Long> getConnectionTaskStatusCount(EndDeviceGroup deviceGroup) {
        return this.doGetConnectionTaskStatusCount(deviceGroup);
    }

    private Map<TaskStatus, Long> doGetConnectionTaskStatusCount(EndDeviceGroup deviceGroup) {
        ConnectionTaskStatusCountSqlBuilder sqlBuilder =
                new ConnectionTaskStatusCountSqlBuilder(
                        this.taskStatusesForCounting(EnumSet.allOf(TaskStatus.class)),
                        deviceGroup,
                        this);
        return this.addMissingTaskStatusCounters(this.deviceDataModelService.fetchTaskStatusCounters(sqlBuilder));
    }

    private Set<ServerConnectionTaskStatus> taskStatusesForCounting(Set<TaskStatus> taskStatuses) {
        return EnumSet.copyOf(
                taskStatuses
                        .stream()
                        .map(ServerConnectionTaskStatus::forTaskStatus)
                        .collect(Collectors.toList()));
    }

    /**
     * Returns a QueryExecutor that supports building a sub-query to match
     * that the ConnectionTask's device is in a QueryEndDeviceGroup.
     *
     * @return The QueryExecutor
     */
    private QueryExecutor<Device> deviceFromDeviceGroupQueryExecutor() {
        return this.deviceDataModelService.dataModel().query(Device.class, DeviceConfiguration.class, DeviceType.class);
    }

    @Override
    public Map<ComPortPool, Map<TaskStatus, Long>> getComPortPoolBreakdown(Set<TaskStatus> taskStatuses) {
        return this.doGetComPortPoolBreakdown(taskStatuses, null);
    }

    @Override
    public Map<ComPortPool, Map<TaskStatus, Long>> getComPortPoolBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        return this.doGetComPortPoolBreakdown(taskStatuses, deviceGroup);
    }

    private Map<ComPortPool, Map<TaskStatus, Long>> doGetComPortPoolBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        ConnectionTaskBreakdownSqlBuilder sqlBuilder =
                new ConnectionTaskComPortPoolBreakdownSqlBuilder(
                        this.taskStatusesForCounting(taskStatuses),
                        deviceGroup,
                        this);
        return this.injectComPortPoolsAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private Map<ComPortPool, Map<TaskStatus, Long>> injectComPortPoolsAndAddMissing(Map<Long, Map<TaskStatus, Long>> statusBreakdown) {
        Map<Long, ComPortPool> comPortPools =
                this.deviceDataModelService.engineConfigurationService()
                    .findAllComPortPools()
                    .stream()
                    .collect(Collectors.toMap(ComPortPool::getId, Function.identity()));
        return this.injectBreakDownsAndAddMissing(statusBreakdown, comPortPools);
    }

    @Override
    public Map<DeviceType, Map<TaskStatus, Long>> getDeviceTypeBreakdown(Set<TaskStatus> taskStatuses) {
        return this.doGetDeviceTypeBreakdown(taskStatuses, null);
    }

    @Override
    public Map<DeviceType, Map<TaskStatus, Long>> getDeviceTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        return this.doGetDeviceTypeBreakdown(taskStatuses, deviceGroup);
    }

    private Map<DeviceType, Map<TaskStatus, Long>> doGetDeviceTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        ConnectionTaskBreakdownSqlBuilder sqlBuilder =
                new ConnectionTaskDeviceTypeBreakdownSqlBuilder(
                        this.taskStatusesForCounting(taskStatuses),
                        deviceGroup,
                        this);
        return this.injectDeviceTypesAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private Map<DeviceType, Map<TaskStatus, Long>> injectDeviceTypesAndAddMissing(Map<Long, Map<TaskStatus, Long>> statusBreakdown) {
        Map<Long, DeviceType> deviceTypes = this.deviceDataModelService.deviceConfigurationService().findAllDeviceTypes().stream().collect(Collectors.toMap(DeviceType::getId, Function.identity()));
        return this.injectBreakDownsAndAddMissing(statusBreakdown, deviceTypes);
    }

    @Override
    public Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> getConnectionTypeBreakdown(Set<TaskStatus> taskStatuses) {
        return this.doGetConnectionTypeBreakdown(taskStatuses, null);
    }

    @Override
    public Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> getConnectionTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        return this.doGetConnectionTypeBreakdown(taskStatuses, deviceGroup);
    }

    private Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> doGetConnectionTypeBreakdown(Set<TaskStatus> taskStatuses, EndDeviceGroup deviceGroup) {
        ConnectionTaskTypeBreakdownSqlBuilder sqlBuilder =
                new ConnectionTaskTypeBreakdownSqlBuilder(
                        this.taskStatusesForCounting(taskStatuses),
                        deviceGroup,
                        this);
        return this.injectConnectionTypesAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private <BDT> Map<BDT, Map<TaskStatus, Long>> injectBreakDownsAndAddMissing(Map<Long, Map<TaskStatus, Long>> breakdown, Map<Long, BDT> allBreakDowns) {
        Map<BDT, Map<TaskStatus, Long>> breakDownByDeviceType = this.emptyBreakdown(allBreakDowns);
        for (Long breakDownId : breakdown.keySet()) {
            breakDownByDeviceType.put(allBreakDowns.get(breakDownId), breakdown.get(breakDownId));
        }
        return breakDownByDeviceType;
    }

    private <BDT> Map<BDT, Map<TaskStatus, Long>> emptyBreakdown(Map<Long, BDT> breakDowns) {
        Map<BDT, Map<TaskStatus, Long>> emptyBreakdown = new HashMap<>();
        for (BDT breakDown : breakDowns.values()) {
            Map<TaskStatus, Long> emptyCounters = new HashMap<>();
            this.addMissingTaskStatusCounters(emptyCounters);
            emptyBreakdown.put(breakDown, emptyCounters);
        }
        return emptyBreakdown;
    }

    private Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> injectConnectionTypesAndAddMissing(Map<Long, Map<TaskStatus, Long>> statusBreakdown) {
        Map<Long, ConnectionTypePluggableClass> connectionTypePluggableClasses = this.deviceDataModelService.protocolPluggableService().findAllConnectionTypePluggableClasses().stream().collect(Collectors
                .toMap(ConnectionTypePluggableClass::getId, Function.identity()));
        return this.injectBreakDownsAndAddMissing(statusBreakdown, connectionTypePluggableClasses);
    }

    private Map<TaskStatus, Long> addMissingTaskStatusCounters(Map<TaskStatus, Long> counters) {
        return this.deviceDataModelService.addMissingTaskStatusCounters(counters);
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByFilter(ConnectionTaskFilterSpecification filter, int pageStart, int pageSize) {
        ConnectionTaskFilterSqlBuilder sqlBuilder =
                new ConnectionTaskFilterSqlBuilder(
                        filter,
                        this.deviceDataModelService.clock(),
                        this.deviceFromDeviceGroupQueryExecutor());
        DataMapper<ConnectionTask> dataMapper = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class);
        return this.fetchConnectionTasks(dataMapper, sqlBuilder.build(dataMapper, pageStart + 1, pageSize)); // SQL is 1-based
    }

    @Override
    public List<ConnectionTypePluggableClass> findConnectionTypeByFilter(ConnectionTaskFilterSpecification filter) {
        // TODO provide native query....
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = new ArrayList<>();
        List<String> javaClassNames = this.findConnectionTasksByFilter(filter, 0, Integer.MAX_VALUE - 1).stream().map(ct -> ct.getPluggableClass().getJavaClassName()).collect(Collectors.toList());
        this.protocolPluggableService.findAllConnectionTypePluggableClasses().stream().
                filter(pluggableClass -> javaClassNames.contains(pluggableClass.getJavaClassName())).
                forEach(connectionTypePluggableClasses::add);

        return connectionTypePluggableClasses;
    }

    private List<ConnectionTask> fetchConnectionTasks(DataMapper<ConnectionTask> dataMapper, SqlBuilder sqlBuilder) {
        try (Fetcher<ConnectionTask> fetcher = dataMapper.fetcher(sqlBuilder)) {
            Iterator<ConnectionTask> connectionTaskIterator = fetcher.iterator();
            List<ConnectionTask> connectionTasks = new ArrayList<>();
            while (connectionTaskIterator.hasNext()) {
                connectionTasks.add(connectionTaskIterator.next());
            }
            return connectionTasks;
        }
    }

    @Override
    public void setDefaultConnectionTask(ConnectionTask newDefaultConnectionTask) {
        this.doSetDefaultConnectionTask(newDefaultConnectionTask.getDevice(), (ConnectionTaskImpl) newDefaultConnectionTask);
    }

    public void doSetDefaultConnectionTask(final Device device, final ConnectionTaskImpl newDefaultConnectionTask) {
        this.clearOldDefault(device, newDefaultConnectionTask);
        if (newDefaultConnectionTask != null) {
            newDefaultConnectionTask.setAsDefault();
        }
        else {
            this.eventService.postEvent(EventType.CONNECTIONTASK_CLEARDEFAULT.topic(), device);
        }
    }

    private void clearOldDefault(Device device, ConnectionTaskImpl newDefaultConnectionTask) {
        List<ConnectionTask> connectionTasks = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).find(ConnectionTaskFields.DEVICE.fieldName(), device);
        connectionTasks
                .stream()
                .filter(connectionTask -> isPreviousDefault(newDefaultConnectionTask, connectionTask))
                .map(ConnectionTaskImpl.class::cast)
                .forEach(ConnectionTaskImpl::clearDefault);
    }

    @Override
    public void clearDefaultConnectionTask(Device device) {
        this.doSetDefaultConnectionTask(device, null);
    }

    private boolean isPreviousDefault(ConnectionTask newDefaultConnectionTask, ConnectionTask connectionTask) {
        return connectionTask.isDefault()
                && ((newDefaultConnectionTask == null)
                || (connectionTask.getId() != newDefaultConnectionTask.getId()));
    }

    @Override
    public <T extends ConnectionTask> T attemptLockConnectionTask(T connectionTask, ComServer comServer) {
        Optional<ConnectionTask> lockResult = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).lockNoWait(connectionTask.getId());
        if (lockResult.isPresent()) {
            T lockedConnectionTask = (T) lockResult.get();
            if (lockedConnectionTask.getExecutingComServer() == null) {
                ((ConnectionTaskImpl) lockedConnectionTask).updateExecutingComServer(comServer);
                return lockedConnectionTask;
            } else {
                // No database lock but business lock is already set
                return null;
            }
        } else {
            // ConnectionTask no longer exists, attempt to lock fails
            return null;
        }
    }

    @Override
    public void unlockConnectionTask(ConnectionTask connectionTask) {
        this.unlockConnectionTask((ConnectionTaskImpl) connectionTask);
    }

    private void unlockConnectionTask(ConnectionTaskImpl connectionTask) {
        connectionTask.updateExecutingComServer(null);
    }

    @Override
    public boolean hasConnectionTasks(ComPortPool comPortPool) {
        List<ConnectionTask> connectionTasks =
                this.deviceDataModelService.dataModel().query(ConnectionTask.class).
                        select(where("comPortPool").isEqualTo(comPortPool),
                                new Order[0], false, new String[0],
                                1, 1);
        return !connectionTasks.isEmpty();
    }

    @Override
    public boolean hasConnectionTasks(PartialConnectionTask partialConnectionTask) {
        Condition condition = where(ConnectionTaskFields.PARTIAL_CONNECTION_TASK.fieldName()).isEqualTo(partialConnectionTask).
                and(where(ConnectionTaskFields.OBSOLETE_DATE.fieldName()).isNull());
        List<ConnectionTask> connectionTasks = this.deviceDataModelService.dataModel().query(ConnectionTask.class).select(condition, new Order[0], false, new String[0], 1, 1);
        return !connectionTasks.isEmpty();
    }

    @Override
    public List<ComSession> findAllSessionsFor(ConnectionTask<?, ?> connectionTask) {
        return this.deviceDataModelService.dataModel().mapper(ComSession.class).
                select(where(ComSessionImpl.Fields.CONNECTION_TASK.fieldName()).isEqualTo(connectionTask));
    }

    @Override
    public ComSessionBuilder buildComSession(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Instant startTime) {
        return new ComSessionBuilderImpl(this.deviceDataModelService.dataModel(), connectionTask, comPortPool, comPort, startTime);
    }

    @Override
    public Optional<ComSession> findComSession(long id) {
        return this.deviceDataModelService.dataModel().mapper(ComSession.class).getOptional(id);
    }

    @Override
    public List<ComSession> findComSessions(ComPort comPort) {
        return this.deviceDataModelService.dataModel().mapper(ComSession.class).find("comPort", comPort);
    }

    @Override
    public List<ComSession> findComSessions(ComPortPool comPortPool) {
        return this.deviceDataModelService.dataModel().mapper(ComSession.class).find("comPortPool", comPortPool);
    }

    @Override
    public long countConnectionTasksLastComSessionsWithAtLeastOneFailedTask() {
        return this.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(false);
    }

    @Override
    public long countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask() {
        return this.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(true);
    }

    private long countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(boolean waitingOnly) {
        SqlBuilder sqlBuilder = new SqlBuilder("select count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct where ct.obsolete_date is null");
        if (waitingOnly) {
            sqlBuilder.append(" and nextexecutiontimestamp >");
            sqlBuilder.addLong(this.toSeconds(this.deviceDataModelService.clock().instant()));
            sqlBuilder.append(" and ct.comserver is null and ct.status = 0 and ct.currentretrycount = 0 and ct.lastExecutionFailed = 0 and ct.lastsuccessfulcommunicationend is not null");
        } else {
            sqlBuilder.append(" and ct.nextexecutiontimestamp is not null");
        }
        sqlBuilder.append(" and ct.lastSessionSuccessIndicator = 0");
        this.appendConnectionTypeHeatMapComTaskExecutionSessionConditions(true, sqlBuilder);
        try (PreparedStatement stmnt = sqlBuilder.prepare(this.deviceDataModelService.dataModel().getConnection(true))) {
            try (ResultSet resultSet = stmnt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return 0;
    }

    private void appendConnectionTypeHeatMapComTaskExecutionSessionConditions(boolean atLeastOneFailingComTask, SqlBuilder sqlBuilder) {
        if (atLeastOneFailingComTask) {
            sqlBuilder.append(" and");
        } else {
            sqlBuilder.append(" and not");
        }
        sqlBuilder.append(" exists (select * from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
        sqlBuilder.append(" ctes where ctes.COMSESSION = ct.lastSession and ctes.SUCCESSINDICATOR > 0)");
    }

    @Override
    public Map<ComSession.SuccessIndicator, Long> getConnectionTaskLastComSessionSuccessIndicatorCount() {
        SqlBuilder sqlBuilder = new SqlBuilder("select ct.lastSessionSuccessIndicator, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct where ct.nextexecutiontimestamp is not null");
        sqlBuilder.append("      and ct.obsolete_date is null");
        sqlBuilder.append("      and ct.lastsession is not null");
        sqlBuilder.append("    group by ct.lastSessionSuccessIndicator");
        return this.addMissingSuccessIndicatorCounters(this.fetchSuccessIndicatorCounters(sqlBuilder));
    }

    @Override
    public Map<ComSession.SuccessIndicator, Long> getConnectionTaskLastComSessionSuccessIndicatorCount(EndDeviceGroup deviceGroup) {
        SqlBuilder sqlBuilder = new SqlBuilder("select ct.lastSessionSuccessIndicator, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct where ct.nextexecutiontimestamp is not null");
        sqlBuilder.append("      and ct.obsolete_date is null");
        sqlBuilder.append("      and ct.lastsession is not null");
        this.appendDeviceGroupConditions(deviceGroup, sqlBuilder, "ct");
        this.appendRestrictedStatesClause(sqlBuilder, "ct");
        sqlBuilder.append(" group by ct.lastSessionSuccessIndicator");
        return this.addMissingSuccessIndicatorCounters(this.fetchSuccessIndicatorCounters(sqlBuilder));
    }

    private Map<ComSession.SuccessIndicator, Long> fetchSuccessIndicatorCounters(SqlBuilder builder) {
        Map<ComSession.SuccessIndicator, Long> counters = new HashMap<>();
        try (PreparedStatement stmnt = builder.prepare(this.deviceDataModelService.dataModel().getConnection(true))) {
            this.fetchSuccessIndicatorCounters(stmnt, counters);
        } catch (SQLException ex) {
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
        } else {
            return EnumSet.complementOf(EnumSet.copyOf(successIndicators));
        }
    }

    @Override
    public Map<ConnectionTypePluggableClass, List<Long>> getConnectionTypeHeatMap() {
        return this.doGetConnectionTypeHeatMap(null);
    }

    @Override
    public Map<ConnectionTypePluggableClass, List<Long>> getConnectionTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.doGetConnectionTypeHeatMap(deviceGroup);
    }

    private Map<ConnectionTypePluggableClass, List<Long>> doGetConnectionTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.fetchConnectionTypeHeatMapCounters(
                this.connectionTypeHeatMapSqlBuilder(
                        deviceGroup,
                        this.connectionTypeHeapMapFailureIndicators(),
                        "connectiontypepluggableclass"));
    }

    private Map<ConnectionTypePluggableClass, List<Long>> fetchConnectionTypeHeatMapCounters(SqlBuilder sqlBuilder) {
        try (PreparedStatement statement = sqlBuilder.prepare(this.deviceDataModelService.dataModel().getConnection(true))) {
            return this.fetchConnectionTypeHeatMapCounters(statement);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private Map<ConnectionTypePluggableClass, List<Long>> fetchConnectionTypeHeatMapCounters(PreparedStatement statement) throws SQLException {
        Map<Long, ConnectionTypePluggableClass> connectionTypePluggableClasses =
                this.deviceDataModelService.protocolPluggableService().findAllConnectionTypePluggableClasses().
                        stream().
                        collect(Collectors.toMap(ConnectionTypePluggableClass::getId, Function.identity()));
        Map<ConnectionTypePluggableClass, List<Long>> heatMap =
                connectionTypePluggableClasses.values().stream().collect(
                        Collectors.toMap(
                                Function.identity(),
                                this::missingSuccessIndicatorCounters));
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long connectionTypePluggableClassId = resultSet.getLong(1);
                heatMap.put(connectionTypePluggableClasses.get(connectionTypePluggableClassId), this.toConnectionTypeHeatMapCounters(resultSet));
            }
        }
        return heatMap;
    }

    private List<Long> toConnectionTypeHeatMapCounters(ResultSet resultSet) throws SQLException {
        int columnIndex = 2;    // id of pluggable class is at 1 and we have already read that
        long completeSuccess = resultSet.getLong(columnIndex);
        columnIndex++;
        long atLeastOneFailure = resultSet.getLong(columnIndex);
        columnIndex++;
        long failureSetupError = resultSet.getLong(columnIndex + this.connectionTypeHeapMapFailureIndicators().indexOf(ComSession.SuccessIndicator.SetupError));
        long failureBroken = resultSet.getLong(columnIndex + this.connectionTypeHeapMapFailureIndicators().indexOf(ComSession.SuccessIndicator.Broken));
        return Arrays.asList(atLeastOneFailure, completeSuccess, failureSetupError, failureBroken);
    }

    private List<Long> missingSuccessIndicatorCounters(ConnectionTypePluggableClass connectionTypePluggableClass) {
        return this.orderSuccessIndicatorCounters(null, null);
    }

    private List<Long> missingSuccessIndicatorCounters(ComPortPool comPortPool) {
        return this.orderSuccessIndicatorCounters(null, null);
    }

    private List<Long> orderSuccessIndicatorCounters(Map<ComSession.SuccessIndicator, Long> successIndicatorCounters, Map<ComSession.SuccessIndicator, Long> failingTaskCounters) {
        List<Long> counters = new ArrayList<>(ComSession.SuccessIndicator.values().length + 1);
        this.addSuccessIndicatorCounter(counters, failingTaskCounters, ComSession.SuccessIndicator.Success);
        this.addSuccessIndicatorCounter(counters, successIndicatorCounters, ComSession.SuccessIndicator.Success);
        this.addSuccessIndicatorCounter(counters, successIndicatorCounters, ComSession.SuccessIndicator.SetupError);
        this.addSuccessIndicatorCounter(counters, successIndicatorCounters, ComSession.SuccessIndicator.Broken);
        return counters;
    }

    private void addSuccessIndicatorCounter(List<Long> targetCounters, Map<ComSession.SuccessIndicator, Long> sourceCounters, ComSession.SuccessIndicator successIndicator) {
        if (sourceCounters != null) {
            targetCounters.add(sourceCounters.get(successIndicator));
        } else {
            targetCounters.add(0L);
        }
    }

    @Override
    public Map<DeviceType, List<Long>> getConnectionsDeviceTypeHeatMap() {
        return this.doGetConnectionsDeviceTypeHeatMap(null);
    }

    @Override
    public Map<DeviceType, List<Long>> getConnectionsDeviceTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.doGetConnectionsDeviceTypeHeatMap(deviceGroup);
    }

    private Map<DeviceType, List<Long>> doGetConnectionsDeviceTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.fetchDeviceTypeHeatMapCounters(
                this.connectionTypeHeatMapSqlBuilder(
                        deviceGroup,
                        this.connectionTypeHeapMapFailureIndicators(),
                        "dev", "devicetype",
                        "DDC_DEVICE dev", "ct.device = dev.id"));
    }

    private Map<DeviceType, List<Long>> fetchDeviceTypeHeatMapCounters(SqlBuilder sqlBuilder) {
        try (PreparedStatement statement = sqlBuilder.prepare(this.deviceDataModelService.dataModel().getConnection(true))) {
            return this.fetchDeviceTypeHeatMapCounters(statement);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private Map<DeviceType, List<Long>> fetchDeviceTypeHeatMapCounters(PreparedStatement statement) throws SQLException {
        Map<Long, DeviceType> deviceTypes =
                this.deviceDataModelService
                        .deviceConfigurationService()
                        .findAllDeviceTypes()
                        .find()
                        .stream()
                        .collect(Collectors.toMap(
                                DeviceType::getId,
                                Function.identity()));
        Map<DeviceType, List<Long>> heatMap =
                deviceTypes.values().stream().collect(
                        Collectors.toMap(
                                Function.identity(),
                                this::missingCompletionCodeCounters));
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long deviceTypeId = resultSet.getLong(1);
                heatMap.put(deviceTypes.get(deviceTypeId), this.toConnectionTypeHeatMapCounters(resultSet));
            }
        }
        return heatMap;
    }

    private List<Long> missingCompletionCodeCounters(DeviceType deviceType) {
        return Stream.of(CompletionCode.values()).map(code -> 0L).collect(Collectors.toList());
    }

    @Override
    public Map<ComPortPool, List<Long>> getConnectionsComPortPoolHeatMap() {
        return this.doGetConnectionsComPortPoolHeatMap(null);
    }

    @Override
    public Map<ComPortPool, List<Long>> getConnectionsComPortPoolHeatMap(EndDeviceGroup deviceGroup) {
        return this.doGetConnectionsComPortPoolHeatMap(deviceGroup);
    }

    private Map<ComPortPool, List<Long>> doGetConnectionsComPortPoolHeatMap(EndDeviceGroup deviceGroup) {
        return this.fetchComPortPoolHeatMapCounters(
                this.connectionTypeHeatMapSqlBuilder(
                        deviceGroup,
                        this.connectionTypeHeapMapFailureIndicators(),
                        "comportpool"));
    }

    private Map<ComPortPool, List<Long>> fetchComPortPoolHeatMapCounters(SqlBuilder sqlBuilder) {
        try (PreparedStatement statement = sqlBuilder.prepare(this.deviceDataModelService.dataModel().getConnection(true))) {
            return this.fetchComPortPoolHeatMapCounters(statement);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private Map<ComPortPool, List<Long>> fetchComPortPoolHeatMapCounters(PreparedStatement statement) throws SQLException {
        Map<Long, ComPortPool> comPortPools =
                this.deviceDataModelService.engineConfigurationService().findAllComPortPools().
                        stream().
                        collect(Collectors.toMap(
                                ComPortPool::getId,
                                Function.identity()));
        Map<ComPortPool, List<Long>> heatMap =
                comPortPools.values().stream().collect(Collectors.toMap(
                        Function.identity(),
                        this::missingSuccessIndicatorCounters));
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long comPortPoolId = resultSet.getLong(1);
                heatMap.put(comPortPools.get(comPortPoolId), this.toConnectionTypeHeatMapCounters(resultSet));
            }
        }
        return heatMap;
    }

    private Optional<AmrSystem> getMdcAmrSystem() {
        return this.meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
    }

    private SqlBuilder connectionTypeHeatMapSqlBuilder(
            final EndDeviceGroup deviceGroup,
            final List<ComSession.SuccessIndicator> failureIndicators,
            final String groupByFieldName) {
        return this.connectionTypeHeatMapSqlBuilder(deviceGroup, failureIndicators, "ct", groupByFieldName, "", "");
    }

    private SqlBuilder connectionTypeHeatMapSqlBuilder(
            final EndDeviceGroup deviceGroup,
            final List<ComSession.SuccessIndicator> failureIndicators,
            final String groupByEntityAliasName,
            final String groupByFieldName,
            final String additionalFromClause,
            final String additionalJoinClause) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("WITH failedTask as (");
        sqlBuilder.append("  select comsession");
        sqlBuilder.append("    from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
        sqlBuilder.append("   where successindicator > 0");
        sqlBuilder.append("   group by comsession)");
        sqlBuilder.append("select ");
        sqlBuilder.append(groupByFieldName);
        sqlBuilder.append(", sum(completeSucces), sum(atLeastOneFailure), ");
        sqlBuilder.append(
                failureIndicators
                        .stream()
                        .map(i -> "sum(" + this.connectionTypeHeatMapFailureIndicatorCaseClauseNameFor(i) + ")")
                        .collect(Collectors.joining(",")));
        sqlBuilder.append("  from (");
        sqlBuilder.append("        select ");
        sqlBuilder.append(groupByEntityAliasName);
        sqlBuilder.append(".");
        sqlBuilder.append(groupByFieldName);
        sqlBuilder.append(", ct.lastSessionSuccessIndicator,");
        sqlBuilder.append("          CASE WHEN ct.lastSessionSuccessIndicator = 0");
        sqlBuilder.append("                AND failedTask.comsession IS NULL");
        sqlBuilder.append("               THEN 1");
        sqlBuilder.append("               ELSE 0");
        sqlBuilder.append("          END completeSucces,");
        sqlBuilder.append("          CASE WHEN ct.lastSessionSuccessIndicator = 0");
        sqlBuilder.append("                AND failedTask.comsession IS NOT NULL");
        sqlBuilder.append("               THEN 1");
        sqlBuilder.append("               ELSE 0");
        sqlBuilder.append("          END atLeastOneFailure,");
        sqlBuilder.append(
                failureIndicators
                        .stream()
                        .map(this::connectionTypeHeatMapFailureIndicatorCaseClause)
                        .collect(Collectors.joining(",")));
        sqlBuilder.append("        from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct, ");
        sqlBuilder.append(additionalFromClause);
        if (!additionalFromClause.isEmpty()) {
            sqlBuilder.append(", ");
        }
        sqlBuilder.append("failedTask where ct.obsolete_date is null and ct.status = 0 and ");
        sqlBuilder.append(additionalJoinClause);
        if (!additionalJoinClause.isEmpty()) {
            sqlBuilder.append(" and ");
        }
        sqlBuilder.append("ct.lastSession = failedTask.comSession(+)");
        this.appendRestrictedStatesClause(sqlBuilder, "ct");
        this.appendDeviceGroupConditions(deviceGroup, sqlBuilder, "ct");
        sqlBuilder.append("       )");
        sqlBuilder.append(" group by " + groupByFieldName);
        return sqlBuilder;
    }

    private String connectionTypeHeatMapFailureIndicatorCaseClause(ComSession.SuccessIndicator indicator) {
        return "CASE WHEN ct.lastSessionSuccessIndicator = " + indicator.ordinal() + " THEN 1 ELSE 0 END " + this.connectionTypeHeatMapFailureIndicatorCaseClauseNameFor(indicator);
    }

    private String connectionTypeHeatMapFailureIndicatorCaseClauseNameFor(ComSession.SuccessIndicator indicator) {
        return "failure" + indicator.name();
    }

    private List<ComSession.SuccessIndicator> connectionTypeHeapMapFailureIndicators() {
        return Arrays.asList(ComSession.SuccessIndicator.SetupError, ComSession.SuccessIndicator.Broken);
    }

    Clock clock() {
        return this.deviceDataModelService.clock();
    }

    void appendDeviceGroupConditions(EndDeviceGroup deviceGroup, SqlBuilder sqlBuilder, String connectionTaskAliasName) {
        if (deviceGroup != null) {
            sqlBuilder.append(" and ");
            sqlBuilder.append(connectionTaskAliasName);
            sqlBuilder.append(".device in (");
            if (deviceGroup instanceof QueryEndDeviceGroup) {
                QueryExecutor<Device> queryExecutor = this.deviceFromDeviceGroupQueryExecutor();
                sqlBuilder.add(queryExecutor.asFragment(((QueryEndDeviceGroup) deviceGroup).getCondition(), "id"));
            }
            else {
                sqlBuilder.add(((EnumeratedEndDeviceGroup) deviceGroup).getAmrIdSubQuery(getMdcAmrSystem().get()).toFragment());
            }
            sqlBuilder.append(")");
        }
    }

    /*
     select ED.amrid
       from MTR_ENDDEVICESTATUS ES,
            (select FS.ID
               from FSM_STATE FS
              where FS.OBSOLETE_TIMESTAMP IS NULL
                and FS.NAME NOT IN ('dlc.default.inStock', 'dlc.default.decommissioned')
            ) FS,
            MTR_ENDDEVICE ED
      where ES.STARTTIME <= 1436517667000
        and ES.ENDTIME > 1436517667000
        and ED.ID = ES.ENDDEVICE
        and ES.STATE = FS.ID;
     */
    void appendRestrictedStatesClause(SqlBuilder sqlBuilder, String connectionTaskAliasName) {
        long currentTime = this.clock.millis();
        sqlBuilder.append(" and ");
        sqlBuilder.append(connectionTaskAliasName);
        sqlBuilder.append(".device in (");
        sqlBuilder.append("select ED.amrid from ");
        sqlBuilder.append("MTR_ENDDEVICESTATUS ES,");
        sqlBuilder.append("(select FS.ID from FSM_STATE FS where FS.OBSOLETE_TIMESTAMP IS NULL and FS.NAME NOT IN (");
        sqlBuilder.addObject(DefaultState.IN_STOCK.getKey());
        sqlBuilder.append(", ");
        sqlBuilder.addObject(DefaultState.DECOMMISSIONED.getKey());
        sqlBuilder.append(")) FS, MTR_ENDDEVICE ED where ES.STARTTIME <= ");
        sqlBuilder.addLong(currentTime);
        sqlBuilder.append(" and ES.ENDTIME > ");
        sqlBuilder.addLong(currentTime);
        sqlBuilder.append(" and ED.ID = ES.ENDDEVICE and ES.STATE = FS.ID)");
    }

}