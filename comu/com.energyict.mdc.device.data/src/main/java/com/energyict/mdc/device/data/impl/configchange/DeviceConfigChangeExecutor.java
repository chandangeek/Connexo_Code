/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.EventType;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Applies the actual change in DeviceConfiguration for a single Device
 */
public final class DeviceConfigChangeExecutor {

    private final EventService eventService;
    private final DeviceService deviceService;
    private final Clock clock;

    public DeviceConfigChangeExecutor(DeviceService deviceService, Clock clock, EventService eventService) {
        this.deviceService = deviceService;
        this.eventService = eventService;
        this.clock = clock;
    }

    public Device execute(ServerDeviceForConfigChange device, DeviceConfiguration destinationDeviceConfiguration) {
        Instant configChangeTimeStamp = clock.instant();
        final DeviceConfiguration originDeviceConfiguration = device.getDeviceConfiguration();
        prepareForChangeDeviceConfig(device, destinationDeviceConfiguration, configChangeTimeStamp);
        device.setNewDeviceConfiguration(destinationDeviceConfiguration);
        Stream.of(
                LoadProfileConfigChangeItems.getInstance(),
                LogBookConfigChangeItem.getInstance(),
                ConnectionTaskConfigChangeItem.getInstance(),
                ComTaskExecutionConfigChangeItem.getInstance(),
                ProtocolDialectPropertyChangeItem.getInstance())
                .forEach(performDataSourceChanges(device, destinationDeviceConfiguration, originDeviceConfiguration));
        device.save();
        this.eventService.postEvent(EventType.DEVICE_CONFIGURATION_CHANGED.topic(), device);
        return device;
    }

    private Consumer<DataSourceConfigChangeItem> performDataSourceChanges(ServerDeviceForConfigChange device, DeviceConfiguration destinationDeviceConfiguration, DeviceConfiguration originDeviceConfiguration) {
        return abstractConfigChangeItem -> abstractConfigChangeItem.apply(device, originDeviceConfiguration, destinationDeviceConfiguration);
    }

    /**
     * Prepare the device for his new deviceConfiguration.
     * <ul>
     * <li>lock the device so no other process can update the device</li>
     * <li>validate if we <i>can</i> do a change DeviceConfig with the given destination deviceConfig</li>
     * </ul>
     *
     * @param device                         the device to change it's configuration
     * @param destinationDeviceConfiguration the configuration to change to
     * @param configChangeTimeStamp          the timeStamp of the config change
     */
    private void prepareForChangeDeviceConfig(ServerDeviceForConfigChange device, DeviceConfiguration destinationDeviceConfiguration, Instant configChangeTimeStamp) {
        this.deviceService.findAndLockDeviceByIdAndVersion(device.getId(), device.getVersion());
        device.validateDeviceCanChangeConfig(destinationDeviceConfiguration);
        this.eventService.postEvent(EventType.DEVICE_CONFIG_CHANGE_VALIDATE.topic(), Pair.of(device, destinationDeviceConfiguration));
    }

}
