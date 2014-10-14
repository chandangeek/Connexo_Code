package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.CommunicationTopologyEntry;
import com.energyict.mdc.device.data.ConnectionTaskFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ServerComTaskExecution;
import com.energyict.mdc.device.data.impl.ClauseAwareSqlBuilder;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.ScheduledComTaskExecutionIdRange;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.tasks.history.ComSessionImpl;
import com.energyict.mdc.device.data.impl.tasks.history.ComTaskExecutionSessionImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionUpdater;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CommunicationErrorType;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import org.joda.time.DateTimeConstants;

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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link CommunicationTaskService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-01 (13:37)
 */
public class CommunicationTaskServiceImpl implements ServerCommunicationTaskService {

    private static final Logger LOGGER = Logger.getLogger(CommunicationTaskServiceImpl.class.getName());

    private final DeviceDataModelService deviceDataModelService;

    @Inject
    public CommunicationTaskServiceImpl(DeviceDataModelService deviceDataModelService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
    }

    @Override
    public void releaseInterruptedComTasks(ComServer comServer) {
        SqlBuilder sqlBuilder = new SqlBuilder("UPDATE " + TableSpecs.DDC_COMTASKEXEC.name() + " SET comport = NULL, executionStart = null WHERE comport in (select id from mdc_comport where COMSERVERID = ");
        sqlBuilder.addLong(comServer.getId());
        sqlBuilder.append(")");
        this.deviceDataModelService.executeUpdate(sqlBuilder);
    }

    @Override
    public TimeDuration releaseTimedOutComTasks(ComServer comServer) {
        int waitTime = -1;
        List<ComPortPool> containingComPortPoolsForComServer = this.deviceDataModelService.engineModelService().findContainingComPortPoolsForComServer(comServer);
        for (ComPortPool comPortPool : containingComPortPoolsForComServer) {
            this.releaseTimedOutComTasks((OutboundComPortPool) comPortPool);
            waitTime = this.minimumWaitTime(waitTime, ((OutboundComPortPool) comPortPool).getTaskExecutionTimeout().getSeconds());
        }
        if (waitTime <= 0) {
            return new TimeDuration(1, TimeDuration.TimeUnit.DAYS);
        } else {
            return new TimeDuration(waitTime, TimeDuration.TimeUnit.SECONDS);
        }
    }

    private int minimumWaitTime(int currentWaitTime, int comPortPoolTaskExecutionTimeout) {
        if (currentWaitTime < 0) {
            return comPortPoolTaskExecutionTimeout;
        } else {
            return Math.min(currentWaitTime, comPortPoolTaskExecutionTimeout);
        }
    }

    private void releaseTimedOutComTasks(OutboundComPortPool outboundComPortPool) {
        long now = this.toSeconds(this.deviceDataModelService.clock().now());
        int timeOutSeconds = outboundComPortPool.getTaskExecutionTimeout().getSeconds();
        this.deviceDataModelService.executeUpdate(this.releaseTimedOutComTaskExecutionsSqlBuilder(outboundComPortPool, now, timeOutSeconds));
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

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByFilter(ComTaskExecutionFilterSpecification filter, int pageStart, int pageSize) {
        ComTaskExecutionFilterSqlBuilder sqlBuilder = new ComTaskExecutionFilterSqlBuilder(filter, this.deviceDataModelService.clock(), this.deviceFromDeviceGroupQueryExecutor());
        DataMapper<ComTaskExecution> dataMapper = this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class);
        return this.fetchComTaskExecutions(dataMapper, sqlBuilder.build(dataMapper, pageStart + 1, pageSize)); // SQL is 1-based
    }

    private List<ComTaskExecution> fetchComTaskExecutions(DataMapper<ComTaskExecution> dataMapper, SqlBuilder sqlBuilder) {
        try (Fetcher<ComTaskExecution> fetcher = dataMapper.fetcher(sqlBuilder)) {
            Iterator<ComTaskExecution> comTaskExecutionIterator = fetcher.iterator();
            List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
            while (comTaskExecutionIterator.hasNext()) {
                comTaskExecutions.add(comTaskExecutionIterator.next());
            }
            return comTaskExecutions;
        }
    }

    @Override
    public Map<TaskStatus, Long> getComTaskExecutionStatusCount() {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = EnumSet.allOf(TaskStatus.class);
        filter.deviceGroups = new HashSet<>();
        return this.getComTaskExecutionStatusCount(filter);
    }

