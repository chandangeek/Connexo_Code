package com.energyict.protocolimpl.iec1107.abba1700.counters;

import com.energyict.dlms.DLMSUtils;
import com.energyict.protocolimpl.iec1107.abba1700.ABBA1700;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Test the parsing of the phaseFailure counter and which phase actually failed
 */
public class PhaseFailureCounterTest {

    @Test
    public void testParse() throws Exception {
        byte[] response = DLMSUtils.hexStringToByteArray("33303030393332323746344433303232374634444336323137463444303330323031");
        ABBA1700 protocol = new ABBA1700();
        protocol.init(null, null, TimeZone.getTimeZone("Europe/Brussels"), Logger.getAnonymousLogger());
        PhaseFailureCounter pc = new PhaseFailureCounter(protocol);
        pc.parse(ProtocolUtils.convert2ascii(response));
        assertEquals(48, pc.getCounter());
        assertEquals(new Date(1300173955000L), pc.getMostRecentEventTime());
        assertEquals(3, pc.getFirstFailedPhase());
        assertEquals(new Date(1300173856000L), pc.getSecondMostRecentEventTime());
        assertEquals(2, pc.getSecondFailedPhase());
        assertEquals(new Date(1300173750000L), pc.getThirdMostRecentEventTime());
        assertEquals(1, pc.getThirdFailedPhase());
    }
}
