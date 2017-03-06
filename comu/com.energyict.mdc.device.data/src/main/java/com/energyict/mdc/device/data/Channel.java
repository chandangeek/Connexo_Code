/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.protocol.api.device.BaseChannel;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface Channel extends BaseChannel {

    @Override
    Device getDevice();

    @Override
    LoadProfile getLoadProfile();

    /**
     * Returns the configured interval in seconds.
     * Equivalent to getRtu().getIntervalInSeconds().
     *
     * @return the interval in seconds.
     */
    int getIntervalInSeconds();

    /**
     * Returns the ChannelSpec for which this channel is serving.
     *
     * @return the serving ChannelSpec
     */
    ChannelSpec getChannelSpec();

    String getName();

    TimeDuration getInterval();

    Optional<Instant> getLastReading();

    long getId();

    ObisCode getObisCode();

    Optional<BigDecimal> getOverflow();

    int getNrOfFractionDigits();

    ReadingType getReadingType();

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
     * Provides the value of the multiplier at the given timeStamp of this channel. The value will only be present if
     * the multiplier is larger than one (1)
     *
     * @return the optional multiplier
     */
    Optional<BigDecimal> getMultiplier(Instant timeStamp);

    /**
     * Returns the data of this Channel.
     *
     * @param interval The interval for which data will be returned
     * @return data for this channel
     */
    List<LoadProfileReading> getChannelData(Range<Instant> interval);

    List<LoadProfileJournalReading> getChannelWithHistoryData(Range<Instant> interval, Range<Instant> changed);

    Optional<Instant> getLastDateTime();

    boolean hasData();

    ChannelDataUpdater startEditingData();

    interface ChannelUpdater {
        ChannelUpdater setNumberOfFractionDigits(Integer overruledNbrOfFractionDigits);

        ChannelUpdater setOverflowValue(BigDecimal overruledOverflowValue);

        ChannelUpdater setObisCode(ObisCode overruledObisCode);

        void update();
    }
}