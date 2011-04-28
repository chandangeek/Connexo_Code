package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.cbo.TimeDuration;

import java.util.Date;

/**
 * Straightforward implementation of the <CODE>ChannelFullProtocolShadow</CODE> interface
 */
public class ChannelFullProtocolShadowImpl implements ChannelFullProtocolShadow{

    private Date lastReading;
    private TimeDuration timeDuration;
    private int channelIndex;
    private int loadProfileIndex;

    public Date getLastReading() {
        return lastReading;
    }

    public void setLastReading(final Date lastReading) {
        this.lastReading = lastReading;
    }

    public TimeDuration getTimeDuration() {
        return timeDuration;
    }

    public void setTimeDuration(final TimeDuration timeDuration) {
        this.timeDuration = timeDuration;
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    public void setChannelIndex(final int channelIndex) {
        this.channelIndex = channelIndex;
    }

    /**
     * Returns the interval in seconds for this channel. The seconds are calculated using the {@link com.energyict.cbo.TimeDuration}. For asynchronous periods this means:
     * <ul>
     * <li> YEARS : amount * {@link #oneYearDurationInSeconds};
     * <li> MONTHS : amount * {@link #oneMonthDurationInSeconds};
     * </ul>
     * @return the configured interval of this channel in seconds
     */
    public int getIntervalInSeconds() {
        return this.timeDuration.getSeconds();
    }

    public int getLoadProfileIndex() {
        return this.loadProfileIndex;
    }

    public void setLoadProfileIndex(final int loadProfileIndex) {
        this.loadProfileIndex = loadProfileIndex;
    }
}
