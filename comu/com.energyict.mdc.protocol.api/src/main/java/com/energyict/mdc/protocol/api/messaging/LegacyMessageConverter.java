/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.messaging;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;

import java.util.Set;

public interface LegacyMessageConverter {

    /**
     * @return a <code>List</code> of protocol supported {@link DeviceMessageId}s
     */
    public Set<DeviceMessageId> getSupportedMessages();

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
    public String format(PropertySpec propertySpec, Object messageAttribute);

    /**
     * Converts the given OfflineDeviceMessage to a legacy MessageEntry
     *
     * @param offlineDeviceMessage the offlineDeviceMessage that needs to be converted.
     * @return the converted legacy MessageEntry
     */
    public MessageEntry toMessageEntry(final OfflineDeviceMessage offlineDeviceMessage);

    /**
     * Sets the used MessagingProtocol so the converter can make proper use
     * of the original functionality which created the xml formatted messages
     */
    public void setMessagingProtocol(Messaging messagingProtocol);

}