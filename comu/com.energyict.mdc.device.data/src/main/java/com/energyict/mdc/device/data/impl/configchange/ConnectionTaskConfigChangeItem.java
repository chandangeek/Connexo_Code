/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.device.config.DeviceConfigChangeAction;
import com.energyict.mdc.device.config.DeviceConfigChangeEngine;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Applies changes to the connectionTasks of the Device
 */
public class ConnectionTaskConfigChangeItem extends AbstractConfigChangeItem {

    private static ConnectionTaskConfigChangeItem INSTANCE = new ConnectionTaskConfigChangeItem();

    private ConnectionTaskConfigChangeItem() {
    }

    static DataSourceConfigChangeItem getInstance(){
        return INSTANCE;
    }

    @Override
    public void apply(ServerDeviceForConfigChange device, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        final Optional<DeviceConfigConflictMapping> conflict = getDeviceConfigConflictMapping(device, originDeviceConfiguration, destinationDeviceConfiguration);

        conflict.ifPresent(deviceConfigConflictMapping -> {
            removeConnectionsTasksFromDevice(device, deviceConfigConflictMapping);
            mapConnectionsTasksFromDevice(device, deviceConfigConflictMapping);
        });

        List<DeviceConfigChangeAction<PartialConnectionTask>> connectionTaskActions = DeviceConfigChangeEngine.INSTANCE.getConnectionTaskConfigChangeActions(originDeviceConfiguration, destinationDeviceConfiguration);
        List<DeviceConfigChangeAction<PartialConnectionTask>> matchItems = getMatchItems(connectionTaskActions);
        List<PartialConnectionTask> removeItems = getRemoveItems(connectionTaskActions);
        matchItems.stream().forEach(matchedConnectionTask
                -> device.getConnectionTasks().stream()
                .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == matchedConnectionTask.getOrigin().getId()).findFirst()
                .ifPresent(updateConnectionTaskWithNewPartialConnectionTask(matchedConnectionTask.getDestination())));
        removeItems.forEach(partialConnectionTask -> device.getConnectionTasks().stream().filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == partialConnectionTask.getId()).findAny().ifPresent(device::removeConnectionTask));
    }

    private void removeConnectionsTasksFromDevice(ServerDeviceForConfigChange device, DeviceConfigConflictMapping deviceConfigConflictMapping) {
        final List<ConnectionTask<?, ?>> connectionTasksToRemove = deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().stream()
                .filter(solutionsForRemove()).flatMap(conflictingConnectionMethodSolution -> device.getConnectionTasks().stream().filter(onSameConnectionTaskForOrigin(conflictingConnectionMethodSolution))).collect(Collectors.toList());
        connectionTasksToRemove.stream().forEach(device::removeConnectionTask);
    }

    private void mapConnectionsTasksFromDevice(ServerDeviceForConfigChange device, DeviceConfigConflictMapping deviceConfigConflictMapping) {
        final List<ConflictingConnectionMethodSolution> conflictsToMap = deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().stream()
                .filter(solutionsForMap()).collect(Collectors.toList());
        conflictsToMap.stream().forEach(conflictingConnectionMethodSolution -> device.getConnectionTasks().stream().filter(onSameConnectionTaskForOrigin(conflictingConnectionMethodSolution))
                .forEach(updateConnectionTaskWithNewPartialConnectionTask(conflictingConnectionMethodSolution.getDestinationDataSource())));
    }

    private Consumer<ConnectionTask<?, ?>> updateConnectionTaskWithNewPartialConnectionTask(PartialConnectionTask destination) {
        //noinspection unchecked
        return connectionTask -> ((ServerConnectionTaskForConfigChange) connectionTask).setNewPartialConnectionTask(destination);
    }

    private Predicate<ConnectionTask<?, ?>> onSameConnectionTaskForOrigin(ConflictingConnectionMethodSolution conflictingConnectionMethodSolution) {
        return connectionTask -> connectionTask.getPartialConnectionTask().getId() == conflictingConnectionMethodSolution.getOriginDataSource().getId();
    }
}
