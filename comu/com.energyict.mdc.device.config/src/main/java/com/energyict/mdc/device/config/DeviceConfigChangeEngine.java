/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigChangeConnectionTaskItem;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigChangeSecuritySetItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static com.elster.jupiter.util.streams.Predicates.not;

/**
 * This engine will check every DeviceConfiguration and it will determine the actions that need to take place if one wants to change
 * the DeviceConfiguration of a Device from one DeviceConfiguration to another.
 * Each DeviceConfigChangeAction has an ActionType which indicates what needs to happen (add, remove, match or conflict).
 */
public final class DeviceConfigChangeEngine {

    public static final DeviceConfigChangeEngine INSTANCE = new DeviceConfigChangeEngine();

    private DeviceConfigChangeEngine() {
    }

    /**
     * Calculates the DeviceConfigChangeActions which can cause CONFLICTS. Each DeviceConfiguration of the given
     * DeviceType is investigated whether it conflicts with any other DeviceConfiguration of the DeviceType.
     * <p>
     * Note that only the 'attributes' which can cause conflicts are considered.
     *
     * @param deviceType the deviceType for which the actions should be calculated
     * @return the calculated DeviceConfigChangeActions
     */
    public List<DeviceConfigChangeAction> calculateDeviceConfigChangeActionsForConflicts(DeviceType deviceType) {
        if (!deviceType.isDataloggerSlave()) {
            List<DeviceConfigChangeAction> deviceConfigChangeActions = new ArrayList<>();
            deviceType.getConfigurations().stream()
                    .filter(DeviceConfiguration::isActive) // only perform the check on active device configurations
                    .filter(not(DeviceConfiguration::isDataloggerEnabled))  // don't do the calculation for dataLogger enabled devices
                    .forEach(
                            origin -> deviceType.getConfigurations().stream()
                                    .filter(DeviceConfiguration::isActive) // only active configs
                                    .filter(not(DeviceConfiguration::isDataloggerEnabled)) // don't do the calculation for datalogger enabled devices
                                    .filter(destinationConfig -> destinationConfig.getId() != origin.getId())
                                    .forEach(destination -> calculateForPossibleConflictingAttributes(origin, destination, deviceConfigChangeActions))
                    );
            return Collections.unmodifiableList(deviceConfigChangeActions);
        } else {
            return Collections.emptyList();
        }
    }

    private void calculateForPossibleConflictingAttributes(DeviceConfiguration origin, DeviceConfiguration destination, List<DeviceConfigChangeAction> deviceConfigChangeActions) {
        deviceConfigChangeActions.addAll(getConnectionTaskConfigChangeActions(origin, destination));
        deviceConfigChangeActions.addAll(getSecuritySetConfigChangeActions(origin, destination));
    }

    /**
     * Calculates the deviceConfigChangeActions for securityPropertySets
     *
     * @param origin      the origin DeviceConfiguration
     * @param destination the destination DeviceConfiguration
     * @return a list of actions that need to happen when we change the config of a device from the origin config to the destination config
     */
    public List<DeviceConfigChangeAction<SecurityPropertySet>> getSecuritySetConfigChangeActions(DeviceConfiguration origin, DeviceConfiguration destination) {
        return calculateDeviceConfigChangeActionsFor(new DeviceConfigChangeSecuritySetItem(origin, destination));
    }

    /**
     * Calculates the deviceConfigChangeActions for partialConnectionMethods
     *
     * @param origin      the origin DeviceConfiguration
     * @param destination the destination DeviceConfiguration
     * @return a list of actions that need to happen when we change the config of a device from the origin config to the destination config
     */
    public List<DeviceConfigChangeAction<PartialConnectionTask>> getConnectionTaskConfigChangeActions(DeviceConfiguration origin, DeviceConfiguration destination) {
        return calculateDeviceConfigChangeActionsFor(new DeviceConfigChangeConnectionTaskItem(origin, destination));
    }

    /**
     * Calculates the deviceConfigChangeActions for a single given deviceConfigChangeItem.
     *
     * @param deviceConfigChangeItem the DeviceConfigChangeItem to calculate the actions for
     * @param <T>                    the Type of DeviceConfiguration 'attribute' (ex. LoadProfileSpec, LogBookSpec, ...) that we need to calculate the actions for
     * @return a list of actions that need to happen when we change the config of a device from the origin config to the destination config
     */
    public <T extends HasId> List<DeviceConfigChangeAction<T>> calculateDeviceConfigChangeActionsFor(DeviceConfigChangeItem<T> deviceConfigChangeItem) {
        Set<Long> matchedIds = new HashSet<>();
        Set<Long> conflictIds = new HashSet<>();
        final List<DeviceConfigChangeAction<T>> changeActions = new ArrayList<>();
        /*
        Need to do the loops in separate streams in order to get the exact matches FIRST, then the CONFLICTS, then the rest
        */
        // 1 - First find exact match
        findAndCreateExactMatchDeviceConfigChangeActions(deviceConfigChangeItem, matchedIds, changeActions);
        // 2 - Find match on name or type to create a conflict
        findAndCreateConflictDeviceConfigChangeActions(deviceConfigChangeItem, matchedIds, conflictIds, changeActions);
        // 3 - Add all others (create Remove for inverse change)
        findAndCreateAddOrRemoveDeviceConfigChangeActions(deviceConfigChangeItem, matchedIds, conflictIds, changeActions);

        return changeActions;
    }

