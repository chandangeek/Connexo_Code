/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedMessageListDeviceCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link CollectedMessageListDeviceCommand}
 */
public class CollectedMessageListEvent extends AbstractCollectedDataProcessingEventImpl<CollectedMessageList> {

    public CollectedMessageListEvent(ServiceProvider serviceProvider, CollectedMessageList messageList) {
        super(serviceProvider, messageList);
    }

    @Override
    public String getDescription() {
        return CollectedMessageListDeviceCommand.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedMessageList messageList = getPayload();

        writer.key("collectedMessageList");
        writer.object();
        writer.key("collectedMessages");
        writer.array();
        for (CollectedMessage each : messageList.getCollectedMessages()) {
            writer.object();
            writer.key("messageIdentifier").value(each.getMessageIdentifier().toString());
            writer.key("protocolInformation").value(each.getDeviceProtocolInformation());
            writer.endObject();
        }
        writer.endArray();
        writer.endObject();
    }

}
