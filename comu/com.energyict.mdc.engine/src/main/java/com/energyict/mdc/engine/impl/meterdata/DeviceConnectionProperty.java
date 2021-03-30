/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceConnectionProperty;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceConnectionPropertyForAllOutboundConnections;
import com.energyict.mdc.upl.meterdata.CollectedDeviceInfo;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.util.Collections;
import java.util.Map;

/**
 * Provides an implementation for the {@link CollectedDeviceInfo} interface
 * for address properties of an ip based {@link ConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:29)
 */
public class DeviceConnectionProperty extends CollectedDeviceData implements CollectedDeviceInfo {

    private DeviceIdentifier deviceIdentifier;
    private ConnectionTask connectionTask;
    private Map<String, Object> connectionPropertyNameAndValue;

    public DeviceConnectionProperty(DeviceIdentifier deviceIdentifier, Object propertyValue, String connectionTaskPropertyName) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.connectionPropertyNameAndValue = Collections.singletonMap(connectionTaskPropertyName, propertyValue);
    }

    public DeviceConnectionProperty(DeviceIdentifier deviceIdentifier, Map<String, Object> connectionPropertyNameAndValue) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.connectionPropertyNameAndValue = connectionPropertyNameAndValue;
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

    public ConnectionTask getConnectionTask () {
        return connectionTask;
    }

    public Map<String, Object> getConnectionPropertyNameAndValue() {
        return connectionPropertyNameAndValue;
    }

    @Override
    public void postProcess (ConnectionTask connectionTask) {
        this.connectionTask = connectionTask;
    }

}