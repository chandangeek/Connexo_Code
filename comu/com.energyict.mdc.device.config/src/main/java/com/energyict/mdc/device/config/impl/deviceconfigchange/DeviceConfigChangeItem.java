package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceConfiguration;

import java.util.List;
import java.util.function.Predicate;

/**
 * Copyrights EnergyICT
 * Date: 16/07/15
 * Time: 15:04
 */
interface DeviceConfigChangeItem<T extends HasId> {
    DeviceConfiguration getOriginDeviceConfig();

    DeviceConfiguration getDestinationDeviceConfig();

    List<T> getOriginItems();

    List<T> getDestinationItems();

    Predicate<T> exactSameItem(T item);

    Predicate<T> isItAConflict(T item);
}
