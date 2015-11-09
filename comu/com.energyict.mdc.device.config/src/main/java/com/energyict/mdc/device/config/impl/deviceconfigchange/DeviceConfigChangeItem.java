package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.device.config.DeviceConfiguration;

import com.elster.jupiter.util.HasId;

import java.util.List;
import java.util.function.Predicate;

/**
 * Serves as a helper item to calculate the difference in T on a particular DeviceConfiguration
 */
interface DeviceConfigChangeItem<T extends HasId> {
    DeviceConfiguration getOriginDeviceConfig();

    DeviceConfiguration getDestinationDeviceConfig();

    List<T> getOriginItems();

    List<T> getDestinationItems();

    Predicate<T> exactSameItem(T item);

    Predicate<T> isItAConflict(T item);
}
