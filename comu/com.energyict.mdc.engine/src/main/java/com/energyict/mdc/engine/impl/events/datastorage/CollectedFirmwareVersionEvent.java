/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedFirmwareVersionDeviceCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link CollectedFirmwareVersionDeviceCommand}
 */
public class CollectedFirmwareVersionEvent extends AbstractCollectedDataProcessingEventImpl<CollectedFirmwareVersion> {

    public CollectedFirmwareVersionEvent(ServiceProvider serviceProvider, CollectedFirmwareVersion firmwareVersion) {
        super(serviceProvider, firmwareVersion);
    }

    @Override
    public String getDescription() {
        return CollectedFirmwareVersionDeviceCommand.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedFirmwareVersion firmwareVersion = getPayload();

        writer.key("collectedFirmwareVersion");
        writer.object();
        writer.key("deviceIdentifier").value(firmwareVersion.getDeviceIdentifier().toString());
        if (firmwareVersion.getActiveMeterFirmwareVersion().isPresent()) {
            writer.key("activeMeterFirmwareVersion").value(firmwareVersion.getActiveMeterFirmwareVersion().get());
        }
        if (firmwareVersion.getPassiveMeterFirmwareVersion().isPresent()) {
            writer.key("passiveMeterFirmwareVersion").value(firmwareVersion.getPassiveMeterFirmwareVersion().get());
        }
        if (firmwareVersion.getActiveCommunicationFirmwareVersion().isPresent()) {
            writer.key("activeCommunicationFirmwareVersion").value(firmwareVersion.getActiveCommunicationFirmwareVersion().get());
        }
        if (firmwareVersion.getPassiveCommunicationFirmwareVersion().isPresent()) {
            writer.key("passiveCommunicationrFirmwareVersion").value(firmwareVersion.getPassiveCommunicationFirmwareVersion().get());
        }
        writer.endObject();
    }

}
