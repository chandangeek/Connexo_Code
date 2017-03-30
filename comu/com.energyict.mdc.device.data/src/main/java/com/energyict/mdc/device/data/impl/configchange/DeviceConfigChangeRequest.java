/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.data.Device;

/**
 * Serves as a Business lock object for the action 'change device config'.
 * It works closely together with the {@link DeviceConfigChangeInAction} 'locks'.
 * These are created for each device in the 'change device config' action. Removing these
 * will trigger an eventhandler which checks if the request object can be removed.
 */
public interface DeviceConfigChangeRequest extends HasId {

    /**
     * Indication for self destruction if no other DeviceInAction objects exist for this business lock
     */
    void notifyDeviceInActionIsRemoved();

    /**
     * Adds a device in action
     *
     * @param device the device
     */
    DeviceConfigChangeInActionImpl addDeviceInAction(Device device);
}
