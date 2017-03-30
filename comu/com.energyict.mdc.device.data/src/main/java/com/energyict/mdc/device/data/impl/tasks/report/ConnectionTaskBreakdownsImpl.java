/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.ConnectionTaskBreakdowns;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link ConnectionTaskBreakdowns} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-05 (12:10)
 */
public class ConnectionTaskBreakdownsImpl implements ConnectionTaskBreakdowns, BreakdownResultProcessor {

    private final EngineConfigurationService engineConfigurationService;
    private final ProtocolPluggableService protocolPluggableService;
    private final DeviceConfigurationService deviceConfigurationService;

    Map<TaskStatus, Long> statusCounters = new HashMap<>();
    Map<Long, Map<TaskStatus, Long>> comPortPoolCounters = new HashMap<>();
    Map<Long, Map<TaskStatus, Long>> connectionTypeCounters = new HashMap<>();
    Map<Long, Map<TaskStatus, Long>> deviceTypeCounters = new HashMap<>();

    @Inject
    public ConnectionTaskBreakdownsImpl(EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService) {
        this.engineConfigurationService = engineConfigurationService;
        this.protocolPluggableService = protocolPluggableService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.initializeCounters();
    }

    private void initializeCounters() {
        this.statusCounters = this.statusCountMapWithAllZeros();
        this.comPortPoolCounters = new HashMap<>();
        this.connectionTypeCounters = new HashMap<>();
        this.deviceTypeCounters = new HashMap<>();
    }

    @Override
    public Map<TaskStatus, Long> getStatusBreakdown() {
        return Collections.unmodifiableMap(this.statusCounters);
    }

    @Override
    public Map<ComPortPool, Map<TaskStatus, Long>> getComPortPoolBreakdown() {
        Map<ComPortPool, Map<TaskStatus, Long>> breakDown = new HashMap<>();
        this.engineConfigurationService
                .findAllComPortPools()
                .stream()
                .forEach(pool -> breakDown.put(pool, this.statusCountMapWithAllZeros()));
        this.comPortPoolCounters.forEach((comPortPoolId, counters) -> this.addToComPortPoolBreakdown(comPortPoolId, counters, breakDown));
        return breakDown;
    }

    private Map<TaskStatus, Long> statusCountMapWithAllZeros() {
        HashMap<TaskStatus, Long> map = new HashMap<>();
        Stream.of(TaskStatus.values()).forEach(status -> map.put(status, 0L));
        return map;
    }

    private void addToComPortPoolBreakdown(Long comPortPoolId, Map<TaskStatus, Long> counters, Map<ComPortPool, Map<TaskStatus, Long>> breakDown) {
        ComPortPool comPortPool = this.findById(comPortPoolId, breakDown.keySet());
        breakDown.put(comPortPool, counters);
    }

    private <T extends HasId> T findById(long id, Set<T> idBusinessObjects) {
        return idBusinessObjects.stream().filter(cs -> cs.getId() == id).findAny().get();
    }

    @Override
    public Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> getConnectionTypeBreakdown() {
        Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> breakDown = new HashMap<>();
        this.protocolPluggableService
                .findAllConnectionTypePluggableClasses()
                .stream()
                .forEach(pc -> breakDown.put(pc, this.statusCountMapWithAllZeros()));
        this.connectionTypeCounters.forEach((pcId, counters) -> this.addToConnectionTypeBreakdown(pcId, counters, breakDown));
        return breakDown;
    }

    private void addToConnectionTypeBreakdown(Long id, Map<TaskStatus, Long> counters, Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> breakDown) {
        ConnectionTypePluggableClass comPortPool = this.findById(id, breakDown.keySet());
        breakDown.put(comPortPool, counters);
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

    public void addComPortPoolStatusCount(BreakdownResult row) {
        this.mergeStatusCounter(this.comPortPoolCounters, row.getBreakdownTargetId(), row.getStatus(), row.getCount());
    }

    public void addConnectionTypeStatusCount(BreakdownResult row) {
        this.mergeStatusCounter(this.connectionTypeCounters, row.getBreakdownTargetId(), row.getStatus(), row.getCount());
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
    public void addComScheduleStatusCount(BreakdownResult breakdownResult) {
        throw new UnsupportedOperationException("ComSchedule aspect is not supported for ConnectionTasks");
    }

    @Override
    public void addComTaskStatusCount(BreakdownResult breakdownResult) {
        throw new UnsupportedOperationException("ComTask aspect is not supported for ConnectionTasks");
    }

}