package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import org.json.JSONException;
import org.json.JSONWriter;

public class CollectedFirmwareVersionEvent extends AbstractCollectedDataProcessingEventImpl<CollectedFirmwareVersion> {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.firmwareVersion.description";

    public CollectedFirmwareVersionEvent(ServiceProvider serviceProvider, CollectedFirmwareVersion firmwareVersion) {
        super(serviceProvider, firmwareVersion);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
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