    private <T extends HasId> void findAndCreateAddOrRemoveDeviceConfigChangeActions(DeviceConfigChangeItem<T> deviceConfigChangeItem, Set<Long> matchedIds, Set<Long> conflictIds, List<DeviceConfigChangeAction<T>> deviceConfigChangeActions) {
        deviceConfigChangeItem.getOriginItems().stream()
                .filter(isItAlreadyAHandledItem(matchedIds).negate())
                .filter(isItAlreadyAHandledItem(conflictIds).negate())
                .forEach(originItem -> createRemoveAction(deviceConfigChangeItem.getOriginDeviceConfig(), deviceConfigChangeItem.getDestinationDeviceConfig(), originItem, deviceConfigChangeActions));
        deviceConfigChangeItem.getDestinationItems().stream()
                .filter(isItAlreadyAHandledItem(matchedIds).negate())
                .filter(isItAlreadyAHandledItem(conflictIds).negate())
                .forEach(destinationItem -> createAddAction(deviceConfigChangeItem.getOriginDeviceConfig(), deviceConfigChangeItem.getDestinationDeviceConfig(), destinationItem, deviceConfigChangeActions));
    }

    private <T extends HasId> void findAndCreateConflictDeviceConfigChangeActions(DeviceConfigChangeItem<T> deviceConfigChangeItem, Set<Long> matchedIds, Set<Long> conflictIds, List<DeviceConfigChangeAction<T>> deviceConfigChangeActions) {
        deviceConfigChangeItem.getOriginItems().stream().filter(isItAlreadyAHandledItem(matchedIds).negate())
                .forEach(originItem -> deviceConfigChangeItem.getDestinationItems().stream()
                        .filter(isItAlreadyAHandledItem(matchedIds).negate())
                        .filter(deviceConfigChangeItem.isItAConflict(originItem))
                        .forEach(destinationItem -> {
                                    createConflictAction(deviceConfigChangeItem.getOriginDeviceConfig(), deviceConfigChangeItem.getDestinationDeviceConfig(), originItem, destinationItem, deviceConfigChangeActions);
                                    conflictIds.add(originItem.getId());
                                    conflictIds.add(destinationItem.getId());
                                }
                        )
                );
    }

    private <T extends HasId> void findAndCreateExactMatchDeviceConfigChangeActions(DeviceConfigChangeItem<T> deviceConfigChangeItem, Set<Long> matchedIds, List<DeviceConfigChangeAction<T>> deviceConfigChangeActions) {
        deviceConfigChangeItem.getOriginItems().forEach(
                originItem -> {
                    Optional<T> exactMatch = deviceConfigChangeItem.getDestinationItems().stream().filter(deviceConfigChangeItem.exactSameItem(originItem)).findFirst();
                    exactMatch.ifPresent(destinationItem -> {
                        createExactMatchAction(deviceConfigChangeItem.getOriginDeviceConfig(), deviceConfigChangeItem.getDestinationDeviceConfig(), originItem, destinationItem, deviceConfigChangeActions);
                        matchedIds.add(destinationItem.getId());
                        matchedIds.add(originItem.getId());
                    });
                }
        );
    }

    private <T extends HasId> Predicate<T> isItAlreadyAHandledItem(Set<Long> matches) {
        return item -> matches.contains(item.getId());
    }

    private <T extends HasId> void createConflictAction(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig, T origin, T destination, List<DeviceConfigChangeAction<T>> deviceConfigChangeActions) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfig, destinationDeviceConfig);
        action.setOrigin(origin);
        action.setDestination(destination);
        action.setActionType(DeviceConfigChangeActionType.CONFLICT);
        deviceConfigChangeActions.add(action);
    }

    private <T extends HasId> void createRemoveAction(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig, T origin, List<DeviceConfigChangeAction<T>> deviceConfigChangeActions) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfig, destinationDeviceConfig);
        action.setOrigin(origin);
        action.setActionType(DeviceConfigChangeActionType.REMOVE);
        deviceConfigChangeActions.add(action);
    }

    private <T extends HasId> void createAddAction(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig, T destination, List<DeviceConfigChangeAction<T>> deviceConfigChangeActions) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfig, destinationDeviceConfig);
        action.setDestination(destination);
        action.setActionType(DeviceConfigChangeActionType.ADD);
        deviceConfigChangeActions.add(action);
    }

    private <T extends HasId> void createExactMatchAction(DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration, T origin, T destination, List<DeviceConfigChangeAction<T>> deviceConfigChangeActions) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfiguration, destinationDeviceConfiguration);
        action.setOrigin(origin);
        action.setDestination(destination);
        action.setActionType(DeviceConfigChangeActionType.MATCH);
        deviceConfigChangeActions.add(action);
    }
}
