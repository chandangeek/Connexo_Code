package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 3/14/14
 * Time: 11:43 AM
 */
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

    public String getName();

    public TimeDuration getInterval();

    public Optional<Instant> getLastReading();

    long getId();

    ObisCode getObisCode();

    BigDecimal getOverflow();

    ReadingType getReadingType();

    /**
     * Returns the data of this Channel.
     *
     * @param interval The interval for which data will be returned
     * @return data for this channel
     */
    List<LoadProfileReading> getChannelData(Range<Instant> interval);

    Optional<Instant> getLastDateTime();

    public boolean hasData();

    public ChannelDataUpdater startEditingData();

}