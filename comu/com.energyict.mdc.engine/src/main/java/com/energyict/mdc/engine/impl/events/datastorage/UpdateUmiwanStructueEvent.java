/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceConnectionProperty;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link UpdateDeviceConnectionProperty}
 */
public class UpdateUmiwanStructueEvent extends AbstractCollectedDataProcessingEventImpl {

    private MessageIdentifier messageIdentifier;

    public UpdateUmiwanStructueEvent(ServiceProvider serviceProvider, MessageIdentifier messageIdentifier) {
        super(serviceProvider);
        if (messageIdentifier == null) {
            throw new IllegalArgumentException("Device identifier cannot be null");
        }
        this.messageIdentifier = messageIdentifier;
    }

    @Override
    public String getDescription() {
        return UpdateDeviceConnectionProperty.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        writer.key("updateUmiwanStructure");
        writer.object();
        writer.key("deviceIdentifier").value(this.messageIdentifier.toString());
        writer.endObject();
    }
}
