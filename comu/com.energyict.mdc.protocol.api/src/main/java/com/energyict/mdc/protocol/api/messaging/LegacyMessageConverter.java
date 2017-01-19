package com.energyict.mdc.protocol.api.messaging;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import java.util.Set;

/**
 * Provides functionality to do conversion between the <i>new</i>
 * {@link DeviceMessageSpec}s and the <i>old</i> MessageSpecs.
 * <p>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:15
 */
public interface LegacyMessageConverter {

    /**
     * @return a <code>List</code> of protocol supported {@link DeviceMessageId}s
     */
    Set<DeviceMessageId> getSupportedMessages();

    /**
     * Requests to format the given messageAttribute to a proper format which is <b>known</b>
     * by the {@link DeviceProtocol deviceProtocol}.
     * When the framework will request to perform a certain message, then the formatted
     * values will be delivered to the DeviceProtocol.
     *
     * @param propertySpec     the spec defining the type of the attribute
     * @param messageAttribute the messageAttribute value that needs to be formatted.
     * @return a properly formatted version of the given messageAttribute
     */
    String format(PropertySpec propertySpec, Object messageAttribute);

    /**
     * Converts the given OfflineDeviceMessage to a legacy MessageEntry
     *
     * @param offlineDeviceMessage the offlineDeviceMessage that needs to be converted.
     * @return the converted legacy MessageEntry
     */
    MessageEntry toMessageEntry(final OfflineDeviceMessage offlineDeviceMessage);

    /**
     * Sets the used MessagingProtocol so the converter can make proper use
     * of the original functionality which created the xml formatted messages
     */
    void setMessagingProtocol(Messaging messagingProtocol);

}