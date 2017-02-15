/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceMessage;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link UpdateDeviceMessage}
 */
public class UpdateDeviceMessageEvent extends AbstractCollectedDataProcessingEventImpl  {

    private MessageIdentifier messageIdentifier;
    private DeviceMessageStatus deviceMessageStatus;
    private String protocolInfo;

    public UpdateDeviceMessageEvent(ServiceProvider serviceProvider,
                                    MessageIdentifier messageIdentifier,
                                    DeviceMessageStatus deviceMessageStatus,
                                    String protocolInfo) {
        super(serviceProvider);
        this.messageIdentifier = messageIdentifier;
        this.deviceMessageStatus = deviceMessageStatus;
        this.protocolInfo = protocolInfo;
    }

    @Override
    public String getDescription() {
        return UpdateDeviceMessage.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        writer.key("updateDeviceMessage");
        writer.object();
        if (messageIdentifier != null) {
            DeviceMessage message = messageIdentifier.getDeviceMessage();
            if (message !=  null) {
                writer.key("deviceMessageId").value(message.getId());
            }
        }
        if (deviceMessageStatus != null) {
            writer.key("deviceMessageStatus").value(deviceMessageStatus);
        }
        if (protocolInfo != null) {
            writer.key("protocolInfo").value(protocolInfo);
        }
        writer.endObject();
    }
}
