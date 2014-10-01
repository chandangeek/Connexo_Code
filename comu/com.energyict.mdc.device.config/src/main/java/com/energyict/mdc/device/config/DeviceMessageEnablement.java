package com.energyict.mdc.device.config;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.Set;

/**
 * Enables the usage of a {@link DeviceMessage}
 * or an entire DeviceMessageCategory
 * on a DeviceConfiguration.

 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-04 (09:57)
 */
public interface DeviceMessageEnablement {

    /**
     * Gets the Set of DeviceMessageUserActions
     * that a user of the system MUST have
     * to be able to create a {@link DeviceMessage}
     * that is enabled here.
     *
     * @return The Set of DeviceMessageUserAction
     */
    public Set<DeviceMessageUserAction> getUserActions ();

    /**
     * @return true if the implementation of this enablement is for a complete Category, false otherwise
     */
    public boolean isCategory();

    /**
     * @return true if the implementation of this enablement is for a single Device message, false otherwise
     */
    public boolean isSpecificMessage();

    /**
     * @return the id of the DeviceMessage
     */
    public DeviceMessageId getDeviceMessageId();

    /**
     * @return the DeviceMessageCategory which is fully enabled
     */
    public DeviceMessageCategory getDeviceMessageCategory();

}