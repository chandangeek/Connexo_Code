package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
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

        messageTag.add(new MessageValue(radixAttribute.getValue() + DELIMITER +
                registerAddressAttribute.getValue() + DELIMITER +
                registerValueAttribute.getValue()));
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
