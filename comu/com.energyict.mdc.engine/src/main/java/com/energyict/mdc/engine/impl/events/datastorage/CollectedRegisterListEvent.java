/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedRegisterListDeviceCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;

import org.json.JSONException;
import org.json.JSONWriter;

import java.time.Instant;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link CollectedRegisterListDeviceCommand}
 */
public class CollectedRegisterListEvent extends AbstractCollectedDataProcessingEventImpl<CollectedRegisterList> {

    public CollectedRegisterListEvent(ServiceProvider serviceProvider, CollectedRegisterList registerList) {
        super(serviceProvider, registerList);
    }

    @Override
    public String getDescription() {
        return CollectedRegisterListDeviceCommand.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedRegisterList registerList = getPayload();
       writer.key("collectedRegisterList");

        writer.object();
        writer.key("deviceIdentifier").value(registerList.getDeviceIdentifier().toString());
        writer.key("collectedRegisters");
        writer.array();
        for (CollectedRegister each: registerList.getCollectedRegisters()){
            writer.object();
            Quantity quantity =  each.getCollectedQuantity();
            if (quantity != null) {
                writer.key("quantity");
                writer.object();
                writer.key("amount").value(quantity.getAmount());
                writer.key("unit").value(quantity.getUnit());
                writer.endObject();
            }
            String text = each.getText();
            if (text != null){
                writer.key("text").value(text);
            }
            Instant readTime = each.getReadTime();
            if (readTime != null){
                writer.key("readTime").value(readTime);
            }
            Instant fromTime = each.getFromTime();
            if (readTime != null){
                writer.key("fromTime").value(fromTime);
            }
            Instant toTime = each.getToTime();
            if (readTime != null){
                writer.key("toTime").value(toTime);
            }
            Instant eventTime = each.getEventTime();
            if (eventTime != null){
                writer.key("eventTime").value(eventTime);
            }
            writer.endObject();
        }
        writer.endArray();
        writer.endObject();
    }

}
