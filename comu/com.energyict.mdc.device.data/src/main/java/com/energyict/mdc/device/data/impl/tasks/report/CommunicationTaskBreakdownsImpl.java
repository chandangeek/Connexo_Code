/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.CommunicationTaskBreakdowns;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CommunicationTaskBreakdowns} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-28 (08:57)
 */
public class CommunicationTaskBreakdownsImpl implements CommunicationTaskBreakdowns, BreakdownResultProcessor {

    private final SchedulingService schedulingService;
    private final TaskService taskService;
    private final DeviceConfigurationService deviceConfigurationService;

    private Map<TaskStatus, Long> statusCounters = new HashMap<>();
    private Map<Long, Map<TaskStatus, Long>> comScheduleCounters = new HashMap<>();
    private Map<Long, Map<TaskStatus, Long>> comTaskCounters = new HashMap<>();
    private Map<Long, Map<TaskStatus, Long>> deviceTypeCounters = new HashMap<>();

    @Inject
    public CommunicationTaskBreakdownsImpl(SchedulingService schedulingService, TaskService taskService, DeviceConfigurationService deviceConfigurationService) {
        super();
        this.schedulingService = schedulingService;
        this.taskService = taskService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.initializeCounters();
    }

    private void initializeCounters() {
        this.statusCounters = this.statusCountMapWithAllZeros();
        this.comScheduleCounters = new HashMap<>();
        this.comTaskCounters = new HashMap<>();
        this.deviceTypeCounters = new HashMap<>();
    }

    private Map<TaskStatus, Long> statusCountMapWithAllZeros() {
        HashMap<TaskStatus, Long> map = new HashMap<>();
        Stream.of(TaskStatus.values()).forEach(status -> map.put(status, 0L));
        return map;
    }

    @Override
    public Map<TaskStatus, Long> getStatusBreakdown() {
        return Collections.unmodifiableMap(this.statusCounters);
    }

    @Override
    public Map<ComSchedule, Map<TaskStatus, Long>> getComScheduleBreakdown() {
        Map<ComSchedule, Map<TaskStatus, Long>> breakDown = new HashMap<>();
        this.schedulingService
                .findAllSchedules()
                .stream()
                .forEach(comSchedule -> breakDown.put(comSchedule, this.statusCountMapWithAllZeros()));
        this.comScheduleCounters.forEach((comScheduleId, counters) -> this.addToComScheduleBreakdown(comScheduleId, counters, breakDown));
        return breakDown;
    }

    private void addToComScheduleBreakdown(Long comScheduleId, Map<TaskStatus, Long> counters, Map<ComSchedule, Map<TaskStatus, Long>> breakDown) {
        ComSchedule comSchedule = this.findById(comScheduleId, breakDown.keySet());
        breakDown.put(comSchedule, counters);
    }

    private <T extends HasId> T findById(long id, Set<T> idBusinessObjects) {
        return idBusinessObjects.stream().filter(cs -> cs.getId() == id).findAny().get();
    }

    @Override
    public Map<ComTask, Map<TaskStatus, Long>> getComTaskBreakdown() {
        Map<ComTask, Map<TaskStatus, Long>> breakDown = new HashMap<>();
        this.taskService
                .findAllComTasks()
                .stream()
                .forEach(comTask -> breakDown.put(comTask, this.statusCountMapWithAllZeros()));
        this.comTaskCounters.forEach((comTaskId, counters) -> this.addToComTaskBreakdown(comTaskId, counters, breakDown));
        return breakDown;
    }

    private void addToComTaskBreakdown(Long comTaskId, Map<TaskStatus, Long> counters, Map<ComTask, Map<TaskStatus, Long>> breakDown) {
        ComTask comTask = this.findById(comTaskId, breakDown.keySet());
        breakDown.put(comTask, counters);
    }

    @Override
    public Map<DeviceType, Map<TaskStatus, Long>> getDeviceTypeBreakdown() {
        Map<DeviceType, Map<TaskStatus, Long>> breakDown = new HashMap<>();
        this.deviceConfigurationService
                .findAllDeviceTypes()
                .stream()
                .forEach(deviceType -> breakDown.put(deviceType, this.statusCountMapWithAllZeros()));
        this.deviceTypeCounters.forEach((deviceTypeId, counters) -> this.addToDeviceTypeBreakdown(deviceTypeId, counters, breakDown));
        return breakDown;
    }

    private void addToDeviceTypeBreakdown(Long deviceTypeId, Map<TaskStatus, Long> counters, Map<DeviceType, Map<TaskStatus, Long>> breakDown) {
        DeviceType deviceType = this.findById(deviceTypeId, breakDown.keySet());
        breakDown.put(deviceType, counters);
    }

    public void addOverallStatusCount(BreakdownResult row) {
        this.statusCounters.merge(row.getStatus(), row.getCount(), this::mergeCounters);
    }

    public void addComScheduleStatusCount(BreakdownResult row) {
        this.mergeStatusCounter(this.comScheduleCounters, row.getBreakdownTargetId(), row.getStatus(), row.getCount());
    }

    public void addComTaskStatusCount(BreakdownResult row) {
        this.mergeStatusCounter(this.comTaskCounters, row.getBreakdownTargetId(), row.getStatus(), row.getCount());
    }

    public void addDeviceTypeStatusCount(BreakdownResult row) {
        this.mergeStatusCounter(this.deviceTypeCounters, row.getBreakdownTargetId(), row.getStatus(), row.getCount());
    }

    private void mergeStatusCounter(Map<Long, Map<TaskStatus, Long>> counters, Long targetId, TaskStatus status, long count) {
        Map<TaskStatus, Long> availableCounters = counters.get(targetId);
        if (availableCounters == null) {
            Map<TaskStatus, Long> newCounters = this.statusCountMapWithAllZeros();
            newCounters.put(status, count);
            counters.put(targetId, newCounters);
        }
        else {
            availableCounters.merge(status, count, this::mergeCounters);
        }
    }

    private Long mergeCounters(Long l1, Long l2) {
        return l1 + l2;
    }

    @Override
    public void addConnectionTypeStatusCount(BreakdownResult breakdownResult) {
        throw new UnsupportedOperationException("ConnectionType aspect is not supported for ComTaskExecutions");
    }

    @Override
    public void addComPortPoolStatusCount(BreakdownResult breakdownResult) {
        throw new UnsupportedOperationException("ComPortPool aspect is not supported for ComTaskExecutions");
    }

}