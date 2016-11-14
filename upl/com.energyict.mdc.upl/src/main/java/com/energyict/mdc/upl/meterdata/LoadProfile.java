package com.energyict.mdc.upl.meterdata;

import java.util.Date;

/**
 * LoadProfile represents a loadprofile on a data logger or energy meter.
 * Each LoadProfile has a number of channels to store load profile data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-14 (11:43)
 */
public interface LoadProfile {

    /**
     * return the end time of the last interval read from the device.
     *
     * @return end time of the last interval.
     */
    Date getLastReading();

}