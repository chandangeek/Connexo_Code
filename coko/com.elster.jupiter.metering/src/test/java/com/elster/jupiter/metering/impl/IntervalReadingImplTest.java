package com.elster.jupiter.metering.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.readings.ProfileStatus;

public class IntervalReadingImplTest extends AbstractBaseReadingImplTest {

    private static final ProfileStatus PROFILE_STATUS = ProfileStatus.of(ProfileStatus.Flag.POWERUP,ProfileStatus.Flag.POWERDOWN);

    @Override
    BaseReadingRecordImpl createInstanceToTest(ChannelImpl channel, TimeSeriesEntry entry) {
        return new IntervalReadingRecordImpl(channel, entry);
    }

    @Test
    public void testGetIntervalReadingImpl() {
        ChannelImpl channel = getChannel();
        TimeSeriesEntry entry = mock(TimeSeriesEntry.class);
        when(entry.getLong(1)).thenReturn(PROFILE_STATUS.getBits());
        IntervalReadingRecordImpl intervalReading = new IntervalReadingRecordImpl(channel, entry);
        assertThat(intervalReading.getProfileStatus()).isEqualTo(PROFILE_STATUS);
    }

}
