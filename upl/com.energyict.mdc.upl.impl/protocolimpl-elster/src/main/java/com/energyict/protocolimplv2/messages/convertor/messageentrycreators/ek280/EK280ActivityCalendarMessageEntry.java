package com.energyict.protocolimplv2.messages.convertor.messageentrycreators.ek280;

import com.elster.protocolimpl.dlms.messaging.XmlMessageWriter;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

/**
 * @author sva
 * @since 1/09/2015 - 14:27
 */
public class EK280ActivityCalendarMessageEntry implements MessageEntryCreator {

    private static final String messageTag = "UploadPassiveTariff";
    private static final String codeTableAttributeTag = "CodeTableId";
    private static final String activationTimeAttributeTag = "ActivationTime";
    private static final String defaultTariffCodeAttributeTag = "DefaultTariff";
    private final TariffCalendarFinder calendarFinder;
    private final TariffCalendarExtractor calendarExtractor;
    private final DeviceMessageFileExtractor messageFileExtractor;
    private final DeviceMessageFileFinder deviceMessageFileFinder;

    public EK280ActivityCalendarMessageEntry(TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, DeviceMessageFileFinder deviceMessageFileFinder) {
        this.calendarFinder = calendarFinder;
        this.calendarExtractor = calendarExtractor;
        this.messageFileExtractor = messageFileExtractor;
        this.deviceMessageFileFinder = deviceMessageFileFinder;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        String codeTable = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.activityCalendarAttributeName).getValue();
        String activationDate = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.activityCalendarActivationDateAttributeName).getValue();
        String defaultTariffCode = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.defaultTariffCodeAttrributeName).getValue();

        MessageTag msgTag = new MessageTag(messageTag);
        msgTag.add(new MessageAttribute(codeTableAttributeTag, codeTable));
        msgTag.add(new MessageAttribute(activationTimeAttributeTag, activationDate));
        msgTag.add(new MessageAttribute(defaultTariffCodeAttributeTag, defaultTariffCode));
        msgTag.add(new MessageValue(" "));
        return MessageEntry
                .fromContent(getXmlMessageWriter().writeNormalTag(msgTag))
                .andMessage(offlineDeviceMessage)
                .finish();
    }

    private XmlMessageWriter getXmlMessageWriter() {
        return new XmlMessageWriter(this.calendarFinder, this.calendarExtractor, deviceMessageFileFinder, messageFileExtractor);
    }
}