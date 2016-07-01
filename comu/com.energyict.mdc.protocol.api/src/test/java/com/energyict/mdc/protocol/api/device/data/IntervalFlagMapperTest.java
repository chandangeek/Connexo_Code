package com.energyict.mdc.protocol.api.device.data;

import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.ProtocolReadingQualities;
import com.energyict.mdc.common.interval.IntervalStateBits;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 13/05/2016 - 17:25
 */
public class IntervalFlagMapperTest {

    @Test
    public void testValidFlags() {
        int intervalFlags = IntervalStateBits.BADTIME | IntervalStateBits.BATTERY_LOW | IntervalStateBits.TEST | IntervalStateBits.SHORTLONG;

        List<ReadingQualityType> readingQualityTypes = IntervalFlagMapper.map(intervalFlags);

        assertTrue(readingQualityTypes.contains(ProtocolReadingQualities.BADTIME.getReadingQualityType()));
        assertTrue(readingQualityTypes.contains(ProtocolReadingQualities.BATTERY_LOW.getReadingQualityType()));
        assertTrue(readingQualityTypes.contains(ProtocolReadingQualities.TEST.getReadingQualityType()));
        assertTrue(readingQualityTypes.contains(ProtocolReadingQualities.SHORTLONG.getReadingQualityType()));
    }

    @Test
    public void testInvalidFlags() {
        int intervalFlags = 32768;  //Interval state INITIALFAILVALIDATION is not mapped
        List<ReadingQualityType> readingQualityTypes = IntervalFlagMapper.map(intervalFlags);
        assertEquals(0, readingQualityTypes.size());
    }
}