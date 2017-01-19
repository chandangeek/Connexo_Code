package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedRegisterListDeviceCommand;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegisterList;
import org.json.JSONException;
import org.json.JSONWriter;

import java.util.Date;

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
            Date readTime = each.getReadTime();
            if (readTime != null){
                writer.key("readTime").value(readTime.toInstant());
            }
            Date fromTime = each.getFromTime();
            if (fromTime != null){
                writer.key("fromTime").value(fromTime.toInstant());
            }
            Date toTime = each.getToTime();
            if (toTime != null){
                writer.key("toTime").value(toTime.toInstant());
            }
            Date eventTime = each.getEventTime();
            if (eventTime != null){
                writer.key("eventTime").value(eventTime.toInstant());
            }
            writer.endObject();
        }
        writer.endArray();
        writer.endObject();
    }

}
