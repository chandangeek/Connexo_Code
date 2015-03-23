package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

/**
 * Supports the life cycle of a {@link Device} as defined by the
 * {@link DeviceLifeCycle} of the Device's {@link DeviceType type}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (15:54)
 */
public interface DeviceLifeCycleService {

    String COMPONENT_NAME = "DLC";

    public void execute(AuthorizedTransitionAction action, Device device) throws DeviceLifeCycleActionViolationException;

}