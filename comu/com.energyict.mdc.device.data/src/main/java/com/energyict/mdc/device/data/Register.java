package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.protocol.api.device.BaseRegister;

import java.util.Date;
import java.util.List;

/**
 * Defines the non-persistent representation of a Register.
 * A <i>Register</i> for a {@link Device} will be a wrapper around
 * the {@link com.energyict.mdc.device.config.RegisterSpec} of
 * the {@link com.energyict.mdc.device.config.DeviceConfiguration}
 * of the {@link Device}
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/14
 * Time: 10:32
 */
public interface Register extends BaseRegister {

    Device getDevice();

    /**
     * Returns the register's specification object
     *
     * @return the spec
     */
    RegisterSpec getRegisterSpec();

    /**
     * Gets a list of ReadingRecords, filtered by the given interval.
     *
     * @param interval the Interval to fetch readings from
     * @return The List of ReadingRecord
     */
    List<ReadingRecord> getRegisterReadings(Interval interval);

    public Optional<ReadingRecord> getLastReading();

    /**
     * Return the &quot;timestamp&quot of the last reading for this Register.
     *
     * @return the <code>RegisterReading</code>
     * @see #getLastReading()
     */
    public Optional<Date> getLastReadingDate();

}