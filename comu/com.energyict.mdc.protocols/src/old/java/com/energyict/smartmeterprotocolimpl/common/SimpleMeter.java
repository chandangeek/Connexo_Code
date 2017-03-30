/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.common;

import java.util.TimeZone;
import java.util.logging.Logger;

public interface SimpleMeter {

    /**
     * Return the DeviceTimeZone
     *
     * @return the DeviceTimeZone
     */
    TimeZone getTimeZone();

    /**
     * Getter for the used Logger
     *
     * @return the Logger
     */
    Logger getLogger();

    /**
     * The serialNumber of the meter
     *
     * @return the serialNumber of the meter
     */
    String getSerialNumber();

    /**
     * Get the physical address of the Meter. Mostly this will be an index of the meterList 
     *
     * @return the physical Address of the Meter.
     */
    int getPhysicalAddress();
}
