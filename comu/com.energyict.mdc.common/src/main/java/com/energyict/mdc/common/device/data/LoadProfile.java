/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.data;

import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.common.device.config.LoadProfileSpec;

import com.energyict.obis.ObisCode;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;

/**
 * Models a LoadProfile on a Device. It <i>bundles</i> channels
 * with the same interval.
 * <p>
 *
 * Date: 3/17/14
 * Time: 3:30 PM
 */
public interface LoadProfile extends com.energyict.mdc.upl.meterdata.LoadProfile, HasId {

    Device getDevice();

    /**
     * Gets the ID of the LoadProfileType
     *
     * @return the ID of the LoadProfileType
     */
    long getLoadProfileTypeId();

    /**
     * Gets the ObisCode which is configured for the LoadProfileType of this LoadProfile
     *
     * @return the ObisCode which is configured on the LoadProfileType
     */
    ObisCode getLoadProfileTypeObisCode();

    /**
     * Returns the Overruled ObisCode of the LoadProfileSpec.</br>
     * (this is the ObisCode of the LoadProfile that is know to the Device)
     *
     * @return the obisCode of the LoadProfile which is know to the Device
     */
    ObisCode getDeviceObisCode();

    /**
     * Returns the receiver's {@link Channel}s.
     *
     * @return a <CODE>List</CODE> of <CODE>Channel</CODE> objects
     */
    List<Channel> getChannels();

    /**
     * Indicates if this is a virtual load profile i.e. if the Device of this load profile needs a proxy for load profile AND
     * the B-field of the Obis code of the load profile type of this load profile does NOT contain a wildcard.
     *
     * @return boolean
     */
    boolean isVirtualLoadProfile();

    /**
     * Gets the interval of the LoadProfile.
     *
     * @return the Interval of the LoadProfile
     */
    TimeDuration getInterval();

    /**
     * Gets the data of all of this load profile's channels.
     *
     * @param interval The interval over which data will be returned
     * @return data for all channels of this loadprofile
     */
    List<LoadProfileReading> getChannelData(Range<Instant> interval);

    /**
     * Gets the {@link LoadProfileSpec} which
     * this LoadProfile is modeled by.
     *
     * @return the used LoadProfileSpec
     */
    LoadProfileSpec getLoadProfileSpec();

    long getVersion();

    /**
     * Get the {@link LoadProfileUpdater}
     * for updating LoadProfile without invoke of device
     */
    LoadProfileUpdater getUpdater();

    /**
     * Defines an <i>update</i> component to update a {@link LoadProfile} implementation
     */
    interface LoadProfileUpdater {

        /**
         * Updates the last reading if the argument is later than
         * the current last reading.
         *
         * @param lastReading the new last reading.
         */
        LoadProfileUpdater setLastReadingIfLater(Instant lastReading);

        /**
         * Updates the last reading.
         *
         * @param lastReading the new last reading
         */
        LoadProfileUpdater setLastReading(Instant lastReading);

        /**
         * Updates the LoadProfile, preferably via his Device.
         */
        void update();
    }

}