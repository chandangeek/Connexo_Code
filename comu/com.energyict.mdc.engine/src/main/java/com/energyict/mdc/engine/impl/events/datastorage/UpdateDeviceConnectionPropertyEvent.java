/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceConnectionProperty;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link UpdateDeviceConnectionProperty}
 */
public class UpdateDeviceConnectionPropertyEvent extends AbstractCollectedDataProcessingEventImpl  {

    private DeviceIdentifier deviceIdentifier;

    public UpdateDeviceConnectionPropertyEvent(ServiceProvider serviceProvider, DeviceIdentifier deviceIdentifier) {
        super(serviceProvider);
        if (deviceIdentifier == null){
            throw new IllegalArgumentException("Device identifier cannot be null");
        }
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public String getDescription() {
        return UpdateDeviceConnectionProperty.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        writer.key("updateConnectionProperty");
        writer.object();
        writer.key("deviceIdentifier").value(this.deviceIdentifier.toString());
        writer.endObject();
    }
}
