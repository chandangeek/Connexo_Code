package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import org.json.JSONException;
import org.json.JSONWriter;

public class CollectedLogBookEvent extends AbstractCollectedDataProcessingEventImpl<CollectedLogBook> {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.firmwareVersion.description";

    public CollectedLogBookEvent(ServiceProvider serviceProvider, CollectedLogBook logbook) {
        super(serviceProvider, logbook);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
       writer.key("collectedLogBook");
        if (this.getPayload() != null){
            CollectedLogBook logbook = getPayload();
            writer.object();
            writer.key("logbookIdentifier").value(logbook.getLogBookIdentifier().toString());
            writer.key("numberOfEvents").value(logbook.getCollectedMeterEvents().size());
            writer.endObject();
        }
    }

}
