package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.device.Channel;

import java.util.Date;

/**
 * Contains logical information from a {@link Channel} from an {@link com.energyict.mdc.protocol.api.device.BaseDevice}
 */
public interface ChannelFullProtocolShadow {

    int oneYearDurationInSeconds = 3600 * 24 * 365;
    int oneMonthDurationInSeconds = 3600 * 24 * 31;

    Date getLastReading();

    void setLastReading(Date lastReading);

    TimeDuration getTimeDuration();

    void setTimeDuration(TimeDuration timeDuration);

    int getChannelIndex();

    void setChannelIndex(int channelIndex);

    /**
     * Returns the interval in seconds for this channel. The seconds are calculated using the {@link TimeDuration}. For asynchroneous periods this means:
     * <ul>
     * <li> YEARS : amount * {@link #oneYearDurationInSeconds};
     * <li> MONTHS : amount * {@link #oneMonthDurationInSeconds};
     * </ul>
     * @return the configured interval of this channel in seconds
     */
    int getIntervalInSeconds();

    int getLoadProfileIndex();

    void setLoadProfileIndex(int loadProfileIndex);
}
