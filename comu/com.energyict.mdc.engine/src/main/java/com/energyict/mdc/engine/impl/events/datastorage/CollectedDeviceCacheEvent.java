/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedDeviceCacheCommand;
import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link CollectedDeviceCacheCommand}
 */
public class CollectedDeviceCacheEvent extends AbstractCollectedDataProcessingEventImpl<UpdatedDeviceCache> {

    public CollectedDeviceCacheEvent(ServiceProvider serviceProvider, UpdatedDeviceCache deviceCache) {
        super(serviceProvider, deviceCache);
    }

    @Override
    public String getDescription() {
        return CollectedDeviceCacheCommand.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        UpdatedDeviceCache cache = getPayload();

        writer.key("collectedDeviceCache");
        writer.object();
        writer.key("deviceIdentifier").value(cache.getDeviceIdentifier().toString());
        writer.endObject();
    }
}
