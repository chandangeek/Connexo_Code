/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComPortPoolHeatMap;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMap;
import com.energyict.mdc.dashboard.CommunicationTaskOverview;
import com.energyict.mdc.dashboard.ConnectionTaskDeviceTypeHeatMap;
import com.energyict.mdc.dashboard.ConnectionTaskOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.ConnectionTypeHeatMap;
import com.energyict.mdc.dashboard.DashboardService;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.CommunicationTaskBreakdowns;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
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

/**
 * Provides an implementation for the {@link DashboardService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (10:27)
 */
@Component(name = "com.energyict.mdc.dashboard", service = {DashboardService.class}, property = "name=DBS")
public class DashboardServiceImpl implements DashboardService {

    private volatile ConnectionTaskReportService connectionTaskReportService;
    private volatile CommunicationTaskReportService communicationTaskReportService;
    private volatile TaskService taskService;

    // For OSGi framework
    public DashboardServiceImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public DashboardServiceImpl(TaskService taskService, ConnectionTaskReportService connectionTaskReportService, CommunicationTaskReportService communicationTaskReportService) {
        this();
        this.setTaskService(taskService);
        this.setConnectionTaskReportService(connectionTaskReportService);
        this.setCommunicationTaskReportService(communicationTaskReportService);
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setConnectionTaskReportService(ConnectionTaskReportService connectionTaskReportService) {
        this.connectionTaskReportService = connectionTaskReportService;
    }

    @Reference
    public void setCommunicationTaskReportService(CommunicationTaskReportService communicationTaskReportService) {
        this.communicationTaskReportService = communicationTaskReportService;
    }

    @Override
    public TaskStatusOverview getConnectionTaskStatusOverview() {
        return this.getConnectionTaskStatusOverview(this.connectionTaskReportService::getConnectionTaskStatusCount);
    }

    @Override
    public TaskStatusOverview getConnectionTaskStatusOverview(EndDeviceGroup deviceGroup) {
        return this.getConnectionTaskStatusOverview(() -> this.connectionTaskReportService.getConnectionTaskStatusCount(deviceGroup));
    }

    private TaskStatusOverview getConnectionTaskStatusOverview(Supplier<Map<TaskStatus, Long>> statusCountersSupplier) {
        return TaskStatusOverviewImpl.from(statusCountersSupplier.get());
    }

    @Override
    public ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview() {
        return this.getComSessionSuccessIndicatorOverview(this.connectionTaskReportService::getConnectionTaskLastComSessionSuccessIndicatorCount);
    }

    @Override
    public ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview(EndDeviceGroup deviceGroup) {
        return this.getComSessionSuccessIndicatorOverview(() -> this.connectionTaskReportService.getConnectionTaskLastComSessionSuccessIndicatorCount(deviceGroup), deviceGroup);
    }

    private ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview(Supplier<Map<ComSession.SuccessIndicator, Long>> successIndicatorCountSupplier) {
        Map<ComSession.SuccessIndicator, Long> successIndicatorCount = successIndicatorCountSupplier.get();
        return ComSessionSuccessIndicatorOverviewImpl.from(
                this.connectionTaskReportService.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(),
                successIndicatorCount);
    }

    private ComSessionSuccessIndicatorOverview getComSessionSuccessIndicatorOverview(Supplier<Map<ComSession.SuccessIndicator, Long>> successIndicatorCountSupplier, EndDeviceGroup deviceGroup) {
        Map<ComSession.SuccessIndicator, Long> successIndicatorCount = successIndicatorCountSupplier.get();
        return ComSessionSuccessIndicatorOverviewImpl.from(
                this.connectionTaskReportService.countConnectionTasksLastComSessionsWithAtLeastOneFailedTask(deviceGroup),
                successIndicatorCount);
    }

    @Override
    public ComPortPoolBreakdown getComPortPoolBreakdown() {
        return this.getComPortPoolBreakdown(() -> this.connectionTaskReportService.getComPortPoolBreakdown(this.breakdownStatusses()));
    }

    @Override
    public ComPortPoolBreakdown getComPortPoolBreakdown(EndDeviceGroup deviceGroup) {
        return this.getComPortPoolBreakdown(() -> this.connectionTaskReportService.getComPortPoolBreakdown(this.breakdownStatusses(), deviceGroup));
    }

    private ComPortPoolBreakdown getComPortPoolBreakdown(Supplier<Map<ComPortPool, Map<TaskStatus, Long>>> rawDataSupplier) {
        return ComPortPoolBreakdownImpl.from(rawDataSupplier.get());
    }

    @Override
    public ConnectionTypeBreakdown getConnectionTypeBreakdown() {
        return this.getConnectionTypeBreakdown(() -> this.connectionTaskReportService.getConnectionTypeBreakdown(this.breakdownStatusses()));
    }

    @Override
    public ConnectionTypeBreakdown getConnectionTypeBreakdown(EndDeviceGroup deviceGroup) {
        return this.getConnectionTypeBreakdown(() -> this.connectionTaskReportService.getConnectionTypeBreakdown(this.breakdownStatusses(), deviceGroup));
    }

    private ConnectionTypeBreakdown getConnectionTypeBreakdown(Supplier<Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>>> rawDataSupplier) {
        return ConnectionTypeBreakdownImpl.from(rawDataSupplier.get());
    }

    @Override
    public DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown() {
        return this.getConnectionTasksDeviceTypeBreakdown(() -> this.connectionTaskReportService.getDeviceTypeBreakdown(this.breakdownStatusses()));
    }

    @Override
    public DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown(EndDeviceGroup deviceGroup) {
        return this.getConnectionTasksDeviceTypeBreakdown(() -> this.connectionTaskReportService.getDeviceTypeBreakdown(this.breakdownStatusses(), deviceGroup));
    }

    private DeviceTypeBreakdown getConnectionTasksDeviceTypeBreakdown(Supplier<Map<DeviceType, Map<TaskStatus, Long>>> rawDataSupplier) {
        return DeviceTypeBreakdownImpl.from(rawDataSupplier.get());
    }

    private Set<TaskStatus> breakdownStatusses() {
        Set<TaskStatus> taskStatuses = EnumSet.noneOf(TaskStatus.class);
        taskStatuses.addAll(TaskStatusses.SUCCESS.taskStatusses());
        taskStatuses.addAll(TaskStatusses.FAILED.taskStatusses());
        taskStatuses.addAll(TaskStatusses.PENDING.taskStatusses());
        return taskStatuses;
    }

    @Override
    public ConnectionTaskOverview getConnectionTaskOverview() {
        return new ConnectionTaskOverviewImpl(
                this.connectionTaskReportService.getConnectionTaskBreakdowns(),
                this.getComSessionSuccessIndicatorOverview());
    }

    @Override
    public ConnectionTaskOverview getConnectionTaskOverview(EndDeviceGroup deviceGroup) {
        return new ConnectionTaskOverviewImpl(
                this.connectionTaskReportService.getConnectionTaskBreakdowns(deviceGroup),
                this.getComSessionSuccessIndicatorOverview(deviceGroup));
    }

    @Override
    public ConnectionTypeHeatMap getConnectionTypeHeatMap() {
        return this.getConnectionTypeHeatMap(this.connectionTaskReportService::getConnectionTypeHeatMap);
    }

    @Override
    public ConnectionTypeHeatMap getConnectionTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.getConnectionTypeHeatMap(() -> this.connectionTaskReportService.getConnectionTypeHeatMap(deviceGroup));
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
        return this.getConnectionsDeviceTypeHeatMap(this.connectionTaskReportService::getConnectionsDeviceTypeHeatMap);
    }

