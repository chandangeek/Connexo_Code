package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.ConnectionTaskFields;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
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
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.joda.time.DateTimeConstants;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    @Inject
    public ConnectionTaskServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
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
        List<ComPortPool> containingComPortPoolsForComServer = this.deviceDataModelService.engineModelService().findContainingComPortPoolsForComServer(outboundCapableComServer);
        for (ComPortPool comPortPool : containingComPortPoolsForComServer) {
            this.releaseTimedOutConnectionTasks((OutboundComPortPool) comPortPool);
        }
    }

    private void releaseTimedOutConnectionTasks(OutboundComPortPool outboundComPortPool) {
        long now = this.toSeconds(this.deviceDataModelService.clock().now());
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

    private long toSeconds(Date time) {
        return time.getTime() / DateTimeConstants.MILLIS_PER_SECOND;
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
        Condition condition = where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).
                          and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull()).
                          and(where(ConnectionTaskFields.PARTIAL_CONNECTION_TASK.fieldName()).isEqualTo(partialConnectionTask));
        List<ConnectionTask> connectionTasks = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition);
        if (connectionTasks.isEmpty()) {
            return Optional.absent();
        }
        else {
            return Optional.of(connectionTasks.get(0));
        }
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByDevice(Device device) {
        Condition condition = where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).
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
    public ConnectionTask findDefaultConnectionTaskForDevice(Device device) {
        Condition condition = where(ConnectionTaskFields.DEVICE.fieldName()).isEqualTo(device).and(where("isDefault").isEqualTo(true)).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        List<ConnectionTask> connectionTasks = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(condition);
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
        return this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).select(ServerConnectionTaskStatus.forTaskStatus(status).condition());
    }

    @Override
    public Map<TaskStatus, Long> getConnectionTaskStatusCount() {
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerConnectionTaskStatus taskStatus : this.taskStatusesForCounting(EnumSet.allOf(TaskStatus.class))) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = new ClauseAwareSqlBuilder(new SqlBuilder());
                this.countByFilterAndTaskStatusSqlBuilder(sqlBuilder, taskStatus);
            }
            else {
                sqlBuilder.unionAll();
                this.countByFilterAndTaskStatusSqlBuilder(sqlBuilder, taskStatus);
            }
        }
        return this.addMissingTaskStatusCounters(this.fetchTaskStatusCounters(sqlBuilder));
    }

    private Set<ServerConnectionTaskStatus> taskStatusesForCounting (Set<TaskStatus> taskStatuses) {
        Set<ServerConnectionTaskStatus> serverTaskStatuses = EnumSet.noneOf(ServerConnectionTaskStatus.class);
        for (TaskStatus taskStatus : taskStatuses) {
            serverTaskStatuses.add(ServerConnectionTaskStatus.forTaskStatus(taskStatus));
        }
        return serverTaskStatuses;
    }

    private void countByFilterAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ServerConnectionTaskStatus taskStatus) {
        ConnectionTaskCurrentStateCounterSqlBuilder countingFilter = new ConnectionTaskCurrentStateCounterSqlBuilder(taskStatus, this.deviceDataModelService.clock());
        countingFilter.appendTo(sqlBuilder);
    }

    @Override
    public Map<ComPortPool, Map<TaskStatus, Long>> getComPortPoolBreakdown(Set<TaskStatus> taskStatuses) {
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerConnectionTaskStatus taskStatus : this.taskStatusesForCounting(taskStatuses)) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = new ClauseAwareSqlBuilder(new SqlBuilder());
                this.countByComPortPoolAndTaskStatusSqlBuilder(sqlBuilder, taskStatus);
            }
            else {
                sqlBuilder.unionAll();
                this.countByComPortPoolAndTaskStatusSqlBuilder(sqlBuilder, taskStatus);
            }
        }
        return this.injectComPortPoolsAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private void countByComPortPoolAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ServerConnectionTaskStatus taskStatus) {
        ConnectionTaskComPortPoolStatusCountSqlBuilder countingFilter = new ConnectionTaskComPortPoolStatusCountSqlBuilder(taskStatus, this.deviceDataModelService.clock());
        countingFilter.appendTo(sqlBuilder);
    }

    private Map<ComPortPool, Map<TaskStatus, Long>> injectComPortPoolsAndAddMissing(Map<Long, Map<TaskStatus, Long>> statusBreakdown) {
        Map<Long, ComPortPool> comPortPools = this.deviceDataModelService.engineModelService().findAllComPortPools().stream().collect(Collectors.toMap(ComPortPool::getId, Function.identity()));
        Map<ComPortPool, Map<TaskStatus, Long>> breakDownByComPortPool = this.emptyComPortPoolBreakdown(comPortPools);
        for (Long comPortPoolId : statusBreakdown.keySet()) {
            breakDownByComPortPool.put(comPortPools.get(comPortPoolId), statusBreakdown.get(comPortPoolId));
        }
        return breakDownByComPortPool;
    }

    private Map<ComPortPool, Map<TaskStatus, Long>> emptyComPortPoolBreakdown(Map<Long, ComPortPool> comPortPools) {
        Map<ComPortPool, Map<TaskStatus, Long>> emptyBreakdown = new HashMap<>();
        for (ComPortPool comPortPool : comPortPools.values()) {
            Map<TaskStatus, Long> emptyCounters = new HashMap<>();
            this.addMissingTaskStatusCounters(emptyCounters);
            emptyBreakdown.put(comPortPool, emptyCounters);
        }
        return emptyBreakdown;
    }

    @Override
    public Map<DeviceType, Map<TaskStatus, Long>> getDeviceTypeBreakdown(Set<TaskStatus> taskStatuses) {
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerConnectionTaskStatus taskStatus : this.taskStatusesForCounting(taskStatuses)) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = new ClauseAwareSqlBuilder(new SqlBuilder());
                this.countByDeviceTypeAndTaskStatusSqlBuilder(sqlBuilder, taskStatus);
            }
            else {
                sqlBuilder.unionAll();
                this.countByDeviceTypeAndTaskStatusSqlBuilder(sqlBuilder, taskStatus);
            }
        }
        return this.injectDeviceTypesAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private void countByDeviceTypeAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ServerConnectionTaskStatus taskStatus) {
        ConnectionTaskDeviceTypeStatusCountSqlBuilder countingFilter = new ConnectionTaskDeviceTypeStatusCountSqlBuilder(taskStatus, this.deviceDataModelService.clock());
        countingFilter.appendTo(sqlBuilder);
    }

    private Map<DeviceType, Map<TaskStatus, Long>> injectDeviceTypesAndAddMissing(Map<Long, Map<TaskStatus, Long>> statusBreakdown) {
        Map<Long, DeviceType> deviceTypes = this.deviceDataModelService.deviceConfigurationService().findAllDeviceTypes().stream().collect(Collectors.toMap(DeviceType::getId, Function.identity()));
        Map<DeviceType, Map<TaskStatus, Long>> breakDownByDeviceType = this.emptyDeviceTypeBreakdown(deviceTypes);
        for (Long DeviceTypeId : statusBreakdown.keySet()) {
            breakDownByDeviceType.put(deviceTypes.get(DeviceTypeId), statusBreakdown.get(DeviceTypeId));
        }
        return breakDownByDeviceType;
    }

    private Map<DeviceType, Map<TaskStatus, Long>> emptyDeviceTypeBreakdown(Map<Long, DeviceType> deviceTypes) {
        Map<DeviceType, Map<TaskStatus, Long>> emptyBreakdown = new HashMap<>();
        for (DeviceType deviceType : deviceTypes.values()) {
            Map<TaskStatus, Long> emptyCounters = new HashMap<>();
            this.addMissingTaskStatusCounters(emptyCounters);
            emptyBreakdown.put(deviceType, emptyCounters);
        }
        return emptyBreakdown;
    }

    @Override
    public Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> getConnectionTypeBreakdown(Set<TaskStatus> taskStatuses) {
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerConnectionTaskStatus taskStatus : this.taskStatusesForCounting(taskStatuses)) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = new ClauseAwareSqlBuilder(new SqlBuilder());
                this.countByConnectionTypeAndTaskStatusSqlBuilder(sqlBuilder, taskStatus);
            }
            else {
                sqlBuilder.unionAll();
                this.countByConnectionTypeAndTaskStatusSqlBuilder(sqlBuilder, taskStatus);
            }
        }
        return this.injectConnectionTypesAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private void countByConnectionTypeAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ServerConnectionTaskStatus taskStatus) {
        ConnectionTaskConnectionTypeStatusCountSqlBuilder countingFilter = new ConnectionTaskConnectionTypeStatusCountSqlBuilder(taskStatus, this.deviceDataModelService.clock());
        countingFilter.appendTo(sqlBuilder);
    }

    private Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> injectConnectionTypesAndAddMissing(Map<Long, Map<TaskStatus, Long>> statusBreakdown) {
        Map<Long, ConnectionTypePluggableClass> connectionTypePluggableClasses = this.deviceDataModelService.protocolPluggableService().findAllConnectionTypePluggableClasses().stream().collect(Collectors.toMap(ConnectionTypePluggableClass::getId, Function.identity()));
        Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> breakDownByConnectionType = this.emptyConnectionTypeBreakdown(connectionTypePluggableClasses);
        for (Long ConnectionTypeId : statusBreakdown.keySet()) {
            breakDownByConnectionType.put(connectionTypePluggableClasses.get(ConnectionTypeId), statusBreakdown.get(ConnectionTypeId));
        }
        return breakDownByConnectionType;
    }

    private Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> emptyConnectionTypeBreakdown(Map<Long, ConnectionTypePluggableClass> connectionTypePluggableClasses) {
        Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> emptyBreakdown = new HashMap<>();
        for (ConnectionTypePluggableClass connectionTypePluggableClass : connectionTypePluggableClasses.values()) {
            Map<TaskStatus, Long> emptyCounters = new HashMap<>();
            this.addMissingTaskStatusCounters(emptyCounters);
            emptyBreakdown.put(connectionTypePluggableClass, emptyCounters);
        }
        return emptyBreakdown;
    }

    private Map<TaskStatus, Long> fetchTaskStatusCounters(ClauseAwareSqlBuilder builder) {
        return this.deviceDataModelService.fetchTaskStatusCounters(builder);
    }

    private Map<TaskStatus, Long> addMissingTaskStatusCounters(Map<TaskStatus, Long> counters) {
        return this.deviceDataModelService.addMissingTaskStatusCounters(counters);
    }

    @Override
    public List<ConnectionTask> findConnectionTasksByFilter(ConnectionTaskFilterSpecification filter, int pageStart, int pageSize) {
        ConnectionTaskFilterSqlBuilder sqlBuilder = new ConnectionTaskFilterSqlBuilder(filter, this.deviceDataModelService.clock());
        DataMapper<ConnectionTask> dataMapper = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class);
        return this.fetchConnectionTasks(dataMapper, sqlBuilder.build(dataMapper, pageStart + 1, pageSize)); // SQL is 1-based
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
        List<ConnectionTask> connectionTasks = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).find(ConnectionTaskFields.DEVICE.fieldName(), device);
        for (ConnectionTask connectionTask : connectionTasks) {
            if (isPreviousDefault(newDefaultConnectionTask, connectionTask)) {
                ((ConnectionTaskImpl) connectionTask).clearDefault();
            }
        }
        this.deviceDataModelService.setOrUpdateDefaultConnectionTaskOnComTaskInDeviceTopology(device, newDefaultConnectionTask);
        if (newDefaultConnectionTask != null) {
            newDefaultConnectionTask.setAsDefault();
        }
    }

    @Override
    public void clearDefaultConnectionTask(Device device) {
        this.doSetDefaultConnectionTask(device, null);
    }

    private boolean isPreviousDefault(ConnectionTask newDefaultConnectionTask, ConnectionTask connectionTask) {
        return  connectionTask.isDefault()
            && (   (newDefaultConnectionTask == null)
                || (connectionTask.getId() != newDefaultConnectionTask.getId()));
    }

    @Override
    public <T extends ConnectionTask> T attemptLockConnectionTask(T connectionTask, ComServer comServer) {
        Optional<ConnectionTask> lockResult = this.deviceDataModelService.dataModel().mapper(ConnectionTask.class).lockNoWait(connectionTask.getId());
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
    public ComSessionBuilder buildComSession(ConnectionTask<?, ?> connectionTask, ComPortPool comPortPool, ComPort comPort, Date startTime) {
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
        sqlBuilder.append(" ct ");
        this.appendConnectionTaskLastComSessionJoinClause(sqlBuilder);
        sqlBuilder.append(" where ct.obsolete_date is null");
        if (waitingOnly) {
            sqlBuilder.append(" and nextexecutiontimestamp >");
            sqlBuilder.addLong(this.toSeconds(this.deviceDataModelService.clock().now()));
            sqlBuilder.append(" and ct.comserver is null and ct.status = 0 and ct.currentretrycount = 0 and ct.lastExecutionFailed = 0 and ct.lastsuccessfulcommunicationend is not null");
        }
        else {
            sqlBuilder.append(" and ct.nextexecutiontimestamp is not null");
        }
        sqlBuilder.append(" and ");
        this.appendConnectionTypeHeatMapComTaskExecutionSessionConditions(true, sqlBuilder);
        try (PreparedStatement stmnt = sqlBuilder.prepare(this.deviceDataModelService.dataModel().getConnection(true))) {
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

    private void appendConnectionTaskLastComSessionJoinClause(SqlBuilder sqlBuilder) {
        sqlBuilder.append(" join ");
        sqlBuilder.append(TableSpecs.DDC_COMSESSION.name());
        sqlBuilder.append(" cs on ct.lastsession = cs.id");
    }

    private void appendConnectionTypeHeatMapComTaskExecutionSessionConditions(boolean atLeastOneFailingComTask, SqlBuilder sqlBuilder) {
        if (atLeastOneFailingComTask) {
            sqlBuilder.append("cs.successindicator = 0 and");
        }
        else {
            sqlBuilder.append(" not");
        }
        sqlBuilder.append(" exists (select * from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
        sqlBuilder.append(" ctes where ctes.COMSESSION = cs.id and ctes.SUCCESSINDICATOR > 0)");
    }

    @Override
    public Map<ComSession.SuccessIndicator, Long> getConnectionTaskLastComSessionSuccessIndicatorCount() {
        SqlBuilder sqlBuilder = new SqlBuilder("select cs.successIndicator, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct ");
        this.appendConnectionTaskLastComSessionJoinClause(sqlBuilder);
        sqlBuilder.append(" where ct.nextexecutiontimestamp is not null and ct.obsolete_date is null group by cs.successindicator");
        return this.addMissingSuccessIndicatorCounters(this.fetchSuccessIndicatorCounters(sqlBuilder));
    }

    private Map<ComSession.SuccessIndicator, Long> fetchSuccessIndicatorCounters(SqlBuilder builder) {
        Map<ComSession.SuccessIndicator, Long> counters = new HashMap<>();
        try (PreparedStatement stmnt = builder.prepare(this.deviceDataModelService.dataModel().getConnection(true))) {
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
        Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters = this.getConnectionTypeHeatMap(false);
        Map<Long, Map<ComSession.SuccessIndicator, Long>> atLeastOneFailingTaskCounters = this.getConnectionTypeHeatMap(true);
        return this.buildConnectionTypeHeatMap(partialCounters, atLeastOneFailingTaskCounters);
    }

    private Map<ConnectionTypePluggableClass, List<Long>> buildConnectionTypeHeatMap(Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters, Map<Long, Map<ComSession.SuccessIndicator, Long>> remainingCounters) {
        Map<Long, ConnectionTypePluggableClass> connectionTypePluggableClasses =
                this.deviceDataModelService.protocolPluggableService().findAllConnectionTypePluggableClasses().
                        stream().
                        collect(Collectors.toMap(ConnectionTypePluggableClass::getId, Function.identity()));
        Map<ConnectionTypePluggableClass, List<Long>> heatMap =
                connectionTypePluggableClasses.values().stream().collect(
                        Collectors.toMap(
                                Function.identity(),
                                this::missingSuccessIndicatorCounters));
        for (Long connectionTypePluggableClassId : this.union(partialCounters.keySet(), remainingCounters.keySet())) {
            ConnectionTypePluggableClass connectionTypePluggableClass = connectionTypePluggableClasses.get(connectionTypePluggableClassId);
            heatMap.put(connectionTypePluggableClass, this.orderSuccessIndicatorCounters(partialCounters.get(connectionTypePluggableClassId), remainingCounters.get(connectionTypePluggableClassId)));
        }
        return heatMap;
    }

    private Map<Long, Map<ComSession.SuccessIndicator, Long>> getConnectionTypeHeatMap(boolean atLeastOneFailingComTask) {
        /* For clarity's sake, here is the formatted SQL when atLeastOneFailingComTask = false:
           SELECT ct.CONNECTIONTYPEPLUGGABLECLASS, cs.successIndicator, count(*)
             FROM DDC_CONNECTIONTASK ct JOIN DDC_COMSESSION cs ON ct.lastcomession = cs.id
            WHERE ct.status = 0
              AND NOT EXISTS (SELECT * FROM DDC_COMTASKEXECSESSION cte
                               WHERE cte.COMSESSION = cs.id
                                 AND cte.SUCCESSINDICATOR > 0)
            GROUP BY ct.CONNECTIONTYPEPLUGGABLECLASS, cs.successIndicator;

           when atLeastOneFailingComTask = false:
           SELECT ct.CONNECTIONTYPEPLUGGABLECLASS, cs.successIndicator, count(*)
             FROM DDC_CONNECTIONTASK ct JOIN DDC_COMSESSION cs ON ct.lastcomession = cs.id
            WHERE ct.status = 0
              AND cs.successindicator = 0
              AND EXISTS (SELECT * FROM DDC_COMTASKEXECSESSION cte
                           WHERE cte.COMSESSION = cs.id
                             AND cte.SUCCESSINDICATOR > 0)
            GROUP BY ct.CONNECTIONTYPEPLUGGABLECLASS, cs.successIndicator;
         */
        SqlBuilder sqlBuilder = new SqlBuilder("select ct.CONNECTIONTYPEPLUGGABLECLASS, cs.successIndicator, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct");
        this.appendConnectionTaskLastComSessionJoinClause(sqlBuilder);
        sqlBuilder.append(" where ct.status = 0 and ");
        this.appendConnectionTypeHeatMapComTaskExecutionSessionConditions(atLeastOneFailingComTask, sqlBuilder);
        sqlBuilder.append(" group by ct.CONNECTIONTYPEPLUGGABLECLASS, cs.successIndicator");
        return this.fetchConnectionTypeHeatMapCounters(sqlBuilder);
    }

    private Map<Long, Map<ComSession.SuccessIndicator, Long>> fetchConnectionTypeHeatMapCounters(SqlBuilder builder) {
        try (PreparedStatement stmnt = builder.prepare(this.deviceDataModelService.dataModel().getConnection(true))) {
            return this.fetchConnectionTypeHeatMapCounters(stmnt);
        }
        catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private Map<Long, Map<ComSession.SuccessIndicator, Long>> fetchConnectionTypeHeatMapCounters(PreparedStatement statement) throws SQLException {
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
        }
        else {
            targetCounters.add(0L);
        }
    }

    @Override
    public Map<DeviceType, List<Long>> getConnectionsDeviceTypeHeatMap() {
        Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters = this.getConnectionsDeviceTypeHeatMap(false);
        Map<Long, Map<ComSession.SuccessIndicator, Long>> atLeastOneFailingComTaskCounters = this.getConnectionsDeviceTypeHeatMap(true);
        return this.buildDeviceTypeHeatMap(partialCounters, atLeastOneFailingComTaskCounters);
    }

    public Map<Long, Map<ComSession.SuccessIndicator, Long>> getConnectionsDeviceTypeHeatMap(boolean atLeastOneFailingComTask) {
        /* For clarity's sake, here is the formatted SQL when atLeastOneFailingComTask == false:
           SELECT dev.DEVICETYPE, cs.successIndicator, count(*)
             FROM DDC_CONNECTIONTASK ct
             JOIN DDC_COMSESSION cs ON ct.lastsession = cs.id
             JOIN DDC_DEVICE dev ON ct.DEVICE = dev.id
            WHERE ct.status = 0
              AND NOT EXISTS (SELECT * FROM DDC_COMTASKEXECSESSION ctes
                               WHERE ctes.COMSESSION = cs.id
                                 AND ctes.SUCCESSINDICATOR > 0)
            GROUP BY dev.DEVICETYPE, cs.successIndicator

            when atLeastOneFailingComTask = true
           SELECT dev.DEVICETYPE, cs.successIndicator, count(*)
             FROM DDC_CONNECTIONTASK ct
             JOIN DDC_COMSESSION cs ON ct.lastsession = cs.id
             JOIN DDC_DEVICE dev ON ct.DEVICE = dev.id
            WHERE ct.status = 0
              AND cs.successindicator = 0
              AND EXISTS (SELECT * FROM DDC_COMTASKEXECSESSION ctes
                               WHERE ctes.COMSESSION = cs.id
                                 AND ctes.SUCCESSINDICATOR > 0)
            GROUP BY dev.DEVICETYPE, cs.successIndicator
         */
        SqlBuilder sqlBuilder = new SqlBuilder("select dev.DEVICETYPE, cs.successIndicator, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct");
        this.appendConnectionTaskLastComSessionJoinClause(sqlBuilder);
        sqlBuilder.append(" join ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" dev on ct.device = dev.id where ct.status = 0 and ");
        this.appendConnectionTypeHeatMapComTaskExecutionSessionConditions(atLeastOneFailingComTask, sqlBuilder);
        sqlBuilder.append(" group by dev.devicetype, cs.successIndicator");
        return this.fetchConnectionTypeHeatMapCounters(sqlBuilder);
    }

    private Map<DeviceType, List<Long>> buildDeviceTypeHeatMap(Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters, Map<Long, Map<ComSession.SuccessIndicator, Long>> remainingCounters) {
        Map<Long, DeviceType> deviceTypes = this.deviceDataModelService.deviceConfigurationService().findAllDeviceTypes().find().
                stream().
                collect(Collectors.toMap(DeviceType::getId, Function.identity()));
        Map<DeviceType, List<Long>> heatMap =
                deviceTypes.values().stream().collect(
                        Collectors.toMap(
                                Function.identity(),
                                this::missingCompletionCodeCounters));
        for (Long deviceTypeId : this.union(partialCounters.keySet(), remainingCounters.keySet())) {
            DeviceType deviceType = deviceTypes.get(deviceTypeId);
            heatMap.put(deviceType, this.orderSuccessIndicatorCounters(partialCounters.get(deviceTypeId), remainingCounters.get(deviceTypeId)));
        }
        return heatMap;
    }

    private List<Long> missingCompletionCodeCounters(DeviceType deviceType) {
        return Stream.of(CompletionCode.values()).map(code -> 0L).collect(Collectors.toList());
    }

    @Override
    public Map<ComPortPool, List<Long>> getConnectionsComPortPoolHeatMap() {
        Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters = this.getConnectionsComPortPoolHeatMap(false);
        Map<Long, Map<ComSession.SuccessIndicator, Long>> atLeastOneFailingCounters = this.getConnectionsComPortPoolHeatMap(true);
        return this.buildComPortPoolHeatMap(partialCounters, atLeastOneFailingCounters);
    }

    private Map<Long, Map<ComSession.SuccessIndicator, Long>> getConnectionsComPortPoolHeatMap(boolean atLeastOneFailingComTask) {
        /* For clarity's sake, here is the formatted SQL when atLeastOneFailingComTask == false:
           SELECT ct.COMPORTPOOL, cs.successIndicator, COUNT(*)
             FROM DDC_CONNECTIONTASK ct JOIN DDC_COMSESSION cs ON ct.lastsession = cs.id
            WHERE ct.status = 0
              AND NOT EXISTS (
                      SELECT *
                        FROM DDC_COMTASKEXECSESSION ctes
                       WHERE ctes.COMSESSION = cs.id
                         AND ctes.SUCCESSINDICATOR > 0)
            GROUP BY ct.COMPORTPOOL, cs.successIndicator

           when attemptLockComTaskExecution == true
           SELECT ct.COMPORTPOOL, cs.successIndicator, COUNT(*)
             FROM DDC_CONNECTIONTASK ct JOIN DDC_COMSESSION cs ON ct.lastsession = cs.id
            WHERE ct.status = 0
              AND cs.successindicator = 0
              AND EXISTS (
                      SELECT *
                        FROM DDC_COMTASKEXECSESSION ctes
                       WHERE ctes.COMSESSION = cs.id
                         AND ctes.SUCCESSINDICATOR > 0)
            GROUP BY ct.COMPORTPOOL, cs.successIndicator
         */
        SqlBuilder sqlBuilder = new SqlBuilder("select ct.COMPORTPOOL, cs.successIndicator, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" ct");
        this.appendConnectionTaskLastComSessionJoinClause(sqlBuilder);
        sqlBuilder.append(" where ct.status = 0 and ");
        this.appendConnectionTypeHeatMapComTaskExecutionSessionConditions(atLeastOneFailingComTask, sqlBuilder);
        sqlBuilder.append(" group by ct.comportpool, cs.successIndicator");
        return this.fetchConnectionTypeHeatMapCounters(sqlBuilder);
    }

    private Map<ComPortPool, List<Long>> buildComPortPoolHeatMap(Map<Long, Map<ComSession.SuccessIndicator, Long>> partialCounters, Map<Long, Map<ComSession.SuccessIndicator, Long>> remainingCounters) {
        Map<Long, ComPortPool> comPortPools =
                this.deviceDataModelService.engineModelService().findAllComPortPools().
                        stream().
                        collect(Collectors.toMap(
                                ComPortPool::getId,
                                Function.identity()));
        Map<ComPortPool, List<Long>> heatMap =
                comPortPools.values().stream().collect(Collectors.toMap(
                        Function.identity(),
                        this::missingSuccessIndicatorCounters));
        for (Long comPortPoolId : this.union(partialCounters.keySet(), remainingCounters.keySet())) {
            ComPortPool comPortPool = comPortPools.get(comPortPoolId);
            heatMap.put(comPortPool, this.orderSuccessIndicatorCounters(partialCounters.get(comPortPoolId), remainingCounters.get(comPortPoolId)));
        }
        return heatMap;
    }

    private Set<Long> union(Set<Long> businessObjectIds, Set<Long> moreBusinessObjectIds) {
        Set<Long> union = new HashSet<>(businessObjectIds);
        union.addAll(moreBusinessObjectIds);
        return union;
    }

}