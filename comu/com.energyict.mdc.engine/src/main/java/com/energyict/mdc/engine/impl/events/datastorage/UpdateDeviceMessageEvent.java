package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceMessage;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Copyrights EnergyICT
 * Date: 18/02/2016
 * Time: 13:54
 */
public class UpdateDeviceMessageEvent extends AbstractCollectedDataProcessingEventImpl  {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.updateDeviceMessage.description";

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
        return DESCRIPTION;
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
