package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.LoadProfileReading;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a measurement/reading of all channels of a load profile in a given interval
 * Created by bvn on 8/1/14.
 */
public class LoadProfileReadingImpl implements LoadProfileReading {
    private Interval interval;
    private Map<Channel, BigDecimal> map = new HashMap<>();

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    @Override
    public void setChannelData(Channel channel, BigDecimal value) {
        map.put(channel, value);
    }

    @Override
    public Set<Map.Entry<Channel, BigDecimal>> getChannelValues() {
        return map.entrySet();
    }
}
