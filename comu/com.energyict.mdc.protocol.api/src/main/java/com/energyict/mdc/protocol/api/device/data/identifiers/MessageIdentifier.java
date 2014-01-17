package com.energyict.mdc.protocol.api.device.data.identifiers;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

/**
 * Uniquely identifies a message that is sent to physical devices.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-28 (17:45)
 */
public interface MessageIdentifier {

    /**
     * Returns the {@link DeviceMessage} that is uniquely identified
     * by this MessageIdentifier.
     *
     * @return the DeviceMessage
     */
    public DeviceMessage getDeviceMessage();

}