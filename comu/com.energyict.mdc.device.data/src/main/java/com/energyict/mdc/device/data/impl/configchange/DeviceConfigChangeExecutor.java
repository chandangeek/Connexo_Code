package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;

import java.util.function.Consumer;
import java.util.stream.Stream;

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
        Stream.of(
                LoadProfileConfigChangeItems.getInstance(),
                LogBookConfigChangeItem.getInstance(),
                ConnectionTaskConfigChangeItem.getInstance(),
                SecurityPropertiesConfigChangeItem.getInstance(),
                ComTaskExecutionConfigChangeItem.getInstance(),
                ProtocolDialectPropertyChangeItem.getInstance())
                .forEach(performDataSourceChanges(device, destinationDeviceConfiguration, originDeviceConfiguration));
        device.save();
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
     * <li>create a new MeterActivation so all <i>new</i> data is stored on the new meterActivation</li>
     * </ul>
     *
     * @param device                         the device to change it's configuration
     * @param destinationDeviceConfiguration the configuration to change to
     */
    private void prepareForChangeDeviceConfig(ServerDeviceForConfigChange device, DeviceConfiguration destinationDeviceConfiguration) {
        device.lock();
        device.validateDeviceCanChangeConfig(destinationDeviceConfiguration);
        device.createNewMeterActivation();
    }

}
