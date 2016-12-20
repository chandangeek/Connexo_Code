package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;

import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 *  Creates a MessageEntry based on the
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#CHANGE_MBUS_CLIENT_ATTRIBUTES}
 * xml tag with an additional
 *  * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#MBUS_CLIENT_CHANNEL} attribute.
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#MBUS_CLIENT_IDENTIFICATION_NUMBER} attribute.
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#MBUS_CLIENT_MANUFACTURER_ID} attribute.
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#MBUS_CLIENT_DEVICE_VERSION} attribute.
 * {@link com.energyict.protocolimpl.messages.RtuMessageConstant#MBUS_CLIENT_DEVICE_TYPE} attribute.
 * <p/>
 */
public class ChangeMBusClientAttributesEntry implements MessageEntryCreator {
    private final String mbusClientChannel;
    private final String identificationNumberAttributeName;
    private final String manufacturerIdAttributeName;
    private final String deviceVersionAttributeName;
    private final String deviceTypeAttributeName;
    /**
     * Default constructor
     *
     * @param identificationNumberAttributeName the name of the OfflineDeviceMessageAttribute representing the MBus client identification number
     */
    public ChangeMBusClientAttributesEntry(String identificationNumberAttributeName, String manufacturerIdAttributeName, String deviceVersionAttributeName, String deviceTypeAttributeName, String mbusClientChannel) {
        this.identificationNumberAttributeName = identificationNumberAttributeName;
        this.manufacturerIdAttributeName = manufacturerIdAttributeName;
        this.deviceVersionAttributeName = deviceVersionAttributeName;
        this.deviceTypeAttributeName = deviceTypeAttributeName;
        this.mbusClientChannel = mbusClientChannel;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute attribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, mbusClientChannel);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.MBUS_INSTALL_CHANNEL);
        messageTag.add(new MessageAttribute(RtuMessageConstant.MBUS_INSTALL_CHANNEL, attribute.getValue()));
        messageTag.add(new MessageValue(" "));
        messagingProtocol.writeTag(messageTag);
        attribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, identificationNumberAttributeName);
        messageTag = new MessageTag(RtuMessageConstant.MBUS_CLIENT_IDENTIFICATION_NUMBER);
        messageTag.add(new MessageAttribute(RtuMessageConstant.MBUS_CLIENT_IDENTIFICATION_NUMBER, attribute.getValue()));
        messageTag.add(new MessageValue(" "));
        messagingProtocol.writeTag(messageTag);
        attribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, manufacturerIdAttributeName);
        messageTag = new MessageTag(RtuMessageConstant.MBUS_CLIENT_MANUFACTURER_ID);
        messageTag.add(new MessageAttribute(RtuMessageConstant.MBUS_CLIENT_MANUFACTURER_ID, attribute.getValue()));
        messageTag.add(new MessageValue(" "));
        messagingProtocol.writeTag(messageTag);
        attribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, deviceTypeAttributeName);
        messageTag = new MessageTag(RtuMessageConstant.MBUS_CLIENT_DEVICE_TYPE);
        messageTag.add(new MessageAttribute(RtuMessageConstant.MBUS_CLIENT_DEVICE_TYPE, attribute.getValue()));
        messageTag.add(new MessageValue(" "));
        messagingProtocol.writeTag(messageTag);
        attribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, deviceVersionAttributeName);
        messageTag = new MessageTag(RtuMessageConstant.MBUS_CLIENT_VERSION);
        messageTag.add(new MessageAttribute(RtuMessageConstant.MBUS_CLIENT_VERSION, attribute.getValue()));
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
