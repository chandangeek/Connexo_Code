package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.Channel;

public class ReadingImplTest extends AbstractBaseReadingImplTest {

    @Override
    BaseReadingImpl createInstanceToTest(Channel channel, TimeSeriesEntry entry) {
        return new ReadingImpl(channel, entry);
    }
}
