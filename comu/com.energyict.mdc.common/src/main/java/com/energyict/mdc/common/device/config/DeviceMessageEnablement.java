/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;

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

    /**
     * @return the dbValue of the DeviceMessage
     */
     public long getDeviceMessageDbValue();

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

    public DeviceConfiguration getDeviceConfiguration();

}