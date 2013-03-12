package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.comserver.adapters.common.LegacyMessageConverter;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Represents a MessageConverter for the legacy WebRTUKP protocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class SmartWebRtuKpMessageConverter implements LegacyMessageConverter {

    private static final String activationDateAttributeName = "ContactorDeviceMessage.activationdate";

    private final List<DeviceMessageSpec> deviceMessageSpecs = Arrays.<DeviceMessageSpec>asList(
            ContactorDeviceMessage.CONTACTOR_OPEN,
            ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE,
            ContactorDeviceMessage.CONTACTOR_CLOSE,
            ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE,
            ContactorDeviceMessage.CHANGE_CONNECT_CONTROL_MODE);

    private Messaging messagingProtocol;

    /**
     * Default constructor for at-runtime instantiation
     */
    public SmartWebRtuKpMessageConverter() {
        super();
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {

        return deviceMessageSpecs;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals("ContactorDeviceMessage.changemode.mode")) {
            return messageAttribute.toString();
        } else if (propertySpec.getName().equals(activationDateAttributeName)) {
            return String.valueOf(((Date) messageAttribute).getTime()); // WebRTU format of the dateTime is milliseconds
        }
        return null;
    }

    @Override
    public MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage) {
        final DeviceMessageSpec deviceMessageSpec = MessageConverterTools.getDeviceMessageSpecForOfflineDeviceMessage(offlineDeviceMessage);

        if (ContactorDeviceMessage.CONTACTOR_OPEN.getPrimaryKey().equals(deviceMessageSpec.getPrimaryKey())) {
            return MessageConverterTools.createConnectLoadMessageEntry(getMessagingProtocol(), offlineDeviceMessage);
        } else if (ContactorDeviceMessage.CONTACTOR_OPEN_WITH_ACTIVATION_DATE.getPrimaryKey().equals(deviceMessageSpec.getPrimaryKey())) {
            return MessageConverterTools.createConnectLoadWithDateMessageEntry(getMessagingProtocol(), offlineDeviceMessage, activationDateAttributeName);
        } else if (ContactorDeviceMessage.CONTACTOR_CLOSE.getPrimaryKey().equals(deviceMessageSpec.getPrimaryKey())) {
            return MessageConverterTools.createDisconnectLoadMessageEntry(getMessagingProtocol(), offlineDeviceMessage);
        } else if (ContactorDeviceMessage.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE.getPrimaryKey().equals(deviceMessageSpec.getPrimaryKey())) {
            return MessageConverterTools.createDisconnectLoadWithDateMessageEntry(getMessagingProtocol(), offlineDeviceMessage, activationDateAttributeName);
        }

        return null;
    }

    @Override
    public void setMessagingProtocol(Messaging messaging) {
        this.messagingProtocol = messaging;
    }

    private Messaging getMessagingProtocol(){
        return this.messagingProtocol;
    }
}
