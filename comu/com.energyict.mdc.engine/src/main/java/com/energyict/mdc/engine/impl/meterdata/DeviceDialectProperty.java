package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceDialectProperty;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

/**
 * Provides an implementation for the {@link CollectedDeviceInfo} interface
 * made specific for a dialect protocol property of a {@link Device}.
 *
 * @author khe
 * @since 2017-01-23 (09:27)
 */
public class DeviceDialectProperty extends CollectedDeviceData implements CollectedDeviceInfo {
    private DeviceIdentifier deviceIdentifier;
    private String propertyName;
    private Object propertyValue;

    public DeviceDialectProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        this.deviceIdentifier = deviceIdentifier;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration comTask) {
        return true; // Make sure this is not filtered
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new UpdateDeviceDialectProperty(this, getComTaskExecution(), serviceProvider);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }

    @Override
    public String toString() {
        DescriptionBuilder builder = new DescriptionBuilderImpl(() -> "DeviceDialectProperty");
        builder.addProperty("deviceIdentifier").append(this.deviceIdentifier);
        builder.addProperty("property").append(propertyName);
        builder.addProperty("value").append(propertyValue.toString());
        return builder.toString();
    }

}