package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionStatusOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.history.ComSession;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
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
    private volatile TaskHistoryService taskHistoryService;
    private volatile ProtocolPluggableService protocolPluggableService;

    public DashboardServiceImpl() {
        super();
    }

    @Inject
    public DashboardServiceImpl(EngineModelService engineModelService, DeviceConfigurationService deviceConfigurationService, DeviceDataService deviceDataService, TaskHistoryService taskHistoryService, ProtocolPluggableService protocolPluggableService) {
        this();
        this.setEngineModelService(engineModelService);
        this.setDeviceConfigurationService(deviceConfigurationService);
        this.setDeviceDataService(deviceDataService);
        this.setTaskHistoryService(taskHistoryService);
        this.setProtocolPluggableService(protocolPluggableService);
    }

    @Override
    public ConnectionStatusOverview getConnectionStatusOverview() {
        ConnectionStatusOverviewImpl overview = new ConnectionStatusOverviewImpl();
        Map<TaskStatus, Long> statusCounters = this.deviceDataService.getConnectionTaskStatusCount();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            overview.add(new CounterImpl<>(taskStatus, statusCounters.get(taskStatus)));
        }
        return overview;
    }

    @Override
    public ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview() {
        ComSessionSuccessIndicatorOverviewImpl overview = new ComSessionSuccessIndicatorOverviewImpl(this.taskHistoryService.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask());
        Map<ComSession.SuccessIndicator, Long> successIndicatorCount = this.taskHistoryService.getConnectionTaskLastComSessionSuccessIndicatorCount();
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
            this.addBreakdownStatusses(filter);
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
            this.addBreakdownStatusses(filter);
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
    public DeviceTypeBreakdown getDeviceTypeBreakdown() {
        DeviceTypeBreakdownImpl breakdown = new DeviceTypeBreakdownImpl();
        for (DeviceType deviceType : this.availableDeviceTypes()) {
            ConnectionTaskFilterSpecification filter = new ConnectionTaskFilterSpecification();
            this.addBreakdownStatusses(filter);
            filter.deviceTypes.add(deviceType);
            Map<TaskStatus, Long> statusCount = this.deviceDataService.getConnectionTaskStatusCount(filter);
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(deviceType, this.successCount(statusCount), this.failedCount(statusCount), this.pendingCount(statusCount)));
        }
        return breakdown;
    }

    private void addBreakdownStatusses(ConnectionTaskFilterSpecification filter) {
        filter.taskStatuses = EnumSet.noneOf(TaskStatus.class);
        filter.taskStatuses.addAll(this.successTaskStatusses());
        filter.taskStatuses.addAll(this.failedTaskStatusses());
        filter.taskStatuses.addAll(this.pendingTaskStatusses());
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
    public void setTaskHistoryService(TaskHistoryService taskHistoryService) {
        this.taskHistoryService = taskHistoryService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

}