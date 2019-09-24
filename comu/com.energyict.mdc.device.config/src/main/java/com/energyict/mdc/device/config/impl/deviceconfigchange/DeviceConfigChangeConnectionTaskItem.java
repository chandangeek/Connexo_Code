/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl.deviceconfigchange;

import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.config.DeviceConfigChangeItem;

import java.util.List;
import java.util.function.Predicate;

/**
 * Serves as a helper item to calculate the difference in PartialConnectionTasks for the foreseen DeviceConfigurations
 */
public class DeviceConfigChangeConnectionTaskItem implements DeviceConfigChangeItem<PartialConnectionTask> {

    private final DeviceConfiguration originDeviceConfig;
    private final DeviceConfiguration destinationDeviceConfig;

    public DeviceConfigChangeConnectionTaskItem(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig) {
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

    @Override
    public List<PartialConnectionTask> getOriginItems(){
        return originDeviceConfig.getPartialConnectionTasks();
    }

    @Override
    public List<PartialConnectionTask> getDestinationItems(){
        return destinationDeviceConfig.getPartialConnectionTasks();
    }

    @Override
    public Predicate<PartialConnectionTask> exactSameItem(PartialConnectionTask originConnectionTask) {
        return partialConnectionTask -> partialConnectionTask.getName().equals(originConnectionTask.getName())
                && partialConnectionTask.getPluggableClass().getId() == originConnectionTask.getPluggableClass().getId();
    }

    @Override
    public Predicate<PartialConnectionTask> isItAConflict(PartialConnectionTask originConnectionTask) {
        return partialConnectionTask -> !partialConnectionTask.getName().equals(originConnectionTask.getName())
                && partialConnectionTask.getPluggableClass().getId() == originConnectionTask.getPluggableClass().getId();
    }
}
