package com.energyict.mdc.engine.impl.events.datastorage;

import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommandImpl;
import com.energyict.mdc.issues.Warning;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import org.json.JSONException;
import org.json.JSONWriter;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 18/02/2016
 * Time: 11:47
 */
public class MeterDataStorageEvent extends AbstractCollectedDataProcessingEventImpl{

    private final static String DESCRIPTION = "collectedDataProcessingEvent.meterDataStorage.description";

    private MeterDataStoreCommandImpl command;
    private List<Warning> warnings = Collections.emptyList();

    public MeterDataStorageEvent(ServiceProvider serviceProvider, MeterDataStoreCommandImpl command) {
        super(serviceProvider);
        this.command = command;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
       writer.key("meterDataStorage");
       if (!command.getMeterReadings().isEmpty()){
            writer.key("meterReadings");
            writer.array();
            for(Pair<DeviceIdentifier<Device>, MeterReadingImpl> each: command.getMeterReadings().values()){
                writer.object();
                writer.key("deviceIdentifier").value(each.getFirst().toString());
                writer.key("endDeviceEvents").value(each.getLast().getEvents().size());
                writer.key("readings").value(each.getLast().getReadings().size());
                writer.key("intervalBlocks").value(each.getLast().getIntervalBlocks().size());
                writer.endObject();
            }
            writer.endArray();
        }
        if (!this.warnings.isEmpty()){
             writer.key("warnings");
             writer.array();
             for(Warning each: warnings){
                 writer.object();
                 writer.key("issue");
                 writer.object();
                 writer.key("description").value(each.getDescription());
                 writer.key("isWarning").value(Boolean.TRUE);
                 writer.key("isProblem").value(Boolean.FALSE);
                 writer.endObject();
             }
             writer.endArray();
         }
    }
}
