/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.tasks.support;

import java.util.Date;

/**
 * Provides functionality to <b>get information</b> of the Device Time
 */
public interface DeviceBasicTimeSupport {

    /**
     * @return the actual time of the Device
     */
    public Date getTime();
}
