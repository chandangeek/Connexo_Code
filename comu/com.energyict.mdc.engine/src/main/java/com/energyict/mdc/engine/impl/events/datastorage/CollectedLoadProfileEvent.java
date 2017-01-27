package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedLoadProfileDeviceCommand;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link CollectedLoadProfileDeviceCommand}
 */
public class CollectedLoadProfileEvent extends AbstractCollectedDataProcessingEventImpl<CollectedLoadProfile> {

    public CollectedLoadProfileEvent(ServiceProvider serviceProvider, CollectedLoadProfile loadProfile) {
        super(serviceProvider, loadProfile);
    }

    @Override
    public String getDescription() {
        return CollectedLoadProfileDeviceCommand.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedLoadProfile loadProfile = getPayload();

        writer.key("collectedLoadProfile");
        writer.object();
        writer.key("loadProfileIdentifier");
        writer.object();
        LoadProfileIdentifier loadProfileIdentifier = loadProfile.getLoadProfileIdentifier();
        writer.key("class").value(loadProfileIdentifier.getClass().getSimpleName());
        Introspector loadProfileIntrospector = loadProfileIdentifier.forIntrospection();
        try {
            DeviceIdentifier deviceIdentifier = (DeviceIdentifier) loadProfileIntrospector.getValue("device");
            writer.key("deviceIdentifier").value(deviceIdentifier.toString());
        } catch (IllegalArgumentException e) {
            // The LoadProfileIdentifier does not support "device" so it apparently does not know about the device directly.
        }
        writer.key("identifiers");
        writer.array();
        for (String role: loadProfileIntrospector.getRoles()) {
            writer.object();
            writer.key("value").value(loadProfileIntrospector.getValue(role).toString());
            writer.endObject();
        }
        writer.endArray();
        writer.endObject();
        writer.key("collectedIntervalDataRange");
        writer.object();
        writer.key("start").value(loadProfile.getCollectedIntervalDataRange().hasLowerBound()
                ? loadProfile.getCollectedIntervalDataRange().lowerEndpoint()
                : "-∞");
        writer.key("end").value(loadProfile.getCollectedIntervalDataRange().hasUpperBound()
                ? loadProfile.getCollectedIntervalDataRange().upperEndpoint()
                : "+∞");
        writer.endObject();
        writer.endObject();
    }

}