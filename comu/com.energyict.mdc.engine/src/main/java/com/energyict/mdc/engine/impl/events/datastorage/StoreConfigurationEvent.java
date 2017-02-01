/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.StoreConfigurationUserFile;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link StoreConfigurationUserFile}
 */
public class StoreConfigurationEvent extends AbstractCollectedDataProcessingEventImpl  {

    private DeviceIdentifier deviceIdentifier;

    public StoreConfigurationEvent(AbstractComServerEventImpl.ServiceProvider serviceProvider, DeviceIdentifier deviceIdentifier) {
        super(serviceProvider);
        if (deviceIdentifier == null){
            throw new IllegalArgumentException("DeviceIdentifier cannot be null");
        }
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public String getDescription() {
        return StoreConfigurationUserFile.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        writer.key("storeConfiguration");
        writer.object();
        writer.key("deviceIdentifier").value(this.deviceIdentifier.toString());
        writer.endObject();
    }
}
