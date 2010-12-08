package com.energyict.protocolimpl.dlms;

import com.energyict.dlms.DLMSUtils;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.profile.ApolloProfileIntervalStatusBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 7-dec-2010
 * Time: 12:10:18
 */
public class DLMSProfileIntervalsTest {

    private static String responseIntervals1 = "01020208090c07da0908030b00000080000011420600000000060000000006000000000600000000060000000006000000000208090c07da0908030c0000008000001142060000000006000000000600000000060000000006000000000600000000";
    private static String responseIntervals2 = "01030208090c07da0908030b000000800000114206000000000600000000060000000006000000000600000000060000000002080011420600000000060000000006000000000600000000060000000006000000000208001142060000000006000000000600000000060000000006000000000600000000";
    private static String responseIntervals3 = "01030208090c07da0908030b0000008000001142060000000006000000000600000000060000000006000000000600000000020809001142060000000006000000000600000000060000000006000000000600000000020809001142060000000006000000000600000000060000000006000000000600000000";

    private static String responseIntervals4 = "01030209090c07da0908030b00000080000011420600000000110006000000001140060000000011420600000000020900114206000000001100060000000011400600000000114206000000000209090011420600000000110006000000001140060000000011420600000000";
    private Logger logger = Logger.getAnonymousLogger();

    @Test
    public void testParseIntervals() throws Exception {
        DLMSProfileIntervals profileIntervals = new DLMSProfileIntervals(DLMSUtils.hexStringToByteArray(responseIntervals1), new ApolloProfileIntervalStatusBits());

        // there should be 2 intervals in the profile
        assertEquals(2, profileIntervals.parseIntervals(1).size());

        // both intervals have a status of 66
        assertEquals(5, profileIntervals.parseIntervals(1).get(0).getEiStatus());
        assertEquals(5, profileIntervals.parseIntervals(1).get(1).getEiStatus());
        assertEquals(6, profileIntervals.parseIntervals(1).get(1).getIntervalValues().size());

    }

    @Test
    public void testParseIntervalsWithNullTimeValues() {
        try {
            // need to test with a clock of null
            DLMSProfileIntervals profileIntervals = new DLMSProfileIntervals(DLMSUtils.hexStringToByteArray(responseIntervals2), new ApolloProfileIntervalStatusBits());

            /*
            There should be 3 intervals in the profile.
            We give the profile interval so the parser knows what time he has to add when a null-data is in the time position
            */
            assertEquals(3, profileIntervals.parseIntervals(3600).size());
            assertEquals(new Date(Long.valueOf("1283943600000")), profileIntervals.parseIntervals(3600).get(0).getEndTime());
            assertEquals(new Date(Long.valueOf("1283950800000")), profileIntervals.parseIntervals(3600).get(2).getEndTime());


            // need to test with an OctetString with length zero
            profileIntervals = new DLMSProfileIntervals(DLMSUtils.hexStringToByteArray(responseIntervals3), new ApolloProfileIntervalStatusBits());

            /*
            There should be 3 intervals in the profile.
            We give the profile interval so the parser knows what time he has to add when a null-data is in the time position
            */
            assertEquals(3, profileIntervals.parseIntervals(3600).size());
            assertEquals(new Date(Long.valueOf("1283943600000")), profileIntervals.parseIntervals(3600).get(0).getEndTime());
            assertEquals(new Date(Long.valueOf("1283950800000")), profileIntervals.parseIntervals(3600).get(2).getEndTime());

        } catch (IOException e) {
            logger.finest(e.getMessage());
            fail();
        }
    }

    @Test
    public void testParseIntervalWithMultipleStatuses() {
        try {
            DLMSProfileIntervals profileIntervals = new DLMSProfileIntervals(DLMSUtils.hexStringToByteArray(responseIntervals4),
                    Integer.valueOf("0001", 2), Integer.valueOf("010101010", 2), -1, new ApolloProfileIntervalStatusBits());
            assertEquals(3, profileIntervals.parseIntervals(3600).size());
            assertEquals(new Date(Long.valueOf("1283943600000")), profileIntervals.parseIntervals(3600).get(0).getEndTime());
            assertEquals(new Date(Long.valueOf("1283950800000")), profileIntervals.parseIntervals(3600).get(2).getEndTime());

            assertEquals(4, profileIntervals.parseIntervals(3600).get(0).getIntervalValues().size());
            assertEquals(5, profileIntervals.parseIntervals(3600).get(0).getEiStatus());
            assertEquals(5, ((IntervalValue)profileIntervals.parseIntervals(3600).get(0).getIntervalValues().get(0)).getEiStatus());
            assertEquals(0, ((IntervalValue)profileIntervals.parseIntervals(3600).get(0).getIntervalValues().get(1)).getEiStatus());
            assertEquals(4, ((IntervalValue)profileIntervals.parseIntervals(3600).get(0).getIntervalValues().get(2)).getEiStatus());
            assertEquals(5, ((IntervalValue)profileIntervals.parseIntervals(3600).get(0).getIntervalValues().get(3)).getEiStatus());

        } catch (IOException e) {
            logger.finest(e.getMessage());
            fail();
        }

    }

