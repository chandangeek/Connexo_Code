package com.energyict.protocolimplv2.umi.ei4.profile;

import com.energyict.protocol.IntervalStateBits;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class EI4UmiReadingStatusFlagMapperTest {
    @Test
    public void testMap() throws Exception {
        int expected = IntervalStateBits.SHORTLONG | IntervalStateBits.CONFIGURATIONCHANGE |
                IntervalStateBits.ESTIMATED | IntervalStateBits.CORRUPTED | IntervalStateBits.DEVICE_ERROR |
                IntervalStateBits.WATCHDOGRESET | IntervalStateBits.BADTIME;
        int result = EI4UmiReadingStatusFlagMapper.map(127);
        assertEquals(expected, result);
    }
} 
