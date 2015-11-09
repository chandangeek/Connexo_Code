package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;

import com.elster.jupiter.util.HasId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This engine will check every DeviceConfiguration and it will determine the actions that need to take place if one wants to change
 * the DeviceConfiguration of a Device from one DeviceConfiguration to another.
 * Each DeviceConfigChangeAction has an ActionType which indicates what needs to happen (add, remove, match or conflict).
 */
public final class DeviceConfigChangeEngine {

    public static DeviceConfigChangeEngine INSTANCE = new DeviceConfigChangeEngine();

    private DeviceConfigChangeEngine(){}

    List<DeviceConfigChangeAction> calculateConfigChangeActions(DeviceType deviceType) {
        List<DeviceConfigChangeAction> deviceConfigChangeActions = new ArrayList<>();
        deviceType.getConfigurations().stream().filter(DeviceConfiguration::isActive).forEach(
                origin -> deviceType.getConfigurations().stream()
                        .filter(deviceConfiguration -> deviceConfiguration.isActive() && deviceConfiguration.getId() != origin.getId())
                        .forEach(destination -> calculateFor(origin, destination, deviceConfigChangeActions))
        );
        return deviceConfigChangeActions;
    }

    private void calculateFor(DeviceConfiguration origin, DeviceConfiguration destination, List<DeviceConfigChangeAction> deviceConfigChangeActions) {
        calculateItemsFor(new DeviceConfigChangeConnectionTaskItem(origin, destination), deviceConfigChangeActions);
        calculateItemsFor(new DeviceConfigChangeSecuritySetItem(origin, destination), deviceConfigChangeActions);
    }

    private <T extends HasId> void calculateItemsFor(DeviceConfigChangeItem<T> deviceConfigChangeItem, List<DeviceConfigChangeAction> deviceConfigChangeActions) {
        Set<Long> matchedDestinations = new HashSet<>();
        Set<Long> matchedOrigins = new HashSet<>();
        Set<Long> conflictOrigins = new HashSet<>();
        /*
        Need to do the loops in separate streams in order to get the exact matches FIRST, then the CONFLICTS, then the rest
        */
        // 1 - First find exact match
        findAndCreateExactMatchDeviceConfigChangeActions(deviceConfigChangeItem, matchedDestinations, matchedOrigins, deviceConfigChangeActions);
        // 2 - Find match on name or type to create a conflict
        findAndCreateConflictDeviceConfigChangeActions(deviceConfigChangeItem, matchedDestinations, matchedOrigins, conflictOrigins, deviceConfigChangeActions);
        // 3 - Add all others (create Remove for inverse change)
        findAndCreateAddOrRemoveDeviceConfigChangeActions(deviceConfigChangeItem, matchedOrigins, conflictOrigins, deviceConfigChangeActions);
    }

    private <T extends HasId> void findAndCreateAddOrRemoveDeviceConfigChangeActions(DeviceConfigChangeItem<T> deviceConfigChangeItem, Set<Long> matchedOrigins, Set<Long> conflictOrigins, List<DeviceConfigChangeAction> deviceConfigChangeActions) {
        deviceConfigChangeItem.getOriginItems().stream()
                .filter(isItAlreadyAHandledItem(matchedOrigins).negate())
                .filter(isItAlreadyAHandledItem(conflictOrigins).negate())
                .forEach(originItem -> {
                    createRemoveAction(deviceConfigChangeItem.getOriginDeviceConfig(), deviceConfigChangeItem.getDestinationDeviceConfig(), originItem, deviceConfigChangeActions);
                    createAddAction(deviceConfigChangeItem.getDestinationDeviceConfig(), deviceConfigChangeItem.getOriginDeviceConfig(), originItem, deviceConfigChangeActions);
                });
    }

    private <T extends HasId> void findAndCreateConflictDeviceConfigChangeActions(DeviceConfigChangeItem<T> deviceConfigChangeItem, Set<Long> matchedDestinations, Set<Long> matchedOrigins, Set<Long> conflictOrigins, List<DeviceConfigChangeAction> deviceConfigChangeActions) {
        deviceConfigChangeItem.getOriginItems().stream().filter(isItAlreadyAHandledItem(matchedOrigins).negate())
                .forEach(originItem -> {
                            deviceConfigChangeItem.getDestinationItems().stream()
                                    .filter(isItAlreadyAHandledItem(matchedDestinations).negate())
                                    .filter(deviceConfigChangeItem.isItAConflict(originItem))
                                    .forEach(destinationItem -> {
                                                createConflictAction(deviceConfigChangeItem.getOriginDeviceConfig(), deviceConfigChangeItem.getDestinationDeviceConfig(), originItem, destinationItem, deviceConfigChangeActions);
                                                conflictOrigins.add(originItem.getId());
                                            }
                                    );
                        }
                );
    }

    private <T extends HasId> void findAndCreateExactMatchDeviceConfigChangeActions(DeviceConfigChangeItem<T> deviceConfigChangeItem, Set<Long> matchedDestinations, Set<Long> matchedOrigins, List<DeviceConfigChangeAction> deviceConfigChangeActions) {
        deviceConfigChangeItem.getOriginItems().forEach(
                originItem -> {
                    Optional<T> exactMatch = deviceConfigChangeItem.getDestinationItems().stream().filter(deviceConfigChangeItem.exactSameItem(originItem)).findFirst();
                    exactMatch.ifPresent(destinationItem -> {
                        createExactMatchAction(deviceConfigChangeItem.getOriginDeviceConfig(), deviceConfigChangeItem.getDestinationDeviceConfig(), originItem, destinationItem, deviceConfigChangeActions);
                        matchedDestinations.add(destinationItem.getId());
                        matchedOrigins.add(originItem.getId());
                    });
                }
        );
    }

    private <T extends HasId> Predicate<T> isItAlreadyAHandledItem(Set<Long> matches) {
        return item -> matches.contains(item.getId());
    }

    private <T extends HasId> void createConflictAction(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig, T origin, T destination, List<DeviceConfigChangeAction> deviceConfigChangeActions) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfig, destinationDeviceConfig);
        action.setOrigin(origin);
        action.setDestination(destination);
        action.setActionType(DeviceConfigChangeActionType.CONFLICT);
        deviceConfigChangeActions.add(action);
    }

    private <T extends HasId> void createRemoveAction(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig, T origin, List<DeviceConfigChangeAction> deviceConfigChangeActions) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfig, destinationDeviceConfig);
        action.setOrigin(origin);
        action.setActionType(DeviceConfigChangeActionType.REMOVE);
        deviceConfigChangeActions.add(action);
    }

    private <T extends HasId> void createAddAction(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig, T destination, List<DeviceConfigChangeAction> deviceConfigChangeActions) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfig, destinationDeviceConfig);
        action.setDestination(destination);
        action.setActionType(DeviceConfigChangeActionType.ADD);
        deviceConfigChangeActions.add(action);
    }

    private <T extends HasId> void createExactMatchAction(DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration, T origin, T destination, List<DeviceConfigChangeAction> deviceConfigChangeActions) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfiguration, destinationDeviceConfiguration);
        action.setOrigin(origin);
        action.setDestination(destination);
        action.setActionType(DeviceConfigChangeActionType.MATCH);
        deviceConfigChangeActions.add(action);
    }
}
