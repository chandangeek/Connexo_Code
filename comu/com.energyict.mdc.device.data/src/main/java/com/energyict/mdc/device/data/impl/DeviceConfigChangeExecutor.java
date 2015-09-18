package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigChangeAction;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigChangeActionType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.fest.reflect.core.Reflection.field;

/**
 * Applies the actual change in DeviceConfiguration for a single Device
 */
public final class DeviceConfigChangeExecutor {

    private static DeviceConfigChangeExecutor ourInstance = new DeviceConfigChangeExecutor();

    public static DeviceConfigChangeExecutor getInstance() {
        return ourInstance;
    }

    private DeviceConfigChangeExecutor() {
    }

    public Device execute(ServerDeviceForConfigChange device, DeviceConfiguration destinationDeviceConfiguration) {
        final DeviceConfiguration originDeviceConfiguration = device.getDeviceConfiguration();

        prepareForChangeDeviceConfig(device, destinationDeviceConfiguration);

        device.setNewDeviceConfiguration(destinationDeviceConfiguration);

        performLoadProfileSpecChanges(device, originDeviceConfiguration, destinationDeviceConfiguration);
        performLogBookSpecChanges(device, originDeviceConfiguration, destinationDeviceConfiguration);
        performConnectionTaskChanges(device, originDeviceConfiguration, destinationDeviceConfiguration);


        device.save();
        return device;
    }

    /**
     * Prepare the device for his new deviceConfiguration.
     * <ul>
     * <li>lock the device so no other process can update the device</li>
     * <li>validate if we <i>can</i> do a change DeviceConfig with the given destination deviceConfig</li>
     * <li>create a new MeterActivation so all <i>new</i> data is stored on the new meterActivation</li>
     * </ul>
     *
     * @param device                         the device to change it's configuration
     * @param destinationDeviceConfiguration the configuration to change to
     */
    private void prepareForChangeDeviceConfig(ServerDeviceForConfigChange device, DeviceConfiguration destinationDeviceConfiguration) {
        //TODO check to lock here or somewhere else
        device.lock();
        device.validateDeviceCanChangeConfig(destinationDeviceConfiguration);
        device.createNewMeterActivation();
    }

