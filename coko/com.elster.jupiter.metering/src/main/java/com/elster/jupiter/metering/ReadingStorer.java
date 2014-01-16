package com.elster.jupiter.metering;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.util.time.Interval;

public interface ReadingStorer {

    void addIntervalReading(Channel channel, Date dateTime, ProfileStatus profileStatus, BigDecimal... values);

    void addReading(Channel channel, Date dateTime, BigDecimal value);

    void addReading(Channel channel, Date dateTime, BigDecimal value, Date from);

    void addReading(Channel channel, Date dateTime, BigDecimal value, Date from, Date when);

    boolean overrules();

    void execute();

	void addReading(Channel channel, Reading reading);

    Map<Channel, Interval> getScope();
}
