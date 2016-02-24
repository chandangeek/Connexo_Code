package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.impl.meterdata.UpdatedDeviceCache;
import org.json.JSONException;
import org.json.JSONWriter;

public class CollectedDeviceCacheEvent extends AbstractCollectedDataProcessingEventImpl<UpdatedDeviceCache> {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.deviceCache.description";

    public CollectedDeviceCacheEvent(ServiceProvider serviceProvider, UpdatedDeviceCache deviceCache) {
        super(serviceProvider, deviceCache);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        UpdatedDeviceCache cache = getPayload();

        writer.key("collectedDeviceCache");
        writer.object();
        writer.key("deviceIdentifier").value(cache.getDeviceIdentifier().toString());
        writer.endObject();
    }
}
