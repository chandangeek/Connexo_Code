package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.protocol.api.device.data.*;
import org.json.JSONException;
import org.json.JSONWriter;

public class CollectedRegisterListEvent extends AbstractCollectedDataProcessingEventImpl<CollectedRegisterList> {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.firmwareVersion.description";

    public CollectedRegisterListEvent(ServiceProvider serviceProvider, CollectedRegisterList registerList) {
        super(serviceProvider, registerList);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
       writer.key("collectedRegisterList");
        if (this.getPayload() != null){
            CollectedRegisterList registerList =  getPayload();
            writer.object();
            writer.key("deviceIdentifier").value(registerList.getDeviceIdentifier().toString());
            writer.key("collectedRegisters");
            writer.array();
            for (CollectedRegister each: registerList.getCollectedRegisters()){
                writer.key("readingType").value(each.getReadingType().toString());
                writer.key("readTime").value(each.getReadTime().toString());
            }
            writer.endArray();
            writer.endObject();
        }
    }

}
