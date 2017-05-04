package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * Creates a MessageEntry based on the "WriteSingleRegisters" or "WriteMultipleRegisters" xml tag,
 * as used by most Modbus protocols
 *
 * @author sva
 * @since 24/10/13 - 10:20
 */
public class WriteModbusRegisterMessage implements MessageEntryCreator {

    private static final String DELIMITER = ",";

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(getMessageName(offlineDeviceMessage));

        OfflineDeviceMessageAttribute radixAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.RadixFormatAttributeName);
        OfflineDeviceMessageAttribute registerAddressAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.RegisterAddressAttributeName);
        OfflineDeviceMessageAttribute registerValueAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.RegisterValueAttributeName);

        messageTag.add(new MessageValue(radixAttribute.getDeviceMessageAttributeValue() + DELIMITER +
                registerAddressAttribute.getDeviceMessageAttributeValue() + DELIMITER +
                registerValueAttribute.getDeviceMessageAttributeValue()));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }

    /**
     * Creates the message parent tag based on the name of the given deviceMessage spec enum.
     */
    protected String getMessageName(OfflineDeviceMessage offlineDeviceMessage) {
        return ((Enum) offlineDeviceMessage.getSpecification()).name();
    }
}
