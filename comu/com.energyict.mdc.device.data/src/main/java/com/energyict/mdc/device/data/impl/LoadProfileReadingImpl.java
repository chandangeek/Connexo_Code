package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfileReading;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a measurement/reading of all channels of a load profile in a given interval
 * Created by bvn on 8/1/14.
 */
public class LoadProfileReadingImpl implements LoadProfileReading {
    private Interval interval;
    private Map<Channel, BigDecimal> map = new HashMap<>();
    private Date readingTime;
    private final List<ProfileStatus.Flag> flags = new ArrayList<>();

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public void setChannelData(Channel channel, BigDecimal value) {
        map.put(channel, value);
    }

    @Override
    public Set<Map.Entry<Channel, BigDecimal>> getChannelValues() {
        return map.entrySet();
    }

    @Override
    public void setReadingTime(Date reportedDateTime) {
        this.readingTime = reportedDateTime;
    }

    @Override
    public Date getReadingTime() {
        return readingTime;
    }

    @Override
    public void setFlags(List<ProfileStatus.Flag> flags) {
        this.flags.clear();
        this.flags.addAll(flags);
    }

    @Override
    public List<ProfileStatus.Flag> getFlags() {
        return Collections.unmodifiableList(flags);
    }
}