    @Override
    public ConnectionTaskDeviceTypeHeatMap getConnectionsDeviceTypeHeatMap(EndDeviceGroup deviceGroup) {
        return this.getConnectionsDeviceTypeHeatMap(() -> this.connectionTaskReportService.getConnectionsDeviceTypeHeatMap(deviceGroup));
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
        return this.getConnectionsComPortPoolHeatMap(this.connectionTaskReportService::getConnectionsComPortPoolHeatMap);
    }

    @Override
    public ComPortPoolHeatMap getConnectionsComPortPoolHeatMap(EndDeviceGroup deviceGroup) {
        return this.getConnectionsComPortPoolHeatMap(() -> this.connectionTaskReportService.getConnectionsComPortPoolHeatMap(deviceGroup));
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
        return this.getCommunicationTaskStatusOverview(this.communicationTaskReportService::getComTaskExecutionStatusCount);
    }

    @Override
    public TaskStatusOverview getCommunicationTaskStatusOverview(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTaskStatusOverview(() -> this.communicationTaskReportService.getComTaskExecutionStatusCount(deviceGroup));
    }

    private TaskStatusOverview getCommunicationTaskStatusOverview(Supplier<Map<TaskStatus, Long>> statusCountersSupplier) {
        return TaskStatusOverviewImpl.from(statusCountersSupplier.get());
    }

