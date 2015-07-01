package com.energyict.mdc.device.config;

import com.energyict.mdc.common.HasId;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import aQute.bnd.annotation.ProviderType;

import java.util.Set;

/**
 * Enables the usage of a {@link DeviceMessage}
 * or an entire DeviceMessageCategory
 * on a DeviceConfiguration.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-04 (09:57)
 */
@ProviderType
public interface DeviceMessageEnablement extends HasId {

    /**
     * Gets the Set of DeviceMessageUserActions
     * that a user of the system MUST have
     * to be able to create a {@link DeviceMessage}
     * that is enabled here.
     *
     * @return The Set of DeviceMessageUserAction
     */
    public Set<DeviceMessageUserAction> getUserActions();

    /**
     * @return the id of the DeviceMessage
     */
    public DeviceMessageId getDeviceMessageId();


    void addDeviceMessageCategory(DeviceMessageCategory deviceMessageCategory);

    /**
     * Add the given DeviceMessageUserAction to the enablement
     *
     * @param deviceMessageUserAction the userAction to add
     */
    boolean addDeviceMessageUserAction(DeviceMessageUserAction deviceMessageUserAction);

    /**
     * Remove the given DeviceMessageUserAction from this enablement
     *
     * @param deviceMessageUserAction the userAction to delete
     */
    boolean removeDeviceMessageUserAction(DeviceMessageUserAction deviceMessageUserAction);

}