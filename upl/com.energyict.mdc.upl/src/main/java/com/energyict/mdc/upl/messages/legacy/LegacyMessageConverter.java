package com.energyict.mdc.upl.messages.legacy;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.List;

/**
 * Provides functionality to do conversion between the new and old-style device message specs.
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:15
 */
public interface LegacyMessageConverter {

    /**
     * @return a <code>List</code> of protocol supported {@link DeviceMessageSpec deviceMessages}
     */
    List<DeviceMessageSpec> getSupportedMessages();

    /**
     * Requests to format the given messageAttribute to a proper format
     * as it is <b>known</b> by the legacy deviceProtocol.
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
    MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage);

}
