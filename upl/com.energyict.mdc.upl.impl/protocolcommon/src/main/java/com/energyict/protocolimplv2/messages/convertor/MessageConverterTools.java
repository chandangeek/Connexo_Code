package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttribute;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.Messaging;
import com.energyict.protocolimpl.messages.RtuMessageConstant;

/**
 * Provides convenient methods to help process the conversion of
 * {@link com.energyict.mdc.messages.DeviceMessage DeviceMessages}
 * to {@link com.energyict.protocol.messaging.MessageSpec MessageSpecs}
 * and visa versa.
 */
public class MessageConverterTools {

    /**
     * An offlineDeviceMessageAttribute representing an empty attribute.
     * The name and value of this attribute are both returned as empty Strings (<code>""</code>).
     */
    public static final OfflineDeviceMessageAttribute emptyOfflineDeviceMessageAttribute = new OfflineDeviceMessageAttribute() {
        @Override
        public PropertySpec getPropertySpec() {
            return null;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getDeviceMessageAttributeValue() {
            return "";
        }

        @Override
        public DeviceMessage getDeviceMessage() {
            return null;
        }
    };

    /**
     * Gets the DeviceMessageSpec from the OfflineDeviceMessage using the DeviceMessageFactory.
     * <i>Note that it is allowed to use the ManagerFactory for this, as the deviceMessageFactory
     * doesn't use any database calls.</i>
     *
     * @param offlineDeviceMessage the offlineDeviceMessage to convert
     * @return the deviceMessageSpec
     */
    public static DeviceMessageSpec getDeviceMessageSpecForOfflineDeviceMessage(OfflineDeviceMessage offlineDeviceMessage) {
        return ManagerFactory.getCurrent().getDeviceMessageSpecFactory().fromPrimaryKey(offlineDeviceMessage.getDeviceMessageSpecPrimaryKey().getValue());
    }

    /**
     * Searches for the {@link OfflineDeviceMessageAttribute}
     * in the given {@link OfflineDeviceMessage} which corresponds
     * with the provided name. If no match is found, then the
     * {@link #emptyOfflineDeviceMessageAttribute}
     * attribute is returned
     *
     * @param offlineDeviceMessage the offlineDeviceMessage to search in
     * @param attributeName        the name of the OfflineDeviceMessageAttribute to return
     * @return the requested OfflineDeviceMessageAttribute or {@link #emptyOfflineDeviceMessageAttribute}
     */
    public static OfflineDeviceMessageAttribute getDeviceMessageAttribute(OfflineDeviceMessage offlineDeviceMessage, String attributeName) {
        for (OfflineDeviceMessageAttribute offlineDeviceMessageAttribute : offlineDeviceMessage.getDeviceMessageAttributes()) {
            if (offlineDeviceMessageAttribute.getName().equals(attributeName)) {
                return offlineDeviceMessageAttribute;
            }
        }
        return null;
    }

    public static MessageEntry createConnectLoadMessageEntry(final Messaging messagingProtocol, final OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(RtuMessageConstant.CONNECT_LOAD);
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }

    static MessageEntry createDisconnectLoadWithDateMessageEntry(final Messaging messagingProtocol, final OfflineDeviceMessage offlineDeviceMessage, final String activationDateAttributeName) {
        OfflineDeviceMessageAttribute activationDateAttribute = getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.DISCONNECT_LOAD);
        messageTag.add(new MessageAttribute(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE, activationDateAttribute.getDeviceMessageAttributeValue()));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }

    static MessageEntry createConnectLoadWithDateMessageEntry(final Messaging messagingProtocol, final OfflineDeviceMessage offlineDeviceMessage, final String activationDateAttributeName) {
        OfflineDeviceMessageAttribute activationDateAttribute = getDeviceMessageAttribute(offlineDeviceMessage, activationDateAttributeName);
        MessageTag messageTag = new MessageTag(RtuMessageConstant.CONNECT_LOAD);
        messageTag.add(new MessageAttribute(RtuMessageConstant.DISCONNECT_CONTROL_ACTIVATE_DATE, activationDateAttribute.getDeviceMessageAttributeValue()));
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }

    static MessageEntry createDisconnectLoadMessageEntry(final Messaging messagingProtocol, final OfflineDeviceMessage offlineDeviceMessage) {
        MessageTag messageTag = new MessageTag(RtuMessageConstant.DISCONNECT_LOAD);
        return new MessageEntry(messagingProtocol.writeTag(messageTag), offlineDeviceMessage.getTrackingId());
    }
}