    @Override
    public DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown() {
        return this.getCommunicationTasksDeviceTypeBreakdown(() -> this.communicationTaskReportService.getCommunicationTasksDeviceTypeBreakdown(this.breakdownStatusses()));
    }

    @Override
    public DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTasksDeviceTypeBreakdown(() -> this.communicationTaskReportService.getCommunicationTasksDeviceTypeBreakdown(this.breakdownStatusses(), deviceGroup));
    }

    private DeviceTypeBreakdown getCommunicationTasksDeviceTypeBreakdown(Supplier<Map<DeviceType, Map<TaskStatus, Long>>> breakDownSupplier) {
        return DeviceTypeBreakdownImpl.from(breakDownSupplier.get());
    }

    @Override
    public ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown() {
        return this.getCommunicationTasksComScheduleBreakdown(() -> this.communicationTaskReportService.getCommunicationTasksComScheduleBreakdown(this.breakdownStatusses()));
    }

    @Override
    public ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTasksComScheduleBreakdown(() -> this.communicationTaskReportService.getCommunicationTasksComScheduleBreakdown(this.breakdownStatusses(), deviceGroup));
    }

    private ComScheduleBreakdown getCommunicationTasksComScheduleBreakdown(Supplier<Map<ComSchedule, Map<TaskStatus, Long>>> breakDownSupplier) {
        ComScheduleBreakdownImpl breakdown = new ComScheduleBreakdownImpl();
        Map<ComSchedule, Map<TaskStatus, Long>> comScheduleBreakdown = breakDownSupplier.get();
        for (ComSchedule comSchedule : comScheduleBreakdown.keySet()) {
            Map<TaskStatus, Long> statusCount = comScheduleBreakdown.get(comSchedule);
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(comSchedule, TaskStatusses.SUCCESS.count(statusCount), TaskStatusses.FAILED.count(statusCount), TaskStatusses.PENDING.count(statusCount)));
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
            Map<TaskStatus, Long> statusCount = this.communicationTaskReportService.getComTaskExecutionStatusCount(filter);
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(comTask, TaskStatusses.SUCCESS.count(statusCount), TaskStatusses.FAILED.count(statusCount), TaskStatusses.PENDING.count(statusCount)));
        }
        return breakdown;
    }

    @Override
    public ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview() {
        return this.getCommunicationTaskCompletionResultOverview(this.communicationTaskReportService::getComTaskLastComSessionHighestPriorityCompletionCodeCount);
    }

    @Override
    public ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTaskCompletionResultOverview(() -> this.communicationTaskReportService.getComTaskLastComSessionHighestPriorityCompletionCodeCount(deviceGroup));
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
        return this.getCommunicationTasksHeatMap(this.communicationTaskReportService::getComTasksDeviceTypeHeatMap);
    }

    @Override
    public CommunicationTaskHeatMap getCommunicationTasksHeatMap(EndDeviceGroup deviceGroup) {
        return this.getCommunicationTasksHeatMap(() -> this.communicationTaskReportService.getComTasksDeviceTypeHeatMap(deviceGroup));
    }

    private CommunicationTaskHeatMap getCommunicationTasksHeatMap(Supplier<Map<DeviceType, List<Long>>> rawDataSupplier) {
        Map<DeviceType, List<Long>> rawData = rawDataSupplier.get();
        return new CommunicationTaskHeatMapImpl(rawData);
    }

    private List<ComSession.SuccessIndicator> orderedSuccessIndicators() {
        return Arrays.asList(ComSession.SuccessIndicator.Success, ComSession.SuccessIndicator.SetupError, ComSession.SuccessIndicator.Broken);
    }

    private List<ComTask> availableComTasks() {
        return this.taskService.findAllComTasks().find();
    }

    @Override
    public CommunicationTaskOverview getCommunicationTaskOverview() {
        CommunicationTaskBreakdowns breakdowns = this.communicationTaskReportService.getCommunicationTaskBreakdowns();
        Map<DeviceType, List<Long>> heatMap = this.communicationTaskReportService.getComTasksDeviceTypeHeatMap();
        return new CommunicationTaskOverviewImpl(breakdowns, heatMap);
    }

    @Override
    public CommunicationTaskOverview getCommunicationTaskOverview(EndDeviceGroup deviceGroup) {
        CommunicationTaskBreakdowns breakdowns = this.communicationTaskReportService.getCommunicationTaskBreakdowns(deviceGroup);
        Map<DeviceType, List<Long>> heatMap = this.communicationTaskReportService.getComTasksDeviceTypeHeatMap(deviceGroup);
        return new CommunicationTaskOverviewImpl(breakdowns, heatMap);
    }

}