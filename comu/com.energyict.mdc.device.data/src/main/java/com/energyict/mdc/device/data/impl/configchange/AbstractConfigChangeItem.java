/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.ConflictingSolution;
import com.energyict.mdc.device.config.DeviceConfigChangeAction;
import com.energyict.mdc.device.config.DeviceConfigChangeActionType;
import com.energyict.mdc.device.config.DeviceConfigChangeItem;
import com.energyict.mdc.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.device.config.DeviceConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides common functionality for ConfigChangeItems
 */
public abstract class AbstractConfigChangeItem implements DataSourceConfigChangeItem{

    <T extends HasId> List<T> getAddItems(List<DeviceConfigChangeAction<T>> actions) {
        return actions.stream().filter(actionTypeIs(DeviceConfigChangeActionType.ADD)).map(DeviceConfigChangeAction::getDestination).collect(Collectors.toList());
    }

    <T extends HasId> List<T> getRemoveItems(List<DeviceConfigChangeAction<T>> actions) {
        return actions.stream().filter(actionTypeIs(DeviceConfigChangeActionType.REMOVE)).map(DeviceConfigChangeAction::getOrigin).collect(Collectors.toList());
    }

    Predicate<DeviceConfigChangeAction<?>> actionTypeIs(DeviceConfigChangeActionType deviceConfigChangeActionType) {
        return deviceConfigChangeAction -> deviceConfigChangeAction.getActionType().equals(deviceConfigChangeActionType);
    }

    <T extends HasId> List<DeviceConfigChangeAction<T>> getMatchItems(List<DeviceConfigChangeAction<T>> deviceConfigChangeActions) {
        return deviceConfigChangeActions.stream().filter(actionTypeIs(DeviceConfigChangeActionType.MATCH)).collect(Collectors.toList());
    }

    Optional<DeviceConfigConflictMapping> getDeviceConfigConflictMapping(ServerDeviceForConfigChange device, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        return device.getDeviceType().getDeviceConfigConflictMappings().stream()
                .filter(getDeviceConfigConflictMappingPredicate(originDeviceConfiguration, destinationDeviceConfiguration)).findFirst();
    }

    private Predicate<DeviceConfigConflictMapping> getDeviceConfigConflictMappingPredicate(DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        return deviceConfigConflictMapping -> deviceConfigConflictMapping.getOriginDeviceConfiguration().getId() == originDeviceConfiguration.getId() && deviceConfigConflictMapping.getDestinationDeviceConfiguration().getId() == destinationDeviceConfiguration.getId();
    }

    Predicate<ConflictingSolution<?>> solutionsForRemove() {
        return conflictingSolution -> conflictingSolution.getConflictingMappingAction().equals(DeviceConfigConflictMapping.ConflictingMappingAction.REMOVE);
    }

    Predicate<ConflictingSolution<?>> solutionsForMap() {
        return conflictingSolution -> conflictingSolution.getConflictingMappingAction().equals(DeviceConfigConflictMapping.ConflictingMappingAction.MAP);
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
}
