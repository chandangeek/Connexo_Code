package com.energyict.mdc.engine.impl.events.datastorage;

import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommandImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.upl.issue.Warning;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import org.json.JSONException;
import org.json.JSONWriter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link MeterDataStoreCommand}
 */
public class MeterDataStorageEvent extends AbstractCollectedDataProcessingEventImpl{

    private MeterDataStoreCommandImpl command;
    private List<Warning> warnings = Collections.emptyList();

    public MeterDataStorageEvent(ServiceProvider serviceProvider, MeterDataStoreCommandImpl command) {
        super(serviceProvider);
        if (command == null){
            throw new IllegalArgumentException("Command cannot be null.");
        }
        this.command = command;
    }

    @Override
    public String getDescription() {
        return MeterDataStoreCommand.DESCRIPTION_TITLE;
    }

    @Override
    public LogLevel getLogLevel (){
        if (super.getLogLevel() == LogLevel.INFO &&
            command.getMeterReadings().isEmpty() && warnings.isEmpty()){
                return LogLevel.DEBUG;       // MeterDataStorage command did not result in meter readings => only logged for debugging purposes
        }
        return super.getLogLevel();
    }

    private <T> int size(Collection<T> nullable) {
        if (nullable == null) {
            return 0;
        } else {
            return nullable.size();
        }
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
       writer.key("meterDataStorage");
       writer.object();
       if (!command.getMeterReadings().isEmpty()){
            writer.key("meterReadings");
            writer.array();
            for(Pair<DeviceIdentifier, MeterReadingImpl> each: command.getMeterReadings().values()){
                DeviceIdentifier identifier = each.getFirst();
                MeterReadingImpl meterReading = each.getLast();
                if (meterReading != null) {
                    writer.object();
                    writer.key("deviceIdentifier").value(identifier.toString());
                    writer.key("endDeviceEvents").value(this.size(meterReading.getEvents()));
                    writer.key("readings").value(this.size(meterReading.getReadings()));
                    writer.key("intervalBlocks").value(this.size(meterReading.getIntervalBlocks()));
                    writer.endObject();
                }
            }
            writer.endArray();
        }
        if (!warnings.isEmpty()){
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
        writer.endObject();
    }
}
