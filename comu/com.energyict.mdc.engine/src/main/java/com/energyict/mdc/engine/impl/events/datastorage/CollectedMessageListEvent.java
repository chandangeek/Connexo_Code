package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.protocol.api.device.data.*;
import org.json.JSONException;
import org.json.JSONWriter;

public class CollectedMessageListEvent extends AbstractCollectedDataProcessingEventImpl<CollectedMessageList> {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.firmwareVersion.description";

    public CollectedMessageListEvent(ServiceProvider serviceProvider, CollectedMessageList messageList) {
        super(serviceProvider, messageList);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
       writer.key("collectedMessageList");
        if (this.getPayload() != null){
            CollectedMessageList messageList =  getPayload();
            writer.object();
            writer.key("collectedMessages");
            writer.array();
            for (CollectedMessage each: messageList.getCollectedMessages()){
                writer.key("messageIdentifier").value(each.getMessageIdentifier().toString());
                writer.key("protocolInformation").value(each.getDeviceProtocolInformation());
            }
            writer.endArray();
            writer.endObject();
        }
    }

}
