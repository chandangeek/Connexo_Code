package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceIpAddress;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.device.data.CollectedAddressProperties;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

/**
 * Provides an implementation for the {@link CollectedAddressProperties} interface
 * for address properties of an ip based {@link ConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:29)
 */
public class DeviceIpAddress extends CollectedDeviceData implements CollectedAddressProperties {

    private DeviceIdentifier deviceIdentifier;
    private String ipAddress;
    private ConnectionTask connectionTask;
    private String connectionTaskPropertyName;

    public DeviceIpAddress (DeviceIdentifier deviceIdentifier, String ipAddress, String connectionTaskPropertyName) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.ipAddress = ipAddress;
        this.connectionTaskPropertyName = connectionTaskPropertyName;
    }

    @Override
    public boolean isConfiguredIn (DataCollectionConfiguration configuration) {
        return true;    // Make sure this is not filtered
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier () {
        return this.deviceIdentifier;
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new UpdateDeviceIpAddress(this, this.getComTaskExecution(), serviceProvider);
    }

    public String getIpAddress () {
        return ipAddress;
    }

    public ConnectionTask getConnectionTask () {
        return connectionTask;
    }

    public String getConnectionTaskPropertyName () {
        return connectionTaskPropertyName;
    }

    @Override
    public void postProcess (ConnectionTask connectionTask) {
        this.connectionTask = connectionTask;
    }

}