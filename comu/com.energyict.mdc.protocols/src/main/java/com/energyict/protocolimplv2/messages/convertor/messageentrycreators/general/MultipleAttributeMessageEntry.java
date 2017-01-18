package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.MessageAttribute;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.protocol.api.messaging.Messaging;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

import java.util.Arrays;
import java.util.List;

/**
 * Creates XML: <tag attributeName1="value1" attributeName2="value2" attributeName3="value3"> </tag>
 * <p/>
 * Copyrights EnergyICT
 * Date: 24/10/13
 * Time: 9:48
 * Author: khe
 */
public class MultipleAttributeMessageEntry implements MessageEntryCreator {

    private final List<String> attributeTags;
    private final String tag;

    /**
     * @param tag           the main tag for the XML
     * @param attributeTags list of tags for the attributes.
     *                      Note: These should be in the exact same order as the list of property specs of that device message.
     */
    public MultipleAttributeMessageEntry(String tag, String... attributeTags) {
        this.attributeTags = Arrays.asList(attributeTags);
        this.tag = tag;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(tag);
        messageTag.add(new MessageValue(" "));
        for (int index = 0; index < attributeTags.size(); index++) {
            String attributeTag = attributeTags.get(index);
            String attributeName = offlineDeviceMessage.getSpecification().getPropertySpecs().get(index).getName();
            String value = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, attributeName).getDeviceMessageAttributeValue();
            messageTag.add(new MessageAttribute(attributeTag, value));
        }
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}