/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.protocol.IntervalFlagMapper;
import com.energyict.protocol.ProtocolReadingQualities;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntervalFlagMapperTest {

    @Test
    public void testValidFlags() {
        int intervalFlags = IntervalStateBits.BADTIME | IntervalStateBits.BATTERY_LOW | IntervalStateBits.TEST | IntervalStateBits.SHORTLONG;

        List<String> readingQualityTypes = IntervalFlagMapper.map(intervalFlags);

        assertTrue(readingQualityTypes.contains(ProtocolReadingQualities.BADTIME.getCimCode()));
        assertTrue(readingQualityTypes.contains(ProtocolReadingQualities.BATTERY_LOW.getCimCode()));
        assertTrue(readingQualityTypes.contains(ProtocolReadingQualities.TEST.getCimCode()));
        assertTrue(readingQualityTypes.contains(ProtocolReadingQualities.SHORTLONG.getCimCode()));
    }

    @Test
    public void testInvalidFlags() {
        int intervalFlags = 32768;  //Interval state INITIALFAILVALIDATION is not mapped
        List<String> readingQualityTypes = IntervalFlagMapper.map(intervalFlags);
        assertEquals(0, readingQualityTypes.size());
    }
}