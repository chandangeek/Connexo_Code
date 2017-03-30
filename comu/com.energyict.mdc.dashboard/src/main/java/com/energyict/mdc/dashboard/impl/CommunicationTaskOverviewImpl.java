/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ComCommandCompletionCodeOverview;
import com.energyict.mdc.dashboard.ComScheduleBreakdown;
import com.energyict.mdc.dashboard.ComTaskBreakdown;
import com.energyict.mdc.dashboard.CommunicationTaskHeatMap;
import com.energyict.mdc.dashboard.CommunicationTaskOverview;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.CommunicationTaskBreakdowns;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;

import java.util.List;
import java.util.Map;

/**
 * Provides an implementation for the {@link CommunicationTaskOverview} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-22 (11:18)
 */
class CommunicationTaskOverviewImpl implements CommunicationTaskOverview {

    private TaskStatusOverviewImpl taskStatusOverview = TaskStatusOverviewImpl.empty();
    private ComCommandCompletionCodeOverviewImpl completionCodeOverview = new ComCommandCompletionCodeOverviewImpl();
    private CommunicationTaskHeatMapImpl heatMap = new CommunicationTaskHeatMapImpl();
    private ComScheduleBreakdownImpl comScheduleBreakdown = new ComScheduleBreakdownImpl();
    private ComTaskBreakdownImpl comTaskBreakdown = new ComTaskBreakdownImpl();
    private DeviceTypeBreakdownImpl deviceTypeBreakdown = DeviceTypeBreakdownImpl.empty();

    CommunicationTaskOverviewImpl(CommunicationTaskBreakdowns breakdowns, Map<DeviceType, List<Long>> heatMap) {
        super();
        this.initializeTaskStatusOverview(breakdowns.getStatusBreakdown());
        this.initializeComScheduleBreakdown(breakdowns.getComScheduleBreakdown());
        this.initializeComTaskBreakdown(breakdowns.getComTaskBreakdown());
        this.initializeDeviceTypeBreakdown(breakdowns.getDeviceTypeBreakdown());
        this.initializeHeatMap(heatMap);
        this.initializeCompletionCodeOverview();
    }

    private void initializeTaskStatusOverview(Map<TaskStatus, Long> statusBreakdown) {
        for (TaskStatus taskStatus : TaskStatus.values()) {
            this.taskStatusOverview.add(new CounterImpl<>(taskStatus, statusBreakdown.get(taskStatus)));
        }
    }

    private void initializeComScheduleBreakdown(Map<ComSchedule, Map<TaskStatus, Long>> comScheduleBreakdown) {
        for (ComSchedule comSchedule : comScheduleBreakdown.keySet()) {
            Map<TaskStatus, Long> statusCount = comScheduleBreakdown.get(comSchedule);
            this.comScheduleBreakdown.add(
                    new TaskStatusBreakdownCounterImpl<>(
                            comSchedule,
                            TaskStatusses.SUCCESS.count(statusCount),
                            TaskStatusses.FAILED.count(statusCount),
                            TaskStatusses.PENDING.count(statusCount)));
        }
    }

    private void initializeComTaskBreakdown(Map<ComTask, Map<TaskStatus, Long>> comTaskBreakdown) {
        for (ComTask comTask : comTaskBreakdown.keySet()) {
            Map<TaskStatus, Long> statusCount = comTaskBreakdown.get(comTask);
            this.comTaskBreakdown.add(
                    new TaskStatusBreakdownCounterImpl<>(
                            comTask,
                            TaskStatusses.SUCCESS.count(statusCount),
                            TaskStatusses.FAILED.count(statusCount),
                            TaskStatusses.PENDING.count(statusCount)));
        }
    }

    private void initializeDeviceTypeBreakdown(Map<DeviceType, Map<TaskStatus, Long>> deviceTypeBreakdown) {
        for (DeviceType deviceType : deviceTypeBreakdown.keySet()) {
            Map<TaskStatus, Long> statusCount = deviceTypeBreakdown.get(deviceType);
            this.deviceTypeBreakdown.add(
                    new TaskStatusBreakdownCounterImpl<>(
                            deviceType,
                            TaskStatusses.SUCCESS.count(statusCount),
                            TaskStatusses.FAILED.count(statusCount),
                            TaskStatusses.PENDING.count(statusCount)));
        }
    }

    private void initializeHeatMap(Map<DeviceType, List<Long>> rawData) {
        this.heatMap = new CommunicationTaskHeatMapImpl(rawData);
    }

    private void initializeCompletionCodeOverview() {
        this.completionCodeOverview = this.heatMap.getOverview();
    }

    @Override
    public TaskStatusOverview getStatusOverview() {
        return this.taskStatusOverview;
    }

    @Override
    public ComCommandCompletionCodeOverview getCommunicationTaskCompletionResultOverview() {
        return this.completionCodeOverview;
    }

    @Override
    public ComScheduleBreakdown getComScheduleBreakdown() {
        return this.comScheduleBreakdown;
    }

    @Override
    public ComTaskBreakdown getComTaskBreakdown() {
        return this.comTaskBreakdown;
    }

    @Override
    public DeviceTypeBreakdown getDeviceTypeBreakdown() {
        return this.deviceTypeBreakdown;
    }

    @Override
    public CommunicationTaskHeatMap getHeatMap() {
        return this.heatMap;
    }

}