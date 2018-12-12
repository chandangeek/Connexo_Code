/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceConnectionProperty;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceConnectionPropertyForAllOutboundConnections;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

/**
 * Provides an implementation for the {@link CollectedDeviceInfo} interface
 * for address properties of an ip based {@link ConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:29)
 */
public class DeviceConnectionProperty extends CollectedDeviceData implements CollectedDeviceInfo {

    private DeviceIdentifier deviceIdentifier;
    private Object propertyValue;
    private ConnectionTask connectionTask;
    private String connectionTaskPropertyName;

    public DeviceConnectionProperty(DeviceIdentifier deviceIdentifier, Object propertyValue, String connectionTaskPropertyName) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.propertyValue = propertyValue;
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
        if (getConnectionTask() != null && getConnectionTask().getConnectionType().getDirection().equals(ConnectionType.ConnectionTypeDirection.INBOUND)) {
            return new UpdateDeviceConnectionPropertyForAllOutboundConnections(this, getComTaskExecution(), serviceProvider);
        } else {
            return new UpdateDeviceConnectionProperty(this, this.getComTaskExecution(), serviceProvider);
        }
    }

    public Object getPropertyValue() {
        return propertyValue;
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