    @Test
    public void isIndexTest() {
        try {
            // clockIndex is equal to bit 0, statusIndex is equal to bit 1
            DLMSProfileIntervals profileIntervals = new DLMSProfileIntervals(DLMSUtils.hexStringToByteArray(responseIntervals1), new ApolloProfileIntervalStatusBits());

            assertTrue(profileIntervals.isClockIndex(0));
            assertFalse(profileIntervals.isClockIndex(1));

            assertTrue(profileIntervals.isStatusIndex(1));
            assertFalse(profileIntervals.isStatusIndex(0));

            assertFalse(profileIntervals.isChannelIndex(0));
            assertFalse(profileIntervals.isChannelIndex(1));
            assertTrue(profileIntervals.isChannelIndex(2));
            assertTrue(profileIntervals.isChannelIndex(10));
            assertTrue(profileIntervals.isChannelIndex(100));

            // clockIndex is equal to bit 1, statusIndex is now equal to bit 2
            profileIntervals = new DLMSProfileIntervals(DLMSUtils.hexStringToByteArray(responseIntervals1), Integer.valueOf("0010", 2), Integer.valueOf("0100", 2), -1, new ApolloProfileIntervalStatusBits());

            assertTrue(profileIntervals.isClockIndex(1));
            assertFalse(profileIntervals.isClockIndex(0));

            assertTrue(profileIntervals.isStatusIndex(2));
            assertFalse(profileIntervals.isStatusIndex(0));

            assertTrue(profileIntervals.isChannelIndex(0));
            assertFalse(profileIntervals.isChannelIndex(1));
            assertFalse(profileIntervals.isChannelIndex(2));
            assertTrue(profileIntervals.isChannelIndex(3));
            assertTrue(profileIntervals.isChannelIndex(10));
            assertTrue(profileIntervals.isChannelIndex(100));

            // clockIndex is equal to bit 0, statusIndex is equal to bit 1, channelIndexes are equal to bit 3, 4, 7, 8, 10
            profileIntervals = new DLMSProfileIntervals(DLMSUtils.hexStringToByteArray(responseIntervals1), Integer.valueOf("0001", 2),
                    Integer.valueOf("0010", 2), Integer.valueOf("10110011000", 2), new ApolloProfileIntervalStatusBits());

            assertTrue(profileIntervals.isClockIndex(0));
            assertFalse(profileIntervals.isClockIndex(1));

            assertTrue(profileIntervals.isStatusIndex(1));
            assertFalse(profileIntervals.isStatusIndex(0));

            assertFalse(profileIntervals.isChannelIndex(0));
            assertFalse(profileIntervals.isChannelIndex(1));
            assertFalse(profileIntervals.isChannelIndex(2));
            assertTrue(profileIntervals.isChannelIndex(3));
            assertTrue(profileIntervals.isChannelIndex(4));
            assertFalse(profileIntervals.isChannelIndex(5));
            assertFalse(profileIntervals.isChannelIndex(6));
            assertTrue(profileIntervals.isChannelIndex(7));
            assertTrue(profileIntervals.isChannelIndex(8));
            assertFalse(profileIntervals.isChannelIndex(9));
            assertTrue(profileIntervals.isChannelIndex(10));

            // multiple statusIndexes and channelIndexes
            profileIntervals = new DLMSProfileIntervals(DLMSUtils.hexStringToByteArray(responseIntervals1), Integer.valueOf("0001", 2),
                    Integer.valueOf("010101010", 2), Integer.valueOf("101010100", 2), new ApolloProfileIntervalStatusBits());

            assertTrue(profileIntervals.isClockIndex(0));
            assertFalse(profileIntervals.isClockIndex(1));

            assertFalse(profileIntervals.isStatusIndex(0));
            assertTrue(profileIntervals.isStatusIndex(1));
            assertFalse(profileIntervals.isStatusIndex(2));
            assertTrue(profileIntervals.isStatusIndex(3));
            assertFalse(profileIntervals.isStatusIndex(4));
            assertTrue(profileIntervals.isStatusIndex(5));
            assertFalse(profileIntervals.isStatusIndex(6));
            assertTrue(profileIntervals.isStatusIndex(7));
            assertFalse(profileIntervals.isStatusIndex(8));

            assertFalse(profileIntervals.isChannelIndex(0));
            assertFalse(profileIntervals.isChannelIndex(1));
            assertTrue(profileIntervals.isChannelIndex(2));
            assertFalse(profileIntervals.isChannelIndex(3));
            assertTrue(profileIntervals.isChannelIndex(4));
            assertFalse(profileIntervals.isChannelIndex(5));
            assertTrue(profileIntervals.isChannelIndex(6));
            assertFalse(profileIntervals.isChannelIndex(7));
            assertTrue(profileIntervals.isChannelIndex(8));
        } catch (IOException e) {
            logger.finest(e.getMessage());
            fail();
        }
    }
}