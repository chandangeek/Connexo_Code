/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle;

/**
 * Models the exceptional situation that occurs when
 * an {@link com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction}
 * is executed by the user but failed due to some business constraint violations.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-20 (16:23)
 */
public abstract class DeviceLifeCycleActionViolationException extends RuntimeException {

    public DeviceLifeCycleActionViolationException() {
        super();
    }

    public DeviceLifeCycleActionViolationException(String message) {
        super(message);
    }

}