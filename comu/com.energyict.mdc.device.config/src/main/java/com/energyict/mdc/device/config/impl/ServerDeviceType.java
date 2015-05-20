package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

/**
 * Adds behavior to {@link DeviceType} that is reserved
 * for server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-18 (11:23)
 */
public interface ServerDeviceType extends DeviceType {

    /**
     * Updates the {@link DeviceLifeCycle} and saves the changes.
     *
     * @param deviceLifeCycle The DeviceLifeCycle
     */
    public void updateDeviceLifeCycle(DeviceLifeCycle deviceLifeCycle);

}