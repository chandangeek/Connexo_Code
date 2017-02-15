/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommandImpl;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.Warning;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import org.json.JSONException;
import org.json.JSONWriter;

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

    protected void addPayload(JSONWriter writer) throws JSONException {
       writer.key("meterDataStorage");
       writer.object();
       if (!command.getMeterReadings().isEmpty()){
            writer.key("meterReadings");
            writer.array();
            for(Pair<DeviceIdentifier<Device>, MeterReadingImpl> each: command.getMeterReadings().values()){
                DeviceIdentifier identifier = each.getFirst();
                MeterReadingImpl meterReading = each.getLast();
                if (meterReading != null) {
                    writer.object();
                    writer.key("deviceIdentifier").value(identifier.toString());
                    writer.key("endDeviceEvents").value(meterReading.getEvents() != null ? meterReading.getEvents().size() : 0);
                    writer.key("readings").value(meterReading.getReadings() != null ? meterReading.getReadings().size() : 0);
                    writer.key("intervalBlocks").value(meterReading.getIntervalBlocks() != null ? meterReading.getIntervalBlocks().size() : 0);
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
