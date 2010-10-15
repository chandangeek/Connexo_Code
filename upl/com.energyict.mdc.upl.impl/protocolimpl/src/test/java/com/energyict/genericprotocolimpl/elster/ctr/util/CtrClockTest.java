package com.energyict.genericprotocolimpl.elster.ctr.util;

import com.energyict.genericprotocolimpl.elster.ctr.CtrTest;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 14-okt-2010
 * Time: 17:19:49
 */
public class CtrClockTest extends CtrTest {

    private static final TimeZone CET = TimeZone.getTimeZone("CET");
    private static final TimeZone CEST = TimeZone.getTimeZone("CEST");
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone GMT_P5 = TimeZone.getTimeZone("GMT+5");
    private static final TimeZone GMT_M5 = TimeZone.getTimeZone("GMT-5");

    private static final byte[] QUERY_TIME_RESPONSE = ProtocolTools.getBytesFromHexString(
            "0A000000215000010308005A0A0A0E97" +
                    "0C153300230000000000000000000000" +
                    "00000000000000000000000000000000" +
                    "00000000000000000000000000000000" +
                    "00000000000000000000000000000000" +
                    "00000000000000000000000000000000" +
                    "00000000000000000000000000000000" +
                    "00000000000000000000000000000000" +
                    "00000000000000B1A173ECBA940D", ""
    );

    private static final Date CET_DATE = new Date(1287051711000L); //Thu Oct 14 12:21:51 CEST 2010
    private static final Date GMT_DATE = new Date(1287040911000L); //Thu Oct 14 14:21:51 CEST 2010
    private static final Date GMT_P5_DATE = new Date(1287040911000L); //Thu Oct 14 9:21:51 CEST 2010
    private static final Date GMT_M5_DATE = new Date(1287076911000L); //Thu Oct 14 19:21:51 CEST 2010

    private CtrClock getClock(TimeZone timeZone) {
        return new CtrClock(getDummyRequestFactory(QUERY_TIME_RESPONSE), getLogger(), timeZone);
    }

    @Test
    public void testSetTime() throws Exception {
    }

    @Test
    public void testGetTime() throws Exception {
        assertNotNull(getClock(CET).getTime());
        assertEquals(CET_DATE, getClock(CET).getTime());
    }

    @Test
    public void testTimeZone() throws Exception {
        assertNotNull(getClock(null).getTime());
        assertNotNull(getClock(GMT).getTime());
        assertNotNull(getClock(GMT_M5).getTime());
        assertNotNull(getClock(GMT_P5).getTime());
        assertEquals(GMT_P5_DATE, getClock(GMT_P5).getTime());

        Date gmtDate = getClock(GMT_M5).getTime();
        System.out.println(gmtDate);
        System.out.println(gmtDate.getTime());
        
    }

}
