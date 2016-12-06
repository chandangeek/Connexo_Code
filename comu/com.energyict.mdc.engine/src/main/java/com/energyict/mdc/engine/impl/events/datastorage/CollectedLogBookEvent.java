package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedLogBookDeviceCommand;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
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
        writer.key("deviceIdentifier").value(logBookIdentifier.getDeviceIdentifier().toString());
        writer.key("logbook").value(logBookIdentifier.toString());
        writer.endObject();
        writer.key("numberOfEvents").value(logbook.getCollectedMeterEvents().size());
        writer.endObject();
    }

}
