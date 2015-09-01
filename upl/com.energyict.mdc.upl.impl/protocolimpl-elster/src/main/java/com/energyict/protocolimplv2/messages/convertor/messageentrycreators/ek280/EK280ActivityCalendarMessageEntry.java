package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ek280;

import com.elster.protocolimpl.dlms.messaging.XmlMessageWriter;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

/**
 * @author sva
 * @since 1/09/2015 - 14:27
 */
public class EK280ActivityCalendarMessageEntry implements MessageEntryCreator {

    private final String messageTag = "UploadPassiveTariff";
    private final String codeTableAttributeTag = "CodeTableId";
    private final String activationTimeAttributeTag = "ActivationTime";
    private final String defaultTariffCodeAttributeTag = "DefaultTariff";

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String codeTable = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.activityCalendarCodeTableAttributeName).getDeviceMessageAttributeValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.activityCalendarActivationDateAttributeName).getDeviceMessageAttributeValue();
        String defaultTariffCode = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.defaultTariffCodeAttrributeName).getDeviceMessageAttributeValue();

        MessageTag msgTag = new MessageTag(messageTag);
        msgTag.add(new MessageAttribute(codeTableAttributeTag, codeTable));
        msgTag.add(new MessageAttribute(activationTimeAttributeTag, activationDate));
        msgTag.add(new MessageAttribute(defaultTariffCodeAttributeTag, defaultTariffCode));
        msgTag.add(new MessageValue(" "));
        return new MessageEntry(  getXmlMessageWriter().writeNormalTag(msgTag), offlineDeviceMessage.getTrackingId());
    }

    private XmlMessageWriter getXmlMessageWriter() {
        return new XmlMessageWriter();
    }
}