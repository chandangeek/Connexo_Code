package com.energyict.protocolimpl.iec1107.abba1700.counters;

import com.energyict.dlms.DLMSUtils;
import com.energyict.protocolimpl.iec1107.abba1700.ABBA1700;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Test the parsing for the programmingcounter
 */
public class ProgrammingCounterTest {

    @Test
    public void parseProgrammingCounterTest() throws IOException {
        byte[] response = DLMSUtils.hexStringToByteArray("30423030364337304334344430443735413834444633413734443443");
        ABBA1700 protocol = new ABBA1700();
        protocol.init(null, null, TimeZone.getTimeZone("Europe/Brussels"), Logger.getAnonymousLogger());
        ProgrammingCounter pc = new ProgrammingCounter(protocol);
        pc.parse(ProtocolUtils.convert2ascii(response));
        assertEquals(11, pc.getCounter());
        assertEquals(new Date(1304712268000L), pc.getMostRecentEventTime());
        assertEquals(new Date(1302878445000L), pc.getSecondMostRecentEventTime());
        assertEquals(new Date(1280150483000L), pc.getThirdMostRecentEventTime());
    }

}
