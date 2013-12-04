package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.Channel;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntervalReadingImplTest extends AbstractBaseReadingImplTest {

    private static final long PROFILE_STATUS = 214L;

    @Override
    BaseReadingRecordImpl createInstanceToTest(Channel channel, TimeSeriesEntry entry) {
        return new IntervalReadingRecordImpl(channel, entry);
    }

    @Test
    public void testGetIntervalReadingImpl() {
        Channel channel = mock(Channel.class);
        TimeSeriesEntry entry = mock(TimeSeriesEntry.class);

        when(entry.getLong(1)).thenReturn(PROFILE_STATUS);

        IntervalReadingRecordImpl intervalReading = new IntervalReadingRecordImpl(channel, entry);

        assertThat(intervalReading.getProfileStatus()).isEqualTo(PROFILE_STATUS);
    }

}
