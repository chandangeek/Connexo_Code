package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import org.json.JSONException;
import org.json.JSONWriter;

public class CollectedLoadProfileEvent extends AbstractCollectedDataProcessingEventImpl<CollectedLoadProfile> {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.loadProfile.description";

    public CollectedLoadProfileEvent(ServiceProvider serviceProvider, CollectedLoadProfile loadProfile) {
        super(serviceProvider, loadProfile);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedLoadProfile loadProfile = getPayload();

        writer.key("collectedLoadProfile");
        writer.object();
        writer.key("loadProfileIdentifier");
        writer.object();
        writer.key("class").value(loadProfile.getLoadProfileIdentifier().getClass().getSimpleName());
        writer.key("deviceIdentifier").value(loadProfile.getLoadProfileIdentifier().getDeviceIdentifier().toString());
        writer.key("identifiers");
        writer.array();
        for (Object each : loadProfile.getLoadProfileIdentifier().getIdentifier()) {
            writer.object();
            writer.key("value").value(each.toString());
            writer.endObject();
        }
        writer.endArray();
        writer.endObject();
        writer.key("collectedIntervalDataRange");
        writer.object();
        writer.key("start").value(loadProfile.getCollectedIntervalDataRange().lowerEndpoint());
        writer.key("end").value(loadProfile.getCollectedIntervalDataRange().upperEndpoint());
        writer.endObject();
        writer.endObject();
    }

}
