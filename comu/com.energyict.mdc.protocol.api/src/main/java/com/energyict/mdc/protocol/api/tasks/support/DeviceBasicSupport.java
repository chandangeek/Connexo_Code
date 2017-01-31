/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.tasks.support;

/**
 * Defines functionality which is by default supported by a Device. This functionality will not always be fetched.
 */
public interface DeviceBasicSupport extends DeviceBasicTimeSupport {

    /**
     * @return the SerialNumber of a Device
     */
    public String getSerialNumber();

}
