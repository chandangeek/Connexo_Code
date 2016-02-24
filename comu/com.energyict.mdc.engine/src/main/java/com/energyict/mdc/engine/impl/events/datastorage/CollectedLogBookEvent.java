package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.protocol.api.device.BaseLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import org.json.JSONException;
import org.json.JSONWriter;

public class CollectedLogBookEvent extends AbstractCollectedDataProcessingEventImpl<CollectedLogBook> {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.logBook.description";

    public CollectedLogBookEvent(ServiceProvider serviceProvider, CollectedLogBook logbook) {
        super(serviceProvider, logbook);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedLogBook logbook = getPayload();
        LogBookIdentifier logBookIdentifier = logbook.getLogBookIdentifier();
        BaseLogBook baseLogBook = logBookIdentifier.getLogBook();

        writer.key("collectedLogBook");
        writer.object();
        writer.key("logbookIdentifier");
        writer.object();
        writer.key("class").value(logBookIdentifier.getClass().getSimpleName());
        writer.key("deviceIdentifier").value(logbook.getLogBookIdentifier().getDeviceIdentifier().toString());
        writer.key("logbook").value(logBookIdentifier.toString());
        writer.endObject();
        writer.key("numberOfEvents").value(logbook.getCollectedMeterEvents().size());
        writer.endObject();
    }

}
