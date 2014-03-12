package com.energyict.mdc.device.config;

import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import java.util.Set;

/**
 * Enables the usage of a {@link DeviceMessage}
 * or an entire {@link com.energyict.mdc.messages.DeviceMessageCategory}
 * on a {@link com.energyict.mdc.device.config.DeviceCommunicationConfiguration}.
 * Subclasses will focus on enable single messages or complete message categories.
 * @see DeviceMessageCategoryEnablement
 * @see DeviceMessageSpecEnablement
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-04 (09:57)
 */
public interface DeviceMessageEnablement {

    /**
     * Gets the Set of {@link com.energyict.mdc.messages.DeviceMessageUserActionImpl}
     * that a user of the system MUST have
     * to be able to create a {@link DeviceMessage}
     * that is enabled here.
     *
     * @return The Set of DeviceMessageUserAction
     */
    public Set<DeviceMessageUserAction> getUserActions ();


}