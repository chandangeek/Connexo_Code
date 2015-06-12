package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;

import java.util.Arrays;
import java.util.List;

/**
 * Creates XML: <tag><attributeName1>value1</attributeName1><attributeName2>value2</attributeName2><attributeName3>value3</attributeName3></tag>
 *
 * @author sva
 * @since 29/10/13 - 16:26
 */
public class MultipleInnerTagsMessageEntry implements MessageEntryCreator {

    private final List<String> attributeTags;
    private final String tag;

    /**
     * @param tag           the main tag for the XML
     * @param attributeTags list of tags for the attributes.
     *                      Note: These should be in the exact same order as the list of property specs of that device message.
     */
    public MultipleInnerTagsMessageEntry(String tag, String... attributeTags) {
        this.attributeTags = Arrays.asList(attributeTags);
        this.tag = tag;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(tag);
        for (int index = 0; index < attributeTags.size(); index++) {
            String attributeTag = attributeTags.get(index);
            String attributeName = offlineDeviceMessage.getSpecification().getPropertySpecs().get(index).getName();
            String value = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, attributeName).getDeviceMessageAttributeValue();

            MessageTag innerTag = new MessageTag(attributeTag);
            innerTag.add(new MessageValue(value));
            messageTag.add(innerTag);
        }
        return new MessageEntry(writeTag(messagingProtocol, messageTag), offlineDeviceMessage.getTrackingId());
    }

    private String writeTag(Messaging messagingProtocol, MessageTag messageTag) {
        if (messagingProtocol == null) {
            return SimpleTagWriter.writeTag(messageTag);
        } else {
            return messagingProtocol.writeTag(messageTag);
        }
    }
}