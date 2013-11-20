package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.Channel;

public class ReadingImplTest extends AbstractBaseReadingImplTest {

    @Override
    BaseReadingRecordImpl createInstanceToTest(Channel channel, TimeSeriesEntry entry) {
        return new ReadingRecordImpl(channel, entry);
    }
}
