/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CreateNoLogBooksForDeviceEvent;
import com.energyict.mdc.engine.impl.meterdata.NoLogBooksForDevice;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link CreateNoLogBooksForDeviceEvent}
 */
public class CollectedNoLogBooksForDeviceEvent extends AbstractCollectedDataProcessingEventImpl<NoLogBooksForDevice> {

    public CollectedNoLogBooksForDeviceEvent(ServiceProvider serviceProvider, NoLogBooksForDevice noLogBooksForDevice) {
        super(serviceProvider, noLogBooksForDevice);
    }

    @Override
    public String getDescription() {
        return CreateNoLogBooksForDeviceEvent.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        NoLogBooksForDevice noLogBooksForDevice = getPayload();

        writer.key("noLogBooksForDevice");
        writer.object();
        writer.key("deviceIdentifier").value(noLogBooksForDevice.getDeviceIdentifier().toString());
        writer.endObject();
    }

}
