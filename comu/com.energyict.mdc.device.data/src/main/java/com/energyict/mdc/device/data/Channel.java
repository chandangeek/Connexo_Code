package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseLoadProfile;

import java.math.BigDecimal;
import java.util.Date;

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

    public Phenomenon getPhenomenon();

    public Date getLastReading();

    long getId();

    ObisCode getObisCode();

    BigDecimal getOverflow();

    BigDecimal getMultiplier();

    ReadingType getReadingType();
}
