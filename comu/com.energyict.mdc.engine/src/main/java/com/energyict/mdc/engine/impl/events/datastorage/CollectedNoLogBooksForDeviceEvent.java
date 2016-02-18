package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.impl.meterdata.NoLogBooksForDevice;
import org.json.JSONException;
import org.json.JSONWriter;

public class CollectedNoLogBooksForDeviceEvent extends AbstractCollectedDataProcessingEventImpl<NoLogBooksForDevice> {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.firmwareVersion.description";

    public CollectedNoLogBooksForDeviceEvent(ServiceProvider serviceProvider, NoLogBooksForDevice noLogBooksForDevice) {
        super(serviceProvider, noLogBooksForDevice);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
       writer.key("noLogBooksForDevice");
        if (this.getPayload() != null){
            NoLogBooksForDevice noLogBooksForDevice =  getPayload();
            writer.object();
            writer.key("deviceIdentifier").value(noLogBooksForDevice.getDeviceIdentifier().toString());
            writer.endObject();
        }
    }

}
