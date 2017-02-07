/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.protocol.api.device.BaseRegister;

import aQute.bnd.annotation.ProviderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface Register<R extends Reading, RS extends RegisterSpec> extends BaseRegister {

    Device getDevice();

    /**
     * Returns this Register's specification.
     *
     * @return the spec
     */
    RS getRegisterSpec();

    /**
     * Gets the list of {@link Reading}s whose timestamp is within the given interval.
     *
     * @param interval the Interval
     * @return The List of Reading
     * @see Reading#getTimeStamp()
     */
    List<R> getReadings(Interval interval);

    /**
     * Gets the {@link Reading} with the specified timestamp.
     *
     * @param timestamp The timestamp of the Reading
     * @return The Reading
     * @see Reading#getTimeStamp()
     */
    Optional<R> getReading(Instant timestamp);

    Optional<R> getLastReading();

    /**
     * Return the &quot;timestamp&quot of the last reading for this Register.
     *
     * @return the <code>RegisterReading</code>
     * @see #getLastReading()
     */
    Optional<Instant> getLastReadingDate();

    ReadingType getReadingType();

    boolean hasData();

    RegisterDataUpdater startEditingData();

    /**
     * Returns the readingtype of the calculated value.
     * <ul>
     * <li>Either the delta if the readingType was a bulk and no multiplier was provided</li>
     * <li>Or the multiplied readingType if a multiplier was provided</li>
     * </ul>
     * Depending on the timeStamp a different ReadingType can be provided
     *
     * @param timeStamp the timeStamp for which we want the calculated readingType
     * @return the calculated ReadingType
     */
    Optional<ReadingType> getCalculatedReadingType(Instant timeStamp);

    /**
     * Provides the value of the multiplier of this register at the given timestamp. The value will only be present if
     * the multiplier is larger than one (1)
     *
     * @return the optional multiplier
     */
    Optional<BigDecimal> getMultiplier(Instant timeStamp);

    interface RegisterUpdater {
        RegisterUpdater setNumberOfFractionDigits(Integer overruledNbrOfFractionDigits);

        RegisterUpdater setOverflowValue(BigDecimal overruledOverflowValue);

        RegisterUpdater setObisCode(ObisCode overruledObisCode);

        void update();
    }
}