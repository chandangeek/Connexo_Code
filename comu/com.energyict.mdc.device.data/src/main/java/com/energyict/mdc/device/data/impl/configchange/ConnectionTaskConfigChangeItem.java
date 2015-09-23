package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.*;
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

        final List<DeviceConfigChangeAction<PartialConnectionTask>> matchedConnectionTasks = DeviceConfigChangeEngine.INSTANCE.getConnectionTaskConfigChangeActions(originDeviceConfiguration, destinationDeviceConfiguration).stream().filter(actionTypeIs(DeviceConfigChangeActionType.MATCH)).collect(Collectors.toList());
        matchedConnectionTasks.stream().forEach(matchedConnectionTask
                -> device.getConnectionTasks().stream()
                .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == matchedConnectionTask.getOrigin().getId()).findFirst()
                .ifPresent(updateConnectionTaskWithNewPartialConnectionTask(matchedConnectionTask.getDestination())));
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
