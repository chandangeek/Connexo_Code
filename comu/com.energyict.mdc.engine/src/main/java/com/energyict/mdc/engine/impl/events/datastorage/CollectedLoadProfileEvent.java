package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import org.json.JSONException;
import org.json.JSONWriter;

public class CollectedLoadProfileEvent extends AbstractCollectedDataProcessingEventImpl<CollectedLoadProfile> {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.firmwareVersion.description";

    public CollectedLoadProfileEvent(ServiceProvider serviceProvider, CollectedLoadProfile loadProfile) {
        super(serviceProvider, loadProfile);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
       writer.key("collectedLoadProfile");
        if (this.getPayload() != null){
            CollectedLoadProfile loadProfile = getPayload();
            writer.object();
            writer.key("loadProfileIdentifier").value(loadProfile.getLoadProfileIdentifier().toString());
            writer.key("collectedIntervalDataRange");
            writer.object();
            writer.key("start").value(loadProfile.getCollectedIntervalDataRange().lowerEndpoint());
            writer.key("end").value(loadProfile.getCollectedIntervalDataRange().upperEndpoint());
            writer.endObject();
            writer.endObject();
        }
    }

}
