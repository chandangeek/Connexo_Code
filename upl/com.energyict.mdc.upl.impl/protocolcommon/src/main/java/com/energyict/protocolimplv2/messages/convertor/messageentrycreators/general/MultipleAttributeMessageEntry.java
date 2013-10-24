package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general;

import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.*;
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
     * @param attributeTags list of tags for the attributes. These should be in the exact same order as the list of property specs of that device message.
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
            String attributeTag = attributeTags.get(0);
            String attributeName = offlineDeviceMessage.getSpecification().getPropertySpecs().get(index).getName();
            String value = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, attributeName).getDeviceMessageAttributeValue();
            messageTag.add(new MessageAttribute(attributeTag, value));
        }
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}