    private void performConnectionTaskChanges(ServerDeviceForConfigChange device, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        final Optional<DeviceConfigConflictMapping> conflicts = device.getDeviceType().getDeviceConfigConflictMappings().stream()
                .filter(getDeviceConfigConflictMappingPredicate(originDeviceConfiguration, destinationDeviceConfiguration)).findFirst();

        conflicts.ifPresent(deviceConfigConflictMapping -> {
            removeConnectionsTasksFromDevice(device, deviceConfigConflictMapping);
            mapConnectionsTasksFromDevice(device, deviceConfigConflictMapping);
        });

        final List<DeviceConfigChangeAction<PartialConnectionTask>> matchedConnectionTasks = DeviceConfigChangeEngine.INSTANCE.getConnectionTaskConfigChangeActions(originDeviceConfiguration, destinationDeviceConfiguration).stream().filter(actionTypeIs(DeviceConfigChangeActionType.MATCH)).collect(Collectors.toList());
        matchedConnectionTasks.stream().forEach(matchedConnectionTask
                -> device.getConnectionTasks().stream()
                .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == matchedConnectionTask.getOrigin().getId()).findFirst()
                .ifPresent(updateConnectionTaskWithNewPartialConnectionTask(matchedConnectionTask.getDestination())));
    }

    private Consumer<ConnectionTask<?, ?>> updateConnectionTaskWithNewPartialConnectionTask(PartialConnectionTask destination) {
        //noinspection unchecked
        return connectionTask -> ((ServerConnectionTaskForConfigChange) connectionTask).setNewPartialConnectionTask(destination);
    }

    private void mapConnectionsTasksFromDevice(ServerDeviceForConfigChange device, DeviceConfigConflictMapping deviceConfigConflictMapping) {
        final List<ConflictingConnectionMethodSolution> conflictsToMap = deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().stream()
                .filter(solutionsForMap()).collect(Collectors.toList());
        conflictsToMap.stream().forEach(conflictingConnectionMethodSolution -> device.getConnectionTasks().stream().filter(onSameConnectionTaskForOrigin(conflictingConnectionMethodSolution))
                .forEach(updateConnectionTaskWithNewPartialConnectionTask(conflictingConnectionMethodSolution.getDestinationDataSource())));
    }

    private void removeConnectionsTasksFromDevice(ServerDeviceForConfigChange device, DeviceConfigConflictMapping deviceConfigConflictMapping) {
        final List<ConnectionTask<?, ?>> connectionTasksToRemove = deviceConfigConflictMapping.getConflictingConnectionMethodSolutions().stream()
                .filter(solutionsForRemove()).flatMap(conflictingConnectionMethodSolution -> device.getConnectionTasks().stream().filter(onSameConnectionTaskForOrigin(conflictingConnectionMethodSolution))).collect(Collectors.toList());
        connectionTasksToRemove.stream().forEach(device::removeConnectionTask);
    }

    private Predicate<ConnectionTask<?, ?>> onSameConnectionTaskForOrigin(ConflictingConnectionMethodSolution conflictingConnectionMethodSolution) {
        return connectionTask -> connectionTask.getPartialConnectionTask().getId() == conflictingConnectionMethodSolution.getOriginDataSource().getId();
    }

    private Predicate<ConflictingSolution<?>> solutionsForRemove() {
        return conflictingSolution -> conflictingSolution.getConflictingMappingAction().equals(DeviceConfigConflictMapping.ConflictingMappingAction.REMOVE);
    }

    private Predicate<ConflictingSolution<?>> solutionsForMap() {
        return conflictingSolution -> conflictingSolution.getConflictingMappingAction().equals(DeviceConfigConflictMapping.ConflictingMappingAction.MAP);
    }

    private Predicate<DeviceConfigConflictMapping> getDeviceConfigConflictMappingPredicate(DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        return deviceConfigConflictMapping -> deviceConfigConflictMapping.getOriginDeviceConfiguration().getId() == originDeviceConfiguration.getId() && deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId() == destinationDeviceConfiguration.getId();
    }


    private void performLogBookSpecChanges(ServerDeviceForConfigChange device, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        final List<DeviceConfigChangeAction<LogBookSpec>> logBookActions = DeviceConfigChangeEngine.INSTANCE.calculateDeviceConfigChangeActionsFor(new DeviceConfigChangeLogBookItem(originDeviceConfiguration, destinationDeviceConfiguration));

        final List<LogBookSpec> logBookSpecsToAdd = getAddItems(logBookActions);
        final List<DeviceConfigChangeAction<LogBookSpec>> matchedLogBookSpecs = getMatchItems(logBookActions);
        final List<LogBook> logBooksToRemove = logBookActions.stream()
                .filter(actionTypeIs(DeviceConfigChangeActionType.REMOVE))
                .flatMap(logBookSpecDeviceConfigChangeAction -> device.getLogBooks().stream()
                        .filter(onCorrespondingLogBook(logBookSpecDeviceConfigChangeAction)))
                .collect(Collectors.toList());

        device.removeLogBooks(logBooksToRemove);
        device.addLogBooks(logBookSpecsToAdd);
        matchedLogBookSpecs.stream().forEach(logBookSpecDeviceConfigChangeAction -> device.getLogBooks().stream()
                .filter(onCorrespondingLogBook(logBookSpecDeviceConfigChangeAction))
                .findFirst()
                .ifPresent(logBook -> ((ServerLogBookForConfigChange) logBook).setNewLogBookSpec(logBookSpecDeviceConfigChangeAction.getDestination())));
    }

    private void performLoadProfileSpecChanges(ServerDeviceForConfigChange device, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        final List<DeviceConfigChangeAction<LoadProfileSpec>> loadProfileActions = DeviceConfigChangeEngine.INSTANCE.calculateDeviceConfigChangeActionsFor(new DeviceConfigChangeLoadProfileItem(originDeviceConfiguration, destinationDeviceConfiguration));

        final List<LoadProfileSpec> loadProfileSpecsToAdd = getAddItems(loadProfileActions);
        final List<DeviceConfigChangeAction<LoadProfileSpec>> matchedLoadProfileSpecs = getMatchItems(loadProfileActions);
        final List<LoadProfile> loadProfilesToRemove = loadProfileActions.stream()
                .filter(actionTypeIs(DeviceConfigChangeActionType.REMOVE))
                .flatMap(loadProfileSpecDeviceConfigChangeAction1 -> device.getLoadProfiles().stream()
                        .filter(onCorrespondingLoadProfile(loadProfileSpecDeviceConfigChangeAction1)))
                .collect(Collectors.toList());

        device.removeLoadProfiles(loadProfilesToRemove);
        device.addLoadProfiles(loadProfileSpecsToAdd);
        matchedLoadProfileSpecs.stream().forEach(loadProfileSpecDeviceConfigChangeAction -> device.getLoadProfiles().stream()
                .filter(onCorrespondingLoadProfile(loadProfileSpecDeviceConfigChangeAction))
                .findFirst()
                .ifPresent(loadProfile -> ((ServerLoadProfileForConfigChange) loadProfile).setNewLoadProfileSpec(loadProfileSpecDeviceConfigChangeAction.getDestination())));
    }

    private Predicate<LoadProfile> onCorrespondingLoadProfile(DeviceConfigChangeAction<LoadProfileSpec> loadProfileSpecDeviceConfigChangeAction) {
        return loadProfile -> loadProfile.getLoadProfileSpec().getId() == loadProfileSpecDeviceConfigChangeAction.getOrigin().getId();
    }

    private Predicate<LogBook> onCorrespondingLogBook(DeviceConfigChangeAction<LogBookSpec> deviceConfigChangeAction) {
        return logBook -> logBook.getLogBookSpec().getId() == deviceConfigChangeAction.getOrigin().getId();
    }

    private <T extends HasId> List<DeviceConfigChangeAction<T>> getMatchItems(List<DeviceConfigChangeAction<T>> loadProfileActions) {
        return loadProfileActions.stream().filter(actionTypeIs(DeviceConfigChangeActionType.MATCH)).collect(Collectors.toList());
    }

    private <T extends HasId> List<T> getAddItems(List<DeviceConfigChangeAction<T>> actions) {
        return actions.stream().filter(actionTypeIs(DeviceConfigChangeActionType.ADD)).map(DeviceConfigChangeAction::getDestination).collect(Collectors.toList());
    }

    private Predicate<DeviceConfigChangeAction<?>> actionTypeIs(DeviceConfigChangeActionType deviceConfigChangeActionType) {
        return deviceConfigChangeAction -> deviceConfigChangeAction.getActionType().equals(deviceConfigChangeActionType);
    }

    abstract class AbstractDeviceConfigChangeItem<T extends HasId> implements DeviceConfigChangeItem<T> {

        final DeviceConfiguration originDeviceConfig;
        final DeviceConfiguration destinationDeviceConfig;

        public AbstractDeviceConfigChangeItem(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig) {
            this.originDeviceConfig = originDeviceConfig;
            this.destinationDeviceConfig = destinationDeviceConfig;
        }

        @Override
        public DeviceConfiguration getOriginDeviceConfig() {
            return originDeviceConfig;
        }

        @Override
        public DeviceConfiguration getDestinationDeviceConfig() {
            return destinationDeviceConfig;
        }
    }

    class DeviceConfigChangeLoadProfileItem extends AbstractDeviceConfigChangeItem<LoadProfileSpec> {

        DeviceConfigChangeLoadProfileItem(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig) {
            super(originDeviceConfig, destinationDeviceConfig);
        }

        @Override
        public List<LoadProfileSpec> getOriginItems() {
            return originDeviceConfig.getLoadProfileSpecs();
        }

        @Override
        public List<LoadProfileSpec> getDestinationItems() {
            return destinationDeviceConfig.getLoadProfileSpecs();
        }

        @Override
        public Predicate<LoadProfileSpec> exactSameItem(LoadProfileSpec item) {
            return loadProfileSpec -> loadProfileSpec.getLoadProfileType().getId() == item.getLoadProfileType().getId();
        }

        @Override
        public Predicate<LoadProfileSpec> isItAConflict(LoadProfileSpec item) {
            return loadProfileSpec -> false;
        }
    }

    class DeviceConfigChangeLogBookItem extends AbstractDeviceConfigChangeItem<LogBookSpec> {

        DeviceConfigChangeLogBookItem(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig) {
            super(originDeviceConfig, destinationDeviceConfig);
        }

        @Override
        public List<LogBookSpec> getOriginItems() {
            return originDeviceConfig.getLogBookSpecs();
        }

        @Override
        public List<LogBookSpec> getDestinationItems() {
            return destinationDeviceConfig.getLogBookSpecs();
        }

        @Override
        public Predicate<LogBookSpec> exactSameItem(LogBookSpec item) {
            return loadProfileSpec -> loadProfileSpec.getLogBookType().getId() == item.getLogBookType().getId();
        }

        @Override
        public Predicate<LogBookSpec> isItAConflict(LogBookSpec item) {
            return loadProfileSpec -> false;
        }
    }
}
