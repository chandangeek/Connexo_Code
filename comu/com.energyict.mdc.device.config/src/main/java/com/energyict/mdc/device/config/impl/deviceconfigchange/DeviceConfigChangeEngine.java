package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;

import java.util.*;
import java.util.function.Predicate;

/**
 * Copyrights EnergyICT
 * Date: 15/07/15
 * Time: 10:03
 */
public class DeviceConfigChangeEngine {

    private final DeviceType deviceType;
    private final List<DeviceConfigChangeAction> deviceConfigChangeActions = new ArrayList<>();

    public DeviceConfigChangeEngine(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public List<DeviceConfigChangeAction> getDeviceConfigChangeActions() {
        return deviceConfigChangeActions;
    }

    public void calculateConfigChangeActions() {
        deviceType.getConfigurations().stream().filter(DeviceConfiguration::isActive).forEach(
                origin -> deviceType.getConfigurations().stream()
                        .filter(deviceConfiguration -> deviceConfiguration.isActive() && deviceConfiguration.getId() != origin.getId())
                        .forEach(destination -> calculateFor(origin, destination))
        );
    }

    private void calculateFor(DeviceConfiguration origin, DeviceConfiguration destination) {
        calculateItemsFor(new DeviceConfigChangeConnectionTaskItem(origin, destination));
        calculateItemsFor(new DeviceConfigChangeSecuritySetItem(origin, destination));
    }

    private <T extends HasId> void calculateItemsFor(DeviceConfigChangeItem<T> deviceConfigChangeItem) {
        Set<Long> matchedDestinations = new HashSet<>();
        Set<Long> matchedOrigins = new HashSet<>();
        Set<Long> conflictOrigins = new HashSet<>();

        DeviceConfiguration originDeviceConfig = deviceConfigChangeItem.getOriginDeviceConfig();
        DeviceConfiguration destinationDeviceConfig = deviceConfigChangeItem.getDestinationDeviceConfig();
        /*
        Need to do the loops in separate streams in order to get the exact matches FIRST, then the CONFLICTS, then the rest
        */

        // 1 - First find exact match
        deviceConfigChangeItem.getOriginItems().forEach(
                originItem -> {
                    Optional<T> exactMatch = deviceConfigChangeItem.getDestinationItems().stream().filter(deviceConfigChangeItem.exactSameItem(originItem)).findFirst();
                    exactMatch.ifPresent(destinationItem -> {
                        this.createExactMatchAction(originDeviceConfig, destinationDeviceConfig, originItem, destinationItem);
                        matchedDestinations.add(destinationItem.getId());
                        matchedOrigins.add(originItem.getId());
                    });
                }
        );
        // 2 - Find match on name or type to create a conflict
        deviceConfigChangeItem.getOriginItems().stream().filter(isItAlreadyAHandledItem(matchedOrigins).negate())
                .forEach(originItem -> {
                            deviceConfigChangeItem.getDestinationItems().stream()
                                    .filter(isItAlreadyAHandledItem(matchedDestinations).negate())
                                    .filter(deviceConfigChangeItem.isItAConflict(originItem))
                                    .forEach(destinationItem -> {
                                                createConflictAction(originDeviceConfig, destinationDeviceConfig, originItem, destinationItem);
                                                conflictOrigins.add(originItem.getId());
                                            }
                                    );
                        }
                );
        // 3 - Add all others (create Remove for inverse change)
        deviceConfigChangeItem.getOriginItems().stream()
                .filter(isItAlreadyAHandledItem(matchedOrigins).negate())
                .filter(isItAlreadyAHandledItem(conflictOrigins).negate())
                .forEach(originItem -> {
                    createRemoveAction(originDeviceConfig, destinationDeviceConfig, originItem);
                    createAddAction(destinationDeviceConfig, originDeviceConfig, originItem);
                });
    }

    private <T extends HasId> Predicate<T> isItAlreadyAHandledItem(Set<Long> matches) {
        return item -> matches.contains(item.getId());
    }

    private <T extends HasId> void createConflictAction(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig, T origin, T destination) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfig, destinationDeviceConfig);
        action.setOrigin(origin);
        action.setDestination(destination);
        action.setActionType(DeviceConfigChangeActionType.CONFLICT);
        deviceConfigChangeActions.add(action);
    }

    private <T extends HasId> void createRemoveAction(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig, T origin) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfig, destinationDeviceConfig);
        action.setOrigin(origin);
        action.setActionType(DeviceConfigChangeActionType.REMOVE);
        deviceConfigChangeActions.add(action);
    }

    private <T extends HasId> void createAddAction(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig, T destination) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfig, destinationDeviceConfig);
        action.setDestination(destination);
        action.setActionType(DeviceConfigChangeActionType.ADD);
        deviceConfigChangeActions.add(action);
    }

    private <T extends HasId> void createExactMatchAction(DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration, T origin, T destination) {
        DeviceConfigChangeAction<T> action = new DeviceConfigChangeAction<>(originDeviceConfiguration, destinationDeviceConfiguration);
        action.setOrigin(origin);
        action.setDestination(destination);
        action.setActionType(DeviceConfigChangeActionType.MATCH);
        deviceConfigChangeActions.add(action);
    }
}
