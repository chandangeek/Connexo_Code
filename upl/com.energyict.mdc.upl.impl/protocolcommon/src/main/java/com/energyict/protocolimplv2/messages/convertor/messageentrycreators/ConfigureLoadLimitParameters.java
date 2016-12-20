package com.energyict.protocolimplv2.messages.convertor.messageentrycreators;

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
 * Copyrights EnergyICT
 * Date: 3/04/13
 * Time: 11:24
 */
public class ConfigureLoadLimitParameters implements MessageEntryCreator {

    private final String normalThresholdAttributeName;
    private final String emergencyThresholdAttributeName;
    private final String overThresholdDurationAttributeName;
    private final String emergencyProfileIdAttributeName;
    private final String emergencyProfileActivationDateAttributeName;
    private final String emergencyProfileDurationAttributeName;

    public ConfigureLoadLimitParameters(String normalThresholdAttributeName, String emergencyThresholdAttributeName,
                                        String overThresholdDurationAttributeName, String emergencyProfileIdAttributeName,
                                        String emergencyProfileActivationDateAttributeName, String emergencyProfileDurationAttributeName) {
        this.normalThresholdAttributeName = normalThresholdAttributeName;
        this.emergencyThresholdAttributeName = emergencyThresholdAttributeName;
        this.overThresholdDurationAttributeName = overThresholdDurationAttributeName;
        this.emergencyProfileIdAttributeName = emergencyProfileIdAttributeName;
        this.emergencyProfileActivationDateAttributeName = emergencyProfileActivationDateAttributeName;
        this.emergencyProfileDurationAttributeName = emergencyProfileDurationAttributeName;
    }

    @Override
    public MessageEntry createMessageEntry(Messaging messagingProtocol, OfflineDeviceMessage offlineDeviceMessage) {
        OfflineDeviceMessageAttribute normalThresholdAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, normalThresholdAttributeName);
        OfflineDeviceMessageAttribute emergencyThresholdAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, emergencyThresholdAttributeName);
        OfflineDeviceMessageAttribute overThresholdDurationAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, overThresholdDurationAttributeName);
        OfflineDeviceMessageAttribute emergencyProfileIdAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, emergencyProfileIdAttributeName);
        OfflineDeviceMessageAttribute emergencyProfileActivationDateAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, emergencyProfileActivationDateAttributeName);
        OfflineDeviceMessageAttribute emergencyProfileDurationAttribute = MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, emergencyProfileDurationAttributeName);

        MessageTag messageTag = new MessageTag(RtuMessageConstant.LOAD_LIMIT_CONFIGURE);
        messageTag.add(new MessageAttribute(RtuMessageConstant.LOAD_LIMIT_NORMAL_THRESHOLD, normalThresholdAttribute.getValue()));
        messageTag.add(new MessageAttribute(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_THRESHOLD, emergencyThresholdAttribute.getValue()));
        messageTag.add(new MessageAttribute(RtuMessageConstant.LOAD_LIMIT_MIN_OVER_THRESHOLD_DURATION, overThresholdDurationAttribute.getValue()));
        MessageTag emergencyProfileTag = new MessageTag(RtuMessageConstant.LOAD_LIMIT_EMERGENCY_PROFILE);
        emergencyProfileTag.add(new MessageAttribute(RtuMessageConstant.LOAD_LIMIT_EP_PROFILE_ID, emergencyProfileIdAttribute.getValue()));
        emergencyProfileTag.add(new MessageAttribute(RtuMessageConstant.LOAD_LIMIT_EP_ACTIVATION_TIME, emergencyProfileActivationDateAttribute.getValue()));
        emergencyProfileTag.add(new MessageAttribute(RtuMessageConstant.LOAD_LIMIT_EP_DURATION, emergencyProfileDurationAttribute.getValue()));
        emergencyProfileTag.add(new MessageValue(" "));
        messageTag.add(emergencyProfileTag);
        messageTag.add(new MessageValue(" "));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}
