package com.energyict.genericprotocolimpl.common.pooling;

import com.energyict.cbo.TimeDuration;

import java.util.Date;

/**
 * Contains logical information from a {@link com.energyict.mdw.core.Channel} from an {@link com.energyict.mdw.core.Rtu}
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
     * Returns the interval in seconds for this channel. The seconds are calculated using the {@link com.energyict.cbo.TimeDuration}. For asynchroneous periods this means:
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
