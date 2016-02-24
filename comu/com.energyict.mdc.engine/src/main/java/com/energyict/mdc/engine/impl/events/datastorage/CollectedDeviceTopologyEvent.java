package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import org.json.JSONException;
import org.json.JSONWriter;

public class CollectedDeviceTopologyEvent extends AbstractCollectedDataProcessingEventImpl<CollectedTopology> {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.deviceTopology.description";

    public CollectedDeviceTopologyEvent(ServiceProvider serviceProvider, CollectedTopology topology) {
        super(serviceProvider, topology);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedTopology topology = getPayload();

        writer.key("collectedDeviceTopology");
        writer.object();
        writer.key("deviceIdentifier").value(topology.getDeviceIdentifier().toString());
        writer.key("slaveDeviceIdentifiers");
        writer.array();
        for (DeviceIdentifier each : topology.getSlaveDeviceIdentifiers()) {
            writer.object();
            writer.key("deviceIdentifier").value(each.toString());
            writer.endObject();
        }
        writer.endArray();
        writer.endObject();
    }

}
