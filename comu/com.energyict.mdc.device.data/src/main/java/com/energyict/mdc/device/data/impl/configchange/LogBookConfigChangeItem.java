/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.DeviceConfigChangeAction;
import com.energyict.mdc.device.config.DeviceConfigChangeActionType;
import com.energyict.mdc.device.config.DeviceConfigChangeEngine;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.data.LogBook;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Applies changes to the LogBooks of the Device
 */
public class LogBookConfigChangeItem extends AbstractConfigChangeItem {

    private static LogBookConfigChangeItem INSTANCE = new LogBookConfigChangeItem();

    private LogBookConfigChangeItem() {
    }

    static DataSourceConfigChangeItem getInstance() {
        return INSTANCE;
    }

    @Override
    public void apply(ServerDeviceForConfigChange device, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
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

    private Predicate<LogBook> onCorrespondingLogBook(DeviceConfigChangeAction<LogBookSpec> deviceConfigChangeAction) {
        return logBook -> logBook.getLogBookSpec().getId() == deviceConfigChangeAction.getOrigin().getId();
    }

    private class DeviceConfigChangeLogBookItem extends AbstractDeviceConfigChangeItem<LogBookSpec> {

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
            return loadProfileSpec -> false; // no conflicts possible (for now)
        }
    }
}
