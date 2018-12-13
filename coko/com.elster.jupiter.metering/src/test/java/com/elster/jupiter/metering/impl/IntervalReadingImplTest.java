/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class IntervalReadingImplTest extends AbstractBaseReadingImplTest {

    @Override
    BaseReadingRecordImpl createInstanceToTest(ChannelImpl channel, TimeSeriesEntry entry) {
        return new IntervalReadingRecordImpl(channel, entry);
    }

    @Test
    public void testGetIntervalReadingImpl() {
        ChannelImpl channel = getChannel();
        TimeSeriesEntry entry = mock(TimeSeriesEntry.class);
        IntervalReadingRecordImpl intervalReading = spy(new IntervalReadingRecordImpl(channel, entry));

        ReadingQualityRecord readingQualityRecord1 = mockReadingQuality(ProtocolReadingQualities.POWERUP.getCimCode());
        ReadingQualityRecord readingQualityRecord2 = mockReadingQuality(ProtocolReadingQualities.POWERDOWN.getCimCode());
        doReturn(Arrays.asList(readingQualityRecord1, readingQualityRecord2)).when(intervalReading).getReadingQualities();

        assertEquals(2, intervalReading.getReadingQualities().size());
        assertEquals(true, intervalReading.hasReadingQuality(ProtocolReadingQualities.POWERUP.getReadingQualityType()));
        assertEquals(true, intervalReading.hasReadingQuality(ProtocolReadingQualities.POWERDOWN.getReadingQualityType()));
    }

    private ReadingQualityRecord mockReadingQuality(String code) {
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        ReadingQualityType readingQualityType = new ReadingQualityType(code);
        when(readingQuality.getType()).thenReturn(readingQualityType);
        when(readingQuality.isActual()).thenReturn(true);
        when(readingQuality.getTypeCode()).thenReturn(code);
        return readingQuality;
    }
}