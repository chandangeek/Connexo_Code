package com.energyict.mdc.engine.impl.meterdata;


import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceProtocolProperty;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceInfo;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

/**
 * Provides an implementation for the {@link CollectedDeviceInfo} interface
 * made specific for a general protocol property of a Device
 *
 * @author sva
 * @since 16/10/2014 - 16:11
 */
public class DeviceProtocolProperty extends CollectedDeviceData implements CollectedDeviceInfo {

    private DeviceIdentifier deviceIdentifier;
    private PropertySpec propertySpec;
    private Object propertyValue;
    private ComTaskExecution comTaskExecution;

    public DeviceProtocolProperty(DeviceIdentifier deviceIdentifier, PropertySpec propertySpec, Object propertyValue) {
        this.deviceIdentifier = deviceIdentifier;
        this.propertySpec = propertySpec;
        this.propertyValue = propertyValue;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public void setDataCollectionConfiguration(DataCollectionConfiguration configuration) {
        this.comTaskExecution = (ComTaskExecution) configuration;
    }

    public PropertySpec getPropertySpec() {
        return propertySpec;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }

    @Override
    public DeviceCommand toDeviceCommand(IssueService issueService, MeterDataStoreCommand meterDataStoreCommand) {
        return new UpdateDeviceProtocolProperty(this, issueService, comTaskExecution);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return true; // make sure this is not filtered
    }
}
