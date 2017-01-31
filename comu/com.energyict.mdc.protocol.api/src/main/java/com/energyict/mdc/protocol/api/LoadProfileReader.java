/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;

/**
 * <p>
 * Straightforward ValueObject representing a meter's LoadProfile to read.
 * </p>
 * <p>
 * The 'Date' strategy:<br>
 * <li>If the startDate is null, then we will read data from up to a month ago (current Date - 1 month)
 * <li>If the endDate is null, then we will use the current Date
 */
public class LoadProfileReader {

    /**
     * Holds the <CODE>ObisCode</CODE> from the <CODE>LoadProfile</CODE> to read
     */
    private final ObisCode profileObisCode;
    /**
     * Holds the identifier of the Device
     */
    private final DeviceIdentifier<?> deviceIdentifier;

    /**
     * Holds the serialNumber of the meter for this LoadProfile
     */
    private final String meterSerialNumber;

    /**
     * Holds the Date from where to start fetching data from the <CODE>LoadProfile</CODE>
     */
    private final Instant startReadingTime;

    /**
     * Holds the Date from the <i>LAST</i> interval to fetch in the <CODE>LoadProfile</CODE>
     */
    private final Instant endReadingTime;

    /**
     * Represents the database ID of the LoadProfile to read.
     * We will need this to set in the ProfileData object.
     */
    private final long loadProfileId;

    /**
     * Contains a <CODE>List</CODE> of <b>necessary</b> channels to read from the meter.
     */
    private final List<ChannelInfo> channelInfos;
    private LoadProfileIdentifier loadProfileIdentifier;


    /**
     * Default constructor
     *
     * @param profileObisCode       the <CODE>ObisCode</CODE> from the <CODE>LoadProfile</CODE>
     * @param startReadingTime      the readTime to start reading the <CODE>LoadProfile</CODE>
     * @param endReadingTime        the endTime of the last <CODE>LoadProfile</CODE> interval
     * @param deviceIdentifier      the DeviceIdentifier for the Device which owns the LoadProfile
     * @param channelInfos          the <CODE>List</CODE> of <CODE>ChannelInfo</CODE> representing the channels to read from the profile in the meter
     * @param meterSerialNumber     the serialNumber of the device which owns this LoadProfile
     * @param loadProfileIdentifier the unique identifier of the LoadProfile
     */
    public LoadProfileReader(Clock clock, ObisCode profileObisCode, Instant startReadingTime, Instant endReadingTime, long loadProfileId, DeviceIdentifier<?> deviceIdentifier, List<ChannelInfo> channelInfos, String meterSerialNumber, LoadProfileIdentifier loadProfileIdentifier) {
        this.profileObisCode = profileObisCode;
        if (endReadingTime == null) {
            this.endReadingTime = clock.instant();
        } else {
            this.endReadingTime = endReadingTime;
        }
        if (startReadingTime == null) {
            this.startReadingTime = this.endReadingTime.atOffset(ZoneOffset.UTC).minus(Period.ofMonths(1)).toInstant();
        } else {
            this.startReadingTime = startReadingTime;
        }
        this.loadProfileId = loadProfileId;
        this.deviceIdentifier = deviceIdentifier;
        this.channelInfos = channelInfos;
        this.meterSerialNumber = meterSerialNumber;
        this.loadProfileIdentifier = loadProfileIdentifier;
    }

    /**
     * Getter for the {@link #startReadingTime}
     *
     * @return the {@link #startReadingTime}
     */
    public Instant getStartReadingTime() {
        return startReadingTime;
    }

    /**
     * Getter for the {@link #profileObisCode}
     *
     * @return the {@link #profileObisCode}
     */
    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    /**
     * Getter for the {@link #deviceIdentifier}
     *
     * @return the {@link #deviceIdentifier}
     */
    public DeviceIdentifier<?> getDeviceIdentifier() {
        return deviceIdentifier;
    }

    /**
     * Getter for the {@link #endReadingTime}
     *
     * @return the {@link #endReadingTime}
     */
    public Instant getEndReadingTime() {
        return endReadingTime;
    }

    /**
     * Getter for the {@link #loadProfileId}
     *
     * @return the {@link #loadProfileId}
     */
    public long getLoadProfileId() {
        return loadProfileId;
    }

    /**
     * Getter for the {@link #channelInfos}
     *
     * @return the {@link #channelInfos}
     */
    public List<ChannelInfo> getChannelInfos() {
        return channelInfos;
    }

    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    @Override
    public String toString() {
        return "ObisCode : " + getProfileObisCode() + " for meter : " + getMeterSerialNumber();
    }

    public LoadProfileIdentifier getLoadProfileIdentifier() {
        return loadProfileIdentifier;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        LoadProfileReader that = (LoadProfileReader) other;

        if (getLoadProfileId() != that.getLoadProfileId()) return false;
        if (getProfileObisCode() != null ? !getProfileObisCode().equals(that.getProfileObisCode()) : that.getProfileObisCode() != null)
            return false;
        if (getMeterSerialNumber() != null ? !getMeterSerialNumber().equals(that.getMeterSerialNumber()) : that.getMeterSerialNumber() != null)
            return false;
        if (getStartReadingTime() != null ? !getStartReadingTime().equals(that.getStartReadingTime()) : that.getStartReadingTime() != null)
            return false;
        return !(getEndReadingTime() != null ? !getEndReadingTime().equals(that.getEndReadingTime()) : that.getEndReadingTime() != null);
    }

    @Override
    public int hashCode() {
        int result = getProfileObisCode() != null ? getProfileObisCode().hashCode() : 0;
        result = 31 * result + (getMeterSerialNumber() != null ? getMeterSerialNumber().hashCode() : 0);
        result = 31 * result + (getStartReadingTime() != null ? getStartReadingTime().hashCode() : 0);
        result = 31 * result + (getEndReadingTime() != null ? getEndReadingTime().hashCode() : 0);
        result = 31 * result + ((int) (getLoadProfileId() ^ (getLoadProfileId() >>> 32)));
        return result;
    }

}