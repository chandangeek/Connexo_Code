package com.energyict.mdc.device.lifecycle;

import com.energyict.mdc.device.lifecycle.config.MicroCheck;

/**
 * Models a violation of one of the {@link MicroCheck}s
 * that are configured on an {@link com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (16:21)
 */
public interface DeviceLifeCycleActionViolation {

    public MicroCheck getCheck();

    public String getLocalizedMessage();

}