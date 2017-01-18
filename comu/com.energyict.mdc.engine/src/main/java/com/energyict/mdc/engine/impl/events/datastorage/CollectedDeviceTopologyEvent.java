package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedDeviceTopologyDeviceCommand;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link CollectedDeviceTopologyDeviceCommand}
 */
public class CollectedDeviceTopologyEvent extends AbstractCollectedDataProcessingEventImpl<CollectedTopology> {

    public CollectedDeviceTopologyEvent(ServiceProvider serviceProvider, CollectedTopology topology) {
        super(serviceProvider, topology);
    }

    @Override
    public String getDescription() {
        return CollectedDeviceTopologyDeviceCommand.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedTopology topology = getPayload();

        writer.key("collectedDeviceTopology");
        writer.object();
        writer.key("deviceIdentifier").value(topology.getDeviceIdentifier().toString());
        writer.key("slaveDeviceIdentifiers");
        writer.array();
        for (DeviceIdentifier each : topology.getSlaveDeviceIdentifiers().keySet()) {
            writer.object();
            writer.key("deviceIdentifier").value(each.toString());
            writer.endObject();
        }
        writer.endArray();
        writer.endObject();
    }

}
