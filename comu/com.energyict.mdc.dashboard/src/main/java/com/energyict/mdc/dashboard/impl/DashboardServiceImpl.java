package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComPortPoolHeatMap;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.ConnectionTypeHeatMap;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.DeviceTypeHeatMap;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides an implementation for the {@link DashboardService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (10:27)
 */
@Component(name = "com.energyict.mdc.dashboard", service = {DashboardService.class}, property = "name=DBS")
public class DashboardServiceImpl implements DashboardService {

    private volatile EngineModelService engineModelService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceDataService deviceDataService;
    private volatile ProtocolPluggableService protocolPluggableService;

    public DashboardServiceImpl() {
        super();
    }

    @Inject
    public DashboardServiceImpl(EngineModelService engineModelService, DeviceConfigurationService deviceConfigurationService, DeviceDataService deviceDataService, ProtocolPluggableService protocolPluggableService) {
        this();
        this.setEngineModelService(engineModelService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setDeviceDataService(deviceDataService);
        this.setProtocolPluggableService(protocolPluggableService);
    }

    @Override
    public TaskStatusOverview getConnectionTaskStatusOverview() {
        TaskStatusOverviewImpl overview = new TaskStatusOverviewImpl();
        Map<TaskStatus, Long> statusCounters = this.deviceDataService.getConnectionTaskStatusCount();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            overview.add(new CounterImpl<>(taskStatus, statusCounters.get(taskStatus)));
        }
        return overview;
    }

    @Override
    public ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview() {
        ComSessionSuccessIndicatorOverviewImpl overview = new ComSessionSuccessIndicatorOverviewImpl(this.deviceDataService.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask());
        Map<ComSession.SuccessIndicator, Long> successIndicatorCount = this.deviceDataService.getConnectionTaskLastComSessionSuccessIndicatorCount();
        for (ComSession.SuccessIndicator successIndicator : ComSession.SuccessIndicator.values()) {
            overview.add(new CounterImpl<>(successIndicator, successIndicatorCount.get(successIndicator)));
        }
        return overview;
    }

    @Override
    public ComPortPoolBreakdown getComPortPoolBreakdown() {
        ComPortPoolBreakdownImpl breakdown = new ComPortPoolBreakdownImpl();
        for (ComPortPool comPortPool : this.availableComPortPools()) {
            ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
            filter.useLastComSession = true;
            filter.taskStatuses = this.breakdownStatusses();
            filter.comPortPools.add(comPortPool);
            Map<TaskStatus, Long> statusCount = this.deviceDataService.getConnectionTaskStatusCount(filter);
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(comPortPool, this.successCount(statusCount), this.failedCount(statusCount), this.pendingCount(statusCount)));
        }
        return breakdown;
    }

    private List<ComPortPool> availableComPortPools () {
        return this.engineModelService.findAllComPortPools();
    }

    @Override
    public ConnectionTypeBreakdown getConnectionTypeBreakdown() {
        ConnectionTypeBreakdownImpl breakdown = new ConnectionTypeBreakdownImpl();
        for (ConnectionTypePluggableClass connectionTypePluggableClass : this.availableConnectionTypes()) {
            ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
            filter.useLastComSession = true;
            filter.taskStatuses = this.breakdownStatusses();
            filter.connectionTypes.add(connectionTypePluggableClass);
            Map<TaskStatus, Long> statusCount = this.deviceDataService.getConnectionTaskStatusCount(filter);
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(connectionTypePluggableClass, this.successCount(statusCount), this.failedCount(statusCount), this.pendingCount(statusCount)));
        }
        return breakdown;
    }

    private List<ConnectionTypePluggableClass> availableConnectionTypes () {
        return this.protocolPluggableService.findAllConnectionTypePluggableClasses();
    }

    @Override
    public DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown() {
        DeviceTypeBreakdownImpl breakdown = new DeviceTypeBreakdownImpl();
        for (DeviceType deviceType : this.availableDeviceTypes()) {
            ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
            filter.useLastComSession = true;
            filter.taskStatuses = this.breakdownStatusses();
            filter.deviceTypes.add(deviceType);
            Map<TaskStatus, Long> statusCount = this.deviceDataService.getConnectionTaskStatusCount(filter);
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(deviceType, this.successCount(statusCount), this.failedCount(statusCount), this.pendingCount(statusCount)));
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
        ConnectionTypeHeatMapImpl heatMap = new ConnectionTypeHeatMapImpl();
        Map<ConnectionTypePluggableClass, List<Long>> rawData = this.deviceDataService.getConnectionTypeHeatMap();
        for (ConnectionTypePluggableClass connectionTypePluggableClass : rawData.keySet()) {
            List<Long> counters = rawData.get(connectionTypePluggableClass);
            HeatMapRowImpl<ConnectionTypePluggableClass> heatMapRow = new HeatMapRowImpl<>(connectionTypePluggableClass);
            heatMapRow.add(this.newComSessionSuccessIndicatorOverview(counters));
            heatMap.add(heatMapRow);
        }
        return heatMap;
    }

    @Override
    public DeviceTypeHeatMap getDeviceTypeHeatMap() {
        DeviceTypeHeatMapImpl heatMap = new DeviceTypeHeatMapImpl();
        Map<DeviceType, List<Long>> rawData = this.deviceDataService.getDeviceTypeHeatMap();
        for (DeviceType deviceType : rawData.keySet()) {
            List<Long> counters = rawData.get(deviceType);
            HeatMapRowImpl<DeviceType> heatMapRow = new HeatMapRowImpl<>(deviceType);
            heatMapRow.add(this.newComSessionSuccessIndicatorOverview(counters));
            heatMap.add(heatMapRow);
        }
        return heatMap;
    }

    @Override
    public ComPortPoolHeatMap getComPortPoolHeatMap() {
        ComPortPoolHeatMapImpl heatMap = new ComPortPoolHeatMapImpl();
        Map<ComPortPool, List<Long>> rawData = this.deviceDataService.getComPortPoolHeatMap();
        for (ComPortPool comPortPool : rawData.keySet()) {
            List<Long> counters = rawData.get(comPortPool);
            HeatMapRowImpl<ComPortPool> heatMapRow = new HeatMapRowImpl<>(comPortPool);
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
        TaskStatusOverviewImpl overview = new TaskStatusOverviewImpl();
        Map<TaskStatus, Long> statusCounters = this.deviceDataService.getComTaskExecutionStatusCount();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            overview.add(new CounterImpl<>(taskStatus, statusCounters.get(taskStatus)));
        }
        return overview;
    }

    @Override
    public DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown() {
        DeviceTypeBreakdownImpl breakdown = new DeviceTypeBreakdownImpl();
        for (DeviceType deviceType : this.availableDeviceTypes()) {
            ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
            filter.taskStatuses = this.breakdownStatusses();
            filter.deviceTypes.add(deviceType);
            Map<TaskStatus, Long> statusCount = this.deviceDataService.getComTaskExecutionStatusCount(filter);
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(deviceType, this.successCount(statusCount), this.failedCount(statusCount), this.pendingCount(statusCount)));
        }
        return breakdown;
    }

    private List<ComSession.SuccessIndicator> orderedSuccessIndicators() {
        return Arrays.asList(ComSession.SuccessIndicator.SetupError, ComSession.SuccessIndicator.Broken);
    }

    private List<DeviceType> availableDeviceTypes () {
        return this.deviceConfigurationService.findAllDeviceTypes().find();
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
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

}