package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;

public class ReadingImplTest extends AbstractBaseReadingImplTest {

    @Override
    BaseReadingRecordImpl createInstanceToTest(ChannelImpl channel, TimeSeriesEntry entry) {
        return new ReadingRecordImpl(channel, entry);
    }
}
