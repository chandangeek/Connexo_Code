package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdw.interfacing.mdc.MdcInterfaceProvider;
import com.energyict.protocol.messaging.MessageValueSpec;

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
        public OfflineDeviceMessage getDeviceMessage() {
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
        return MdcInterfaceProvider.instance.get().getMdcInterface().getManager().getDeviceMessageSpecFactory().fromPrimaryKey(offlineDeviceMessage.getDeviceMessageSpecPrimaryKey().getValue());
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
        return emptyOfflineDeviceMessageAttribute;
    }

    static MessageValueSpec getEmptyMessageValueSpec(){
        final MessageValueSpec messageValueSpec = new MessageValueSpec();
        messageValueSpec.setValue(" ");
        return messageValueSpec;
    }
}