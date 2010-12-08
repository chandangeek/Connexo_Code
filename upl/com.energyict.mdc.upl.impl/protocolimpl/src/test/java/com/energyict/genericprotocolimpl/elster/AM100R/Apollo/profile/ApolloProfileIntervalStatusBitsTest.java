package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.profile;

import com.energyict.protocol.IntervalStateBits;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 8-dec-2010
 * Time: 11:47:13
 */
public class ApolloProfileIntervalStatusBitsTest {

    @Test
    public void testGetEisStatusCode() throws Exception {
        ApolloProfileIntervalStatusBits profileStatusBits = new ApolloProfileIntervalStatusBits();
        assertEquals(IntervalStateBits.POWERDOWN|IntervalStateBits.OTHER|IntervalStateBits.CONFIGURATIONCHANGE|
                IntervalStateBits.SHORTLONG|IntervalStateBits.OVERFLOW, profileStatusBits.getEisStatusCode(-1));

        assertEquals(IntervalStateBits.POWERDOWN, profileStatusBits.getEisStatusCode(2));
        assertEquals(IntervalStateBits.OTHER, profileStatusBits.getEisStatusCode(4));
        assertEquals(IntervalStateBits.CONFIGURATIONCHANGE, profileStatusBits.getEisStatusCode(8));
        assertEquals(IntervalStateBits.SHORTLONG, profileStatusBits.getEisStatusCode(16));
        assertEquals(IntervalStateBits.OVERFLOW, profileStatusBits.getEisStatusCode(32));
        assertEquals(IntervalStateBits.SHORTLONG, profileStatusBits.getEisStatusCode(64));
    }
}
