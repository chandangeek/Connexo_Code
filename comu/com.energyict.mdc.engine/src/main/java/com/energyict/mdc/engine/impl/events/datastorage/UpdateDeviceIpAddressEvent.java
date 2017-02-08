/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceIpAddress;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link UpdateDeviceIpAddress}
 */
public class UpdateDeviceIpAddressEvent extends AbstractCollectedDataProcessingEventImpl  {

    private DeviceIdentifier deviceIdentifier;

    public UpdateDeviceIpAddressEvent(ServiceProvider serviceProvider, DeviceIdentifier deviceIdentifier) {
        super(serviceProvider);
        if (deviceIdentifier == null){
            throw new IllegalArgumentException("Device identifier cannot be null");
        }
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public String getDescription() {
        return UpdateDeviceIpAddress.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        writer.key("updateDeviceIpAddress");
        writer.object();
        writer.key("deviceIdentifier").value(this.deviceIdentifier.toString());
        writer.endObject();
    }
}
