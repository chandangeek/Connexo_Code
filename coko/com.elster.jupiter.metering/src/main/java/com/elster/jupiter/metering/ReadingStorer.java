package com.elster.jupiter.metering;

import java.math.BigDecimal;
import java.util.Date;

public interface ReadingStorer {

    void addIntervalReading(Channel channel, Date dateTime, long profileStatus, BigDecimal... values);

    void addReading(Channel channel, Date dateTime, BigDecimal value);

    void addReading(Channel channel, Date dateTime, BigDecimal value, Date from);

    void addReading(Channel channel, Date dateTime, BigDecimal value, Date from, Date when);

    boolean overrules();

    void execute();
}
