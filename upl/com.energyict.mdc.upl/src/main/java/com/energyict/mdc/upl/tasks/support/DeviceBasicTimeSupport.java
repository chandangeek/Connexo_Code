package com.energyict.mdc.upl.tasks.support;

import java.util.Date;

/**
 * Provides functionality to <b>get information</b> of the Device Time
 */
public interface DeviceBasicTimeSupport {

    /**
     * @return the actual time of the Device
     */
    Date getTime();

}