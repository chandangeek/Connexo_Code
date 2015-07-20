package com.energyict.mdc.dashboard.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComPortPoolHeatMap;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMap;
import com.energyict.mdc.dashboard.ConnectionTaskDeviceTypeHeatMap;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.ConnectionTypeHeatMap;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides an implementation for the {@link DashboardService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (10:27)
 */
@Component(name = "com.energyict.mdc.dashboard", service = {DashboardService.class}, property = "name=DBS")
public class DashboardServiceImpl implements DashboardService {

    private volatile ConnectionTaskService connectionTaskService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile TaskService taskService;

    public DashboardServiceImpl() {
        super();
    }

    @Inject
    public DashboardServiceImpl(TaskService taskService, ConnectionTaskService connectionTaskService, CommunicationTaskService communicationTaskService) {
        this();
        this.setTaskService(taskService);
        this.setConnectionTaskService(connectionTaskService);
        this.setCommunicationTaskService(communicationTaskService);
    }

    @Override
    public TaskStatusOverview getConnectionTaskStatusOverview() {
        return this.getConnectionTaskStatusOverview(this.connectionTaskService::getConnectionTaskStatusCount);
    }

    @Override
    public TaskStatusOverview getConnectionTaskStatusOverview(EndDeviceGroup deviceGroup) {
        return this.getConnectionTaskStatusOverview(() -> this.connectionTaskService.getConnectionTaskStatusCount(deviceGroup));
    }

    private TaskStatusOverview getConnectionTaskStatusOverview(Supplier<Map<TaskStatus, Long>> statusCountersSupplier) {
        TaskStatusOverviewImpl overview = new TaskStatusOverviewImpl();
        Map<TaskStatus, Long> statusCounters = statusCountersSupplier.get();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            overview.add(new CounterImpl<>(taskStatus, statusCounters.get(taskStatus)));
        }
        return overview;
    }

    @Override
    public ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview() {
        return this.getComSessionSuccessIndicatorOverview(this.connectionTaskService::getConnectionTaskLastComSessionSuccessIndicatorCount);
    }

    @Override
    public ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview(EndDeviceGroup deviceGroup) {
        return this.getComSessionSuccessIndicatorOverview(() -> this.connectionTaskService.getConnectionTaskLastComSessionSuccessIndicatorCount(deviceGroup));
    }

    private ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview(Supplier<Map<ComSession.SuccessIndicator, Long>> successIndicatorCountSupplier) {
        ComSessionSuccessIndicatorOverviewImpl overview = new ComSessionSuccessIndicatorOverviewImpl(this.connectionTaskService.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask());
        Map<ComSession.SuccessIndicator, Long> successIndicatorCount = successIndicatorCountSupplier.get();
        for (ComSession.SuccessIndicator successIndicator : ComSession.SuccessIndicator.values()) {
            overview.add(new CounterImpl<>(successIndicator, successIndicatorCount.get(successIndicator)));
        }
        return overview;
    }

    @Override
    public ComPortPoolBreakdown getComPortPoolBreakdown() {
        return this.getComPortPoolBreakdown(() -> this.connectionTaskService.getComPortPoolBreakdown(this.breakdownStatusses()));
    }

    @Override
    public ComPortPoolBreakdown getComPortPoolBreakdown(EndDeviceGroup deviceGroup) {
        return this.getComPortPoolBreakdown(() -> this.connectionTaskService.getComPortPoolBreakdown(this.breakdownStatusses(), deviceGroup));
    }

    private ComPortPoolBreakdown getComPortPoolBreakdown(Supplier<Map<ComPortPool, Map<TaskStatus, Long>>> rawDataSupplier) {
        ComPortPoolBreakdownImpl breakdown = new ComPortPoolBreakdownImpl();
        Map<ComPortPool, Map<TaskStatus, Long>> rawData = rawDataSupplier.get();
        for (ComPortPool comPortPool : rawData.keySet()) {
            Map<TaskStatus, Long> statusCount = rawData.get(comPortPool);
            breakdown.add(
                    new TaskStatusBreakdownCounterImpl<>(
                            comPortPool,
                            this.successCount(statusCount),
                            this.failedCount(statusCount),
                            this.pendingCount(statusCount)));
        }
        return breakdown;
    }

    @Override
    public ConnectionTypeBreakdown getConnectionTypeBreakdown() {
        return this.getConnectionTypeBreakdown(() -> this.connectionTaskService.getConnectionTypeBreakdown(this.breakdownStatusses()));
    }

    @Override
    public ConnectionTypeBreakdown getConnectionTypeBreakdown(EndDeviceGroup deviceGroup) {
        return this.getConnectionTypeBreakdown(() -> this.connectionTaskService.getConnectionTypeBreakdown(this.breakdownStatusses(), deviceGroup));
    }

    private ConnectionTypeBreakdown getConnectionTypeBreakdown(Supplier<Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>>> rawDataSupplier) {
        ConnectionTypeBreakdownImpl breakdown = new ConnectionTypeBreakdownImpl();
        Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> rawData = rawDataSupplier.get();
        for (ConnectionTypePluggableClass connectionTypePluggableClass : rawData.keySet()) {
            Map<TaskStatus, Long> statusCount = rawData.get(connectionTypePluggableClass);
            breakdown.add(
                    new TaskStatusBreakdownCounterImpl<>(
                            connectionTypePluggableClass,
                            this.successCount(statusCount),
                            this.failedCount(statusCount),
                            this.pendingCount(statusCount)));
        }
        return breakdown;
    }

    @Override
    public DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown() {
        return this.getConnectionTasksDeviceTypeBreakdown(() -> this.connectionTaskService.getDeviceTypeBreakdown(this.breakdownStatusses()));
    }

    @Override
    public DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown(EndDeviceGroup deviceGroup) {
        return this.getConnectionTasksDeviceTypeBreakdown(() -> this.connectionTaskService.getDeviceTypeBreakdown(this.breakdownStatusses(), deviceGroup));
    }

    private DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown(Supplier<Map<DeviceType, Map<TaskStatus, Long>>> rawDataSupplier) {
        DeviceTypeBreakdownImpl breakdown = new DeviceTypeBreakdownImpl();
        Map<DeviceType, Map<TaskStatus, Long>> rawData = rawDataSupplier.get();
        for (DeviceType deviceType : rawData.keySet()) {
            Map<TaskStatus, Long> statusCount = rawData.get(deviceType);
            breakdown.add(
                    new TaskStatusBreakdownCounterImpl<>(
                            deviceType,
                            this.successCount(statusCount),
                            this.failedCount(statusCount),
                            this.pendingCount(statusCount)));
        }
        return breakdown;
    }

    private Set<TaskStatus> breakdownStatusses() {
        Set<TaskStatus> taskStatuses = EnumSet.noneOf(TaskStatus.class);
        taskStatuses.addAll(this.successTaskStatusses());
        taskStatuses.addAll(this.failedTaskStatusses());
        taskStatuses.addAll(this.pendingTaskStatusses());
        return taskStatuses;
    }

    @Override
    public ConnectionTypeHeatMap getConnectionTypeHeatMap() {
        return this.getConnectionTypeHeatMap(this.connectionTaskService::getConnectionTypeHeatMap);
    }

    @Override
    public ConnectionTypeHeatMap getConnectionTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.getConnectionTypeHeatMap(() -> this.connectionTaskService.getConnectionTypeHeatMap(deviceGroup));
    }

    private ConnectionTypeHeatMap getConnectionTypeHeatMap(Supplier<Map<ConnectionTypePluggableClass, List<Long>>> rawDataSupplier) {
        ConnectionTypeHeatMapImpl heatMap = new ConnectionTypeHeatMapImpl();
        Map<ConnectionTypePluggableClass, List<Long>> rawData = rawDataSupplier.get();
        for (ConnectionTypePluggableClass connectionTypePluggableClass : rawData.keySet()) {
            List<Long> counters = rawData.get(connectionTypePluggableClass);
            ConnectionTaskHeatMapRowImpl<ConnectionTypePluggableClass> heatMapRow = new ConnectionTaskHeatMapRowImpl<>(connectionTypePluggableClass);
            heatMapRow.add(this.newComSessionSuccessIndicatorOverview(counters));
            heatMap.add(heatMapRow);
        }
        return heatMap;
    }

    @Override
    public ConnectionTaskDeviceTypeHeatMap getConnectionsDeviceTypeHeatMap() {
        return this.getConnectionsDeviceTypeHeatMap(this.connectionTaskService::getConnectionsDeviceTypeHeatMap);
    }

    @Override
    public ConnectionTaskDeviceTypeHeatMap getConnectionsDeviceTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.getConnectionsDeviceTypeHeatMap(() -> this.connectionTaskService.getConnectionsDeviceTypeHeatMap(deviceGroup));
    }

    private ConnectionTaskDeviceTypeHeatMap getConnectionsDeviceTypeHeatMap(Supplier<Map<DeviceType, List<Long>>> rawDataSupplier) {
        ConnectionTaskDeviceTypeHeatMapImpl heatMap = new ConnectionTaskDeviceTypeHeatMapImpl();
        Map<DeviceType, List<Long>> rawData = rawDataSupplier.get();
        for (DeviceType deviceType : rawData.keySet()) {
            List<Long> counters = rawData.get(deviceType);
            ConnectionTaskHeatMapRowImpl<DeviceType> heatMapRow = new ConnectionTaskHeatMapRowImpl<>(deviceType);
            heatMapRow.add(this.newComSessionSuccessIndicatorOverview(counters));
            heatMap.add(heatMapRow);
        }
        return heatMap;
    }
    @Override
    public ComPortPoolHeatMap getConnectionsComPortPoolHeatMap() {
        return this.getConnectionsComPortPoolHeatMap(this.connectionTaskService::getConnectionsComPortPoolHeatMap);
    }

    @Override
    public ComPortPoolHeatMap getConnectionsComPortPoolHeatMap(EndDeviceGroup deviceGroup) {
        return this.getConnectionsComPortPoolHeatMap(() -> this.connectionTaskService.getConnectionsComPortPoolHeatMap(deviceGroup));
    }

    private ComPortPoolHeatMap getConnectionsComPortPoolHeatMap(Supplier<Map<ComPortPool, List<Long>>> rawDataSupplier) {
        ComPortPoolHeatMapImpl heatMap = new ComPortPoolHeatMapImpl();
        Map<ComPortPool, List<Long>> rawData = rawDataSupplier.get();
        for (ComPortPool comPortPool : rawData.keySet()) {
            List<Long> counters = rawData.get(comPortPool);
            ConnectionTaskHeatMapRowImpl<ComPortPool> heatMapRow = new ConnectionTaskHeatMapRowImpl<>(comPortPool);
            heatMapRow.add(this.newComSessionSuccessIndicatorOverview(counters));
            heatMap.add(heatMapRow);
        }
        return heatMap;
    }
    private ComSessionSuccessIndicatorOverview newComSessionSuccessIndicatorOverview(List<Long> counters) {
        Iterator<Long> successIndicatorValues = counters.iterator();
        ComSessionSuccessIndicatorOverviewImpl overview = new ComSessionSuccessIndicatorOverviewImpl(successIndicatorValues.next());
        for (ComSession.SuccessIndicator successIndicator : orderedSuccessIndicators()) {
            overview.add(new CounterImpl<>(successIndicator, successIndicatorValues.next()));
        }
        return overview;
    }

    @Override
    public TaskStatusOverview getCommunicationTaskStatusOverview() {
        return this.getCommunicationTaskStatusOverview(this.communicationTaskService::getComTaskExecutionStatusCount);
    }

    @Override
    public TaskStatusOverview getCommunicationTaskStatusOverview(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTaskStatusOverview(() -> this.communicationTaskService.getComTaskExecutionStatusCount(deviceGroup));
    }

    private TaskStatusOverview getCommunicationTaskStatusOverview(Supplier<Map<TaskStatus, Long>> statusCountersSupplier) {
        TaskStatusOverviewImpl overview = new TaskStatusOverviewImpl();
        Map<TaskStatus, Long> statusCounters = statusCountersSupplier.get();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            overview.add(new CounterImpl<>(taskStatus, statusCounters.get(taskStatus)));
        }
        return overview;
    }

    @Override
    public DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown() {
        return this.getCommunicationTasksDeviceTypeBreakdown(() -> this.communicationTaskService.getCommunicationTasksDeviceTypeBreakdown(this.breakdownStatusses()));
    }

    @Override
    public DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTasksDeviceTypeBreakdown(() -> this.communicationTaskService.getCommunicationTasksDeviceTypeBreakdown(this.breakdownStatusses(), deviceGroup));
    }

    private DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown(Supplier<Map<DeviceType, Map<TaskStatus, Long>>> breakDownSupplier) {
        DeviceTypeBreakdownImpl breakdown = new DeviceTypeBreakdownImpl();
        Map<DeviceType, Map<TaskStatus, Long>> deviceTypeBreakdown = breakDownSupplier.get();
        for (DeviceType deviceType : deviceTypeBreakdown.keySet()) {
            Map<TaskStatus, Long> statusCount = deviceTypeBreakdown.get(deviceType);
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(deviceType, this.successCount(statusCount), this.failedCount(statusCount), this.pendingCount(statusCount)));
        }
        return breakdown;
    }

    @Override
    public ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown() {
        return this.getCommunicationTasksComScheduleBreakdown(() -> this.communicationTaskService.getCommunicationTasksComScheduleBreakdown(this.breakdownStatusses()));
    }

    @Override
    public ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTasksComScheduleBreakdown(() -> this.communicationTaskService.getCommunicationTasksComScheduleBreakdown(this.breakdownStatusses(), deviceGroup));
    }

    private ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown(Supplier<Map<ComSchedule, Map<TaskStatus, Long>>> breakDownSupplier) {
        ComScheduleBreakdownImpl breakdown = new ComScheduleBreakdownImpl();
        Map<ComSchedule, Map<TaskStatus, Long>> comScheduleBreakdown = breakDownSupplier.get();
        for (ComSchedule comSchedule : comScheduleBreakdown.keySet()) {
            Map<TaskStatus, Long> statusCount = comScheduleBreakdown.get(comSchedule);
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(comSchedule, this.successCount(statusCount), this.failedCount(statusCount), this.pendingCount(statusCount)));
        }
        return breakdown;
    }

    @Override
    public ComTaskBreakdown getCommunicationTasksBreakdown() {
        return this.getCommunicationTasksBreakdown(HashSet::new);
    }

    @Override
    public ComTaskBreakdown getCommunicationTasksBreakdown(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTasksBreakdown(() -> this.asSet(deviceGroup));
    }

    private Set<EndDeviceGroup> asSet(EndDeviceGroup deviceGroup) {
        return Stream.of(deviceGroup).collect(Collectors.toSet());
    }

    private ComTaskBreakdown getCommunicationTasksBreakdown(Supplier<Set<EndDeviceGroup>> deviceGroupsSupplier) {
        ComTaskBreakdownImpl breakdown = new ComTaskBreakdownImpl();
        for (ComTask comTask : this.availableComTasks()) {
            ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
            filter.taskStatuses = this.breakdownStatusses();
            filter.comTasks.add(comTask);
            filter.deviceGroups = deviceGroupsSupplier.get();
            Map<TaskStatus, Long> statusCount = this.communicationTaskService.getComTaskExecutionStatusCount(filter);
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(comTask, this.successCount(statusCount), this.failedCount(statusCount), this.pendingCount(statusCount)));
        }
        return breakdown;
    }

    @Override
    public ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview() {
        return this.getCommunicationTaskCompletionResultOverview(this.communicationTaskService::getComTaskLastComSessionHighestPriorityCompletionCodeCount);
    }

    @Override
    public ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTaskCompletionResultOverview(() -> this.communicationTaskService.getComTaskLastComSessionHighestPriorityCompletionCodeCount(deviceGroup));
    }

    private ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview(Supplier<Map<CompletionCode, Long>> completionCodeCountSupplier) {
        ComCommandCompletionCodeOverviewImpl overview = new ComCommandCompletionCodeOverviewImpl();
        Map<CompletionCode, Long> completionCodeCount = completionCodeCountSupplier.get();
        for (CompletionCode completionCode : CompletionCode.values()) {
            overview.add(new CounterImpl<>(completionCode, completionCodeCount.get(completionCode)));
        }
        return overview;
    }

    @Override
    public CommunicationTaskHeatMap getCommunicationTasksHeatMap() {
        return this.getCommunicationTasksHeatMap(this.communicationTaskService::getComTasksDeviceTypeHeatMap);
    }

    @Override
    public CommunicationTaskHeatMap getCommunicationTasksHeatMap(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTasksHeatMap(() -> this.communicationTaskService.getComTasksDeviceTypeHeatMap(deviceGroup));
    }

    private CommunicationTaskHeatMap getCommunicationTasksHeatMap(Supplier<Map<DeviceType, List<Long>>> rawDataSupplier) {
        CommunicationTaskHeatMapImpl heatMap = new CommunicationTaskHeatMapImpl();
        Map<DeviceType, List<Long>> rawData = rawDataSupplier.get();
        for (DeviceType deviceType : rawData.keySet()) {
            List<Long> counters = rawData.get(deviceType);
            CommunicationTaskHeatMapRowImpl heatMapRow = new CommunicationTaskHeatMapRowImpl(deviceType);
            heatMapRow.add(this.newComCommandCompletionCodeOverview(counters));
            heatMap.add(heatMapRow);
        }
        return heatMap;
    }

    private ComCommandCompletionCodeOverview newComCommandCompletionCodeOverview(List<Long> counters) {
        Iterator<Long> completionCodeValues = counters.iterator();
        ComCommandCompletionCodeOverviewImpl overview = new ComCommandCompletionCodeOverviewImpl();
        for (CompletionCode completionCode : CompletionCode.values()) {
            overview.add(new CounterImpl<>(completionCode, completionCodeValues.next()));
        }
        return overview;
    }

    private List<ComSession.SuccessIndicator> orderedSuccessIndicators() {
        return Arrays.asList(ComSession.SuccessIndicator.Success, ComSession.SuccessIndicator.SetupError, ComSession.SuccessIndicator.Broken);
    }

    private List<ComTask> availableComTasks() {
        return this.taskService.findAllComTasks().find();
    }

    private EnumSet<TaskStatus> successTaskStatusses() {
        return EnumSet.of(TaskStatus.Waiting);
    }

    private EnumSet<TaskStatus> failedTaskStatusses() {
        return EnumSet.of(TaskStatus.Failed, TaskStatus.NeverCompleted);
    }

    private EnumSet<TaskStatus> pendingTaskStatusses() {
        return EnumSet.of(TaskStatus.Pending, TaskStatus.Busy, TaskStatus.Retrying);
    }

    private long successCount(Map<TaskStatus, Long> statusCount) {
        return this.count(statusCount, this.successTaskStatusses());
    }

    private long failedCount(Map<TaskStatus, Long> statusCount) {
        return this.count(statusCount, this.failedTaskStatusses());
    }

    private long pendingCount(Map<TaskStatus, Long> statusCount) {
        return this.count(statusCount, this.pendingTaskStatusses());
    }

    private long count(Map<TaskStatus, Long> statusCount, Set<TaskStatus> taskStatusses) {
        long total = 0;
        for (TaskStatus taskStatus : taskStatusses) {
            total = total + statusCount.get(taskStatus);
        }
        return total;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

}