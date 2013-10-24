package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;

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
        String messageName = ((Enum) offlineDeviceMessage.getSpecification()).name();
        return messageName;
    }
}