    @Override
    public Map<TaskStatus, Long> getComTaskExecutionStatusCount(QueryEndDeviceGroup deviceGroup) {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = EnumSet.allOf(TaskStatus.class);
        filter.deviceGroups = this.asSet(deviceGroup);
        return this.getComTaskExecutionStatusCount(filter);
    }

    private Set<QueryEndDeviceGroup> asSet(QueryEndDeviceGroup deviceGroup) {
        return Stream.of(deviceGroup).collect(Collectors.toSet());
    }

    @Override
    public Map<TaskStatus, Long> getComTaskExecutionStatusCount(ComTaskExecutionFilterSpecification filter) {
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerComTaskStatus taskStatus : this.taskStatusesForCounting(filter)) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = new ClauseAwareSqlBuilder(new SqlBuilder());
                this.countByFilterAndTaskStatusSqlBuilder(sqlBuilder, filter, taskStatus);
            } else {
                sqlBuilder.unionAll();
                this.countByFilterAndTaskStatusSqlBuilder(sqlBuilder, filter, taskStatus);
            }
        }
        return this.addMissingTaskStatusCounters(this.fetchTaskStatusCounters(sqlBuilder));
    }

    private Set<ServerComTaskStatus> taskStatusesForCounting(ComTaskExecutionFilterSpecification filter) {
        return taskStatusesForCounting(filter.taskStatuses);
    }

    private Set<ServerComTaskStatus> taskStatusesForCounting(Set<TaskStatus> taskStatuses) {
        Set<ServerComTaskStatus> serverTaskStatuses = EnumSet.noneOf(ServerComTaskStatus.class);
        for (TaskStatus taskStatus : taskStatuses) {
            serverTaskStatuses.add(ServerComTaskStatus.forTaskStatus(taskStatus));
        }
        return serverTaskStatuses;
    }

    private void countByFilterAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ComTaskExecutionFilterSpecification filter, ServerComTaskStatus taskStatus) {
        ComTaskExecutionFilterMatchCounterSqlBuilder countingFilter =
                new ComTaskExecutionFilterMatchCounterSqlBuilder(
                        taskStatus,
                        filter,
                        this.deviceDataModelService.clock(),
                        this.deviceFromDeviceGroupQueryExecutor());
        countingFilter.appendTo(sqlBuilder);
    }

    private Map<TaskStatus, Long> fetchTaskStatusCounters(ClauseAwareSqlBuilder builder) {
        return this.deviceDataModelService.fetchTaskStatusCounters(builder);
    }

    private Map<TaskStatus, Long> addMissingTaskStatusCounters(Map<TaskStatus, Long> counters) {
        return this.deviceDataModelService.addMissingTaskStatusCounters(counters);
    }

    @Override
    public Map<ComSchedule, Map<TaskStatus, Long>> getCommunicationTasksComScheduleBreakdown(Set<TaskStatus> taskStatuses) {
        return this.getCommunicationTasksComScheduleBreakdown(taskStatuses, Collections.emptySet());
    }

    @Override
    public Map<ComSchedule, Map<TaskStatus, Long>> getCommunicationTasksComScheduleBreakdown(Set<TaskStatus> taskStatuses, QueryEndDeviceGroup deviceGroup) {
        return this.getCommunicationTasksComScheduleBreakdown(taskStatuses, this.asSet(deviceGroup));
    }

    private Map<ComSchedule, Map<TaskStatus, Long>> getCommunicationTasksComScheduleBreakdown(Set<TaskStatus> taskStatuses, Set<QueryEndDeviceGroup> deviceGroups) {
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerComTaskStatus taskStatus : this.taskStatusesForCounting(taskStatuses)) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = new ClauseAwareSqlBuilder(new SqlBuilder());
                this.countByComScheduleAndTaskStatusSqlBuilder(sqlBuilder, taskStatus, deviceGroups);
            }
            else {
                sqlBuilder.unionAll();
                this.countByComScheduleAndTaskStatusSqlBuilder(sqlBuilder, taskStatus, deviceGroups);
            }
        }
        return this.injectComSchedulesAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private void countByComScheduleAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ServerComTaskStatus taskStatus, Set<QueryEndDeviceGroup> deviceGroups) {
        ComTaskExecutionFilterSpecification filterSpecification = new ComTaskExecutionFilterSpecification();
        filterSpecification.deviceGroups = deviceGroups;
        ComTaskExecutionComScheduleCounterSqlBuilder countingFilter =
                new ComTaskExecutionComScheduleCounterSqlBuilder(
                        taskStatus,
                        this.deviceDataModelService.clock(),
                        filterSpecification,
                        this.deviceFromDeviceGroupQueryExecutor());
        countingFilter.appendTo(sqlBuilder);
    }

    private Map<ComSchedule, Map<TaskStatus, Long>> injectComSchedulesAndAddMissing(Map<Long, Map<TaskStatus, Long>> breakdown) {
        Map<Long, ComSchedule> comSchedules =
                this.deviceDataModelService.schedulingService().findAllSchedules().stream().
                        collect(Collectors.toMap(ComSchedule::getId, Function.identity()));
        return this.injectBreakDownsAndAddMissing(breakdown, comSchedules);
    }

    @Override
    public Map<DeviceType, Map<TaskStatus, Long>> getCommunicationTasksDeviceTypeBreakdown(Set<TaskStatus> taskStatuses) {
        return this.getCommunicationTasksDeviceTypeBreakdown(taskStatuses, Collections.emptySet());
    }

    @Override
    public Map<DeviceType, Map<TaskStatus, Long>> getCommunicationTasksDeviceTypeBreakdown(Set<TaskStatus> taskStatuses, QueryEndDeviceGroup deviceGroup) {
        return this.getCommunicationTasksDeviceTypeBreakdown(taskStatuses, this.asSet(deviceGroup));
    }

    private Map<DeviceType, Map<TaskStatus, Long>> getCommunicationTasksDeviceTypeBreakdown(Set<TaskStatus> taskStatuses, Set<QueryEndDeviceGroup> deviceGroups) {
        ComTaskExecutionFilterSpecification filterSpecification = new ComTaskExecutionFilterSpecification();
        filterSpecification.deviceGroups = deviceGroups;
        ClauseAwareSqlBuilder sqlBuilder = null;
        for (ServerComTaskStatus taskStatus : this.taskStatusesForCounting(taskStatuses)) {
            // Check first pass
            if (sqlBuilder == null) {
                sqlBuilder = new ClauseAwareSqlBuilder(new SqlBuilder());
                this.countByDeviceTypeAndTaskStatusSqlBuilder(sqlBuilder, filterSpecification, taskStatus);
            }
            else {
                sqlBuilder.unionAll();
                this.countByDeviceTypeAndTaskStatusSqlBuilder(sqlBuilder, filterSpecification, taskStatus);
            }
        }
        return this.injectDeviceTypesAndAddMissing(this.deviceDataModelService.fetchTaskStatusBreakdown(sqlBuilder));
    }

    private void countByDeviceTypeAndTaskStatusSqlBuilder(ClauseAwareSqlBuilder sqlBuilder, ComTaskExecutionFilterSpecification filterSpecification, ServerComTaskStatus taskStatus) {
        ComTaskExecutionDeviceTypeCounterSqlBuilder countingFilter =
                new ComTaskExecutionDeviceTypeCounterSqlBuilder(
                        taskStatus,
                        this.deviceDataModelService.clock(),
                        filterSpecification,
                        this.deviceFromDeviceGroupQueryExecutor());
        countingFilter.appendTo(sqlBuilder);
    }

    /**
     * Returns a QueryExecutor that supports building a subquery to match
     * that the ConnectionTask's device is in a QueryEndDeviceGroup.
     *
     * @return The QueryExecutor
     */
    private QueryExecutor<Device> deviceFromDeviceGroupQueryExecutor() {
        return this.deviceDataModelService.dataModel().query(Device.class, DeviceType.class, DeviceConfiguration.class);
    }

    private Map<DeviceType, Map<TaskStatus, Long>> injectDeviceTypesAndAddMissing(Map<Long, Map<TaskStatus, Long>> breakdown) {
        Map<Long, DeviceType> deviceTypes =
                this.deviceDataModelService.deviceConfigurationService().findAllDeviceTypes().stream().
                        collect(Collectors.toMap(DeviceType::getId, Function.identity()));
        return this.injectBreakDownsAndAddMissing(breakdown, deviceTypes);
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

    @Override
    public void setOrUpdateDefaultConnectionTaskOnComTaskInDeviceTopology(Device device, ConnectionTask defaultConnectionTask) {
        List<ComTaskExecution> comTaskExecutions = this.findComTaskExecutionsWithDefaultConnectionTaskForCompleteTopologyButNotLinkedYet(device, defaultConnectionTask);
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            ComTaskExecutionUpdater<? extends ComTaskExecutionUpdater<?, ?>, ? extends ComTaskExecution> comTaskExecutionUpdater = comTaskExecution.getUpdater();
            comTaskExecutionUpdater.useDefaultConnectionTask(defaultConnectionTask);
            comTaskExecutionUpdater.update();
        }
    }

    /**
     * Constructs a list of {@link ComTaskExecution} which are linked to
     * the default {@link ConnectionTask} for the entire topology of the specified Device,
     * but are not linked yet to the given Default connectionTask
     *
     * @param device         the Device for which we need to search the ComTaskExecution
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
        List<ComTaskExecution> comTaskExecutions = this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).select(query);
        scheduledComTasks.addAll(comTaskExecutions);
        for (Device slave : device.getPhysicalConnectedDevices()) {
            this.collectComTaskWithDefaultConnectionTaskForCompleteTopology(slave, scheduledComTasks, connectionTask);
        }
    }

    @Override
    public void removePreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.removePreferredConnectionTaskSqlBuilder(comTask, deviceConfiguration, partialConnectionTask));
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
        sqlBuilder.append("  where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("    and partialconnectiontask =");   // Match the connection task
        sqlBuilder.addLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void switchFromDefaultConnectionTaskToPreferredConnectionTask(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.switchFromDefaultConnectionTaskToPreferredConnectionTaskSqlBuilder(comTask, deviceConfiguration, partialConnectionTask));
    }

    private SqlBuilder switchFromDefaultConnectionTaskToPreferredConnectionTaskSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask partialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set useDefaultConnectionTask = 0, connectionTask = ");
        sqlBuilder.append("select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("   and partialconnectiontask =");  //Match the connection task against the same device
        sqlBuilder.addLong(partialConnectionTask.getId());
        sqlBuilder.append("   and obsolete_date is null)");
        sqlBuilder.append(" where comtask =");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void switchOnDefault(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.deviceDataModelService.executeUpdate(this.switchOnDefaultSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder switchOnDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set useDefaultConnectionTask = 1, connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("    and isdefault = 1");  // Match the default connection task against the same device
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.append(" where comtask =");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void switchOffDefault(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.deviceDataModelService.executeUpdate(this.switchOffDefaultSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder switchOffDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set useDefaultConnectionTask = 0");
        sqlBuilder.append(" where comtask =");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void switchFromPreferredConnectionTaskToDefault(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.switchFromPreferredConnectionTaskToDefaultSqlBuilder(comTask, deviceConfiguration, previousPartialConnectionTask));
    }

    private SqlBuilder switchFromPreferredConnectionTaskToDefaultSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set useDefaultConnectionTask = 1, connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("    and isdefault = 1");  // Match the default connection task against the same device
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.append(" where comtask =");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and connectionTask = ");
        sqlBuilder.append("(select id from ");
        sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
        sqlBuilder.append(" where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("    and partialconnectiontask =");   // Match the connection task
        sqlBuilder.addLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("    and obsolete_date is null)");
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void preferredConnectionTaskChanged(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask, PartialConnectionTask newPartialConnectionTask) {
        this.deviceDataModelService.executeUpdate(this.preferredConnectionTaskChangedSqlBuilder(comTask, deviceConfiguration, previousPartialConnectionTask, newPartialConnectionTask));
    }

    private SqlBuilder preferredConnectionTaskChangedSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration, PartialConnectionTask previousPartialConnectionTask, PartialConnectionTask newPartialConnectionTask) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set connectionTask = ");
        sqlBuilder.append("   (select id from " + TableSpecs.DDC_CONNECTIONTASK.name() + "");
        sqlBuilder.append("     where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("       and partialconnectiontask =");  //Match the connection task against the same device
        sqlBuilder.addLong(newPartialConnectionTask.getId());
        sqlBuilder.append("       and obsolete_date is null)");
        // Avoid comTaskExecutions that use the default connection
        sqlBuilder.append(" where useDefaultConnectionTask = 0");
        sqlBuilder.append("   and comtask =");  // Match the ComTask
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and connectionTask = ");
        sqlBuilder.append("     (select id from " + TableSpecs.DDC_CONNECTIONTASK.name() + "");
        sqlBuilder.append("       where device = ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(".device");
        sqlBuilder.append("         and partialconnectiontask =");   // Match the previous connection task
        sqlBuilder.addLong(previousPartialConnectionTask.getId());
        sqlBuilder.append("         and obsolete_date is null)");
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
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
        try (PreparedStatement statement = sqlBuilder.prepare(this.deviceDataModelService.dataModel().getConnection(true))) {
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count != 0;
                }
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
        return false;
    }

    @Override
    public boolean hasComTaskExecutions(ComSchedule comSchedule) {
        Condition condition = where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        List<ComTaskExecution> comTaskExecutions = this.deviceDataModelService.dataModel().query(ComTaskExecution.class).select(condition, new Order[0], false, new String[0], 1, 1);
        return !comTaskExecutions.isEmpty();
    }

    @Override
    public Optional<ScheduledComTaskExecutionIdRange> getScheduledComTaskExecutionIdRange(long comScheduleId) {
        try (PreparedStatement preparedStatement = this.getMinMaxComTaskExecutionIdPreparedStatement()) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.first();  // There is always at least one row since we are counting
                long minId = resultSet.getLong(0);
                if (resultSet.wasNull()) {
                    return Optional.absent();    // There were not ComTaskExecutions
                } else {
                    long maxId = resultSet.getLong(1);
                    return Optional.of(new ScheduledComTaskExecutionIdRange(comScheduleId, minId, maxId));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new UnderlyingSQLFailedException(e);
        }
    }

    private PreparedStatement getMinMaxComTaskExecutionIdPreparedStatement() throws SQLException {
        return this.deviceDataModelService.dataModel().getConnection(true).prepareStatement(this.getMinMaxComTaskExecutionIdStatement());
    }

    private String getMinMaxComTaskExecutionIdStatement() {
        return "SELECT MIN(id), MAX(id) FROM " + TableSpecs.DDC_COMTASKEXEC.name() + " WHERE comschedule = ? AND obsolete_date IS NULL";
    }

    @Override
    public void obsoleteComTaskExecutionsInRange(ScheduledComTaskExecutionIdRange idRange) {
        try (PreparedStatement preparedStatement = this.getObsoleteComTaskExecutionInRangePreparedStatement()) {
            preparedStatement.setDate(1, this.nowAsSqlDate());
            preparedStatement.setLong(2, idRange.comScheduleId);
            preparedStatement.setLong(3, idRange.minId);
            preparedStatement.setLong(4, idRange.maxId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private java.sql.Date nowAsSqlDate() {
        return new java.sql.Date(this.deviceDataModelService.clock().now().getTime());
    }

    private PreparedStatement getObsoleteComTaskExecutionInRangePreparedStatement() throws SQLException {
        return this.deviceDataModelService.dataModel().getConnection(true).prepareStatement(this.getObsoleteComTaskExecutionInRangeStatement());
    }

    private String getObsoleteComTaskExecutionInRangeStatement() {
        return "UPDATE " + TableSpecs.DDC_COMTASKEXEC.name() + " SET OBSOLETE_DATE = ? WHERE comschedule = ? AND id BETWEEN ? AND ?";
    }

    @Override
    public void preferredPriorityChanged(ComTask comTask, DeviceConfiguration deviceConfiguration, int previousPreferredPriority, int newPreferredPriority) {
        this.deviceDataModelService.executeUpdate(this.preferredPriorityChangedSqlBuilder(comTask, deviceConfiguration, previousPreferredPriority, newPreferredPriority));
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
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        return sqlBuilder;
    }

    @Override
    public void suspendAll(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.deviceDataModelService.executeUpdate(this.suspendAllSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder suspendAllSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set nextExecutionTimestamp = null");
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        sqlBuilder.append("   and comtask =");
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and comport is null");    // exclude tasks that are currently executing
        sqlBuilder.append("   and plannedNextExecutionTimestamp is not null");  // Exclude tasks that have been put on hold manually
        return sqlBuilder;
    }

    @Override
    public void resumeAll(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        this.deviceDataModelService.executeUpdate(this.resumeAllSqlBuilder(comTask, deviceConfiguration));
    }

    private SqlBuilder resumeAllSqlBuilder(ComTask comTask, DeviceConfiguration deviceConfiguration) {
        SqlBuilder sqlBuilder = new SqlBuilder("update ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" set nextExecutionTimestamp = plannedNextExecutionTimestamp");
        sqlBuilder.append("   and device in (select id from ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" where deviceConfigId =");  // Match device of the specified DeviceConfiguration
        sqlBuilder.addLong(deviceConfiguration.getId());
        sqlBuilder.append(")");
        sqlBuilder.append("   and comtask =");
        sqlBuilder.addLong(comTask.getId());
        sqlBuilder.append("   and nextExecutionTimestamp is null");      // Only tasks that were suspended
        sqlBuilder.append("   and plannedNextExecutionTimestamp is not null");  // Only tasks that were suspended
        return sqlBuilder;
    }

    @Override
    public ComTaskExecution findComTaskExecution(long id) {
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).getUnique("id", id).orNull();
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByDevice(Device device) {
        Condition condition = where(ComTaskExecutionFields.DEVICE.name()).isEqualTo(device.getId()).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).select(condition);
    }

    @Override
    public List<ComTaskExecution> findAllComTaskExecutionsIncludingObsoleteForDevice(Device device) {
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).find(ComTaskExecutionFields.DEVICE.fieldName(), device);
    }

    @Override
    public ComTaskExecution attemptLockComTaskExecution(ComTaskExecution comTaskExecution, ComPort comPort) {
        Optional<ComTaskExecution> lockResult = this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).lockNoWait(comTaskExecution.getId());
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
    public Finder<ComTaskExecution> findComTaskExecutionsByConnectionTask(ConnectionTask<?, ?> connectionTask) {
        return DefaultFinder.of(ComTaskExecution.class, where(ComTaskExecutionFields.CONNECTIONTASK.fieldName()).isEqualTo(connectionTask), this.deviceDataModelService.dataModel()).sorted("executionStart", false);
    }

    @Override
    public List<ComTaskExecution> findComTasksByDefaultConnectionTask(Device device) {
        return this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class).find(ComTaskExecutionFields.DEVICE.fieldName(), device, ComTaskExecutionFields.USEDEFAULTCONNECTIONTASK.fieldName(), true);
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByComSchedule(ComSchedule comSchedule) {
        return this.deviceDataModelService.dataModel().query(ComTaskExecution.class)
                .select(where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule)
                        .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull()));
    }

    @Override
    public List<ComTaskExecution> findComTaskExecutionsByComScheduleWithinRange(ComSchedule comSchedule, long minId, long maxId) {
        return this.deviceDataModelService.dataModel().query(ComTaskExecution.class)
                .select(where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule)
                        .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull())
                        .and(where(ComTaskExecutionFields.ID.fieldName()).between(minId).and(maxId)));
    }

    @Override
    public Fetcher<ComTaskExecution> getPlannedComTaskExecutionsFor(ComPort comPort) {
        List<OutboundComPortPool> comPortPools = this.deviceDataModelService.engineModelService().findContainingComPortPoolsForComPort((OutboundComPort) comPort);
        if (!comPortPools.isEmpty()) {
            long nowInSeconds = this.deviceDataModelService.clock().now().getTime() / DateTimeConstants.MILLIS_PER_SECOND;
            DataMapper<ComTaskExecution> mapper = this.deviceDataModelService.dataModel().mapper(ComTaskExecution.class);
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
            Date now = this.deviceDataModelService.clock().now();
            Condition condition = where("connectionTask.paused").isEqualTo(false)
                    .and(where("connectionTask.comServer").isNull())
                    .and(where("connectionTask.obsoleteDate").isNull())
                    .and(where("connectionTask." + ConnectionTaskFields.DEVICE.name()).isEqualTo(device))
                    .and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull())
                    .and(where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(now))
                    .and(where("connectionTask.nextExecutionTimestamp").isLessThanOrEqual(now)
                            .or(where(ComTaskExecutionFields.IGNORENEXTEXECUTIONSPECSFORINBOUND.fieldName()).isEqualTo(true)))
                    .and(where(ComTaskExecutionFields.COMPORT.fieldName()).isNull())
                    .and(where("connectionTask.comPortPool").isEqualTo(inboundComPortPool));
            return this.deviceDataModelService.dataModel().query(ComTaskExecution.class, ConnectionTask.class).select(condition,
                    Order.ascending(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()),
                    Order.ascending(ComTaskExecutionFields.PLANNED_PRIORITY.fieldName()),
                    Order.ascending(ComTaskExecutionFields.CONNECTIONTASK.fieldName()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean areComTasksStillPending(Collection<Long> comTaskExecutionIds) {
        Date now = this.deviceDataModelService.clock().now();
        Condition condition = where(ComTaskExecutionFields.NEXTEXECUTIONTIMESTAMP.fieldName()).isLessThanOrEqual(now)
                .and(ListOperator.IN.contains("id", new ArrayList<>(comTaskExecutionIds)))
                .and(where("connectionTask.comServer").isNull());
        return !this.deviceDataModelService.dataModel().query(ComTaskExecution.class, ConnectionTask.class).select(condition).isEmpty();
    }

    @Override
    public Optional<ComTaskExecutionSession> findLastSessionFor(ComTaskExecution comTaskExecution) {
        Condition condition = where(ComTaskExecutionSessionImpl.Fields.COM_TASK_EXECUTION.fieldName()).isEqualTo(comTaskExecution);
        Finder<ComTaskExecutionSession> page =
                DefaultFinder.
                        of(ComTaskExecutionSession.class, condition, this.deviceDataModelService.dataModel()).
                        sorted(ComTaskExecutionSessionImpl.Fields.START_DATE.fieldName(), false).
                        paged(1, 1);
        List<ComTaskExecutionSession> allSessions = page.find();
        if (allSessions.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.of(allSessions.get(0));
        }
    }

    @Override
    public Map<DeviceType, List<Long>> getComTasksDeviceTypeHeatMap() {
        /* For clarity's sake, here is the formatted SQL
           select dev.DEVICETYPE, ctes.highestPrioCompletionCode, count(*)
            from DDC_COMTASKEXEC cte
            join DDC_COMTASKEXECSESSION ctes on cte.lastsession = ctes.id
            join DDC_DEVICE dev on cte.device = dev.id
           group by dev.DEVICETYPE, ctes.highestPrioCompletionCode
         */
        return this.getComTasksDeviceTypeHeatMap(null);
    }

    @Override
    public Map<DeviceType, List<Long>> getComTasksDeviceTypeHeatMap(QueryEndDeviceGroup deviceGroup) {
        SqlBuilder sqlBuilder = new SqlBuilder("select dev.DEVICETYPE, ctes.highestPrioCompletionCode, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" cte join ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
        sqlBuilder.append(" ctes on cte.lastsession = ctes.id join ");
        sqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        sqlBuilder.append(" dev on cte.device = dev.id ");
        this.appendDeviceGroupConditions(deviceGroup, sqlBuilder);
        sqlBuilder.append(" group by dev.devicetype, ctes.highestPrioCompletionCode");
        Map<Long, Map<CompletionCode, Long>> partialCounters = this.fetchComTaskHeatMapCounters(sqlBuilder);
        return this.buildDeviceTypeHeatMap(partialCounters);
    }

    private Map<DeviceType, List<Long>> buildDeviceTypeHeatMap(Map<Long, Map<CompletionCode, Long>> partialCounters) {
        Map<Long, DeviceType> deviceTypes = this.deviceDataModelService.deviceConfigurationService().findAllDeviceTypes().find().
                stream().
                collect(Collectors.toMap(DeviceType::getId, Function.identity()));
        Map<DeviceType, List<Long>> heatMap =
                deviceTypes.values().stream().collect(
                        Collectors.toMap(
                                Function.identity(),
                                this::missingCompletionCodeCounters));
        for (Long deviceTypeId : partialCounters.keySet()) {
            DeviceType deviceType = deviceTypes.get(deviceTypeId);
            heatMap.put(deviceType, this.orderedCompletionCodeCounters(partialCounters.get(deviceTypeId)));
        }
        return heatMap;
    }

    private List<Long> missingCompletionCodeCounters(DeviceType deviceType) {
        return Stream.of(CompletionCode.values()).map(code -> 0L).collect(Collectors.toList());
    }

    private List<Long> orderedCompletionCodeCounters(Map<CompletionCode, Long> completionCodeCounters) {
        List<Long> counters = new ArrayList<>(CompletionCode.values().length);
        for (CompletionCode completionCode : CompletionCode.values()) {
            counters.add(completionCodeCounters.get(completionCode));
        }
        return counters;
    }

    private Map<Long, Map<CompletionCode, Long>> fetchComTaskHeatMapCounters(SqlBuilder builder) {
        try (PreparedStatement stmnt = builder.prepare(this.deviceDataModelService.dataModel().getConnection(true))) {
            return this.fetchComTaskHeatMapCounters(stmnt);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
    }

    private Map<Long, Map<CompletionCode, Long>> fetchComTaskHeatMapCounters(PreparedStatement statement) throws SQLException {
        Map<Long, Map<CompletionCode, Long>> counters = new HashMap<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long businessObjectId = resultSet.getLong(1);
                int completionCodeOrdinal = resultSet.getInt(2);
                long counter = resultSet.getLong(3);
                Map<CompletionCode, Long> successIndicatorCounters = this.getOrPutCompletionCodeCounters(businessObjectId, counters);
                successIndicatorCounters.put(CompletionCode.fromOrdinal(completionCodeOrdinal), counter);
            }
        }
        return counters;
    }

    private Map<CompletionCode, Long> getOrPutCompletionCodeCounters(long businessObjectId, Map<Long, Map<CompletionCode, Long>> counters) {
        Map<CompletionCode, Long> completionCodeCounters = counters.get(businessObjectId);
        if (completionCodeCounters == null) {
            completionCodeCounters = new HashMap<>();
            for (CompletionCode missing : EnumSet.allOf(CompletionCode.class)) {
                completionCodeCounters.put(missing, 0L);
            }
            counters.put(businessObjectId, completionCodeCounters);
        }
        return completionCodeCounters;
    }

    @Override
    public Map<CompletionCode, Long> getComTaskLastComSessionHighestPriorityCompletionCodeCount() {
        SqlBuilder sqlBuilder = new SqlBuilder("select ctes.highestPrioCompletionCode, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" cte join ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
        sqlBuilder.append(" ctes on cte.lastsession = ctes.id where obsolete_date is null group by ctes.highestPrioCompletionCode");
        return this.addMissingCompletionCodeCounters(this.fetchCompletionCodeCounters(sqlBuilder));
    }

    @Override
    public Map<CompletionCode, Long> getComTaskLastComSessionHighestPriorityCompletionCodeCount(QueryEndDeviceGroup deviceGroup) {
        SqlBuilder sqlBuilder = new SqlBuilder("select ctes.highestPrioCompletionCode, count(*) from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
        sqlBuilder.append(" cte join ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXECSESSION.name());
        sqlBuilder.append(" ctes on cte.lastsession = ctes.id where obsolete_date is null ");
        this.appendDeviceGroupConditions(deviceGroup, sqlBuilder);
        sqlBuilder.append(" group by ctes.highestPrioCompletionCode");
        return this.addMissingCompletionCodeCounters(this.fetchCompletionCodeCounters(sqlBuilder));
    }

    private void appendDeviceGroupConditions(QueryEndDeviceGroup deviceGroup, SqlBuilder sqlBuilder) {
        if (deviceGroup != null) {
            sqlBuilder.append(" and cte.device in (");
            QueryExecutor<Device> queryExecutor = this.deviceFromDeviceGroupQueryExecutor();
            sqlBuilder.add(queryExecutor.asFragment(deviceGroup.getCondition(), "id"));
            sqlBuilder.append(")");
        }
    }

    private Map<CompletionCode, Long> fetchCompletionCodeCounters(SqlBuilder builder) {
        Map<CompletionCode, Long> counters = new HashMap<>();
        try (PreparedStatement stmnt = builder.prepare(this.deviceDataModelService.dataModel().getConnection(true))) {
            this.fetchCompletionCodeCounters(stmnt, counters);
        } catch (SQLException ex) {
            throw new UnderlyingSQLFailedException(ex);
        }
        return counters;
    }

    private void fetchCompletionCodeCounters(PreparedStatement statement, Map<CompletionCode, Long> counters) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int completionCodeOrdinal = resultSet.getInt(1);
                long counter = resultSet.getLong(2);
                counters.put(CompletionCode.fromOrdinal(completionCodeOrdinal), counter);
            }
        }
    }

    private Map<CompletionCode, Long> addMissingCompletionCodeCounters(Map<CompletionCode, Long> counters) {
        for (CompletionCode missing : this.completionCodeComplement(counters.keySet())) {
            counters.put(missing, 0L);
        }
        return counters;
    }

    private EnumSet<CompletionCode> completionCodeComplement(Set<CompletionCode> completionCodes) {
        if (completionCodes.isEmpty()) {
            return EnumSet.allOf(CompletionCode.class);
        } else {
            return EnumSet.complementOf(EnumSet.copyOf(completionCodes));
        }
    }

    @Override
    public List<ComTaskExecutionSession> findByComTaskExecution(ComTaskExecution comTaskExecution) {
        return this.deviceDataModelService.dataModel().
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
        } else {
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
        List<ComTaskExecutionSession> comTaskExecutionSessions = this.deviceDataModelService.dataModel().query(ComTaskExecutionSession.class, ComSession.class, Device.class).select(execSessionCondition);
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
            } else {
                superCondition = superCondition.and(condition);
            }
        }
        return superCondition;
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