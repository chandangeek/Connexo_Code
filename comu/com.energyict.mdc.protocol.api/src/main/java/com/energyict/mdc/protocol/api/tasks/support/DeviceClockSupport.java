/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.tasks.support;

import java.util.Date;

/**
 * Defines functionality to <b>change</b> the Clock of a Device
 */
public interface DeviceClockSupport extends DeviceBasicTimeSupport {

    /**
     * Write the given new time to the Device.
     *
     * @param timeToSet the new time to set
     */
    public void setTime(Date timeToSet);

}
