package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.util.time.Interval;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bvn on 8/1/14.
 */
public interface LoadProfileReading {

    Interval getInterval();

    void setInterval(Interval interval);

    Set<Map.Entry<Channel, BigDecimal>> getChannelValues();

    void setReadingTime(Date reportedDateTime);

    Date getReadingTime();

    void setFlags(List<ProfileStatus.Flag> flags);

    List<ProfileStatus.Flag> getFlags();
}
