package com.energyict.mdc.device.data;

import com.elster.jupiter.util.time.Interval;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * Created by bvn on 8/1/14.
 */
public interface LoadProfileReading {

    Interval getInterval();

    void setInterval(Interval interval);

    void setChannelData(Channel channel, BigDecimal value);

    Set<Map.Entry<Channel, BigDecimal>> getChannelValues();
}
