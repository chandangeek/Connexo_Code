/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.offline;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Offline;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Represents an Offline version of LoadProfile.
 *
 * @author gna
 * @since 30/05/12 - 9:36
 */
public interface OfflineLoadProfile extends Offline {

    /**
     * Returns the database ID of this {@link OfflineDevice Rtus'} LoadProfile.
     *
     * @return the ID of the LoadProfile
     */
    long getLoadProfileId();

    /**
     * Returns the database ID of the LoadProfileType of this LoadProfile.
     *
     * @return the ID of the LoadProfileType
     */
    long getLoadProfileTypeId();

    /**
     * Returns the ObisCode for the LoadProfileType.
     *
     * @return the ObisCode (referring to a generic collection of channels having the same interval)
     */
    ObisCode getObisCode();

    /**
     * Returns the LoadProfile integration period.
     *
     * @return the integration period.
     */
    TimeDuration getInterval();

    /**
     * Returns the end time of the last interval read from the device.
     *
     * @return end time of the last interval.
     */
    Optional<Instant> getLastReading();

    /**
     * Returns the ID of the Device for the LoadProfile object.
     *
     * @return the ID of the Device.
     */
    long getDeviceId();

    /**
     * Returns the SerialNumber of the Master Device.
     *
     * @return the SerialNumber of the Master Device
     */
    String getMasterSerialNumber();

    /**
     * Returns the receiver's {@link OfflineLoadProfileChannel}.<br/>
     * <b>Be aware that this will only return the channels of the MASTER rtu.</b>
     * If you require all channels of this LoadProfile, including those of the slave devices with the same LoadProfileType, then use
     * {@link #getAllChannels()} instead.
     *
     * @return a <CODE>List</CODE> of {@link OfflineLoadProfileChannel} objects
     */
    List<OfflineLoadProfileChannel> getChannels();

    /**
     * Returns the receiver's {@link OfflineLoadProfileChannel} AND the {@link OfflineLoadProfileChannel} of
     * all slave devices belonging to load profiles of the same type.
     *
     * @return a <CODE>List</CODE> of {@link OfflineLoadProfileChannel} objects
     */
    List<OfflineLoadProfileChannel> getAllChannels();

    String getDeviceMRID();

    DeviceIdentifier<? extends BaseDevice> getDeviceIdentifier();

    LoadProfileIdentifier getLoadProfileIdentifier();

    default boolean isDataLoggerSlaveLoadProfile(){
        return false;
    }

}