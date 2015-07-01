package com.energyict.mdc.device.data;

import com.energyict.mdc.common.HasId;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.time.TimeDuration;
import com.google.common.collect.Range;

import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Models a LoadProfile on a Device. It <i>bundles</i> channels
 * with the same interval.
 * <p/>
 * Copyrights EnergyICT
 * Date: 3/17/14
 * Time: 3:30 PM
 */
@ProviderType
public interface LoadProfile extends BaseLoadProfile<Channel>, HasId {

    @Override
    Device getDevice();

    /**
     * return the end time of the last interval read from the device.
     *
     * @return end time of the last interval.
     */
    Optional<Instant> getLastReading();

    /**
     * Returns the receiver's {@link com.energyict.mdc.protocol.api.device.BaseChannel}s.
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
    public List<LoadProfileReading> getChannelData(Range<Instant> interval);

    /**
     * Gets the {@link com.energyict.mdc.device.config.LoadProfileSpec} which
     * this LoadProfile is modeled by.
     *
     * @return the used LoadProfileSpec
     */
    LoadProfileSpec getLoadProfileSpec();

    /**
     * Defines an <i>update</i> component to update a {@link com.energyict.mdc.device.data.LoadProfile} implementation
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