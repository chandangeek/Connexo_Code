/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedBreakerStatusDeviceCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link CollectedBreakerStatusDeviceCommand}
 */
public class CollectedBreakerStatusEvent extends AbstractCollectedDataProcessingEventImpl<CollectedBreakerStatus> {

    public CollectedBreakerStatusEvent(ServiceProvider serviceProvider, CollectedBreakerStatus breakerStatus) {
        super(serviceProvider, breakerStatus);
    }

    @Override
    public String getDescription() {
        return CollectedBreakerStatusDeviceCommand.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedBreakerStatus breakerStatus = getPayload();

        writer.key("collectedBreakerStatus");
        writer.object();
        writer.key("deviceIdentifier").value(breakerStatus.getDeviceIdentifier().toString());
        if (breakerStatus.getBreakerStatus().isPresent()) {
            writer.key("breakerStatus").value(breakerStatus.getBreakerStatus().get().name().toLowerCase());
        }
        writer.endObject();
    }
}