package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by bvn on 8/1/14.
 */
public interface LoadProfileReading {

    Interval getInterval();

    void setInterval(Interval interval);

    Map<Channel, BigDecimal> getChannelValues();

    Map<Channel, DataValidationStatus> getChannelValidationStates();

    void setReadingTime(Date reportedDateTime);

    Date getReadingTime();

    void setFlags(List<ProfileStatus.Flag> flags);

    List<ProfileStatus.Flag> getFlags();
}
