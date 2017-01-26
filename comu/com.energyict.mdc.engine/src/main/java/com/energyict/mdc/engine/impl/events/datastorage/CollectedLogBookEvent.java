package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedLogBookDeviceCommand;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link CollectedLogBookDeviceCommand}
 */
public class CollectedLogBookEvent extends AbstractCollectedDataProcessingEventImpl<CollectedLogBook> {

    public CollectedLogBookEvent(ServiceProvider serviceProvider, CollectedLogBook logbook) {
        super(serviceProvider, logbook);
    }

    @Override
    public String getDescription() {
        return CollectedLogBookDeviceCommand.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedLogBook logbook = getPayload();
        LogBookIdentifier logBookIdentifier = logbook.getLogBookIdentifier();

        writer.key("collectedLogBook");
        writer.object();
        writer.key("logbookIdentifier");
        writer.object();
        writer.key("class").value(logBookIdentifier.getClass().getSimpleName());
        try {
            DeviceIdentifier deviceIdentifier = (DeviceIdentifier) logBookIdentifier.forIntrospection().getValue("device");
            writer.key("deviceIdentifier").value(deviceIdentifier.toString());
        } catch (IllegalArgumentException e) {
            // The LogBookIdentifier does not support "device" so it apparently does not know about the device directly.
        }
        writer.key("logbook").value(logBookIdentifier.toString());
        writer.endObject();
        writer.key("numberOfEvents").value(logbook.getCollectedMeterEvents().size());
        writer.endObject();
    }

}
