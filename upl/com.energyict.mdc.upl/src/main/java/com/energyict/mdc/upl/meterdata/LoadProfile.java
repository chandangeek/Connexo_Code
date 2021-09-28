package com.energyict.mdc.upl.meterdata;

import aQute.bnd.annotation.ConsumerType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import com.energyict.obis.ObisCode;

import java.util.Date;
import java.util.Optional;

/**
 * LoadProfile represents a loadprofile on a data logger or energy meter.
 * Each LoadProfile has a number of channels to store load profile data.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-11-14 (11:43)
 */
@ConsumerType
public interface LoadProfile {

    /**
     * return the end time of the last interval read from the device.
     *
     * @return end time of the last interval.
     */
    Date getLastReading();

    DeviceIdentifier getDeviceIdentifier();

    ObisCode getObisCode();

    /**
     *
     * @return end time of the last consistent interval
     */
    Optional<Date> getLastConsecutiveReading();

}