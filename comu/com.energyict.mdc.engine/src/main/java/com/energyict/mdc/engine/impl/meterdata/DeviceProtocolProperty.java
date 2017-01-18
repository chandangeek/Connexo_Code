package com.energyict.mdc.engine.impl.meterdata;


import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceProtocolProperty;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

/**
 * Provides an implementation for the {@link CollectedDeviceInfo} interface
 * made specific for a general protocol property of a Device
 *
 * @author sva
 * @since 16/10/2014 - 16:11
 */
public class DeviceProtocolProperty extends CollectedDeviceData implements CollectedDeviceInfo {

    private DeviceIdentifier deviceIdentifier;
    private String propertyName;
    private Object propertyValue;
    private ComTaskExecution comTaskExecution;

    public DeviceProtocolProperty(DeviceIdentifier deviceIdentifier, String propertyName, Object propertyValue) {
        this.deviceIdentifier = deviceIdentifier;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
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
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new UpdateDeviceProtocolProperty(this, comTaskExecution, serviceProvider);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return true; // make sure this is not filtered
    }

    @Override
    public String toString() {
        DescriptionBuilder builder = new DescriptionBuilderImpl(() -> "DeviceProtocolProperty");
        builder.addProperty("deviceIdentifier").append(this.deviceIdentifier);
        builder.addProperty("property").append(propertyName);
        builder.addProperty("value").append(propertyValue.toString());
        return builder.toString();
    }
}