package com.energyict.mdc.device.data.impl;

import java.util.TimeZone;

/**
 * Provides the Default System {@link java.util.TimeZone}
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/14
 * Time: 09:08
 */
public interface DefaultSystemTimeZoneFactory {

    /**
     * Gets the default TimeZone for the system
     *
     * @return the systems' default TimeZone
     */
    public TimeZone getDefaultTimeZone();

}
